import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class StepTrackerServer {
    private static final int PORT = 8003; // Port number for your server
    private static final String DB_URL = "jdbc:mysql://localhost:3307/step_tracker"; // Update with your DB URL and port
    private static final String DB_USERNAME = "root"; // Update with your DB username
    private static final String DB_PASSWORD = "ryynkml22"; // Update with your DB password

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Listening on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                // Handle client request in a separate thread
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting the server: " + e.getMessage());
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {

                // Read the request line
                String requestLine = in.readLine();
                if (requestLine == null || !requestLine.startsWith("POST")) {
                    sendResponse(out, "405 Method Not Allowed", "Only POST method is supported.");
                    return;
                }

                // Read and discard headers
                String headerLine;
                int contentLength = 0;
                while (!(headerLine = in.readLine()).isEmpty()) {
                    if (headerLine.startsWith("Content-Length:")) {
                        contentLength = Integer.parseInt(headerLine.split(":")[1].trim());
                    }
                }

                // Read the body
                char[] body = new char[contentLength];
                in.read(body);

                // Assuming the body is in the format "weight=65&duration=30"
                String[] parameters = new String(body).split("&");
                if (parameters.length == 2) {
                    double weight = Double.parseDouble(parameters[0].split("=")[1]);
                    int duration = Integer.parseInt(parameters[1].split("=")[1]);

                    // Calculate calories burned
                    double calories = (3.5 * 3.8 * weight * duration) / 200;

                    // Send the calculated calories back to the client
                    sendResponse(out, "200 OK", String.valueOf(calories));
                } else {
                    sendResponse(out, "400 Bad Request", "Invalid data format");
                }
            } catch (IOException | SQLException e) {
                System.err.println("Error handling client request: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
            }
        }

        private void sendResponse(PrintWriter out, String status, String body) {
            out.println("HTTP/1.1 " + status);
            out.println("Content-Type: text/plain");
            out.println("Content-Length: " + body.length());
            out.println();
            out.println(body);
        }
    }
}
