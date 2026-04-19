
import java.io.*;
import java.net.*;
import java.util.concurrent.CountDownLatch;

public class PeerListener extends Thread {

    private int port;
    private PeerManager manager;
    private ServerSocket serverSocket;
    // Le latch permet d'attendre que le serveur soit prêt avant de lire le port
    private final CountDownLatch latch = new CountDownLatch(1);

    public PeerListener(int port, PeerManager manager) {
        this.port = port;
        this.manager = manager;
    }

    public int getServerPort() {
        try {
            // On attend que le serveur soit initialisé dans le run()
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return serverSocket.getLocalPort();
    }

    @Override
    public void run() {
        try {
            // Création du socket (si port=0, le système en choisit un libre)
            this.serverSocket = new ServerSocket(this.port);

            // On libère le latch : le port est maintenant disponible pour ClientUI
            latch.countDown();

            while (!serverSocket.isClosed()) {
                Socket s = serverSocket.accept();

                // Lecture du message HELLO pour identifier le pair
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                PrintWriter out = new PrintWriter(s.getOutputStream(), true);

                String hello = in.readLine();

                //System.out.println("Mon message recu de ma connection " + hello);
                if (hello != null && hello.startsWith("GG|HELLO|")) {
                    String remotePeerName = hello.split("\\|")[2].trim();

                    // On crée la connexion et on l'ajoute au manager
                    PeerConnection pc = new PeerConnection(s, manager, remotePeerName, in, out);
                    manager.addPeer(remotePeerName, pc);
                    pc.start();

                    //System.out.println("Nouveau pair connecté : " + remotePeerName);
                }else{
                    System.out.println("Autre conneion");
                }
            }
        } catch (IOException e) {
            //System.out.println("Mon message derreur est: "+e);
            if (!serverSocket.isClosed()) {
                System.out.println("Erreur PeerListener : " + e.getMessage());
            }
        }
    }

    public void stopListener() {
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}