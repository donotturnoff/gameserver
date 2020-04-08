import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.net.*;
import java.io.*;
import java.util.Set;

public class Server {
    private Set<ServerThread> serverThreads;
    private ServerSocket serverSocket;
    private int port;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Requires one argument: port number");
            System.exit(1);
        } else {
            Server server = new Server(Integer.parseInt(args[0]));
            server.run();
        }
    }

    private Server(int port) {
        this.port = port;
        serverThreads = new HashSet<>();
    }

    private void run() {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                for (ServerThread t : serverThreads){
                    t.close();
                }
                System.out.println("Application terminating");
                //TODO: log application termination
            }
        });

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            //TODO: log server startup
            System.out.println("Listening on port " + serverSocket.getLocalPort());
        } catch (IOException e){
            //TODO: Log Fatal Error
            System.out.println("Fatal error: " + e);
            System.exit(2);
        }
        while (true){
            try {
                Socket c = serverSocket.accept();
                System.out.println("Accepted connection from " + c); //TODO: log connection (with better format)
                ServerThread t = new ServerThread(this, c);
                serverThreads.add(t);
                (new Thread(t)).start();

            } catch (IOException e) {
                //TODO: Log Connection Error
                //This quits the system should it encounter an error when creating a port for the server
                System.out.println("Connection Error");
            }
        }
    }

    void closeThread(ServerThread c){
        serverThreads.remove(c);
        //TODO: Log Closure
    }
}
