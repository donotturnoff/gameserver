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
        while (true){
            try {
                serverSocket = new ServerSocket(port);
                serverSocket.setReuseAddress(true);
                serverThreads.add(new ServerThread(serverSocket.accept()));

            } catch (IOException e) {
                //TODO: Decide whether the error is worth letting run up to the main funciton for catching there
                //TODO: Log Fatal Error
                //This quits the system should it encounter an error when creating a port for the server
                System.out.println("FATAL ERROR");
                System.exit(1);
            }
        }
    }

    public void closeThread(ServerThread c){
        serverThreads.remove(c);
        //TODO: Log Closure
    }
}
