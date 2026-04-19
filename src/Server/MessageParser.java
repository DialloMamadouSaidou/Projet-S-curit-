import java.io.*;
import java.util.*;

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
        List<String> all_request = List.of("CONNECT", "LIST_ROOM", "LEAVE_ROOM", "QUICK_ROOM", "GAME_STARTED", "COMBINAISON");


        if(all_request.contains(action)){
            return action;
        }
        if (action.equals("CREATE_ROOM") && parser_message.length == 5) return action;

        if (action.equals("JOIN_ROOM") && parser_message.length == 3) return action;

        return null;

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

    private int transform_string_in_int(String valeur){
        int content;
        try {
            content = Integer.parseInt(valeur);
        }catch(NumberFormatException e){
            content = -1;
        }
        return content;
    }
    public void handle(String message, PrintWriter out, ClientHandlerTCP context){
        System.out.println("Mon message est: "+ message);
        String salle;
        boolean reponse;
        System.out.println("Les parties de mon message sont: " + String.join(" ", typeMessage(message)));
        String[] parts = getParts(message);
        String mon_message = typeMessage(message);
        System.out.println("Mon message est: "+ mon_message);
        switch (mon_message){
            case "CONNECT":
                String nom = message.split("\\|")[2].trim();
                int port = Integer.parseInt(message.split("\\|")[3].trim());

                context.setupP2P(nom, port);

                context.setNom(nom);

                out.println("GG|OK|Bienvenue " + nom);
                break;
            case "COMBINAISON":
                System.out.println("Ma combinaison");
                break;
            case "CREATE_ROOM":
                System.out.println("Mon message est: " + message);

                String name_salle = parts[2];
                int max_joueur = transform_string_in_int(parts[3]);
                int max_tentative = transform_string_in_int(parts[4]);

                if(max_joueur > 0 && max_tentative > 0){
                    context.create_salle(name_salle, max_joueur, max_tentative);
                    out.println(message);
                }else{
                    out.println("GG|FORMAT_ERROR|PAS|STRING_0_FLOAT");
                }
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
                int reponse_context = context.add_player_to_room(salle, context.getNom());

                if (reponse_context == 1){
                    GameRoom temp = context.get_game_room(salle);
                    String list_joueur = temp.liste_joueur();
                    out.println("GG|JOINED|"+salle+"|"+list_joueur);
                }else if(reponse_context == 0){
                    out.println("GG|SALLE_REMPLI");
                }
                else if(reponse_context == -2){
                    out.println("GG|JOUEUR_EXISTE_DEJA");
                }
                else{
                    out.println("GG|SALLNOTEXIST");
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

            case "GAME_STARTED":
                salle = parts[2];
                GameRoom ma_game = context.get_game_room(salle);

                reponse = ma_game.is_exist_in_sall(context.getNom());

                if (reponse){
                    boolean is_starting = ma_game.is_gaming();
                    //Scanner scanner = new Scanner(System.in);
                    //
                    System.out.println(is_starting);
                    System.out.println("Mon joueur est: "+ context.getNom());
                    System.out.println("Mon starteur est: "+ ma_game.get_starteur());
                    if (!is_starting){
                        ma_game.start_game();
                        ma_game.setStarteur(context.getNom());
                    /*
                    System.out.println("Entrez votre combinaison secrete separé par des virgules \",\": ");
                    String combinaison = scanner.nextLine().trim();
                    List<String> combine = Arrays.asList(combinaison.split(","));
                    context.add_combinaison_in_sall(salle, combine);

                     */
                        out.println("GG|CHOSE_COMBINATION");
                        //out.println("GG|GAME_STARTED");
                    }else{
                        String master = ma_game.get_starteur().trim();

                        if(master.equals(context.getNom().trim())){
                            out.println("GG|YOU_ARE_A_MASTER");
                        }else{

                            out.println("GG|SEND_MASTER|"+ master + "|"+ma_game.liste_joueur_info()+"|"+ma_game.getMaxTentatives());
                        }
                    }
                }else{
                        out.println("GG|VOUS_NEPOUVEZ_PAS_COMMENCEZ_LA_PARTIE_VOUS_NETES_PAS_DE_LA_SALLE");
                }
                //Je dois envoyé ici toute les informations pour commencer le peer to peer


            default:
                System.out.println("Choix invalide");
        }
        out.flush();

    }

}
