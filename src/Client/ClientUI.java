import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientUI {

    private Scanner scanner;
    private ClientConnection connection;
    private MessageParser parser;

    //-----------------Pour la connexion Peer to Peer

    private int p2pPort;
    private PeerManager peerManager;
    private PeerListener peerListener;
    private String playerName;

    public ClientUI(){
        scanner = new Scanner(System.in);
        parser = new MessageParser();
        connection = new ClientConnection();
    }
    public void setName(String nameJoeur){this.playerName = nameJoeur;}

    private boolean connectToServer(){
        this.peerManager = new PeerManager();
        this.peerListener = new PeerListener(0, peerManager);
        this.peerListener.start();
        this.p2pPort = peerListener.getServerPort();

        System.out.println("IP du server : ");
        String ip = scanner.nextLine();

        System.out.println("Port du server : ");
        int port = Integer.parseInt(scanner.nextLine());

        boolean connected = connection.connect(ip, port);
        if(!connected){
            System.out.println(connection.getLastError());
            return false;
        }

        System.out.print("Nom du joueur :");
        String name = scanner.nextLine();
        setName(name);
        connection.sendMessage("GG|CONNECT|" + name + "|"+this.p2pPort);

        String response = connection.readMessage();

        parser.displayMessageDetails(response);
        return true;
    }

    private void connectToPeers(String playersInfo, String nameSalle, String nameMaster){
        String[] players = playersInfo.split(";");
        for (String p : players) {
            String[] details = p.split(":");
            String name = details[0].trim();
            int port = Integer.parseInt(details[1].trim());

            if(p.trim().isEmpty()){continue;}

            try {
                // On force l'usage de localhost
                Socket s = new Socket("127.0.0.1", port);
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                PrintWriter out = new PrintWriter(s.getOutputStream(), true);

                PeerConnection pc = new PeerConnection(s, peerManager, name, in, out);
                peerManager.addPeer(name, pc);
                pc.send("GG|HELLO|"+name);
                System.out.println("Name Salle: " + nameSalle);
                System.out.println("Mon name est: " + name);
                System.out.println("Mon name master est: "+ nameMaster);
                peerManager.add_gamer(nameSalle, pc);
                /*
                if (!name.equals(nameMaster)){

                    peerManager.add_gamer(nameSalle, pc);

                }
                *
                 */
                pc.start();



            } catch (IOException e) {
                System.out.println("Erreur de connexion locale à " + name);
            }
        }
    }
    private void mainMenu(){
        boolean running = true;

        while(running){
            System.out.println("\n=== MENU PRINCIPAL ===");
            System.out.println("1. Lister les salles");
            System.out.println("2. Créer une salle");
            System.out.println("3. Rejoindre une salle");
            System.out.println("4. Quitter une salle");
            System.out.println("5.Demarrer un jeu");
            System.out.println("0. Quitter");
            System.out.println("Entrez votre choix: ");

            String choice = scanner.nextLine();
            System.out.println("Mon choix est: "+ choice);
            switch(choice){
                case "1":
                    listRooms();
                    System.out.println("Choix 1");
                    break;
                case "2":
                    createRoom();
                    System.out.println("Choix 2");
                    break;
                case "3":
                    joinRoom();
                    System.out.println("Choix 3");
                    break;
                case "4":
                    Leave_room();
                    System.out.println("Choix 4");
                    break;
                case "5":
                    System.out.print("Entrez  le nom de la salle à laquelle vous voulez commencez la partie: ");
                    String name_salle = scanner.nextLine();
                    start_game(name_salle);
                    System.out.println("Diallo");
                    break;
                case "0":
                    running = false;
                    connection.closeConnection();
                    break;
                    default:
                        System.out.println("Choix invalide.");
            }
        }
    }

    public void start(){
        System.out.println("=== GUES GAME CLIENT ===");


        if(!connectToServer()){
            System.out.println("Impossible de se  connecter.");
            return;
        }
        mainMenu();
    }

    public void start_game(String nom_salle){
        System.out.println("Mamadou Saidou");
        String startMsg = "GG|GAME_START|";

        connection.sendMessage("GG|GAME_STARTED|"+nom_salle);
        if(peerManager != null){
            peerManager.broadcast(startMsg);
        }
        String message = connection.readMessage();
        System.out.println("Mon message est: " + message);
        Scanner scanner1 = new Scanner(System.in);
        if(message.equals("GG|CHOSE_COMBINATION")){
            System.out.println("Vous commencez la partie: entrez votre combinaison séparé par virgule: ");
            String comb = scanner1.nextLine();
            connection.sendMessage("GG|COMBINAISON|"+comb);
        }else if(message.startsWith("GG|SEND_MASTER|")){

            String[] parts = message.split("\\|");
            String masterName = parts[2];
            String playerInfo = parts[3];
            connectToPeers(playerInfo, nom_salle, masterName);

            peerManager.add_master_game(nom_salle, masterName);
            try { Thread.sleep(500); } catch (InterruptedException e) {}
            String combinaison;
            String choice;

            boolean again = true;
            while(again){
                System.out.println("Entrez votre combinaison pour plusieurs choix separé les avec des \",\": ");
                combinaison = scanner.nextLine();
                peerManager.send_combine(nom_salle, combinaison, playerName);
                System.out.print("Voulez-vous continuez (O/o) our (n/N): ");
                choice = scanner.nextLine();

                if (choice.equals("n") || choice.equals("N")) {
                    again = false;
                }
            }

        }

        parser.displayMessageDetails(message);
    }
    public void listRooms(){
        connection.sendMessage("GG|LIST_ROOM");
        String response = connection.readMessage();
        parser.displayMessageDetails(response);

    }

    public void createRoom(){
        System.out.println("Nom de la salle :");
        String room = scanner.nextLine();

        System.out.println("Max joueurs :");
        String maxPlayers = scanner.nextLine();

        System.out.println("Max players :");
        String maxAttemps = scanner.nextLine();

        String message = "GG|CREATE_ROOM|" + room + "|" + maxPlayers + "|" + maxAttemps;
        connection.sendMessage(message);
        String response = connection.readMessage();


        parser.displayMessageDetails(response);
    }

    private void joinRoom(){
        System.out.println("Nom de la salle :");
        String room = scanner.nextLine();

        connection.sendMessage("GG|JOIN_ROOM|" + room);
        String response = connection.readMessage();
        parser.displayMessageDetails(response);

    }

    private void Leave_room(){

        System.out.println("Nom de la salle : ");
        String room = scanner.nextLine().trim();
        connection.sendMessage("GG|LEAVE_ROOM|"+room);
        String reponse = connection.readMessage();
        parser.displayMessageDetails(reponse);
    }


}
