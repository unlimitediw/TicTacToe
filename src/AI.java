import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

/**
 * AI Class:
 * AI Class is used to memory the environment value
 * It is mainly used to calculate the point judgement value and evaluation value
 * And use these to do the MinMax and alpha-beta pruning to find the optimal solution
 * The characteristic of my AI is that the evaluation and point judgement is quite overall and effective
 * The domain of our MinMax is correctly minimize
 */
public class AI {

    int n;                      //Size of board
    int m;                      //Size of win
    int floor;                  //MinMax floor
    double w;                   //Weight parameter
    double p;                   //Potential parameter
    double b;                   //Bias parameter
    double ourWeight;           //Our total potential parameter
    double oppWeight;           //Opponent total potential parameter
    double evaluationValue;     //Evaluation value provided by evaluation functions
    double alpha = -999999999;  //Alpha pruning
    double beta = 999999999;    //Beta pruning
    int pointP;                 //Dropped point parameter

    String chess;               //Chess type

    Point[][] TTT;              //Memory information for each point in board

    //Domain of MinMax neighbour in each turn(the best points in each turn of MinMax)
    List<OptimalList> optimalLists = new ArrayList<>();

    int optX, optY;             //Optimal point(optX,optY) base our algorithm

    //PART OF INITIALIZATION
    AI(double w, double p, double b, int pointP, double ourWeight, double oppWeight, int n, int m, int floor) {
        this.w = w;
        this.p = p;
        this.b = b;
        this.n = n;
        this.m = m;
        this.ourWeight = ourWeight;
        this.oppWeight = oppWeight;
        this.floor = floor;
        this.pointP = pointP;
        this.evaluationValue = 0;
        TTT = new Point[n][n];
        //Initialization for board and points
        for (int x = 0; x < TTT.length; x++) {
            for (int y = 0; y < TTT[0].length; y++) {
                TTT[x][y] = new Point(x, y, w, p, b, n, ourWeight, oppWeight);
                TTT[x][y].x = x;
                TTT[x][y].y = y;
                double center = ((double) n - 1.0) / 2.0;//9.5 when n = 20.
                TTT[x][y].bias = Math.pow((double) x - center, 2) + Math.pow((double) y - center, 2);//Initialize bias
            }
        }
    }

    //Initialization for potential of each point in board
    void PotentialIni(AI ai, int type) {
        for (int x = 0; x < TTT.length; x++) {
            for (int y = 0; y < ai.TTT[0].length; y++) {
                CalculatePotential(x, y, 0, ai);
                CalculatePotential(x, y, 1, ai);
                ai.TTT[x][y].totalPotential(type);
            }
        }
    }


    //PART OF MAKE MOVE
    //Play for turn 1 only
    void Play(int optX, int optY, int turn, AI ai) {
        if (turn % 2 == 1) {
            ai.TTT[optX][optY].chess = "X";
            ResetStartEnd(optX, optY, 0, ai);
            CalculateWeight(optX, optY, 1, ai);
            CalculateWeight(optX, optY, 0, ai);
            AdjustPotential(optX, optY, ai);
            PotentialReset(optX, optY, ai, 0);
            FindOptimalXYList(ai);
        } else {
            ai.TTT[optX][optY].chess = "O";
            ResetStartEnd(optX, optY, 1, ai);
            CalculateWeight(optX, optY, 0, ai);
            CalculateWeight(optX, optY, 1, ai);
            AdjustPotential(optX, optY, ai);
            PotentialReset(optX, optY, ai, 1);
            FindOptimalXYList(ai);
        }
    }

    //Play for turns larger than 2
    void PlayMinMax(AI ai, int x, int y, int turn) {
        if (turn % 2 == 1) {
            NewMinMax(ai,x,y,ai.floor,ai.alpha,ai.beta,0,true);
        }
        else {
            NewMinMax(ai,x,y,ai.floor,ai.alpha,ai.beta,1,true);
        }
        ai.Play(ai.optX,ai.optY,turn,ai);
        DisplayBoard.displayBoard(ai.TTT,ai);
    }

