package client;

import java.io.*;
import java.net.Socket;

public class PeerConnection extends Thread {

    private Socket socket;
    private PeerManager manager;
    private String peerName;

    private BufferedReader in;
    private PrintWriter out;

    public PeerConnection(Socket socket, PeerManager manager, String peerName) {
        this.socket = socket;
        this.manager = manager;
        this.peerName = peerName;

        try {
            // IMPORTANT : initialiser ici !
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream());
        } catch (Exception e) {
            System.out.println("Erreur initialisation PeerConnection pour " + peerName + " : " + e.getMessage());
        }
    }

    public void send(String msg) {
        if (out != null) {
            out.println(msg);
            out.flush();
        } else {
            System.out.println("ERREUR : out est null pour " + peerName);
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (Exception ignored) {}
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                manager.handleMessage(peerName, line);
            }
        } catch (Exception e) {
            System.out.println("Déconnexion de " + peerName);
        }
    }
}
