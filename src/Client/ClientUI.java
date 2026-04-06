import java.awt.*;
import java.util.Scanner;

public class ClientUI {

    private Scanner scanner;
    private ClientConnection connection;
    private MessageParser parser;

    public ClientUI(){
        scanner = new Scanner(System.in);
        parser = new MessageParser();
        connection = new ClientConnection();
    }

    private boolean connectToServer(){
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

        connection.sendMessage("GG|CONNECT| " + name);

        String response = connection.readMessage();

        parser.displayMessageDetails(response);
        return true;
    }

    private void mainMenu(){
        boolean running = true;

        while(running){
            System.out.println("\n=== MENU PRINCIPAL ===");
            System.out.println("1. Lister les salles");
            System.out.println("2. Créer une salle");
            System.out.println("3. Rejoindre une salle");
            System.out.println("0. Quitter");

            System.out.println("Entrez votre choix: ");

            String choice = scanner.nextLine();
            System.out.println("Mon choix est: "+ choice);
            switch(choice){
                case "1":
                    //listRooms();
                    System.out.println("Choix 1");
                    break;
                case "2":
                    //createRoom();
                    System.out.println("Choix 2");
                    break;
                case "3":
                    //joinRoom();
                    System.out.println("Choix 4");
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

    public void listRooms(){
        connection.sendMessage("GG|LIST ROOMS");
        String response = connection.readMessage();
        parser.displayMessageDetails(response);

        if(parser.getMessageType(response).equals("ROOM_LIST")){
            String field= parser.getField(response, 2);

            for(String room : parser.parseListField(field)){
                System.out.println("- " + room);
            }
        }
    }

    public void createRoom(){
        System.out.println("Nom de la salle :");
        String room = scanner.nextLine();

        System.out.println("Max joueurs :");
        String maxPlayers = scanner.nextLine();

        System.out.println("Max players :");
        String maxAttemps = scanner.nextLine();

        String message = "GG|CREATE_ROOM| " + room + "|" + maxPlayers + "|" + maxAttemps;
        connection.sendMessage(message);
        String response = connection.readMessage();
        parser.displayMessageDetails(response);
    }

    private void joinRoom(){
        System.out.println("Nom de la salle :");
        String room = scanner.nextLine();

        connection.sendMessage("GG|JOIN ROOM| " + room);
        String response = connection.readMessage();
        parser.displayMessageDetails(response);
        if(parser.getMessageType(response).equals("ROOM_JOIN")){
            String playersField = parser.getField(response, 3);

            System.out.println("Joueurs dans la salle :");

            for(String player : parser.parseListField(playersField)){
                System.out.println("- " + player);
            }
        }
    }


}