    //SOME FUNCTION USEFUL
    //Copy AI class
    AI copyAI(AI ai){
        AI subAI = new AI(ai.w, ai.p, ai.b, ai.pointP, ai.ourWeight, ai.oppWeight, ai.n, ai.m, ai.floor);
        for (int k = 0; k < subAI.TTT.length; k++) {
            for (int t = 0; t < TTT[0].length; t++) {
                subAI.TTT[k][t] = new Point(k, t, ai.w, ai.p, ai.b, ai.n, ai.ourWeight, ai.oppWeight);
                subAI.TTT[k][t].rowWeight[0] = ai.TTT[k][t].rowWeight[0];
                subAI.TTT[k][t].rowWeight[1] = ai.TTT[k][t].rowWeight[1];
                subAI.TTT[k][t].colWeight[0] = ai.TTT[k][t].colWeight[0];
                subAI.TTT[k][t].colWeight[1] = ai.TTT[k][t].colWeight[1];
                subAI.TTT[k][t].rDWeight[0] = ai.TTT[k][t].rDWeight[0];
                subAI.TTT[k][t].rDWeight[1] = ai.TTT[k][t].rDWeight[1];
                subAI.TTT[k][t].lDWeight[0] = ai.TTT[k][t].lDWeight[0];
                subAI.TTT[k][t].lDWeight[1] = ai.TTT[k][t].lDWeight[1];
                subAI.TTT[k][t].rowStart[0] = ai.TTT[k][t].rowStart[0];
                subAI.TTT[k][t].rowStart[1] = ai.TTT[k][t].rowStart[1];
                subAI.TTT[k][t].rowEnd[0] = ai.TTT[k][t].rowEnd[0];
                subAI.TTT[k][t].rowEnd[1] = ai.TTT[k][t].rowEnd[1];
                subAI.TTT[k][t].colStart[0] = ai.TTT[k][t].colStart[0];
                subAI.TTT[k][t].colStart[1] = ai.TTT[k][t].colStart[1];
                subAI.TTT[k][t].colEnd[0] = ai.TTT[k][t].colEnd[0];
                subAI.TTT[k][t].colEnd[1] = ai.TTT[k][t].colEnd[1];
                subAI.TTT[k][t].rDStart[0] = ai.TTT[k][t].rDStart[0];
                subAI.TTT[k][t].rDStart[1] = ai.TTT[k][t].rDStart[1];
                subAI.TTT[k][t].rDEnd[0] = ai.TTT[k][t].rDEnd[0];
                subAI.TTT[k][t].rDEnd[1] = ai.TTT[k][t].rDEnd[1];
                subAI.TTT[k][t].lDStart[0] = ai.TTT[k][t].lDStart[0];
                subAI.TTT[k][t].lDStart[1] = ai.TTT[k][t].lDStart[1];
                subAI.TTT[k][t].lDEnd[0] = ai.TTT[k][t].lDEnd[0];
                subAI.TTT[k][t].lDEnd[1] = ai.TTT[k][t].lDEnd[1];
                subAI.TTT[k][t].potential[0] = ai.TTT[k][t].potential[0];
                subAI.TTT[k][t].potential[1] = ai.TTT[k][t].potential[1];
                subAI.TTT[k][t].chess = ai.TTT[k][t].chess;
                subAI.TTT[k][t].myWeight = ai.TTT[k][t].myWeight;
                subAI.TTT[k][t].aiWeight = ai.TTT[k][t].aiWeight;
                subAI.TTT[k][t].totalPotential = ai.TTT[k][t].totalPotential;
                subAI.TTT[k][t].bias = ai.TTT[k][t].bias;
            }
        }
        return subAI;
    }

    //Check win or not
    //Check the best or worst situation in MinMax
    boolean CheckPoint(AI ai,int x,int y, String chess,boolean minMaxMode){
        //row
        boolean WIN = false;
        int memo =0;
        for(int i =1;i <ai.m;i++){
            if((y+i>= n)||(ai.TTT[x][y+i].chess!=chess)){
                memo = i -1;
                break;
            }
            if(i== m-1){
                WIN =true;
            }
        }
        for(int i =1;i <ai.m;i++){
            if((y-i<0)||(ai.TTT[x][y-i].chess!=chess)){
                break;
            }
            memo++;
            if(memo== m-1){
                WIN =true;
            }
        }
        //column
        memo = 0;
        for(int i =1;i <ai.m;i++){
            if((x+i>= n)||(ai.TTT[x+i][y].chess!=chess)){
                memo = i -1;
                break;
            }
            if(i== m-1){
                WIN =true;
            }
        }
        for(int i =1;i <ai.m;i++){
            if((x-i<0)||(ai.TTT[x-i][y].chess!=chess)){
                break;
            }
            memo++;
            if(memo== m-1){
                WIN =true;
            }
        }
        //rightDiagonal
        memo = 0;
        int offset = x - y;
        for(int i =1;i <ai.m;i++) {
            if (offset > 0) {
                if (y - i < 0 || (ai.TTT[x - i][y - i].chess != chess)) {
                    memo = i-1;
                    break;
                }
            } else {
                if (x - i < 0 || (ai.TTT[x - i][y - i].chess != chess)) {
                    memo = i-1;
                    break;
                }
            }
            if(i== m-1){
                WIN =true;
            }
        }
        for(int i =1;i <ai.m;i++) {
            if (offset > 0) {
                if (x + i >= n || (ai.TTT[x + i][y + i].chess != chess)) {
                    break;
                }
            } else {
                if (y+i >= n || (ai.TTT[x + i][y + i].chess != chess)) {
                    break;
                }
            }
            memo ++;
            if(memo== m-1){
                WIN =true;
            }
        }
        //leftDiagonal
        memo = 0;
        int sumset = x+y;
        for(int i = 1;i<ai.m;i++){
            if (sumset > n - 1) {//19 when n = 20
                if (x+i>=n||(ai.TTT[x + i][y-i].chess != chess)){
                    memo = i-1;
                    break;
                }
            } else {
                if(y-i<0||(ai.TTT[x + i][y-i].chess != chess)){
                    memo = i-1;
                    break;
                }
            }
            if(i== m-1){
                WIN =true;
            }
        }
        for(int i = 1;i<ai.m;i++){
            if (sumset > n - 1) {//19 when n = 20
                if (y+i>=n||(ai.TTT[x - i][y+i].chess != chess)){
                    break;
                }
            } else {
                if(x-i<0||(ai.TTT[x - i][y+i].chess != chess)){
                    break;
                }
            }
            memo ++;
            if(memo== m-1){
                WIN =true;
            }
        }
        if(WIN){
            if(minMaxMode){
                if(chess == "X"){
                    ai.evaluationValue = -9999998;
                    return true;
                }
                else{
                    ai.evaluationValue = 9999998;
                    return true;
                }
            }
            else{
                return true;
            }
        }
        return false;
    }

