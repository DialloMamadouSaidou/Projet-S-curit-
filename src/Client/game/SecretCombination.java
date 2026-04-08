package client.game;

import java.util.*;

public class SecretCombination {

    private List<Color> colors;

    public SecretCombination(List<Color> colors) {
        this.colors = new ArrayList<>(colors);
    }

    //  Générer une combinaison aléatoire
    public static SecretCombination random() {
        Random r = new Random();
        List<Color> colors = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            colors.add(Color.values()[r.nextInt(Color.values().length)]);
        }
        return new SecretCombination(colors);
    }

    //  Évaluer une guess 
    public Feedback evaluate(List<Color> guess) {

        int correctPositions = 0;
        int correctColors = 0;

        // Copies pour gérer les doublons
        List<Color> secretCopy = new ArrayList<>(colors);
        List<Color> guessCopy = new ArrayList<>(guess);

        // Compter les positions correctes
        for (int i = 0; i < 4; i++) {
            if (guessCopy.get(i) == secretCopy.get(i)) {
                correctPositions++;
                // Marquer comme utilisés
                secretCopy.set(i, null);
                guessCopy.set(i, null);
            }
        }

        // Compter les couleurs correctes 
        for (int i = 0; i < 4; i++) {
            Color g = guessCopy.get(i);
            if (g != null && secretCopy.contains(g)) {
                correctColors++;
                // Retirer la couleur trouvée pour éviter les doublons
                secretCopy.set(secretCopy.indexOf(g), null);
            }
        }

        return new Feedback(correctColors, correctPositions);
    }

    public List<Color> getColors() {
        return new ArrayList<>(colors);
    }
}
