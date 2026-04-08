package client.game;

public enum Color {
    RED, GREEN, BLUE, YELLOW, ORANGE;

    public static Color fromString(String s) {
        return Color.valueOf(s.toUpperCase());
    }

    @Override
    public String toString() {
        return name();
    }
}
