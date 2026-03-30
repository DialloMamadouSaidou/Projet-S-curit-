import java.io.*;
import java.net.*;

import java.util.*;


public class TCPClientHandler {

    public static String muterChaine(String mot){

        Random random = new Random();

        char[] caracteres = mot.toCharArray();

        int indexAleatoire = random.nextInt(caracteres.length);

        char nouvelle_lettre = (char) ('a' + random.nextInt(26));

        while (nouvelle_lettre == caracteres[indexAleatoire]) {
            nouvelle_lettre = (char) ('a' + random.nextInt(26));
        }
        caracteres[indexAleatoire] = nouvelle_lettre;

        return new String(caracteres);
    }
    public void handle(BufferedReader in, PrintWriter out){
        Random random = new Random();


        try{

            String message = in.readLine();

            System.out.println("Le client a dit: " + message);
            if(message.contains("REQ:")){
                int K = random.nextInt(20) + 1;
                String original = message.split(":")[1];
                String versionMute = original;
                String historique = original;

                for(int i=1; i<K; i++){
                    versionMute = muterChaine(original) ;
                    historique += " : " + versionMute;
                }


                out.println("RESP: " + historique);
            }else{
                out.println("Message non conforme.");
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}