import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.logging.*;

import org.json.*;

public class ServerThread implements Runnable {

    private Server main;
    private Socket c;
    private PrintWriter out;
    private BufferedReader in;
    private static final Logger logger = Logger.getLogger(ServerThread.class.getName());

    static {
        try {
            final FileHandler fhandler = new FileHandler("log%u.txt", true);
            fhandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fhandler);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to open log file", e);
        }
    }

    ServerThread(Server main, Socket c) throws IOException {
        this.main = main;
        this.c = c;
        this.in = new BufferedReader(new InputStreamReader(c.getInputStream()));
        this.out = new PrintWriter(c.getOutputStream());
    }

    @Override
    public void run() {
        String[] req;
        try {
            req = recv();
            System.out.println(extractHeaders(req[0]));
            System.out.println(extractBody(req[1]));
            send("HTTP/1.1 200 OK");
        } catch (IOException e) {
            send("HTTP/1.1 500 Internal Server Error");
            logger.log(Level.INFO, "Failed to receive request : 500", e);
        } catch (BadRequestException e) {
            send("HTTP/1.1 400 Bad Request");
            logger.log(Level.INFO, "Received bad request : 400", e);
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
            if (line == null){
                break;
            }
            requestBuilder.append(line).append("\r\n");
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                if (key.equals("Content-Length")) {
                    String val = parts[1].trim();
                    contentLength = Integer.parseInt(val);
                }
            }
        } while (!line.isEmpty());
        char[] bodyBuffer = new char[contentLength];
        int bytesRead = in.read(bodyBuffer, 0, contentLength);
        if (bytesRead < 0) {
            logger.log(Level.INFO, "Body shorter than stated Content-Length");
        }
        return new String[]{requestBuilder.toString(), new String(bodyBuffer)};
    }

    private void send(String s) {
        out.write(s);
        out.flush();
    }

    private HashMap<String, String> extractHeaders(String headString) {
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
        return headers;
    }

    private JSONObject extractBody(String bodyString) throws BadRequestException {
        try {
            if (bodyString.isBlank()) {
                return new JSONObject();
            } else {
                return new JSONObject(bodyString);
            }
        } catch (JSONException e) {
            logger.log(Level.INFO, "Malformed JSON in body", e);
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
            logger.log(Level.INFO, "Closed client connection to " + c);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to close connection to " + c + " gracefully.");
        } finally {
            main.closeThread(this);
        }
    }
}
