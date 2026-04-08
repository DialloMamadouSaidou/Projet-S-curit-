package client;

import client.game.Color;
import client.game.SecretCombination;
import client.game.Feedback;

import java.net.Socket;
import java.util.*;

public class PeerManager {

    private String myName;
    private int myPort;

    private Map<String, PeerConnection> peers = new HashMap<>();

    private SecretCombination secret = null;
    private boolean iAmMaster = false;

    public PeerManager(String myName, int myPort) {
        this.myName = myName;
        this.myPort = myPort;
    }

    // Port P2P générique basé sur le nom
    private int getPortFor(String name) {
        name = name.trim();
        return 6000 + Math.abs(name.hashCode() % 1000);
    }

    public void addPeer(String name, PeerConnection pc) {
        peers.put(name, pc);
    }

    // Connexion aux pairs après GAME_STARTED
    public void connectToPeers(String csv) {
        String[] names = csv.split(",");
        for (String n : names) {
            n = n.trim();
            if (n.isEmpty() || n.equals(myName)) continue;

            try {
                int port = getPortFor(n);
                Socket s = new Socket("localhost", port);

                PeerConnection pc = new PeerConnection(s, this, n);
                peers.put(n, pc);
                pc.start();

                pc.send("GG|HELLO|" + myName);

                System.out.println("Connecté à " + n + " sur le port " + port);

            } catch (Exception e) {
                System.out.println("Impossible de se connecter à " + n + " : " + e.getMessage());
            }
        }
    }

    public void kickPlayer(String name) {
        PeerConnection pc = peers.remove(name);
        if (pc != null) {
            pc.close(); // Ferme la connexion
            System.out.println(name + " a été expulsé du P2P.");
        }
    }

    // Diffusion P2P
    public void broadcast(String msg) {
        for (PeerConnection pc : peers.values()) {
            pc.send(msg);
        }
    }

    // Définition du maître
    public void setMaster(String masterName) {
        if (masterName.equals(myName)) {
            iAmMaster = true;
            secret = SecretCombination.random();

            System.out.println("Je suis maître. Combinaison générée : " + secret.getColors());

        } else {
            System.out.println("Le maître est : " + masterName);
        }
    }

    // Réception d’un message P2P
    public void handleMessage(String from, String msg) {

        String[] p = msg.split("\\|");
        if (!p[0].equals("GG")) return;

        switch (p[1]) {

            case "SECRET_SET":
                setMaster(p[2]);
                break;

            case "GUESS":
                if (!iAmMaster || secret == null) return;

                List<Color> guess = new ArrayList<>();

                for (int i = 2; i < 6; i++) {
                    guess.add(Color.fromString(p[i]));
                }

                Feedback fb = secret.evaluate(guess);

                PeerConnection pc = peers.get(from);
                if (pc != null) {
                    pc.send("GG|FEEDBACK|" +
                            fb.getTotalCorrectColors() + "|" +
                            fb.getCorrectPositions());
                }

                System.out.println("Guess de " + from + " : " + guess +
                        " → couleurs=" + fb.getTotalCorrectColors() +
                        ", positions=" + fb.getCorrectPositions());

                break;

            case "FEEDBACK":
                String correctColors = p[2];
                String correctPositions = p[3];

                System.out.println("GG|FEEDBACK|" + correctColors + "|" + correctPositions);
                break;
        }
    }
}