    //PART OF MINMAX
    double NewMinMax(AI ai, int x, int y, int floor, double alpha, double beta, int player,boolean firstfloor) {
        //Play the opponent's last point
        ai.Play(x, y, player, ai);
        if(firstfloor){
            if(player == 0) System.out.println("O" + " x: "+x + " y: "+y);
            else System.out.println("X" + " x: "+x + " y: "+y);
            for(int i = 0;i< n;i++){
                for(int j = 0;j<n;j++){
                    if(ai.TTT[i][j].chess == "X"){
                        if(CheckPoint(ai,i,j,"X",false)){
                            System.out.println("XWIN:" + " x: "+i+" y: "+j );
                            System.exit(1);
                        }
                    }
                    else if(ai.TTT[i][j].chess == "O"){
                        if(CheckPoint(ai,i,j,"O",false)){
                            System.out.println("OWIN:" + " x: "+i+" y: "+j );
                            System.exit(1);
                        }
                    }
                    else if(ai.TTT[i][j].chess == "_") {
                        if(CheckPoint(ai,i,j,"X",false)||CheckPoint(ai,i,j,"O",false)){
                            ai.optX = i;
                            ai.optY = j;
                            return 0;
                        }
                    }
                }
            }
            DisplayBoard.displayBoard(ai.TTT, ai);
        }

        //Calculate evaluation value in the leafs of MinMax tree
        if (floor == 0) {
            ai.EvaluateFunction(ai);
            return ai.evaluationValue;
        }
        //Find Max
        //Renew alpha value
        //Alpha-beta pruning
        if (player == 1) {
            double v = -99999999;
            for (int i = 0; i < ai.optimalLists.size(); i++) {
                AI subAI = copyAI(ai);
                double g;
                if(CheckPoint(subAI,ai.optimalLists.get(i).x,ai.optimalLists.get(i).y,"O",true)){
                    g = subAI.evaluationValue;
                }
                else {
                    g = NewMinMax(subAI,ai.optimalLists.get(i).x,ai.optimalLists.get(i).y,floor-1,alpha,beta,0,false);
                }
                if(firstfloor){
                    if(g>v){
                        ai.optX = ai.optimalLists.get(i).x;
                        ai.optY = ai.optimalLists.get(i).y;
                        v = g;
                    }
                }
                else{
                    v = Math.max(v,g);
                }
                alpha = Math.max(alpha,v);
                if(beta < alpha){
                    break;
                }
            }
            return v;
        }
        //Find Min
        //Renew beta value
        //Alpha-beta pruning
        else{
            double v = 99999999;
            for (int i = 0; i < ai.optimalLists.size(); i++) {
                AI subAI = copyAI(ai);
                double g;
                if(CheckPoint(subAI,ai.optimalLists.get(i).x,ai.optimalLists.get(i).y,"X",true)){
                    g = subAI.evaluationValue;
                }
                else{
                    g = NewMinMax(subAI,ai.optimalLists.get(i).x,ai.optimalLists.get(i).y,floor-1,alpha,beta,1,false);
                }
                if(firstfloor){
                    if(g<v){
                        ai.optX = ai.optimalLists.get(i).x;
                        ai.optY = ai.optimalLists.get(i).y;
                        v = g;
                    }
                }
                v = Math.min(v,g);
                alpha = Math.min(alpha,v);
                if(beta < alpha) break;
            }
            return v;
        }
    }

