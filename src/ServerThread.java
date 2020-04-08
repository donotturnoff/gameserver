import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.*;

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
            send("HTTP/1.1 200 OK");
            System.out.println(extractHeaders(req));
        } catch (IOException e) {
            System.out.println("Failed to receive request");
            send("HTTP/1.1 500 Internal Server Error");
            //TODO: log recv error
        }
        close();
    }

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

    private HashMap<String, String> extractHeaders(String req) {
        HashMap<String, String> headers = new HashMap<>();
        String[] lines = req.split("\r\n");
        String[] firstLineParts = lines[0].split(" ");
        headers.put("Method", firstLineParts[0]);
        headers.put("Path", firstLineParts[1]);
        headers.put("Protocol", firstLineParts[2]);
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isEmpty()) {
                break;
            }
            String[] parts = line.split(":", 2);
            if (parts.length > 1) {
                String key = parts[0].trim();
                String val = parts[1].trim();
                headers.put(key, val);
            }
        }
        return headers;
    }

    private JSONObject extractBody(String req) {
        JSONObject body = new JSONObject();
        return body;
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
