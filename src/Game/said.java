import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class said{

    public static void main(String[] args) {
        System.out.println("hello world");

        Map<String, List<String>>all_game = new ConcurrentHashMap<>();

        all_game.computeIfAbsent("saidou", k -> new ArrayList<>()).add("diallo");
        all_game.computeIfAbsent("saidou", k -> new ArrayList<>()).add("baba");
        List<String> maListe = all_game.get("saidou");

        if (maListe != null) {
            for (String nom : maListe) {
                System.out.println("Élément trouvé : " + nom);
            }
        } else {
            System.out.println("La clé 'saidou' n'existe pas.");
        }

    }
}