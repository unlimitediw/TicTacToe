import PlayHelper.*;

import java.util.Scanner;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

public class TicTacToe {

    public static void main(String[] args) throws Exception {
        int m;                       // m is the target number
        int n;                       // n is the row/column number of the board
        String teamId1 = "1052";     // Our teamId
        String teamId2;              // Opponent TeamID
        PlayHelper ph;               // Help to connect to the server
        AI newAI;

        //setup board
        Scanner in = new Scanner(System.in);
        System.out.print("Please enter the target number:");
        m = Integer.parseInt(in.next());
        System.out.print("Please enter the board scale:");
        n = Integer.parseInt(in.next());
        //Setup board based on the board size
        if(n<13) {
            newAI = new AI(10, 0.1, 1, 15, 0.6, 1.2, n, m, 4);
        }else if(n<16){
            newAI = new AI(10, 0.1, 1, 10, 1.2, 0.6, n, m, 4);
        }else{
            newAI = new AI(10, 0.05, 1, 15, 1.5, 0.2, n, m, 4);
        }
        //Create game with teamIds, if a game is already exist, enter the game id.
        while (true) {
            System.out.print("Have you created a game yet? (Y/N) ");
            String gameCreated = in.next();
            //if the game is already exist, ener the gameid.
            if (gameCreated.equals("Y")) {
                System.out.print("Great! You can enter game id now:");
                n = Integer.parseInt(in.next());
                ph = new PlayHelper(n);
                //verify if the gameId;
                try {
                    //try to get the last move from server
                    List<Move> moves = ph.GetLastMoves(1);
                    //the game must be new game, else
                    if (moves.size() != 0) {
                        System.out.println("Oops! There is already some moves in this game. Could you please check the gameID?");
                        System.out.print("Let's do it again! ");
                        continue;
                    }
                    //if fail to get move from server
                } catch (Exception e) {
                    System.out.println("Oops! There is no such game assigned with this gameID in the server. Could you please check the gameID?");
                    System.out.print("Let's do it again! ");
                    continue;
                }
                break;
                //if the game is not created yet
            } else if (gameCreated.equals("N")) {
                System.out.println();
                System.out.print("Alright, please enter your opponent's teamid:");
                teamId2 = in.next();

                //try to verify opponent's teamid
                try {
                    ph = new PlayHelper(teamId1, teamId2);
                    System.out.println("The gameid is:" + ph.GetGameId());
                } catch (Exception e) {// if fail to create a game
                    System.out.println("Something is wrong! Could you please check your opponent's teamID?");
                    System.out.print("Let's do it again! ");
                    continue;
                }
                break;
            } else {//if the input is not X nor Y, ask the user again.
                System.out.println();
                System.out.print("I'm sorry, I didn't understand what you meant. ");
            }
        }

        while (true) {
            System.out.println("Who wanna do the first move?(Y=You,O=Opponent)");
            boolean userFirst;
            String firstMove = in.next();
            //if the user play the game first
            if (firstMove.equals("Y")) {
                try {
                    userFirst = true;
                    //try to start to play the game
                    Play(ph, newAI, userFirst);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                break;
                //if the opponent play the game first
            } else if (firstMove.equals("O")) {

                try {
                    userFirst = false;
                    Play(ph, newAI, userFirst);

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                break;
            } else {
                System.out.print("I'm sorry, I didn't understand what you meant. ");
            }
        }

        System.out.println("Game Over!");
        System.out.println("Press any key to stop");
        System.in.read();
    }

    /**
     * Start Play the game
     *
     * @param ph the PlayHelper helps to connenct to server
     * @param newAI the board
     * @param userFirst if the user do the first move
     * @throws Exception the Http Connection may fail
     */
    private static void Play(PlayHelper ph, AI newAI, boolean userFirst) throws Exception {


        newAI.PotentialIni(newAI, 0);
        int turn = 1;
        long t = 0;
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        // outerLoop:
        while (turn < newAI.n * newAI.n + 1) {
            //the first step if the user do the first step
            if (turn == 1) {
                if (userFirst) {
                    newAI.Play(newAI.n / 2, newAI.n / 2, turn,newAI);
                    ph.MakeMoves(newAI.n / 2, newAI.n / 2);
                    turn++;
                    DisplayBoard.displayBoard(newAI.TTT,newAI);
                    t = System.currentTimeMillis();
                    System.out.println(df.format(new Date()));
                    continue;
                }//the remain steps if the user do the first step
            } else if (turn % 2 == 1) {
                if (userFirst) {
                    Move m = Get(ph, t);
                    if(m==null){
                        break;
                    }
                    newAI.PlayMinMax(newAI,m.GetX(), m.GetY(), turn);
                    ph.MakeMoves(newAI.optX, newAI.optY);
                }
            } else {//the opponent's steps
                if (!userFirst) {
                    Move m = Get(ph, t);
                    if(m==null){
                        break;
                    }
                    newAI.PlayMinMax(newAI,m.GetX(), m.GetY(), turn);
                    ph.MakeMoves(newAI.optX, newAI.optY);
                }
            }

            turn++;
            t = System.currentTimeMillis();
            System.out.println(df.format(new Date()));
        }
    }

    /**
     * Get the Opponent's move from server.
     * @param ph the PlayHelper
     * @param t the time when user's move post to server successfully
     * @return the opponent's last move. null means opponent is time out
     * @throws Exception Http Request may fail
     */
    private static Move Get(PlayHelper ph, long t) throws Exception {
        //monitor of the opponent time
        while (System.currentTimeMillis() - t < 130000) {
            //if they not time out
            if (System.currentTimeMillis() - t < 125000) {
                //try to get the move
                Move m;
                try {
                    m = ph.GetLastMoves(1).get(0);
                }catch(Exception e){
                    Thread.sleep(5000);
                    continue;
                }
                if (m.GetTeamID() != 1052) {
                    return m;
                    //if there is no new move
                } else {
                    //wait 5s , and give moves from server again
                    Thread.sleep(5000);
                }
            } else {//if time out
                SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                System.out.println(df.format(new Date()));
                //if the opponent times out.
                System.out.println("Oops! It seems like your opponent's time was out");
                break;
            }
        }
        return null;
    }
}
