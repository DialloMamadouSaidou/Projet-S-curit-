import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MessageParser {
    //Cette variable est utilisé poour les jeux avec le serveur.
    private Map<String,List<String>> info_gaming = new ConcurrentHashMap<>();

    public Map<String,List<String>> get_info_gaming(){return this.info_gaming;}
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
        if(action.equals("WINNER") && parser_message.length == 3) return action;
        if(action.equals("QUICK_PLAYER")) return action;
        if(action.equals("GAME_WITH_SERVER")) return action;

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
            case "CREATE_ROOM":
                System.out.println("Mon message est: " + message);

                String name_salle = parts[2];
                int max_joueur = transform_string_in_int(parts[3]);
                int max_tentative = transform_string_in_int(parts[4]);

                if(max_joueur > 0 && max_tentative > 0){
                    boolean is_creat = context.create_salle(name_salle, max_joueur, max_tentative);

                    if(is_creat){
                        out.println("GG|ROOM|"+name_salle+"|CREATE");
                    }else{
                        out.println("GG|ROOM|"+name_salle+"|EXIST");
                    }

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

                if(ma_game != null){
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
                }else {
                    out.println("GG|SALL_NOT_EXIST");
                }

                //Je dois envoyé ici toute les informations pour commencer le peer to peer
            case "WINNER":
                String nom_salle = parts[2];
                GameRoom ma_games = context.get_game_room(nom_salle);
                if(ma_games != null){
                    ma_games.end_game();
                }

                break;

            case "QUICK_PLAYER":
                String rooom = parts[2];
                String player_to_quick = parts[3];

                boolean answer = context.quick_player(rooom, player_to_quick);
                if(answer){
                    out.println("GG|PLAYER_QUICK");
                }else{
                    out.println("GG|YOU_ARE_NOT_A_ADMIN");
                }
                //GameRoom salle_concerne = context.get_game_room()

            case "GAME_WITH_SERVER":
                String name_joueur = parts[2];
                String combine = parts[3];
                System.out.println("Jeu avec le server");

                List<String> combinaison = obtien_joueur(name_joueur);

                if(combinaison == null){
                    List<String> temp_list = put_info_for_gamer();
                    System.out.println(temp_list);
                    ajout_combinaison(name_joueur, temp_list);

                    boolean reponses = is_egal_reponse(temp_list, combine);

                    if(reponses){
                        out.println("GG|WINNER");
                        //-On redemarre un autre level pour autre cas

                        temp_list = put_info_for_gamer();
                        ajout_combinaison(name_joueur, temp_list);
                    }else {
                        String reponse_to_joueur = send_reponse_to_gamer(temp_list, combine);
                        out.println("GG|FEEDBACK|"+reponse_to_joueur);
                    }
                }else {

                    //ajout_combinaison(name_joueur, temp_list);

                    boolean reponses = is_egal_reponse(combinaison, combine);

                    if(reponses){
                        out.println("GG|WINNER");
                        //-On redemarre un autre level pour autre cas

                        List<String> temp_list = put_info_for_gamer();
                        ajout_combinaison(name_joueur, temp_list);
                    }else {
                        String reponse_to_joueur = send_reponse_to_gamer(combinaison, combine);
                        out.println("GG|FEEDBACK|"+reponse_to_joueur);
                    }
                }
                break;

            default:
                System.out.println("Choix invalide");
        }
        out.flush();

    }

    public List<String> obtien_joueur(String nom_joeur){
        return this.info_gaming.get(nom_joeur);
    }

    public void ajout_combinaison(String nom_joueur, List<String> combine){
        this.info_gaming.put(nom_joueur, combine);
    }
    public List<String> put_info_for_gamer() {
        // Un large choix d'éléments pour augmenter la difficulté
        List<String> options = Arrays.asList(
                "Rouge", "Bleu", "Vert", "Jaune", "Orange",
                "Violet", "Rose", "Marron", "Gris", "Cyan"
        );

        // On mélange la liste
        Collections.shuffle(options);

        // On ne prend que les 4 premiers après le mélange
        return options.stream()
                .limit(4)
                .collect(Collectors.toList());
    }

    private boolean is_egal_reponse(List<String> list_reponse, String reponse_joueur){
        List<String> list = Arrays.stream(reponse_joueur.split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        if(list_reponse.equals(list)){
            return true;
        }
        return false;
    }

    public String send_reponse_to_gamer(List<String> list_reponse, String reponse_joueur){
        String reponse = "";
        List<String> list = Arrays.stream(reponse_joueur.split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        for(String i: list){

            int index = list_reponse.indexOf(i.trim());
            if(index != -1) {reponse += "|"+i+"|"+(index+1);}
        }
        return reponse;
    }
}
