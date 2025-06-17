import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CalorieFetchServer {
    private static final int PORT = 8004;
    private static final String DB_URL = "jdbc:mysql://localhost:3307/step_tracker";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "ryynkml22";
    private static final String TARGET_SERVER_IP = "172.20.10.4";
    private static final int TARGET_SERVER_PORT = 8003;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("CalorieFetchServer is running on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting CalorieFetchServer: " + e.getMessage());
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

                // Read request line
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

                    // Send the data to StepTrackerServer to calculate calories
                    double caloriesBurned = sendDataToStepTrackerServer(weight, duration);
                    if (caloriesBurned == -1) {
                        sendResponse(out, "500 Internal Server Error", "Error calculating calories.");
                        return;
                    }

                    // Calculate weight loss
                    double weightLoss = caloriesBurned / 7700.0;

                    // Insert weight loss into weight_loss_data table
                    String insertWeightLossSql = "INSERT INTO weight_loss_data (weight, duration, weight_loss) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertWeightLossSql)) {
                        insertStmt.setDouble(1, weight);
                        insertStmt.setInt(2, duration);
                        insertStmt.setDouble(3, weightLoss);
                        insertStmt.executeUpdate();
                    }

                    // Send response to client
                    sendResponse(out, "200 OK", "Weight Loss (kg): " + weightLoss);

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

        private double sendDataToStepTrackerServer(double weight, int duration) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(TARGET_SERVER_IP, TARGET_SERVER_PORT), 2000);

                try (PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    String requestBody = "weight=" + weight + "&duration=" + duration;
                    out.println("POST /calculate HTTP/1.1");
                    out.println("Host: " + TARGET_SERVER_IP);
                    out.println("Content-Type: application/x-www-form-urlencoded");
                    out.println("Content-Length: " + requestBody.length());
                    out.println();
                    out.println(requestBody);

                    String responseLine;
                    while ((responseLine = in.readLine()) != null) {
                        if (responseLine.startsWith("HTTP/1.1 200 OK")) {
                            while ((responseLine = in.readLine()) != null && !responseLine.isEmpty()) {
                                // Skip headers
                            }
                            // Read body
                            String body = in.readLine();
                            return Double.parseDouble(body);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error communicating with StepTrackerServer: " + e.getMessage());
            }
            return -1; // Indicate that an error occurred
        }

        private void sendResponse(PrintWriter out, String status, String body) {
            out.println("HTTP/1.1 " + status);
            out.println("Content-Type: text/plain");
            out.println("Content-Length: " + body.length());
            out.println("Access-Control-Allow-Origin: *"); // CORS header
            out.println();
            out.println(body);
        }
    }
}
