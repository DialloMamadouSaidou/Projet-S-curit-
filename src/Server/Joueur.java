import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

public class Joueur {

    public String name_joueur;
    private int port;
    private int nombre_tentative;

    private Map<String, List<String>> all_reponse = new ConcurrentHashMap<>();

    public Joueur(String name_joueur, int port, int nombre_tentative) {
        this.name_joueur = name_joueur;
        this.port =  port;
        this.nombre_tentative = nombre_tentative;
    }

    public String getName_joueur(){return name_joueur;}
    public int getPort(){return port;}
    public int getNombre_tentative(){return nombre_tentative;}

    public void add_combinaision(String name_game, List<String> combinaison){

        all_reponse.put(name_game, combinaison);
    }
}
