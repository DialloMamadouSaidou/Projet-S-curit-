import java.net.*;

import java.util.ArrayList;
import java.util.List;
//cm@villesaguenay.qc.ca

public class TCPServer {

    public static void main(String[] args){
        List<Thread> clientThreads = new ArrayList<Thread>();

        ServerSocket ss = null;

        try {
            ss = new ServerSocket(3031);
            Socket s;
            while(!ss.isClosed()) {
                s = ss.accept();
                ClientHandlerTCP ch = new ClientHandlerTCP();
                ch.addInput(s);
                ch.addInput(s);
                ch.setHandler(new MessageParser());
                Thread t = new Thread(ch);
                t.start();
                clientThreads.add(t);
                System.out.println("Accepted client");

            }
            for(Thread ct: clientThreads) {
                ct.join();
            }


        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                for(Thread ct: clientThreads) {
                    ct.join();
                }
                ss.close();
            }catch (Exception ee){ee.printStackTrace();}
        }
    }
}

 /*
            String bindIp = "127.0.0.1";
            int port = 9090;
            InetAddress bindAddress = InetAddress.getByName(bindIp);
            serverSocket = new ServerSocket(port, 50, bindAddress);

            while(true){

                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected from " + clientSocket.getRemoteSocketAddress());

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                String line;

                while((line = in.readLine()) != null){
                    System.out.println("Received: "+ line);
                    out.println("Echo: " + line);
                }
                clientSocket.close();
                System.out.println("Client disconnected");

             */