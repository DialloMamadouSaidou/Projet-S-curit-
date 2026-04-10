import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;


public class MessageParser {

    public boolean isValidMessage(String message){
        if(message == null || message.trim().isEmpty()){
            return false;
        }

        String [] champ_message = message.split("\\|");
        return message.startsWith("GG|") && champ_message.length >= 2;
    }

    public String[] getParts(String message){
        if(!isValidMessage(message)){
            return new String[0];
        }
        return  message.split("\\|");
    }

    public  String getField(String message){
        String[] parts = getParts(message);
        int index = 0;
        if (index<parts.length){
            return parts[index];
        }
        return null;
    }

    //Gestion des requestes
    /*
    *  Pour les requêtes envoyé au serveur, le constat est le suivant:
    * GG|CONNECT,
    * GG|CREATE_ROOM|NOM_SALL|
    * GG|LIST_ROOM
    * GG|JOIN_ROOM|NOM_SALLE
    * GG|LEAVE_ROOM
    * GG|KICK_ROOM
    * ON remarque chaque requete contient aux moins deux champs
    * si le split s'est fait par |
    * */

    public String getMessageType(String message){
        String[] parts = getParts(message);

        if(parts.length >= 2){
            return parts[1];
        }
        return null;
    }

    public String typeMessage(String message) {

        if(!isValidMessage(message)){
            return null;
        }
        String[] parser_message = message.split("\\|");

        String action = parser_message[1].toUpperCase();

        //je recupere ici les requete qui demandent seulement deux paramètres.
        List<String> all_request = List.of("CONNECT", "LIST_ROOM", "LEAVE_ROOM", "QUICK_ROOM");


        if(all_request.contains(action)){
            return action;
        }
        if (action.equals("CREATE_ROOM") && parser_message.length == 5) return action;

        if (action.equals("JOIN_ROOM") && parser_message.length == 3) return action;

        return null;

    }
    public List<String> parseListField(String field){
        List<String> items = new ArrayList<>();

        if(field == null || field.trim().isEmpty()){
            return items;
        }
        String[] parts = field.split(",");
        for(String part : parts){
            items.add(part.trim());
        }
        return items;
    }

    public boolean hasMinimumFields(String message, int minFields){
        String[] parts = getParts(message);
        return parts.length >= minFields;
    }

    public void displayMessageDetails(String message){
        if(!isValidMessage(message)){
            System.out.println("Message invalide");
            return;
        }
        String[] parts = getParts(message);

        System.out.println("Message reçu : " + message);

        //for(int i =2 ; i <parts.length; i++){
           // System.out.println("Champ " + (i - 1) + ": " + parts[i]);
        //}

    }

    public void handle(String message, PrintWriter out, ClientHandlerTCP context){
        String salle;
        boolean reponse;
        System.out.println("Les parties de mon message sont: " + String.join(" ", typeMessage(message)));
        String[] parts = getParts(message);
        String mon_message = typeMessage(message);

        switch (mon_message){
            case "CONNECT":
                String nom = message.split("\\|")[2].trim();
                context.setNom(nom);

                out.println("GG|OK|Bienvenue " + nom);
                break;
            case "CREATE_ROOM":
                System.out.println("Mon message est: " + message);

                String name_salle = parts[2];
                int max_joueur = Integer.parseInt(parts[3]);
                int max_tentative = Integer.parseInt(parts[4]);

                context.create_salle(name_salle, max_joueur, max_tentative);
                out.println(message);

                break;

            case "LIST_ROOM":
                String valeur = "";
                for(String val: context.liste_salle_active()){
                    System.out.println(" - " + val);
                    valeur += val + ", " ;
                }
                out.println("GG|ROOM_LIST|"+valeur);
                break;

            case "JOIN_ROOM":
                salle = parts[2];
                System.out.println("Mon nom :" + context.getNom());
                reponse = context.add_player_to_room(salle, context.getNom());

                if (reponse){
                    GameRoom temp = context.get_game_room(salle);
                    String list_joueur = temp.liste_joueur();
                    out.println("GG|JOINED|"+salle+"|"+list_joueur);
                }else{
                    out.println("GG|NOTJOINED");
                }
                break;

            case "LEAVE_ROOM":
                salle = parts[2];
                reponse = context.remove_user_in_salle(salle, context.getNom());
                if(reponse){
                    out.println("GG|LEFT_ROOM|"+salle);
                }else{
                    out.println("GG|NOTCONTENT|"+salle);
                }
                break;
            default:
                System.out.println("Choix invalide");
        }
        out.flush();

    }

}