import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
public class StepTrackerClient {
private static final String SERVER_ADDRESS = "localhost"; // Change to your server's IP address or hostname
private static final int SERVER_PORT = 8002; // Change to the port your server is listening on
public static void main(String[] args) {
try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
PrintWriter out = new PrintWriter(socket.getOutputStream(),
true);
BufferedReader in = new BufferedReader(new
InputStreamReader(socket.getInputStream()));
BufferedReader stdIn = new BufferedReader(new
InputStreamReader(System.in))) {
System.out.println("Connected to server.");
// Get user input for step data
double height = getUserInputAsDouble(stdIn, "Enter height (in meters): ");
double weight = getUserInputAsDouble(stdIn, "Enter weight (in kilograms): ");
int steps = getUserInputAsInt(stdIn, "Enter number of steps: ");
int duration = getUserInputAsInt(stdIn, "Enter duration (in minutes): ");
// Send step data to server
String stepData = height + "," + weight + "," + steps + "," +
duration;
out.println(stepData);
// Receive response from server
String response = in.readLine();
System.out.println("Server response: " + response);
} catch (IOException e) {
System.err.println("Error: " + e.getMessage());
}
}
private static double getUserInputAsDouble(BufferedReader stdIn, String
prompt) throws IOException {
while (true) {
try {
System.out.print(prompt);
return Double.parseDouble(stdIn.readLine());
} catch (NumberFormatException e) {
System.out.println("Invalid input. Please enter a valid number.");
}
}
}
private static int getUserInputAsInt(BufferedReader stdIn, String prompt)
throws IOException {
while (true) {
try {
System.out.print(prompt);
return Integer.parseInt(stdIn.readLine());
} catch (NumberFormatException e) {
System.out.println("Invalid input. Please enter a valid integer.");
}
}
}
}
