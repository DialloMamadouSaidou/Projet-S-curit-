package client.game;


public class Feedback {

    private final int totalCorrectColors;
    private final int correctPositions;

    public Feedback(int totalCorrectColors, int correctPositions) {
        this.totalCorrectColors = totalCorrectColors;
        this.correctPositions = correctPositions;
    }

    public int getTotalCorrectColors() {
        return totalCorrectColors;
    }

    public int getCorrectPositions() {
        return correctPositions;
    }

    @Override
    public String toString() {
        return "Feedback{colors=" + totalCorrectColors +
               ", positions=" + correctPositions + "}";
    }
}
