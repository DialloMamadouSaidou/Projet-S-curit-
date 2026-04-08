package client;

import java.io.*;
import java.net.*;

public class PeerListener extends Thread {

    private int port;
    private PeerManager manager;

    public PeerListener(int port, PeerManager manager) {
        this.port = port;
        this.manager = manager;
    }

    @Override
    public void run() {
        try (ServerSocket ss = new ServerSocket(port)) {

            System.out.println("PeerListener en écoute sur le port " + port);

            while (true) {
                Socket s = ss.accept();

                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                PrintWriter out = new PrintWriter(s.getOutputStream());

                // Le premier message doit être : GG|HELLO|Nom
                String hello = in.readLine();
                if (hello == null) continue;

                String[] p = hello.split("\\|");
                if (p.length < 3 || !p[0].equals("GG") || !p[1].equals("HELLO")) {
                    System.out.println("Message HELLO invalide reçu : " + hello);
                    s.close();
                    continue;
                }

                String peerName = p[2].trim();

                // Création de la connexion P2P
                PeerConnection pc = new PeerConnection(s, manager, peerName);
                manager.addPeer(peerName, pc);
                pc.start();

                System.out.println("Connexion P2P entrante depuis " + peerName);
            }

        } catch (Exception e) {
            System.out.println("Erreur PeerListener : " + e.getMessage());
        }
    }
}