    //PART OF POINT JUDGE(PORTION OF EVALUATION FUNCTION)
    //After dropping at (x,y)
    //Reset the corresponding start and end value of each point in the row, col, rightDiagonal, leftDiagonal of the origin point(x,y)
    //E.g. one point in the row of origin point. If the point is on the left of origin point, set the rowEnd value of this point to y-1
    // _ _ _ _ X_ _ _ _ _ O _ _ _ _ _ X _ _
    //When your drop a chess on the O position, then to the space left to O, the RowEnd of these point become the point left to "O"
    //Column, rightDiagonal, leftDiagonal are the same
    void ResetStartEnd(int x, int y, int type, AI ai) {
        //row direction calculate
        int i = ai.TTT[x][y].rowStart[type];
        int end = ai.TTT[x][y].rowEnd[type];
        while (i <= end) {
            if (i < y) ai.TTT[x][i].rowEnd[type] = y - 1;
            if (i > y) ai.TTT[x][i].rowStart[type] = y + 1;
            i++;
        }
        //col direction calculate
        i = ai.TTT[x][y].colStart[type];
        end = ai.TTT[x][y].colEnd[type];
        while (i <= end) {
            if (i < x) ai.TTT[i][y].colEnd[type] = x - 1;
            if (i > x) ai.TTT[i][y].colStart[type] = x + 1;
            i++;
        }
        //RD direction calculate
        i = ai.TTT[x][y].rDStart[type];
        end = ai.TTT[x][y].rDEnd[type];
        int offset = x - y;
        while (i <= end) {
            if (offset > 0) {
                if (i + offset < x) ai.TTT[i + offset][i].rDEnd[type] = x - offset - 1;
                if (i + offset > x) ai.TTT[i + offset][i].rDStart[type] = x - offset + 1;
            } else {
                if (i < x) ai.TTT[i][i - offset].rDEnd[type] = x - 1;
                if (i > x) ai.TTT[i][i - offset].rDStart[type] = x + 1;
            }
            i++;
        }
        //LD direction calculate
        i = ai.TTT[x][y].lDStart[type];
        end = ai.TTT[x][y].lDEnd[type];
        int sumset = x + y;
        while (i <= end) {
            if (sumset > n - 1) {//19 when n = 20
                if (i + sumset - n + 1 < y)
                    ai.TTT[n - 1 - i][i + sumset - n + 1].lDEnd[type] = y - (sumset - n+1) - 1;
                if (i + sumset - n + 1 > y)
                    ai.TTT[n - 1 - i][i + sumset - n + 1].lDStart[type] = y - (sumset - n+1 ) + 1;
            } else {
                if (i < y) ai.TTT[sumset - i][i].lDEnd[type] = y - 1;
                if (i > y) ai.TTT[sumset - i][i].lDStart[type] = y + 1;
            }
            i++;
        }
    }

    //calculate new weight of points in line,row,rd,ld of (x,y) after set(x,y)
    //(x,y)will maximum affect 15 points in weight calculation, the close the more. Start and end will cutoff this effect

