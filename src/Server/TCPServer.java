import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class TCPServer {

    private static Map<String, ClientHandlerTCP> clientMap = new ConcurrentHashMap<>();
    private static Map<String, GameRoom> sallesActives = new ConcurrentHashMap<>();
    public static void main(String[] args){
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(3031);
            Socket s;
            System.out.println("Serveur en Ecoute au port 3031..");

            while(!ss.isClosed()) {
                s = ss.accept();
                ClientHandlerTCP ch = new ClientHandlerTCP();
                ch.setSocket(s);
                ch.setClientActive(sallesActives);
                ch.setClientMap(clientMap);
                ch.setHandler(new MessageParser());
                Thread t = new Thread(ch);
                t.start();
                System.out.println("Accepted client");
            }

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                if(ss != null) ss.close();
            }catch (Exception ee){ee.printStackTrace();}
        }
    }

}
