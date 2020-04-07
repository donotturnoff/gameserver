import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ServerThread implements Runnable {

    private Server main;
    private Socket c;
    private PrintWriter out;
    private BufferedReader in;

    ServerThread(Server main, Socket c) throws IOException {
        this.main = main;
        this.c = c;
        this.in = new BufferedReader(new InputStreamReader(c.getInputStream()));
        this.out = new PrintWriter(c.getOutputStream(), true);
    }

    @Override
    public void run() {
        //TODO: handle connections
    }

    private List<String> read() {
        List<String> lines = new ArrayList<>();
        //TODO: read request into list
        return lines;
    }

    private void write(String s) {
        //TODO: send response
    }

    public void close() {
        try {
            c.shutdownOutput();
            in.close();
            out.close();
            c.close();
        } catch (IOException e) {
            //TODO: log connection close failure
            System.out.println("Failed to close client connection gracefully.");
        } finally {
            main.closeThread(this);
        }
    }
}
