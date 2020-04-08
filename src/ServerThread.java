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
        List<String> lines;
        try {
            lines = recv();
        } catch (IOException e) {
            System.out.println("Failed to receive request");
            //TODO: log recv error
        }
        close();
    }

    private List<String> recv() throws IOException {
        List<String> lines = new ArrayList<>();
        String prevLine;
        String line = "";
        do {
            prevLine = line;
            line = in.readLine();
            lines.add(line);
        } while (line != null && !(line.isEmpty() && prevLine.isEmpty()));
        return lines;
    }

    private void send(String s) {
        out.write(s);
    }

    public void close() {
        try {
            c.shutdownInput();
            c.shutdownOutput();
            in.close();
            out.close();
            c.close();
            //TODO: log connection close success
            System.out.println("Closed client connection to " + c);
        } catch (IOException e) {
            //TODO: log connection close failure
            System.out.println("Failed to close connection to " + c + " gracefully.");
        } finally {
            main.closeThread(this);
        }
    }
}
