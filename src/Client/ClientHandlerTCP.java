import java.io.*;
import java.net.*;

import java.util.ArrayList;
import java.util.List;

public class ClientHandlerTCP implements Runnable {

    private Socket clientSocket;
    private Object server;
    private TCPClientHandler logicHandler;

    public void addInput(Object input){
        if(input instanceof Socket){
            this.clientSocket = (Socket) input;
        }else{
            this.server = input;
        }
    }

    public void setHandler(TCPClientHandler handler){
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
            try {
                clientSocket.close();
            }catch(IOException e){e.printStackTrace();}
        }
    }
}