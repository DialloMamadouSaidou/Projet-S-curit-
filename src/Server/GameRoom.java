import java.util.ArrayList;
import java.util.List;

public class GameRoom {
    private String nomSalle;
    private int maxJoueurs;
    private int maxTentatives;
    private boolean is_starting = false;

    // Le créateur est l'administrateur
    private ClientHandlerTCP administrateur;
    private List<Joueur> listeJoueurs = new ArrayList<>();
    private String starteur;


    public GameRoom(String nom, int maxJ, int maxT, ClientHandlerTCP createur) {
        this.nomSalle = nom;
        this.maxJoueurs = maxJ;
        this.maxTentatives = maxT;
        this.administrateur = createur;
        this.listeJoueurs.add(new Joueur(createur.getNom(), createur.getP2pPort(), 0));
    }

    public boolean isAdmin(String name) {
        return this.administrateur.getNom().equals(name);
    }


    public String get_starteur(){return this.starteur;}
    public void setStarteur(String name_starteur){this.starteur = name_starteur;}
    public boolean is_gaming(){return this.is_starting;}
    public void start_game(){this.is_starting = true;}
    public void end_game(){this.is_starting = false;}
    public int getMaxTentatives(){return this.maxTentatives;}
    public boolean ajout_joueur(String nom_joueur, int port) {
        //ON ajoute un nouveau joueur si la limite nest pas atteinte.

        if(this.maxJoueurs > 0){
            this.listeJoueurs.add(new Joueur(nom_joueur, port, 0));
            this.maxJoueurs--;
            return true;
        }
        return false;
    }

    /*
      Le principe est simple
      un joueur ne commence son jeu que si et seulement si
      il fait partir de la salle

     */
    public boolean is_exist_in_sall(String nom_joueur){
        if (listeJoueurs == null) return false;

        for (Joueur val : listeJoueurs) {

            if (val != null && nom_joueur != null && nom_joueur.equals(val.getName_joueur())) {
                return true;
            }
        }
        return false;
    }

    public void quick_player(String player_name){
        remove_in_sall(player_name);
    }
    public void remove_in_sall(String nom_joueur){

        listeJoueurs.removeIf(joueur -> joueur.getName_joueur().equals(nom_joueur));

    }

    public String liste_joueur(){
        String all_joueur = "";

        for(Joueur val: this.listeJoueurs){
            all_joueur += val.getName_joueur() + ",";
        }

       return all_joueur;
    }

    public String liste_joueur_info() {

        String all_joueur = "";
        for (Joueur val : this.listeJoueurs) {
            all_joueur += val.getName_joueur() + ":" + val.getPort() + ";";
        }

        return all_joueur;
    }

    public void add_combinaison_to_admin(String nom_joeur, List<String> combinaison){

        for(Joueur j: listeJoueurs){

            if(j.name_joueur.equals(nom_joeur)){
                j.add_combinaision(this.nomSalle, combinaison);
            }
        }
    }
}