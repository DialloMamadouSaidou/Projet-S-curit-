import java.util.concurrent.ConcurrentHashMap;
import java.util.*;// Plus sûr pour les Threads

public class PeerManager {
    private String myName;
    private int myPort;
    private Map<String, String> list_game_and_master = new ConcurrentHashMap<>();

    private Map<String, PeerConnection> peers = new ConcurrentHashMap<>();
    private Map<String, List<PeerConnection>> all_gamers = new ConcurrentHashMap<>();

    public PeerManager(){}
    public PeerManager(String myName, int myPort) {
        this.myName = myName;
        this.myPort = myPort;
    }

    public void add_gamer(String name_game, PeerConnection my_peer){

        all_gamers.computeIfAbsent(name_game, k -> new ArrayList<>()).add(my_peer);
    }

    public void reply_to_gamer(String name_game, String reponse){
        List<PeerConnection> all_user = this.all_gamers.get(name_game);

        if (all_user == null) {
            System.out.println("[DEBUG] Aucun joueur trouvé pour la salle : " + name_game);
            return;
        }

        System.out.println("[DEBUG] Envoi à " + all_user.size() + " joueurs dans " + name_game);
        all_user.forEach(pc -> {
            System.out.println("[DEBUG] Envoi vers : " + pc.getName());
            pc.send(reponse);
        });
    }
    public void addPeer(String name, PeerConnection pc) {
        // Éviter d'ajouter deux fois le même joueur
        if (peers.containsKey(name)) {
            System.out.println("Déjà connecté à " + name);
            return;
        }
        peers.put(name, pc);
        System.out.println("[P2P] " + name + " ajouté à la liste des pairs.");
    }

    public void broadcast(String msg) {
        System.out.println("[P2P] Envoi à tous : " + msg);
        peers.forEach((name, pc) -> pc.send(msg));
    }

    public void handleMessage(String from, String msg) {
        // C'est ici que tu vas gérer les règles du jeu (Mastermind / Guess Game)
        if (msg.startsWith("GG|SECRET|")) {
            String nom_de_la_salle = msg.split("\\|")[2];
            reply_to_gamer(nom_de_la_salle, "Reponse du serveur");

        }
        System.out.println("[P2P] Message de " + from + " : " + msg);
    }

    public void removePeer(String name) {
        peers.remove(name);
    }

    public void add_master_game(String name_game, String name_master){
        list_game_and_master.put(name_game, name_master);
    }

    public void send_combine(String name_game, String combine) {
        String master = this.list_game_and_master.get(name_game);
        System.out.println("Mon master est: "+ master);

        if (master == null) {
            System.err.println("Erreur : Aucun master enregistré pour la salle " + name_game);
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
            System.out.println("[P2P] Envoi de la combinaison au master : " + master);
            ma_socket.send("GG|SECRET|"+name_game+"|"+ combine);
        } else {
            System.err.println("Erreur : Impossible de contacter le Master " + master + " après plusieurs tentatives.");
        }
    }
}