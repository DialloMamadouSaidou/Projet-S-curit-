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
    public void ajout_joueur(String nom_joueur, int port) {
        this.listeJoueurs.add(new Joueur(nom_joueur, port, 0));
    }

    public void remove_in_sall(String nom_joueur){

        for(Joueur val: listeJoueurs){

            if(val.getName_joueur().equals(nom_joueur)){
                listeJoueurs.remove(this);
            }
        }

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