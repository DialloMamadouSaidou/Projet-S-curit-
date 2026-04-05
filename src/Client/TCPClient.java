import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Scanner;
import java.net.Socket;


public class TCPClient {

    public static void main(String[] args) {
        ClientUI ui = new ClientUI();
        ui.start();

    }
}