    //First part: Calculate the direct weight of each empty space in row, col, rD, lD with the domain of the start and end
    //Calculate the weight of each empty space on the board
    //Strategy: _ _ _ _ _ _ _ _ O _ O _ _ _ _ _ _ _
    //          n 7 6 5 4 3 2 1 0 1 0 1 2 3 4 5 6 7
    //As shown above
    //We can feel that the point between the two "O" is close to win
    //However, the space between 6 and 6 all share the two points if m = 8
    //Base on it, our algorithm is like a window with length of m, the length will memo the number of same points in the window
    //Be attention, only the space between start point and end point will be take into consideration as we talk before
    //Second part: Calculate the situation of block of edge
    //E.g. X O _ _ _ O _ _
    //To O, although there are two O in the window, we don't believe that it's weight is high. It is obvious in the example of "X O O O O O O O _ ". Only with one "X", all work of O is out of use.
    //Third part: Disperse effective
    //Base on the concept in second part, we can found that even with same "O" the more disperse one is more invincible.
    //Therefore, I design a check in each empty space, if the right neighbour and left neighbour of it is empty, disperse++
    //Finally, we use a sub evaluation function to sum up the three variable into one new variable.
    //Which use the function of WeightReCal() (p.s. You can find at the end of our code)
    //The weightReCal function return the value that will be changed in Point.RowWeight(Col~,RD~,LD~)
    void CalculateWeight(int x, int y, int type, AI ai) {
        //row calculation
        int i = ai.TTT[x][y].rowStart[type];
        int end = ai.TTT[x][y].rowEnd[type];
        while (i <= end) {
            //if same type we only need to add the new weight on old one
            //8 when m = 8
            if (ai.TTT[x][i].chess.equals("_")) {
                if (i > y - m && i < y + m) {
                    int j = ai.TTT[x][i].rowStart[type];
                    int jEnd = ai.TTT[x][i].rowEnd[type];
                    int weight = 0;
                    int obsStart = 0;
                    int obsEnd = 0;
                    int disperse = 0;
                    int totalWeight = 0;
                    int weightCount = 0;
                    while (j <= jEnd) {
                        if (j > i - m && j < i + m) {
                            //Calculate the disperse value
                            if (!TTT[x][j].chess.equals("_")) {
                                weight += 1;
                                if (j != 0 && TTT[x][j - 1].chess == "_") {
                                    disperse++;
                                }
                                if (j != n - 1 && TTT[x][j + 1].chess == "_") {
                                    disperse++;
                                }
                            }
                            //Judge the situation of edge block
                            if (j == ai.TTT[x][i].rowStart[type] && TTT[x][j].chess != "_") {
                                obsStart = 1;
                            }
                            if (j == ai.TTT[x][i].rowStart[type] + m) {
                                obsStart = 0;
                            }
                            if (j >= jEnd - m && TTT[x][jEnd].chess != "_") {
                                obsEnd = 1;
                            }
                            //Calculate the basic weight value and disperse value
                            if (weightCount == m) {
                                //When the window's length is m, omit the end of window and add the head which is like a queue
                                if (TTT[x][j - m].chess != "_") {
                                    weight -= 1;
                                    if (j - m != 0 && TTT[x][j - m - 1].chess == "_") {
                                        disperse--;
                                    }
                                    if (TTT[x][j - m + 1].chess == "_") {
                                        disperse--;
                                    }
                                }
                                //Add the windows' weight into the total weight
                                totalWeight += WeightReCal(weight, obsStart, obsEnd, disperse);
                            } else {
                                weightCount++;
                                if (weightCount == m) totalWeight += WeightReCal(weight, obsStart, obsEnd, disperse);
                            }
                        }
                        j++;
                    }
                    ai.TTT[x][i].rowWeight[type] = totalWeight;
                }
            }
            i++;
        }
        //col calculation
        i = ai.TTT[x][y].colStart[type];
        end = ai.TTT[x][y].colEnd[type];
        while (i <= end) {
            if (ai.TTT[i][y].chess.equals("_")) {
                if (i > x - m && i < x + m) {
                    int j = ai.TTT[i][y].colStart[type];
                    int jEnd = ai.TTT[i][y].colEnd[type];
                    int weight = 0;
                    int obsStart = 0;
                    int obsEnd = 0;
                    int disperse = 0;
                    int totalWeight = 0;
                    int weightCount = 0;
                    while (j <= jEnd) {
                        if (j > i - m && j < i + m) {
                            if (TTT[j][y].chess != "_") {
                                weight += 1;
                                if (j != 0 && TTT[j - 1][y].chess == "_") {
                                    disperse++;
                                }
                                if (j != n - 1 && TTT[j + 1][y].chess == "_") {
                                    disperse++;
                                }
                            }
                            if (j == ai.TTT[i][y].colStart[type] && TTT[j][y].chess != "_") {
                                obsStart = 1;
                            }
                            if (j == ai.TTT[i][y].colStart[type] + m) {
                                obsStart = 0;
                            }
                            if (j >= jEnd - m && TTT[jEnd][y].chess != "_") {
                                obsEnd = 1;
                            }
                            if (weightCount == m) {
                                if (TTT[j - m][y].chess != "_") {
                                    weight -= 1;
                                    if (j - m != 0 && TTT[j - m - 1][y].chess == "_") {
                                        disperse--;
                                    }
                                    if (TTT[j - m + 1][y].chess == "_") {
                                        disperse--;
                                    }
                                }
                                totalWeight += WeightReCal(weight, obsStart, obsEnd, disperse);
                            } else {
                                weightCount++;
                                if (weightCount == m) totalWeight += WeightReCal(weight, obsStart, obsEnd, disperse);
                            }
                        }
                        j++;
                    }
                    ai.TTT[i][y].colWeight[type] = totalWeight;
                }
            }
            i++;
        }
        //rightDiagonal calculation
        i = ai.TTT[x][y].rDStart[type];
        end = ai.TTT[x][y].rDEnd[type];
        int offset = x - y;
        int newI;
        if (offset > 0) newI = i + offset;
        else newI = i;
        while (i <= end) {
            if (ai.TTT[newI][newI - offset].chess.equals("_")) {
                if (newI > x - m && newI < x + m) {
                    int j = ai.TTT[newI][newI - offset].rDStart[type];
                    int jEnd = ai.TTT[newI][newI - offset].rDEnd[type];
                    int weight = 0;
                    int obsStart = 0;
                    int obsEnd = 0;
                    int disperse = 0;
                    int totalWeight = 0;
                    int weightCount = 0;
                    int newJ;
                    if (offset > 0) newJ = j + offset;
                    else newJ = j;
                    while (j <= jEnd) {
                        if (j > i - m && j < i + m) {
                            if (TTT[newJ][newJ - offset].chess != "_") {
                                weight += 1;
                                if (newJ != 0 && newJ - offset != 0 && TTT[newJ - 1][newJ - offset - 1].chess == "_") {
                                    disperse++;
                                }
                                if (newJ != n - 1 && newJ - offset != n - 1 && TTT[newJ + 1][newJ - offset + 1].chess == "_") {
                                    disperse++;
                                }
                            }
                            if (j == ai.TTT[newI][newI - offset].rDStart[type] && TTT[newJ][newJ - offset].chess != "_") {
                                obsStart = 1;
                            }
                            if (j == ai.TTT[newI][newI - offset].rDStart[type] + m) {
                                obsStart = 0;
                            }
                            if (j >= jEnd - m && TTT[newJ + jEnd - j][newJ - offset + jEnd - j].chess != "_") {
                                obsEnd = 1;
                            }
                            if (weightCount == m) {
                                if (TTT[newJ - m][newJ - offset - m].chess != "_") {
                                    weight -= 1;
                                    if (newJ - m != 0 && newJ - offset - m != 0 && TTT[newJ - m - 1][newJ - offset - m - 1].chess == "_") {
                                        disperse--;
                                    }
                                    if (TTT[newJ - m + 1][newJ - offset - m + 1].chess == "_") {
                                        disperse--;
                                    }
                                }
                                totalWeight += WeightReCal(weight, obsStart, obsEnd, disperse);
                            } else {
                                weightCount++;
                                if (weightCount == m) totalWeight += WeightReCal(weight, obsStart, obsEnd, disperse);
                            }
                        }
                        j++;
                        newJ++;
                    }
                    ai.TTT[newI][newI - offset].rDWeight[type] = totalWeight;
                }
            }
            i++;
            newI++;
        }
        //leftDiagonal calculation
        i = ai.TTT[x][y].lDStart[type];
        end = ai.TTT[x][y].lDEnd[type];
        int sumset = x + y;
        if (sumset > n - 1) newI = n - 1 - i;
        else newI = sumset - i;
        while (i <= end) {
            if (ai.TTT[newI][sumset - newI].chess.equals("_")) {
                if (newI > x - m && newI < x + m) {
                    int j = ai.TTT[newI][sumset - newI].lDStart[type];
                    int jEnd = ai.TTT[newI][sumset - newI].lDEnd[type];
                    int weight = 0;
                    int obsStart = 0;
                    int obsEnd = 0;
                    int disperse = 0;
                    int totalWeight = 0;
                    int weightCount = 0;
                    int newJ;
                    if (sumset > n - 1) newJ = n - 1 - j;
                    else newJ = sumset - j;
                    while (j <= jEnd) {
                        if (j > i - m && j < i + m) {
                            if (TTT[newJ][sumset - newJ].chess != "_") {
                                weight += 1;
                                if (newJ != n - 1 && sumset - newJ != 0 && TTT[newJ + 1][sumset - newJ - 1].chess == "_") {
                                    disperse++;
                                }
                                if (newJ != 0 && sumset - newJ != n - 1 && TTT[newJ - 1][sumset - newJ + 1].chess == "_") {
                                    disperse++;
                                }
                            }
                            if (j == ai.TTT[newI][sumset - newI].lDStart[type] && TTT[newJ][sumset - newJ].chess != "_") {
                                obsStart = 1;
                            }
                            if (j == ai.TTT[newI][sumset - newI].lDStart[type] + m) {
                                obsStart = 0;
                            }
                            if (j >= jEnd - m && TTT[newJ - (jEnd - j)][sumset - newJ + (jEnd - j)].chess != "_") {
                                obsEnd = 1;
                            }
                            if (weightCount == m) {
                                if (TTT[newJ + m][sumset - newJ - m].chess != "_") {
                                    weight -= 1;
                                    if (newJ + m != n - 1 && sumset - newJ - m != 0 && TTT[newJ + m + 1][sumset - newJ - m - 1].chess == "_") {
                                        disperse--;
                                    }
                                    if (TTT[newJ + m - 1][sumset - newJ - m + 1].chess == "_") {
                                        disperse--;
                                    }
                                }
                                totalWeight += WeightReCal(weight, obsStart, obsEnd, disperse);
                            } else {
                                weightCount++;
                                if (weightCount == m) totalWeight += WeightReCal(weight, obsStart, obsEnd, disperse);
                            }
                        }
                        j++;
                        newJ--;
                    }
                    ai.TTT[newI][sumset - newI].lDWeight[type] = totalWeight;
                }
            }
            i++;
            newI--;
        }

    }

