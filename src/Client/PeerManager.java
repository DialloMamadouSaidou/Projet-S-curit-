import java.util.concurrent.ConcurrentHashMap;
import java.util.*;// Plus sûr pour les Threads
import java.util.concurrent.CopyOnWriteArrayList;

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

        all_gamers.computeIfAbsent(name_game, k -> new CopyOnWriteArrayList<PeerConnection>()).add(my_peer);
        System.out.println("[DEBUG] Joueur " + my_peer.getName() + " ajouté à la salle " + name_game);
        int nbJoueurs = all_gamers.get(name_game).size();
        System.out.println("[DEBUG] Salle " + name_game + " a maintenant " + nbJoueurs + " joueurs.");

    }

    public void reply_to_gamer(String name_game, String reponse){
        List<PeerConnection> all_user = this.all_gamers.get(name_game);
        System.out.println("Je suis la");
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
        String[] parts = msg.split("\\|");

        System.out.println("Mon message du print est: "+msg);
        // Cas 1 : Nouveau joueur qui se présente
        if (msg.startsWith("GG|HELLO|")) {
            String nameSender = parts[2];
            String roomName = parts[3];

            PeerConnection pc = peers.get(nameSender);
            if (pc != null) {
                this.add_gamer(roomName, pc);
                System.out.println("[MASTER] Nouveau joueur enregistré dans la salle : " + roomName);
            }
        }

        // Cas 2 : Réception d'une tentative de jeu
        if (msg.startsWith("GG|SECRET|")) {
            String nom_de_la_salle = parts[2];
            String name_gamer = parts[3];
            //String combinaison = parts[3];
            PeerConnection pcc = peers.get(name_gamer);
            //pcc.send("Reponse du serveur");
            //System.out.println("Mon pc est: " + pcc.getName());
            if (pcc != null) {
                System.out.println("[JEU] Envoi de la réponse à : " + name_gamer);
                pcc.send("GG|RESULT|Bulls:1|Cows:2"); // Utilise un vrai protocole !
            } else {
                // Option de secours : Si on ne trouve pas par le nom, on répond à celui qui a envoyé le message
                System.err.println("[ERREUR] Impossible de trouver le pair : " + name_gamer);
                System.out.println("[DEBUG] Liste des pairs connus : " + peers.keySet());

                // On essaie de récupérer la connexion de celui qui vient de nous parler
                PeerConnection origin = peers.get(from);
                if (origin != null) {
                    origin.send("Erreur : Je ne te reconnais pas sous le nom " + name_gamer);
                }
            }
            System.out.println("[JEU] Secret reçu pour la salle " + nom_de_la_salle);
            //reply_to_gamer(nom_de_la_salle, "GG|RESULT|Bulls:1|Cows:2");
        }
        System.out.println("[P2P] Message de " + from + " : " + msg);
    }

    public void removePeer(String name) {
        peers.remove(name);
    }

    public void add_master_game(String name_game, String name_master){
        list_game_and_master.put(name_game, name_master);
    }

    public void send_combine(String name_game, String combine, String nameGamer) {
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
            ma_socket.send("GG|SECRET|"+name_game+"|"+ nameGamer+"|"+combine);
        } else {
            System.err.println("Erreur : Impossible de contacter le Master " + master + " après plusieurs tentatives.");
        }
    }
}