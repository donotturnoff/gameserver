import java.util.ArrayList;
import java.util.List;

public class Server {
    private List<Thread> serverThreads;

    public static void main(String[] args) {
        Server server = new Server(10);
        server.run();
    }

    private Server(int initial_thread_count) {
        serverThreads = new ArrayList<>(initial_thread_count);
        for (int i = 0; i < initial_thread_count; i++) {
            serverThreads.add(new Thread(new ServerThread()));
        }
    }

    private void run() {
        for (Thread thread: serverThreads) {
            thread.start();
        }
    }
}
