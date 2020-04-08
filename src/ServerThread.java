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
        this.out = new PrintWriter(c.getOutputStream());
    }

    @Override
    public void run() {
        String req;
        try {
            req = recv();
        } catch (IOException e) {
            System.out.println("Failed to receive request");
            //TODO: log recv error
        }
        send("HTTP/1.1 200 OK");
        close();
    }

    Socket getSocket(){ return c; }

    private String recv() throws IOException {
        //TODO: correctly handle chunked transfer
        StringBuilder requestBuilder = new StringBuilder();
        int contentLength = 0;
        String line;
        do {
            line = in.readLine();
            requestBuilder.append(line).append("\r\n");
            String[] parts = line.split(":", 2);
            if (parts.length > 1) {
                String key = parts[0].trim();
                if (key.equals("Content-Length")) {
                    String val = parts[1].trim();
                    contentLength = Integer.parseInt(val);
                }
            }
        } while (line != null && !line.isEmpty());
        char[] bodyBuffer = new char[contentLength];
        int bytesRead = in.read(bodyBuffer, 0, contentLength);
        requestBuilder.append(new String(bodyBuffer));
        return requestBuilder.toString();
    }

    private void send(String s) {
        out.write(s);
        out.flush();
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
