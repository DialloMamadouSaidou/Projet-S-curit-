import java.io.*;
import java.util.List;
import java.util.ArrayList;


public class MessageParser {

    public boolean isValidMessage(String message){
        if(message == null || message.trim().isEmpty()){
            return false;
        }

        return message.startsWith("GG|");
    }

    public String[] getParts(String message){
        if(!isValidMessage(message)){
            return new String[0];
        }
        return  message.split("\\|");
    }

    public String getMessageType(String message){
        String[] parts = getParts(message);

        if(parts.length >= 2){
            return parts[1];
        }
        return null;
    }

    public  String getField(String message, int index){
        String[] parts = getParts(message);

        if (index>=0 && index<parts.length){
            return parts[index];
        }
        return null;
    }

    public List<String> parseListField(String field){
        List<String> items = new ArrayList<>();

        if(field == null || field.trim().isEmpty()){
            return items;
        }
        String[] parts = field.split(",");
        for(String part : parts){
            items.add(part.trim());
        }
        return items;
    }

    public boolean hasMinimumFields(String message, int minFields){
        String[] parts = getParts(message);
        return parts.length >= minFields;
    }

    public void displayMessageDetails(String message){
        if(!isValidMessage(message)){
            System.out.println("Message invalide");
            return;
        }
        String[] parts = getParts(message);

        System.out.println("Message reçu : " + message);
        System.out.println("Type : " + getMessageType(message));

        for(int i =2 ; i <parts.length; i++){
            System.out.println("Champ " + (i - 1) + ": " + parts[i]);
        }
    }

    public void handle(BufferedReader in, PrintWriter out){

        try{
            String message = in.readLine();

            System.out.println("Message reçu: " + message);

            out.println(message);
        }catch(IOException e){
            e.printStackTrace();
        }


    }

}