import java.util.ArrayList;
import java.util.List;

public class GameRoom {
    private String nomSalle;
    private int maxJoueurs;
    private int maxTentatives;

    // Le créateur est l'administrateur
    private ClientHandlerTCP administrateur;
    private List<String> listeJoueurs;

    public GameRoom(String nom, int maxJ, int maxT, ClientHandlerTCP createur) {
        this.nomSalle = nom;
        this.maxJoueurs = maxJ;
        this.maxTentatives = maxT;
        this.administrateur = createur;
        this.listeJoueurs = new ArrayList<>();
        this.listeJoueurs.add(createur.getNom());
    }

    public boolean isAdmin(String name) {
        return this.administrateur.getNom().equals(name);
    }

    public void ajout_joueur(String nom_joueur) {
        this.listeJoueurs.add(nom_joueur);
    }

    public void remove_in_sall(String nom_joueur){
        this.listeJoueurs.remove(nom_joueur);
    }

    public String liste_joueur(){
        String all_joueur = "";

        for(String val: this.listeJoueurs){
            all_joueur += val + ",";
        }

       return all_joueur;
    }
}