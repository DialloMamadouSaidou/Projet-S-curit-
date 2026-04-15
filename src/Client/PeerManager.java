import java.util.concurrent.ConcurrentHashMap;
import java.util.*;// Plus sûr pour les Threads

public class PeerManager {
    private String myName;
    private int myPort;
    private Map<String, String> list_game_and_master = new HashMap<>();

    // Utilise ConcurrentHashMap car PeerConnection tourne dans des threads séparés
    private Map<String, PeerConnection> peers = new ConcurrentHashMap<>();

    public PeerManager(){}

    public PeerManager(String myName, int myPort) {
        this.myName = myName;
        this.myPort = myPort;
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
        if (msg.startsWith("GG|SECRET_SET")) {
            System.out.println("Le joueur " + from + " a défini le secret !");
        }
        System.out.println("[P2P] Message de " + from + " : " + msg);
    }

    public void removePeer(String name) {
        peers.remove(name);
    }

    public void add_master_game(String name_game, String name_master){
        list_game_and_master.put(name_game, name_master);
    }

    public void send_combine(String name_game, String combine){

        String master = list_game_and_master.get(name_game);

        if(master != null){
            PeerConnection ma_socket = peers.get(master);
            ma_socket.send("GG|SECRET|"+combine);
        }

    }
}