import java.io.*;
import java.net.Socket;

public class PeerConnection extends Thread {

    private Socket socket;
    private String peerName;
    private PeerManager manager;

    private BufferedReader in;
    private PrintWriter out;


    public PeerConnection(Socket socket, PeerManager manager) {
        this.socket = socket;
        this.manager = manager;
    }

    public PeerConnection(Socket socket, PeerManager manager, String peerName, BufferedReader in, PrintWriter out) {
        this.socket = socket;
        this.manager = manager;
        this.peerName = peerName;
        this.in = in;
        this.out = out;
    }

    public void setSocket(Socket socket){this.socket = socket;}

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
            System.out.println("Mon erreur es: "+e);
            System.out.println("Déconnexion de " + peerName);
        }
    }
}