    //Calculate the total distance between start and end of each row, col, rD, lD of a point
    int CalculateStartEnd(int x, int y, int type, AI ai) {
        int sumOfSE = 0;
        if (!ai.TTT[x][y].chess.equals("_")) sumOfSE += pointP;
        sumOfSE += ai.TTT[x][y].rowEnd[type] - ai.TTT[x][y].rowStart[type];
        sumOfSE += ai.TTT[x][y].colEnd[type] - ai.TTT[x][y].colStart[type];
        sumOfSE += ai.TTT[x][y].rDEnd[type] - ai.TTT[x][y].rDStart[type];
        sumOfSE += ai.TTT[x][y].lDEnd[type] - ai.TTT[x][y].lDStart[type];
        return sumOfSE;
    }

    //Potential
    //What is potential?
    //Imagine you have a box like this
    //  _ X _ _
    //  _ _ _ _
    //  _ X _ _
    //  _ _ _ _
    //The position (0,0) and (3,3) which one do you think is better if you are "O"?
    //Without the consideration of weight
    //(3,3) is still better than (3,3)
    //Because you can find the (0,0) can only develop in the down direction and rightDiagonal direction. Moreover, on this direction, the point (2,1) also restricts the developement of (0,0)
    //In comparision, (3,3) is much better in potential.
    //The calculation of potential of one point is in two level
    //The first level is its row, col, rD, lD which we can get by CalculateStartEnd()
    //The second level is redo the CalculateStartEnd() on the points in its row, col, rD, lD
    void CalculatePotential(int x, int y, int type, AI ai) {

        ai.TTT[x][y].potential[type] = 0;
        int offset = x - y;
        int sumset = x + y;
        //row
        int i = ai.TTT[x][y].rowStart[type];
        int end = ai.TTT[x][y].rowEnd[type];
        while (i <= end) {
            ai.TTT[x][y].potential[type] += CalculateStartEnd(x, i, type, ai);
            i++;
        }
        //row
        i = ai.TTT[x][y].colStart[type];
        end = ai.TTT[x][y].colEnd[type];
        while (i <= end) {
            ai.TTT[x][y].potential[type] += CalculateStartEnd(i, y, type, ai);
            i++;
        }
        //rD
        i = ai.TTT[x][y].rDStart[type];
        end = ai.TTT[x][y].rDEnd[type];
        while (i <= end) {
            if (offset > 0) ai.TTT[x][y].potential[type] += CalculateStartEnd(i + offset, i, type, ai);
            else ai.TTT[x][y].potential[type] += CalculateStartEnd(i, i - offset, type, ai);
            i++;
        }
        //lD
        i = ai.TTT[x][y].lDStart[type];
        end = ai.TTT[x][y].lDEnd[type];
        while (i <= end) {
            if (sumset > n - 1)
                ai.TTT[x][y].potential[type] += CalculateStartEnd(n - 1 - i, i + sumset - n + 1, type, ai);
            else ai.TTT[x][y].potential[type] += CalculateStartEnd(sumset - i, i, type, ai);
            i++;
        }
    }

