package client;

public class MessageParser {

    public MessageParser() {}

    public void displayMessageDetails(String msg) {
        System.out.println("Message reçu : " + msg);
    }

    public String getMessageType(String msg) {
        String[] parts = msg.split("\\|");
        return parts.length > 1 ? parts[1] : "";
    }

    public String getField(String msg, int index) {
        String[] parts = msg.split("\\|");
        return parts.length > index ? parts[index] : "";
    }

    public String[] parseListField(String field) {
        return field.split(",");
    }
}
