import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrafficClient {
    public static final int NUM_CLIENTS = 2; // Number of clients for load testing
    private static final String HOST = "localhost";
    private static final int PORT = 1234;
    private static final int NUM_PACKETS = 100; // Number of packets each client sends

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_CLIENTS);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < NUM_CLIENTS; i++) {
            final int clientId = i;  // Assign a unique ID to each client
            executor.submit(() -> {
                Thread.currentThread().setName("Client-" + clientId);  // Set a unique name for each thread
                try (Socket socket = new Socket(HOST, PORT);
                     OutputStream output = socket.getOutputStream();
                     PrintWriter writer = new PrintWriter(output, true)) {

                    System.out.println(Thread.currentThread().getName() + " connected to server.");

                    for (int j = 0; j < NUM_PACKETS; j++) {
                        String message = "Data packet " + j + " from " + Thread.currentThread().getName();
                        writer.println(message);
                        System.out.println("Sent: " + message);
                        Thread.sleep(10); // Reduce delay for stress testing
                    }
                } catch (IOException | InterruptedException ex) {
                    System.out.println("Client exception: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            // Wait for all clients to finish
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Total time for all clients: " + totalTime + " ms");
    }
}