    //After getting the function of CalculatePotential()
    //We also need to reset the corresponding point which is the point in StartEnd domain of row, col, rD, lD of the dropped point
    void AdjustPotential(int x, int y, AI ai) {
        for (int type = 0; type <= 1; type++) {
            int offset = x - y;
            int sumset = x + y;
            //row
            int i = ai.TTT[x][y].rowStart[type];
            int end = ai.TTT[x][y].rowEnd[type];
            while (i <= end) {
                CalculatePotential(x, i, type, ai);
                i++;
            }
            //col
            i = ai.TTT[x][y].colStart[type];
            end = ai.TTT[x][y].colEnd[type];
            while (i <= end) {
                CalculatePotential(i, y, type, ai);
                i++;
            }
            //rD
            i = ai.TTT[x][y].rDStart[type];
            end = ai.TTT[x][y].rDEnd[type];
            while (i <= end) {
                if (offset > 0) {
                    CalculatePotential(i + offset, i, type, ai);
                } else {
                    CalculatePotential(i, i - offset, type, ai);
                }
                i++;
            }
            //lD
            i = ai.TTT[x][y].lDStart[type];
            end = ai.TTT[x][y].lDEnd[type];
            while (i <= end) {
                if (sumset > n - 1) {
                    CalculatePotential(n - 1 - i, i + sumset - n + 1, type, ai);
                } else {
                    CalculatePotential(sumset - i, i, type, ai);
                }
                i++;
            }
        }
    }

