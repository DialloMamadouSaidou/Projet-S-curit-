import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Scanner;
import java.net.Socket;


public class TCPClient {

    public static void main(String[] args){
        Socket socket = null;
        Scanner scanner = new Scanner(System.in);
        try {
            String localIp = "127.0.0.1";
            int localPort = 8080;
            String serverIp = "127.0.0.1";
            int serverPort = 3031;

            InetAddress localAddress = InetAddress.getByName(localIp);
            InetSocketAddress localSocketAddress = new InetSocketAddress(localAddress, localPort);

            socket = new Socket();
            System.out.println("Entrez votre message: ");
            String message = scanner.nextLine();

            socket.bind(localSocketAddress);
            socket.connect(new InetSocketAddress(serverIp, serverPort));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println(message);
            String response = in.readLine();
            System.out.println("Server says: " + response);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            try{
                if(socket != null){
                    socket.close();
                }
            }catch (Exception ignored){};
        }
    }
}