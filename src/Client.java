import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket socket = new Socket("localhost", 5000);
        System.out.println("Connected to server");

        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("1) Register ");
        System.out.println("2) Login ");
        System.out.println("type '-' to quit");

        while (true) {
            String message = userInput.readLine();
            output.writeObject(message);
            if (message.equals("-")) {
                break;
            }

            String response = (String) input.readObject();
            System.out.println("Received response from server: " + response);
        }

        System.out.println("Closing connection with server");
        input.close();
        output.close();
        userInput.close();
        socket.close();
    }
}