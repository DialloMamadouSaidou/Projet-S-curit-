import java.io.*;
import java.net.*;


public class ClientConnection {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String lastError;


    public String getLastError() {
        return lastError;
    }

    public boolean connect(String serverIp, int serverPort) {
        try{
            if(isConnected()){
                closeConnection();
            }
            socket = new Socket();
            socket.connect(new InetSocketAddress(serverIp, serverPort), 5000);

            in =new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            return true;
        } catch (IOException e){
            lastError = "Erreur de connexion : " +e.getMessage();
            return false;
        }

    }

    public boolean sendMessage(String message){
        lastError = null;

        if(!isConnected()){
            lastError ="Aucune connexion active. ";
            return false;
        }
        if(message==null || message.trim().isEmpty()){
            lastError = "Le message est vide. ";
            return true;
        }
        out.println(message);
        return true;
    }

    public String readMessage(){
        lastError = null;

        if(!isConnected()){
            lastError ="Aucune connexion active. ";
            return null;
        }

        try{
            return in.readLine();
        } catch (IOException e){
            lastError = "Erreur de connexion : "+e.getMessage();
            return null;
        }
    }

    public void closeConnection(){
        try {
            if (in != null) {
                in.close();
            }
        }catch (IOException e){
            lastError = "Erreur fermeture lecture : "+e.getMessage();
        }
        if(out != null){
            out.close();
        }
        try{
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }catch (IOException e){
           lastError = "Erreur fermeture socket : "+e.getMessage();
        }
        in  = null;
        out = null;
        socket = null;
    }

    public boolean isConnected(){
        return socket!=null && socket.isConnected() && !socket.isClosed();

    }
}
