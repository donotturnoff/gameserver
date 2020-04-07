import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.net.*;
import java.io.*;

public class Server {
    private HashSet<ServerThread> serverThreads;
    private ServerSocket serverSocket;
    private int port;

    public static void main(String[] args) throws IOException {
        Server server = new Server(Integer.parseInt(args[0]));
        server.run();
    }

    private Server(int port) {this.port = port;}

    private void run() throws IOException {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
        } catch (IOException e){
            //TODO: Log Fatal Error
            System.out.println("Fatal Error : " + e);
            System.exit(1);
        }
        while (true){
            try {
                ServerThread c = new ServerThread(serverSocket.accept());
                serverThreads.add(c);
                (new Thread(c)).start();

            } catch (IOException e) {
                //TODO: Log Connection Error
                //This quits the system should it encounter an error when creating a port for the server
                System.out.println("Connection Error");
            }
        }
    }

    public void closeThread(ServerThread c){
        serverThreads.remove(c);
        //TODO: Log Closure
    }
}
