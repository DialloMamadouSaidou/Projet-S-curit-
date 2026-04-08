package client;

public class ClientConnection {

    private String lastError = "";

    public ClientConnection() {}

    public boolean connect(String ip, int port) {
        // TODO: implémentation réelle
        System.out.println("Connexion simulée à " + ip + ":" + port);
        return true; // Simule une connexion réussie
    }

    public String getLastError() {
        return lastError;
    }

    public void sendMessage(String msg) {
        System.out.println("Message envoyé (simulé) : " + msg);
    }

    public String readMessage() {
        return "SIMULATED_RESPONSE"; // Simule une réponse serveur
    }

    public void closeConnection() {
        System.out.println("Connexion fermée (simulée)");
    }
}
