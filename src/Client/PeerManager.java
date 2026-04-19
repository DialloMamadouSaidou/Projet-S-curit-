import java.util.concurrent.ConcurrentHashMap;
import java.util.*;// Plus sûr pour les Threads
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public class PeerManager {
    private String myName;
    private int myPort;
    private ClientConnection connection;
    private Map<String, String> list_game_and_master = new ConcurrentHashMap<>();
    private List<Map<String, Integer>> info_game = new CopyOnWriteArrayList<Map<String, Integer>>();
    private Map<String, PeerConnection> peers = new ConcurrentHashMap<>();
    private Map<String, Set<PeerConnection>> all_gamers = new ConcurrentHashMap<>();
    private Map<String, List<String>> game_and_combine = new HashMap<>();

    public PeerManager(ClientConnection conn){this.connection = conn;}
    public PeerManager(String myName, int myPort, ClientConnection conn) {
        this.myName = myName;
        this.myPort = myPort;
        this.connection = conn;
    }

    public void setServerConnection(ClientConnection conn){this.connection = conn;}
    public ClientConnection getConnection(){return this.connection;}
    public void get_info_game(List<Map<String, Integer>> info_game){this.info_game = new ArrayList<>(info_game);}
    public void set_game_and_combine(Map<String, List<String>> content_reponse){this.game_and_combine = content_reponse;}
    public List<String> get_combine(String nomSalle){return this.game_and_combine.get(nomSalle);}
    public void add_gamer(String name_game, PeerConnection my_peer){

        all_gamers.computeIfAbsent(name_game, k -> new CopyOnWriteArraySet<PeerConnection>()).add(my_peer);
        int nbJoueurs = all_gamers.get(name_game).size();

    }

    public void reply_to_gamer(String name_game, String reponse){
        Set<PeerConnection> all_user = this.all_gamers.get(name_game);
        System.out.println("Je suis la");
        if (all_user == null) {
            return;
        }

        all_user.forEach(pc -> {

            pc.send(reponse);
        });
    }
    public void addPeer(String name, PeerConnection pc) {
        // Éviter d'ajouter deux fois le même joueur
        if (peers.containsKey(name)) {
            return;
        }
        peers.put(name, pc);
    }

    public void broadcast(String msg) {
        peers.forEach((name, pc) -> pc.send(msg));
    }

    public String send_reponse_to_gamer(List<String> list_reponse, String reponse_joueur){
        String reponse = "";
        List<String> list = Arrays.stream(reponse_joueur.split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        for(String i: list){

            int index = list_reponse.indexOf(i.trim());
            if(index != -1) {reponse += "|"+i+"|"+(index+1);}
        }
        return reponse;
    }

    private boolean is_egal_reponse(List<String> list_reponse, String reponse_joueur){
        List<String> list = Arrays.stream(reponse_joueur.split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        if(list_reponse.equals(list)){
            return true;
        }
        return false;
    }
    public boolean decrementerTentative(String nom_salle) {
        for (Map<String, Integer> game : info_game) {
            if (game.containsKey(nom_salle)) {
                //System.out.println("DEBUG: Salle trouvée ! Tentatives restantes avant : " );
                int actuel = game.get(nom_salle);
                if (actuel > 0) {
                    game.put(nom_salle, actuel - 1);
                    return true;
                }

            }
        }
        return false;
    }
    public void handleMessage(String from, String msg) {
        String[] parts = msg.split("\\|");

        System.out.println("Message est: "+msg);
        // Cas 1 : Nouveau joueur qui se présente
        if (msg.startsWith("GG|HELLO|")) {
            String nameSender = parts[2];
            String roomName = parts[3];

            PeerConnection pc = peers.get(nameSender);
            if (pc != null) {
                this.add_gamer(roomName, pc);
            }
        }

        // Cas 2 : Réception d'une tentative de jeu
        if (msg.startsWith("GG|SECRET|")) {
            String nom_de_la_salle = parts[2];
            String name_game = parts[3];
            String combinaison = parts[4];
            List<String> all_reponse = get_combine(nom_de_la_salle);
            PeerConnection origin = peers.get(from);

            if (combinaison != null){
                boolean is_correct = is_egal_reponse(all_reponse, combinaison);
                //add_gamer(nom_de_la_salle, origin);
                //System.out.println("Is correct: "+ is_correct);
                if (is_correct){
                    if(origin != null){
                        ClientConnection conn = getConnection();
                        conn.sendMessage("GG|WINNER|"+nom_de_la_salle);
                        origin.send("GG|FEEDBACK|WINNER");
                        //reply_to_gamer(name_game, "GG|FEEDBACK|WINNER");
                    }
                }else{
                    String reponse_au_joueur = send_reponse_to_gamer(all_reponse, combinaison);

                    if (origin != null) {
                        origin.send("GG|FEEDBACK" + reponse_au_joueur);
                    }
                }

            }else {
                origin.send("GG|FEEDBACK");
            }
        }


            /*
            //pcc.send("Reponse du serveur");
            //System.out.println("Mon pc est: " + pcc.getName());
            if (pcc != null) {
                System.out.println("[JEU] Envoi de la réponse à : " + name_gamer);
                pcc.send("GG|RESULT|Bulls:1|Cows:2"); // Utilise un vrai protocole !
            } else {
                // Option de secours : Si on ne trouve pas par le nom, on répond à celui qui a envoyé le message
                System.out.println("[DEBUG] Liste des pairs connus : " + peers.keySet());
                System.out.println("Ma combinaison es:t "+get_combine(nom_de_la_salle));
                // On essaie de récupérer la connexion de celui qui vient de nous parler

            }
            */


        //System.out.println("[P2P] Message de " + from + " : " + msg);
    }

    public void removePeer(String name) {
        peers.remove(name);
    }

    public void add_master_game(String name_game, String name_master){
        list_game_and_master.put(name_game, name_master);
    }

    public void send_combine(String name_game, String combine, String nameGamer) {
        String master = this.list_game_and_master.get(name_game);

        if (master == null) {
            return;
        }

        // On tente de récupérer la socket plusieurs fois (max 2 secondes)
        PeerConnection ma_socket = null;
        int tentatives = 0;

        while (ma_socket == null && tentatives < 10) {
            ma_socket = peers.get(master);
            if (ma_socket == null) {
                try {
                    Thread.sleep(200); // On attend 200ms avant de réessayer
                    tentatives++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (ma_socket != null) {
            ma_socket.send("GG|SECRET|"+name_game+"|"+ nameGamer+"|"+combine);
        } else {
            System.err.println("Erreur : Impossible de contacter le Master " + master + " après plusieurs tentatives.");
        }
    }
}