package client;

import java.io.*;
import java.net.Socket;

public class MainClient {

    // Même règle que PeerManager
    private static int getPortFor(String name) {
        name = name.trim();
        return 6000 + Math.abs(name.hashCode() % 1000);
    }

    public static void main(String[] args) {

        try {
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("=== Client Mastermind ===");
            System.out.print("Votre nom : ");
            String myName = console.readLine().trim();

            // Port P2P basé sur le nom (stable et identique à PeerManager)
            int peerPort = getPortFor(myName);
            System.out.println("Port P2P : " + peerPort);

            // Connexion au serveur
            Socket socket = new Socket("localhost", 5000);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            // Gestion P2P
            PeerManager peerManager = new PeerManager(myName, peerPort);

            // Lancement du listener P2P
            new PeerListener(peerPort, peerManager).start();

            // Envoi de CONNECT
            out.println("GG|CONNECT|" + myName);
            out.flush();

            // Thread de réception serveur
            new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        System.out.println("[SERVEUR] " + line);
                        handleServerMessage(line, peerManager);
                    }
                } catch (Exception e) {
                    System.out.println("Déconnecté du serveur.");
                }
            }).start();

            // === Boucle de commandes ===
            while (true) {
                String cmd = console.readLine();
                if (cmd == null) continue;

                // --- Commande P2P : GUESS ---
                if (cmd.startsWith("GG|GUESS|")) {
                    peerManager.broadcast(cmd);
                    continue;
                }

                // --- Commande P2P : SECRET_SET ---
                if (cmd.startsWith("GG|SECRET_SET|")) {
                    String[] p = cmd.split("\\|");
                    if (p.length == 3) {
                        peerManager.setMaster(p[2]);
                    }
                    peerManager.broadcast(cmd);
                    continue;
                }

                // --- Sinon : message pour le serveur ---
                out.println(cmd);
                out.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleServerMessage(String msg, PeerManager peerManager) {

        String[] parts = msg.split("\\|");
        if (!parts[0].equals("GG")) return;

        String type = parts[1];

        switch (type) {

            case "CONNECTED":
                System.out.println("Connecté au serveur en tant que " + parts[2]);
                break;

            case "ROOM_CREATED":
                System.out.println("Salle créée : " + parts[2]);
                break;

            case "ROOM_LIST":
                System.out.println("Salles disponibles : " + parts[2]);
                break;

            case "JOINED_ROOM":
                System.out.println("Vous avez rejoint la salle : " + parts[2]);
                System.out.println("Joueurs : " + parts[3]);
                break;

            case "LEFT_ROOM":
                System.out.println("Vous avez quitté la salle : " + parts[2]);
                break;

            case "GAME_STARTED":
                System.out.println("La partie commence !");
                peerManager.connectToPeers(parts[3]);
                break;

            case "PLAYER_KICKED":
                System.out.println("Joueur expulsé : " + parts[2]);
                peerManager.kickPlayer(parts[2]);
                break;

            case "SERVER_GAME_STARTED":
                System.out.println("Partie contre le serveur démarrée avec " + parts[2] + " tentatives.");
                break;
        }
    }
}
