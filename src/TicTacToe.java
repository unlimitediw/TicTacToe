import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

public class TicTacToe {
    public static void main(String[] args) {

        AI newAI = new AI(10,0.06,1,15,"X","O",1,0.3,12,6,3);
        AI newAIbeta =  new AI(10,0.06,1,15,"X","O",1,0.3,12,6,3);
        //参数 w 是该点距离获胜的距离加权和的系数 （w越大，AI就越耿直，总想着通过直接找最大的点获胜）
        //参数 p 是该点发展空间加权和的系数 （p越大，AI越妖，会想着构造出大量延伸性很强的点，但是也会导致对可以获胜的点视而不见）
        //参数 b 是该点在棋盘的初始位置评估，b越大下棋趋势越向中间聚合
        //参数 pointP 是该点发展方向是出现友方点时的系数，显然方向上哪怕再远只要有己方队友，该点的潜力就更大
        //参数 ourWeight、oppWeight 是该点计算totalPotential的系数
        //我方时totalPotential = ourWeight * AIWeight() + oppWeight * MyWeight() - biasP * bias;
        //敌方时totalPotential = oppWeight * AIWeight() + ourWeight * MyWeight() - biasP * bias
        //经验测试表明，棋盘小时ourWeight大容易获胜，棋盘大时oppWeight大容易获胜，我也不知道为什么
        //参数 floor 为minmax搜索层数

        newAI.PotentialIni(newAI);
        Scanner sc = new Scanner(System.in);

        LinkedList<AI> minMax = new LinkedList<AI>();
        minMax.add(newAI);

        int turn = 1;
        DisplayBoard.displayBoard(newAI.TTT,newAI);
        while(turn < 144){
            /*
            int x = sc.nextInt();
            int y = sc.nextInt();
            if(newAI.TTT[y][x].chess!= "_") continue;
            newAI.Play(x,y,turn,newAI);
            */
            //newAI.Play(0,0,turn,newAI);
            //System.out.println(turn);
            if(turn == 1){
                newAI.PlayMinMax(newAI,newAIbeta.n/2,newAIbeta.n/2,turn);
            }
            else {
                newAI.PlayMinMax(newAI, newAIbeta.optX, newAIbeta.optY, turn);
            }
            newAIbeta.PlayMinMax(newAIbeta,newAI.optX,newAI.optY,turn+1);
            turn+=2;
        }
    }
}
