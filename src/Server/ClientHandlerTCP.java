import java.io.*;
import java.net.*;
import java.util.List;

public class ClientHandlerTCP implements Runnable {

    private Socket clientSocket;
    private Object server;
    private List<ClientHandlerTCP> clientList;
    private MessageParser logicHandler;

    public void addInput(Object input){
        if(input instanceof Socket){
            this.clientSocket = (Socket) input;
        }else if(input instanceof List){
            this.clientList = (List<ClientHandlerTCP>) input;
        }else{
            this.server = input;
        }
    }

    public void setHandler(MessageParser handler){
        this.logicHandler = handler;
    }

    public void run(){

        try(
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                ){
            logicHandler.handle(in, out);
        }catch (IOException e){
            e.printStackTrace();
        }finally{
            desinscrire();
        }
    }

    public void desinscrire(){

        if(clientList != null){
            clientList.remove(this);
            System.out.println("Client rétiré de la liste du serveur");
        }
        try {
            if(clientSocket != null && !clientSocket.isClosed()){
                clientSocket.close();
            }
        }catch(IOException e){e.printStackTrace();}
    }
}