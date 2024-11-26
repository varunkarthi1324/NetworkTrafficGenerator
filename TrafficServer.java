import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.CountDownLatch;

public class TrafficServer {
    private static final int PORT = 1234;
    private static final AtomicLong totalBytes = new AtomicLong(0);
    private static final AtomicLong totalClients = new AtomicLong(0);
    private static long startTime = 0;
    private static long endTime = 0;
    private static final int EXPECTED_CLIENTS = 2; // Set this to the expected number of clients
    private static final CountDownLatch latch = new CountDownLatch(EXPECTED_CLIENTS);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            startTime = System.currentTimeMillis(); // Start time for the entire operation

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected from " + socket.getInetAddress());

                new ServerThread(socket).start();
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    static class ServerThread extends Thread {
        private Socket socket;

        public ServerThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            long clientBytes = 0;

            try (InputStream input = socket.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {

                String message;
                while ((message = reader.readLine()) != null) {
                    clientBytes += message.getBytes().length;
                    System.out.println("Received: " + message);
                }
            } catch (IOException ex) {
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                totalBytes.addAndGet(clientBytes);
                totalClients.incrementAndGet();
                latch.countDown();
                System.out.println("Client data received: " + clientBytes + " bytes");

                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                if (totalClients.get() == EXPECTED_CLIENTS) {
                    endTime = System.currentTimeMillis();

                    long totalBytesReceived = totalBytes.get();
                    long totalTimeElapsed = endTime - startTime;
                    double overallDataRate = (totalBytesReceived / 1024.0) / (totalTimeElapsed / 1000.0); // in KB/s

                    System.out.println("Total data received: " + totalBytesReceived + " bytes");
                    System.out.println("Total time elapsed: " + totalTimeElapsed + " ms");
                    System.out.println("Overall data transfer rate: " + overallDataRate + " KB/s");
                }
            }
        }
    }
}
