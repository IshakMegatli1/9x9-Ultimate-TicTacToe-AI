import java.io.*;
import java.net.*;
import javax.swing.JOptionPane;

public class Client {

    private static Board globalBoard; // Plateau 9x9
    private static AIPlayer ai;         // Ton IA (Alpha-Beta/Minimax)
    private static Mark cpuMark;        // Ta marque (X ou O)
    private static Mark opponentMark;   // Marque de l'adversaire
    private static Move lastMove;       // Dernier coup joué

    public static void main(String[] args) {
        // Détermine l'adresse IP du serveur
        String serverIP = "localhost";
        if (args.length > 0) {
            serverIP = args[0];
        } else {
            serverIP = JOptionPane.showInputDialog(null,
                    "Entrez l'adresse IP du serveur :", "Configuration",
                    JOptionPane.QUESTION_MESSAGE);
            if (serverIP == null || serverIP.trim().isEmpty()) {
                serverIP = "localhost";
            }
        }
        System.out.println("Connexion au serveur: " + serverIP + " sur le port 8888");

        Socket myClient;
        BufferedInputStream input;
        BufferedOutputStream output;

        try {
            myClient = new Socket(serverIP, 8888);
            input  = new BufferedInputStream(myClient.getInputStream());
            output = new BufferedOutputStream(myClient.getOutputStream());

            // Initialise un plateau vide et un coup fictif par défaut
            globalBoard = new Board();
            lastMove = new Move(4, 4);

            // Boucle principale de lecture des commandes
            while (true) {
                int cmdInt = input.read();
                if (cmdInt == -1) {
                    break; // Fin du flux
                }
                char cmd = (char) cmdInt;
                System.out.println("Commande reçue: " + cmd);

                if (cmd == '1') { // Tu es joueur Blanc (X)
                    readBoardState(input, globalBoard);
                    cpuMark = Mark.X;
                    opponentMark = Mark.O;
                    ai = new AIPlayer(cpuMark, 3000); // 3 secondes de temps max
                    System.out.println("Nouvelle partie! Je suis BLANC (X).");

                    // Envoie immédiatement le premier coup (car le serveur n'envoie pas '3' pour X)
                    Move myFirstMove = ai.getBestMove(globalBoard, lastMove);
                    if (myFirstMove == null) {
                        myFirstMove = new Move(0, 0);
                    }
                    globalBoard.play(myFirstMove.getRow(), myFirstMove.getCol(), cpuMark);
                    lastMove = myFirstMove;
                    String moveStr = moveToString(myFirstMove);
                    System.out.println("Premier coup immédiat : " + moveStr);
                    output.write(moveStr.getBytes(), 0, moveStr.length());
                    output.flush();
                }
                else if (cmd == '2') { // Tu es joueur Noir (O)
                    readBoardState(input, globalBoard);
                    cpuMark = Mark.O;
                    opponentMark = Mark.X;
                    ai = new AIPlayer(cpuMark, 3000);
                    System.out.println("Nouvelle partie! Je suis NOIR (O).");
                    // On attend que le serveur envoie '3' pour jouer
                }
                else if (cmd == '3') { // C'est à toi de jouer
                    String s = readLineFromServer(input);
                    System.out.println("Dernier coup adverse: " + s);
                    if (!s.equals("A0")) {
                        Move advMove = parseMove(s);
                        globalBoard.play(advMove.getRow(), advMove.getCol(), opponentMark);
                        lastMove = advMove;
                    }
                    Move myBestMove = ai.getBestMove(globalBoard, lastMove);
                    if (myBestMove == null) {
                        myBestMove = new Move(0, 0);
                    }
                    globalBoard.play(myBestMove.getRow(), myBestMove.getCol(), cpuMark);
                    lastMove = myBestMove;
                    String moveStr = moveToString(myBestMove);
                    System.out.println("J'envoie mon coup: " + moveStr);
                    output.write(moveStr.getBytes(), 0, moveStr.length());
                    output.flush();
                }
                else if (cmd == '4') { // Coup invalide
                    System.out.println("Coup invalide! Je dois rejouer.");
                    Move myBestMove = ai.getBestMove(globalBoard, lastMove);
                    if (myBestMove == null) {
                        myBestMove = new Move(0, 0);
                    }
                    globalBoard.play(myBestMove.getRow(), myBestMove.getCol(), cpuMark);
                    lastMove = myBestMove;
                    String moveStr = moveToString(myBestMove);
                    System.out.println("Nouveau coup: " + moveStr);
                    output.write(moveStr.getBytes(), 0, moveStr.length());
                    output.flush();
                }
                else if (cmd == '5') { // Partie terminée
                    String s = readLineFromServer(input);
                    System.out.println("Partie terminée. Dernier coup joué: " + s);
                    break;
                }
            }

            System.out.println("Fin de la connexion.");
            input.close();
            output.close();
            myClient.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Lit l'état initial (81 valeurs) et met à jour le plateau
    private static void readBoardState(BufferedInputStream input, Board board) throws IOException {
        byte[] buffer = new byte[1024];
        int size = input.available();
        input.read(buffer, 0, size);
        String s = new String(buffer).trim();
        System.out.println("Etat initial du plateau (81 valeurs) : " + s);
        String[] boardValues = s.split(" ");
        int index = 0;
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int val = Integer.parseInt(boardValues[index]);
                index++;
                Mark m;
                if (val == 4)      m = Mark.X;
                else if (val == 2) m = Mark.O;
                else               m = Mark.EMPTY;
                board.setCellGlobal(row, col, m);
            }
        }
    }

    // Lit une ligne depuis le serveur (ex. le dernier coup)
    private static String readLineFromServer(BufferedInputStream input) throws IOException {
        byte[] aBuffer = new byte[16];
        int size = input.available();
        input.read(aBuffer, 0, size);
        return new String(aBuffer).trim();
    }

    // Convertit une chaîne (ex: "A9") en Move (row et col en indices 0-based)
    private static Move parseMove(String moveStr) {
        if (moveStr.length() < 2) return new Move(0, 0);
        char letter = moveStr.charAt(0);
        char digit  = moveStr.charAt(1);
        int col = letter - 'A';
        int row = digit - '1';
        return new Move(row, col);
    }

    // Convertit un Move (row, col en indices 0-based) en chaîne (ex: "D6", avec lignes 1..9)
    private static String moveToString(Move move) {
        char letter = (char) ('A' + move.getCol());
        char digit  = (char) ('1' + move.getRow());
        return "" + letter + digit;
    }
}
