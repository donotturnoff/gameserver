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
        //TODO: handle exceptions more generally
        String[] req;
        try {
            req = recv();
            System.out.println(extractHeaders(req[0]));
            System.out.println(extractBody(req[1]));
            send("HTTP/1.1 200 OK");
        } catch (IOException e) {
            System.out.println("Failed to receive request");
            send("HTTP/1.1 500 Internal Server Error");
            //TODO: log recv error
        } catch (BadRequestException e) {
            System.out.println("Received bad request");
            send("HTTP/1.1 400 Bad Request");
            //TODO: log bad request error
        }
        close();
    }

    Socket getSocket() {
        return c;
    }

    private String[] recv() throws IOException {
        //TODO: correctly handle chunked transfer
        StringBuilder requestBuilder = new StringBuilder();
        int contentLength = 0;
        String line;
        do {
            line = in.readLine();
            requestBuilder.append(line).append("\r\n");
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                if (key.equals("Content-Length")) {
                    String val = parts[1].trim();
                    contentLength = Integer.parseInt(val);
                }
            }
        } while (line != null && !line.isEmpty());
        char[] bodyBuffer = new char[contentLength];
        int bytesRead = in.read(bodyBuffer, 0, contentLength);
        return new String[]{requestBuilder.toString(), new String(bodyBuffer)};
    }

    private void send(String s) {
        out.write(s);
        out.flush();
    }

    private HashMap<String, String> extractHeaders(String headString) throws BadRequestException {
        HashMap<String, String> headers = new HashMap<>();
        String[] lines = headString.split("\r\n");
        String[] firstLineParts = lines[0].split(" ");
        headers.put("Method", firstLineParts[0]);
        headers.put("Path", firstLineParts[1]);
        headers.put("Protocol", firstLineParts[2]);
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                String val = parts[1].trim();
                headers.put(key, val);
            }
        }
        if (!headers.containsKey("Content-Length")) {
            throw new BadRequestException("Missing Content-Length header");
        }
        return headers;
    }

    private JSONObject extractBody(String bodyString) throws BadRequestException {
        try {
            return new JSONObject(bodyString);
        } catch (JSONException e) {
            throw new BadRequestException("Malformed JSON in body");
        }
    }

    void close() {
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
