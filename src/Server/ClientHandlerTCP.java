import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandlerTCP implements Runnable {

    private Socket clientSocket;
    private String nom;
    private int p2pPort;
    private String ipClient;
    private Map<String, ClientHandlerTCP> clientMap;
    private Map<String, GameRoom> client_active;
    private MessageParser logicHandler;

    public void setSocket(Socket ss){
        this.clientSocket = ss;
    }

     public void setClientMap(Map<String, ClientHandlerTCP> clientMap){
        this.clientMap = clientMap;
     }
     public void setClientActive(Map<String, GameRoom> clientActive){
        this.client_active = clientActive;
     }
     public Map<String, GameRoom> getClient_active(){return this.client_active;}
    public void setNom(String Nom){this.nom = Nom;}
    public String getNom(){return this.nom;}

    public void setupP2P(String nom, int port){
        this.nom = nom;
        this.p2pPort =  port;
        this.ipClient = clientSocket.getInetAddress().getHostAddress();
    }
    public void create_salle(String name_salle, int max_joueur,  int max_tentatives) {

        GameRoom temp = new GameRoom(name_salle, max_joueur, max_tentatives, this);
        client_active.put(name_salle, temp);
    }

    public boolean remove_user_in_salle(String name_salle, String name_joueur){
        GameRoom ma_game = client_active.get(name_salle);

        if(ma_game != null){
            ma_game.remove_in_sall(name_joueur);
            return true;
        }
        return false;
    }
    public Set<String> liste_salle_active(){
        return this.client_active.keySet();
    }

    public GameRoom get_game_room(String nameSalle){

        return client_active.get(nameSalle);
    }
    public int add_player_to_room(String nom_salle, String nom_joueur){

        /*
            ON verifie dabord si le client existe, pour ne pas se rajouter plusieurs fois dans
            la meme salle
           On retourne 1, si l'ajout du joeueur sest bien passé
           -1 si la salle nexiste pas,
           0 si la salle est déjà atteinte
         */
        GameRoom ma_game_room = this.client_active.get(nom_salle);

        if (ma_game_room != null){
            boolean exist = ma_game_room.is_exist_in_sall(nom_joueur);

            if(exist){
                return -2;
            }
            boolean reponse = ma_game_room.ajout_joueur(nom_joueur, p2pPort);
            if(reponse){
                return 1;
            }else{
                return 0;
            }
        }
        return -1;
    }

    public void add_combinaison_in_sall(String nameSalle, List<String>combine){

        GameRoom ma_game_room = this.client_active.get(nameSalle);
        ma_game_room.add_combinaison_to_admin(this.nom, combine);
    }
    public void setHandler(MessageParser handler){
        this.logicHandler = handler;
    }
    public int getP2pPort() { return p2pPort; }
    public String getIpClient() { return ipClient; }

    public void run(){

        try(
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                ){
            String messageRecu;
            while((messageRecu = in.readLine()) != null){
                logicHandler.handle(messageRecu, out, this);
            }

        }catch (IOException e){
            e.printStackTrace();
        }finally{
            desinscrire();
        }
    }

    public void desinscrire(){

        if(clientMap != null && nom != null){
            clientMap.remove(nom);
            System.out.println("Client rétiré de la liste du serveur");
        }
        try {
            if(clientSocket != null && !clientSocket.isClosed()){
                clientSocket.close();
            }
        }catch(IOException e){e.printStackTrace();}
    }
}