import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Server started at port 5000");
        HashMap<String, ClientHandler> players = new HashMap<String, ClientHandler>();
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("New client connected: " + socket);
            ClientHandler clientHandler = new ClientHandler(socket);
            players.put("",clientHandler);
            clientHandler.setPlayers(players);
            Thread clientThread = new Thread(clientHandler);
            clientThread.start();
        }
    }
}
