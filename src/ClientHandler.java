import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

class ClientHandler implements Runnable {
    private Socket clientSocket;
    ObjectOutputStream output = null;
    String message = "";
    ObjectInputStream input = null;
    DatabaseConnection dbConnection = null;
    HashMap<String, ClientHandler> players = null;
    boolean gameOver = false;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public void setPlayers(HashMap<String, ClientHandler> players) {
        this.players = players;
    }

    public ObjectOutputStream getOutput() {
        return output;
    }

    public ObjectInputStream getInput() {
        return input;
    }

    public void run() {
        try {

            output = new ObjectOutputStream(clientSocket.getOutputStream());
            input = new ObjectInputStream(clientSocket.getInputStream());

            while (true) {
                message = (String) input.readObject();
                System.out.println("Received message from client " + clientSocket + ": " + message);

                if (message.equals("1")) {

                    register();
                }

                if (message.equals("2")) {
                    login();
                }

                if (message.equals("-")) {
                    break;
                }

            }

            System.out.println("Closing connection with client: " + clientSocket);
            input.close();
            output.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void register() throws IOException, ClassNotFoundException {
        while (true) {
            output.flush();

            dbConnection = new DatabaseConnection();
            output.writeObject("Please enter your username: ");
            message = (String) input.readObject();
            String userName = message;
            output.writeObject("Please enter your Password ");
            message = (String) input.readObject();
            String password = message;
            output.writeObject("Please enter your Name ");
            message = (String) input.readObject();
            String name = message;
            int response = dbConnection.register(userName, password, name);
            if (response == 200) {
                output.writeObject("Registred succesfully , press enter to login");
                login();
            } else {
                output.writeObject("Please enter a unique username");
            }
        }
    }

    private void login() throws IOException, ClassNotFoundException {
        while (true) {

            dbConnection = new DatabaseConnection();

            output.writeObject("Please enter your username ");
            message = (String) input.readObject();
            if (message.isEmpty()) { // handle enter key
                message = (String) input.readObject();
            }
            String userName = message;
            output.writeObject("Please enter your Password ");
            message = (String) input.readObject();
            String password = message;

            int response = dbConnection.login(userName, password);
            if (response == 200) {
                ClientHandler value = players.remove("");
                if (value != null) {
                    players.put(userName, value);
                }

                showScore(userName);
            } else if (response == 404) {
                output.writeObject("Username not found");
            } else {
                output.writeObject("unathorized");
            }
        }
    }

    private void showScore(String userName) throws IOException, ClassNotFoundException {
        while (true) {
            dbConnection = new DatabaseConnection();
            int score = dbConnection.getScore(userName);
            output.writeObject("Loged in successfully, press enter to continue \n Your Latest score is: " + score
                    + " \n Choose game Mode: \n 1) Singleplayer \n 2) MultiPlayer");
            message = (String) input.readObject();
            if (message.equals("1")) {
                singlePlayer(userName);
            } else if (message.equals("2")) {
                // TODO
                multiPlayer(userName);
            }else{
                break;
            }
        }
    }

    private void multiPlayer(String userName) throws IOException, ClassNotFoundException {
        ArrayList<Player> currentPlayer = new ArrayList<Player>();
        boolean finished = false;
        output.writeObject("Welcome to MultiPlayer mode , enter team size!");
        message = (String) input.readObject();
        int teamSize = 0;
        teamSize = Integer.parseInt(message);
        String[] team1 = new String[teamSize];
        String[] team2 = new String[teamSize];
        team1[0] = userName;

        output.writeObject("Enter team1 name");
        message = (String) input.readObject();
        String team1Name = message;
        output.writeObject("Enter team2 name");
        message = (String) input.readObject();
        String team2Name = message;
        if (team1Name.equals(team2Name)) {
            output.writeObject("Please enter unique team names");
            showScore(userName);
            message = (String) input.readObject();
            return;
            // return;
        }

        for (int i = 1; i < teamSize; i++) { // team1 loop
            output.writeObject(team1Name + ": Enter player username!");
            message = (String) input.readObject();
            team1[i] = message;
        }
        for (int i = 0; i < teamSize; i++) { // team2 loop
            output.writeObject(team2Name + ": Enter player username!");
            message = (String) input.readObject();
            team2[i] = message;
        }
        for (int i = 0; i < teamSize; i++) { // intalize current players list
            currentPlayer.add(new Player(team1[i], 7, 0));
            currentPlayer.add(new Player(team2[i], 7, 0));
        }
        String word = getWord();
        // convert to chars array
        char[] wordPlay = word.toCharArray();
        char[] underscore = new char[wordPlay.length];
        boolean finishedAttempt = false;
        boolean letterFound = false;
        for (int i = 0; i < wordPlay.length; i++) { // convert to _
            underscore[i] = '_';
        }
        while (true) {
            for (Player player : currentPlayer) {
                if (finishedAttempt == true) {
                    finishedAttempt = false;
                    break;
                }
                String underscoreString = new String(underscore);
                String currentUserName = player.getName();
                for (Player player2 : currentPlayer) {
                    if (player2.attempts <= 0) {
                        finishedAttempt = true;
                        for (Player player3 : currentPlayer) {
                            updateScore(player3.score, player3.name);
                        }
                        return;
                    }
                    int currentScore = player2.getScore();
                    int currentAttempts = player2.getAttempts();
                    String userToBeSent = player2.getName();
                    players.get(userToBeSent).getOutput()
                            .writeObject(underscoreString + "\n User turn: " + currentUserName
                                    + "\n Enter a letter \n score: "
                                    + currentScore + "\n" + word + "\n attempts left: " + currentAttempts);
                }
                String tempLetter = (String) players.get(currentUserName).getInput().readObject();
                if (tempLetter.isEmpty()) { // handle enter key
                    tempLetter = (String) players.get(currentUserName).getInput().readObject();
                }
                char letter = Character.toLowerCase(tempLetter.charAt(0)); // take first char and convert to lower case
                for (int j = 0; j < wordPlay.length; j++) {// check for letter
                    if (wordPlay[j] == letter) {
                        underscore[j] = wordPlay[j];
                        player.score++;
                        letterFound = true;
                    }
                }
                if (letterFound == false) {
                    player.attempts--;
                } else { //reset
                    letterFound = false;
                }
                for (int j = 0; j < wordPlay.length; j++) {// check if there is still _
                    if (underscore[j] == '_') {
                        break;
                    } // guessed the word
                    if (j == wordPlay.length - 1) { // last letter and no _
                        finished = true;
                        for (Player player3 : currentPlayer) {
                            int currentScore = player3.getScore();
                            String currentUser = player3.getName();
                            players.get(currentUser).getOutput()
                                    .writeObject(
                                            "\n score: "
                                                    + currentScore
                                                    + "\n"
                                                    + word);
                            updateScore(currentScore, currentUser);
                            players.get(currentUser).gameOver = true;
                        }
                        break;
                    }
                }
                if (finished == true) {
                    // reset
                    break;
                }
            }
            if (finished == true) {
                finished = false;
                break;
            }
        }

    }

    private void singlePlayer(String username) throws IOException, ClassNotFoundException {
        output.writeObject("Welcome to singleplayer mode , press enter to start guessing!");
        message = (String) input.readObject();
        String word = getWord();

        // convert to chars array
        char[] wordPlay = word.toCharArray();
        int attempts = 7;
        int score = 0;
        char[] underscore = new char[wordPlay.length];
        boolean finished = false;
        boolean letterFound = false;
        for (int i = 0; i < wordPlay.length; i++) { // convert to _
            underscore[i] = '_';
        }
        while (true) {// game flow
            if (attempts < 0) {
                output.writeObject("hardluck" + "\n score: " + score + "\n" + word);
                updateScore(score, username);
                break;
            }
            String underscoreString = new String(underscore);
            output.writeObject(underscoreString + "\n Enter a letter \n score: " + score + "\n" + word
                    + "\n attempts left: " + attempts);
            String tempLetter = (String) input.readObject();
            if (tempLetter.isEmpty()) { // handle enter key
                tempLetter = (String) input.readObject();
            }
            char letter = Character.toLowerCase(tempLetter.charAt(0)); // take first char and convert to lower case
            for (int j = 0; j < wordPlay.length; j++) {// check for letter
                if (wordPlay[j] == letter) {
                    underscore[j] = wordPlay[j];
                    score++;
                    letterFound = true;
                }
            }
            if (letterFound == false) {
                attempts--;
            } else {
                letterFound = false;
            }
            for (int j = 0; j < wordPlay.length; j++) {// check if there is still _
                if (underscore[j] == '_') {
                    break;
                } // guessed the word
                if (j == wordPlay.length - 1) { // last letter and no _
                    output.writeObject("congrats" + "\n score: " + score + "\n" + word);
                    finished = true;
                    updateScore(score, username);
                    break;
                }
            }
            if (finished == true) {
                finished = false; // reset
                break;
            }
        }
    }

    private void updateScore(int score, String username) {
        dbConnection = new DatabaseConnection();
        dbConnection.updateScore(score, username);

    }

    private String getWord() {
        try {
            URL url = new URL("https://random-word-api.herokuapp.com/word");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();

            String tempWord = content.toString();
            String word = tempWord.substring(2, tempWord.length() - 2); // remove quotaions and brackets from word
            return word;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}