    //To do the MinMax
    //We also need to find a good domain in each level to improve the efficiency
    //Basically, we use the point judge(which is based the part we have introduced before, the weight, potential and bias(too basic) function)
    //Point.totalPotential is the point judge value
    //In the future, I will link the size of optimalLists to the remaining empty space in the board
    //Since this value is really related to the efficiency of our algorithm but also really positive correlation to our accuracy
    void FindOptimalXYList(AI ai) {
        optimalLists = new ArrayList<>();
        OptimalList minOptimal = new OptimalList(-1, -1, -9999999);
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                if (ai.TTT[x][y].chess.equals("_")) {
                    if (optimalLists.size() < 10) {
                        optimalLists.add(new OptimalList(x, y, ai.TTT[x][y].totalPotential));
                    } else if(ai.TTT[x][y].totalPotential>minOptimal.pointPotential) {
                        minOptimal = new OptimalList(x, y, ai.TTT[x][y].totalPotential);
                        optimalLists.add(minOptimal);
                        for (int i = 0; i < 11; i++) {
                            if (optimalLists.get(i).pointPotential < minOptimal.pointPotential) {
                                minOptimal = optimalLists.get(i);
                            }
                        }
                        optimalLists.remove(minOptimal);
                    }
                }
            }
        }
    }


    //Reset the totalPotential of Point(base on some Point's value we introduce before)
    //The evaluation function in Point class: totalPotential = ourWeight * AIWeight() + oppWeight * MyWeight() - biasP * bias;
    //AIWeight() is used to get the total weight of a point which we get before plus the potential of a point
    //bias = (X-Xcenter)^2 + (Y-Ycenter)^2
    //We can believe that bias is useful especially at the beginning of the game
    void PotentialReset(int x, int y, AI ai, int type) {
        //Row
        int i = Math.min(ai.TTT[x][y].rowStart[0], ai.TTT[x][y].rowStart[1]);
        int end = Math.max(ai.TTT[x][y].rowEnd[0], ai.TTT[x][y].rowEnd[1]);
        while (i <= end) {
            if (ai.TTT[x][i].chess == "_") {
                ai.TTT[x][i].totalPotential(type);
            }
            i++;
        }
        //Col
        i = Math.min(ai.TTT[x][y].colStart[0], ai.TTT[x][y].colStart[1]);
        end = Math.max(ai.TTT[x][y].colEnd[0], ai.TTT[x][y].colEnd[1]);
        while (i <= end) {
            if (ai.TTT[i][y].chess == "_") {
                ai.TTT[i][y].totalPotential(type);
            }
            i++;
        }
        //RD
        i = Math.min(ai.TTT[x][y].rDStart[0], ai.TTT[x][y].rDStart[1]);
        end = Math.max(ai.TTT[x][y].rDEnd[0], ai.TTT[x][y].rDEnd[1]);
        int offset = x - y;
        int newI;
        if (offset > 0) newI = i + offset;
        else newI = i;
        while (i <= end) {
            if (ai.TTT[newI][newI - offset].chess == "_") {
                ai.TTT[newI][newI - offset].totalPotential(type);
            }
            i++;
            newI++;
        }
        //LD
        i = Math.min(ai.TTT[x][y].lDStart[0], ai.TTT[x][y].lDStart[1]);
        end = Math.max(ai.TTT[x][y].lDEnd[0], ai.TTT[x][y].lDEnd[1]);
        int sumset = x + y;
        if (sumset > n - 1) newI = n - 1 - i;
        else newI = sumset - i;
        while (i <= end) {
            if (ai.TTT[newI][sumset - newI].chess == "_") {
                ai.TTT[newI][sumset - newI].totalPotential(type);
            }
            i++;
            newI--;
        }
    }

    //Now, we have the totalPotential of each point
    //But how to judge the situation. Is that good or bad?
    //We can judge it by evaluation value
    //ai.evaluationValue += ai.TTT[i][j].AIWeight() - ai.TTT[i][j].MyWeight()
    //AIWeight means "O" power，MyWeigh means "X" power
    //So evaluationValue = "O" power - "X" power in the whole board(empty space)
    //Obviously, "O" want to maximize it but "X" want to minimize it
    void EvaluateFunction(AI ai){
        ai.evaluationValue = 0;
        for(int i = 0; i < ai.n; i++){
            for(int j = 0;j<ai.n;j++){
                if(ai.TTT[i][j].chess.equals("_")){
                    ai.evaluationValue += ai.TTT[i][j].AIWeight() - ai.TTT[i][j].MyWeight();
                }
            }
        }
    }

    //WeightReCal() is use to calculate weight with value of weight, edgeBlock?, disperse
    int WeightReCal(int weight, int obsStart, int obsEnd, int disperse) {
        if(weight == m) return 9999999;
        if (obsEnd == 1 && obsStart == 1 && weight != (m - 1)) return 1;
        else if (weight <= m - 4) return weight * 3 + disperse - obsStart - obsEnd;
        else if (weight == m - 3) return 20 + disperse * 2 - (obsStart + obsEnd) * 10;
        else if (weight == m - 2) {
            if (obsStart == 1 || obsEnd == 1) {
                return 40 + disperse * 10;
            } else return 10000 + disperse * 2000;
        } else if (weight == m - 1) {
            return 999999;
        }
        return weight;
    }
}

