import java.util.Arrays;
import java.util.Scanner;

public class TicTacToe {
    public static void main(String[] args) {

        AI newAI = new AI(10,0.02,-1,1,"X","O");
        newAI.PotentialIni(newAI);
        Scanner sc = new Scanner(System.in);

        /*
        //Initialize board
        String[][] TTT = new String[20][20];
        for (String c[]:TTT) {
            Arrays.fill(c,"_");
        }

        //Key value
        */
        int turn = 1;
        DisplayBoard.displayBoard(newAI.TTT,newAI);
        while(turn < 400){
            int x = sc.nextInt();
            int y = sc.nextInt();
            if(newAI.TTT[y][x].chess!= "_") continue;
            newAI.Play(x,y,turn,newAI);
            turn++;
        }
    }
}
