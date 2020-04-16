import java.util.HashSet;
import java.net.*;
import java.io.*;
import java.util.Set;
import java.util.logging.*;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    static {
        try {
            FileHandler fileHandler = new FileHandler("log%u.txt", true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.INFO);
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to open log file", e);
        }
    }

    private Set<ServerThread> serverThreads;
    private ServerSocket serverSocket;
    private int port;

    public static void main(String[] args) {
        if (args.length != 1) {
            logger.log(Level.SEVERE, "Requires one argument: port number");
            System.exit(1);
        } else {
            Server server = new Server(Integer.parseInt(args[0]));
            logger.log(Level.INFO, "Spawned server");
            server.run();
        }
    }

    private Server(int port) {
        this.port = port;
        serverThreads = new HashSet<>();
    }

    private void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (ServerThread t: serverThreads) {
                t.close();
            }
            logger.log(Level.INFO, "Application exiting");
        }));

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            logger.log(Level.INFO, "Listening on port " + serverSocket.getLocalPort());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to start server", e);
            System.exit(2);
        }

        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                Socket c = serverSocket.accept();
                logger.log(Level.INFO,"Accepted connection: " + c);
                ServerThread t = new ServerThread(this, c);
                serverThreads.add(t);
                (new Thread(t)).start();
                logger.log(Level.FINE,"Started new thread to handle " + c);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to accept connection", e);
            }
        }
    }

    void closeThread(ServerThread c) {
        serverThreads.remove(c);
    }
}
