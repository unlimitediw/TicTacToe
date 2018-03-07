import java.lang.Math;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AI {
    //Parameters

    //Initialize chess
    String yourChess;//yourChess
    String opponentChess;//OpponentChess

    //Size of board
    int n;

    //Size of win
    int m;

    //搜索层数
    int floor;

    //Weight parameter
    double w;

    //一次迭代下的潜力权重参数 easy potential parameter
    double p;

    //Distance from center parameter
    double b;

    //Memory maxPotential(a order heap)
    double maxPotential;

    double evaluationValue;

    double[][] evaluationIndi;

    MinMaxState[] minMaxStateArray;

    //CalculateSE中的子系数
    int pointP;

    //Initialize board;
    Point[][] TTT;

    List<OptimalList> optimalLists = new ArrayList<OptimalList>();

    int optX, optY;

    AI(double w, double p, double b, int pointP, String yourChess, String opponentChess, double ourWeight, double oppWeight, int n, int m,int floor) {
        this.w = w;
        this.p = p;
        this.b = b;
        this.n = n;
        this.m = m;
        this.floor = floor;
        this.pointP = pointP;
        this.yourChess = yourChess;
        this.opponentChess = opponentChess;
        this.evaluationValue = 0;
        evaluationIndi = new double[n][n];
        TTT = new Point[n][n];
        for (int x = 0; x < TTT.length; x++) {
            for (int y = 0; y < TTT[0].length; y++) {
                TTT[x][y] = new Point(x, y, w, p, b, n, ourWeight, oppWeight);
                TTT[x][y].x = x;//Initialize x&y position
                TTT[x][y].y = y;
                double center = ((double) n - 1.0) / 2.0;//9.5 when n = 20.
                TTT[x][y].bias = Math.pow((double) x - center, 2) + Math.pow((double) y - center, 2);//Initialize bias
            }
        }
    }

    void PotentialIni(AI ai) {
        for (int x = 0; x < TTT.length; x++) {
            for (int y = 0; y < ai.TTT[0].length; y++) {
                CalculatePotential(x, y, 0, ai);
                CalculatePotential(x, y, 1, ai);
                ai.TTT[x][y].totalPotential(0);
            }
        }
    }

    void Play(int x, int y, int turn, AI ai) {
        if (turn % 2 == 1) {
            FindOptimalXY(ai);
            FindOptimalXYList(ai);
            ai.TTT[optX][optY].chess = "X";
            System.out.println("X");
            System.out.println("x: " + optY + " y: " + optX);
            ResetStartEnd(optX, optY, 0, ai);
            CalculateWeight(optX, optY, 1, ai);
            CalculateWeight(optX, optY, 0, ai);
            AdjustPotential(optX, optY, ai);
            PotentialReset(optX, optY, ai, 0);
            System.out.println(ai.evaluationValue);
            DisplayBoard.displayBoard(ai.TTT, ai);
        } else {
            FindOptimalXY(ai);
            FindOptimalXYList(ai);
            ai.TTT[optX][optY].chess = "O";
            System.out.println("O");
            System.out.println("x: " + optY + " y: " + optX);
            ResetStartEnd(optX, optY, 1, ai);
            CalculateWeight(optX, optY, 0, ai);
            CalculateWeight(optX, optY, 1, ai);
            AdjustPotential(optX, optY, ai);
            PotentialReset(optX, optY, ai, 1);
            System.out.println(ai.evaluationValue);
            DisplayBoard.displayBoard(ai.TTT, ai);
        }
    }

    void MinMax(AI ai,int floor,int count,int x,int y,int turn,MinMaxState[] minMaxStateArray){
        if(count == 0){
            minMaxStateArray = new MinMaxState[floor];
        }
        int k = count + turn%2;
        AI subAI = ai;
        subAI.Play(x,y,k%2,subAI);
        if(count == floor){
            //generate evaluate value
        }
        for(int i = 0; i < subAI.optimalLists.size();i++) {
            MinMax(ai, floor, count++, ai.optimalLists.get(i).x,ai.optimalLists.get(i).y,turn, minMaxStateArray);
        }
    }

    //After dropping at (x,y) reset SE value of the points in SE of (x,y)
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
                    ai.TTT[n - 1 - i][i + sumset - n + 1].lDEnd[type] = y - (sumset - n + 1) - 1;
                if (i + sumset - n + 1 > y)
                    ai.TTT[n - 1 - i][i + sumset - n + 1].lDStart[type] = y - (sumset - n + 1) + 1;
            } else {
                if (i < y) ai.TTT[sumset - i][i].lDEnd[type] = y - 1;
                if (i > y) ai.TTT[sumset - i][i].lDStart[type] = y + 1;
            }
            i++;
        }
    }

    //calculate new weight of points in line,row,rd,ld of (x,y) after set(x,y)
    //(x,y)will maximum affect 15 points in weight calculation, the close the more. Start and end will cutoff this effect
    //如果要改动权重重量与棋数关系 两种都要改
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
                            //System.out.println("ii: " + i+ " y-m: "+(y-m) + " y: "+y);
                            //计算独立weight值与离散程度
                            if (!TTT[x][j].chess.equals("_")) {
                                weight += 1;
                                if (j != 0 && TTT[x][j - 1].chess == "_") {
                                    disperse++;
                                }
                                if (j != n - 1 && TTT[x][j + 1].chess == "_") {
                                    disperse++;
                                }
                            }
                            //计算开头或结尾与敌方相接的情况
                            if (j == ai.TTT[x][i].rowStart[type] && TTT[x][j].chess != "_") {
                                obsStart = 1;
                            }
                            if (j == ai.TTT[x][i].rowStart[type] + m) {
                                obsStart = 0;
                            }
                            if (j >= jEnd - m && TTT[x][jEnd].chess != "_") {
                                obsEnd = 1;
                            }
                            //计算总体weight值
                            if (weightCount == m) {
                                //达到窗口长度M时剪掉尾巴的disperse和weight
                                if (TTT[x][j - m].chess != "_") {
                                    weight -= 1;
                                    if (j - m != 0 && TTT[x][j - m - 1].chess == "_") {
                                        disperse--;
                                    }
                                    if (TTT[x][j - m + 1].chess == "_") {
                                        disperse--;
                                    }
                                }
                                //将该窗口的独立weight值加入总体weight值
                                totalWeight += WeightReCal(weight, obsStart, obsEnd, disperse);
                            } else {
                                weightCount++;
                                if (weightCount == m) totalWeight += WeightReCal(weight, obsStart, obsEnd, disperse);
                            }
                            if (weight >= m - 1 && obsStart == 0 && obsEnd == 0 && disperse == 2) {
                                System.out.println("ax: " + x + " y: " + i);
                                System.out.println("row");
                                System.out.println("weight: " + weight);
                                System.out.println("i: " + i);
                                System.exit(1);
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
                            //计算独立weight值与离散程度

                            if (TTT[j][y].chess != "_") {
                                weight += 1;
                                if (j != 0 && TTT[j - 1][y].chess == "_") {
                                    disperse++;
                                }
                                if (j != n - 1 && TTT[j + 1][y].chess == "_") {
                                    disperse++;
                                }
                            }
                            //计算开头或结尾与敌方相接的情况
                            if (j == ai.TTT[i][y].colStart[type] && TTT[j][y].chess != "_") {
                                obsStart = 1;
                            }
                            if (j == ai.TTT[i][y].colStart[type] + m) {
                                obsStart = 0;
                            }
                            if (j >= jEnd - m && TTT[jEnd][y].chess != "_") {
                                obsEnd = 1;
                            }
                            //计算总体weight值
                            if (weightCount == m) {
                                //达到窗口长度M时剪掉尾巴的disperse和weight
                                if (TTT[j - m][y].chess != "_") {
                                    weight -= 1;
                                    if (j - m != 0 && TTT[j - m - 1][y].chess == "_") {
                                        disperse--;
                                    }
                                    if (TTT[j - m + 1][y].chess == "_") {
                                        disperse--;
                                    }
                                }
                                //将该窗口的独立weight值加入总体weight值
                                totalWeight += WeightReCal(weight, obsStart, obsEnd, disperse);
                            } else {
                                weightCount++;
                                if (weightCount == m) totalWeight += WeightReCal(weight, obsStart, obsEnd, disperse);
                            }
                            if (weight >= m - 1 && obsStart == 0 && obsEnd == 0 && disperse == 2) {
                                System.out.println("ax: " + i + " y: " + y);
                                System.out.println("col");
                                System.out.println("weight: " + weight);
                                System.exit(1);
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
                            //计算独立weight值与离散程度
                            if (TTT[newJ][newJ - offset].chess != "_") {
                                weight += 1;
                                if (newJ != 0 && newJ - offset != 0 && TTT[newJ - 1][newJ - offset - 1].chess == "_") {
                                    disperse++;
                                }
                                if (newJ != n - 1 && newJ - offset != n - 1 && TTT[newJ + 1][newJ - offset + 1].chess == "_") {
                                    disperse++;
                                }
                            }
                            //计算开头或结尾与敌方相接的情况
                            if (j == ai.TTT[newI][newI - offset].rDStart[type] && TTT[newJ][newJ - offset].chess != "_") {
                                obsStart = 1;
                            }
                            if (j == ai.TTT[newI][newI - offset].rDStart[type] + m) {
                                obsStart = 0;
                            }
                            if (j >= jEnd - m && TTT[newJ + jEnd - j][newJ - offset + jEnd - j].chess != "_") {
                                obsEnd = 1;
                            }
                            //计算总体weight值
                            if (weightCount == m) {
                                //达到窗口长度M时剪掉尾巴的disperse和weight
                                if (TTT[newJ - m][newJ - offset - m].chess != "_") {
                                    weight -= 1;
                                    if (newJ - m != 0 && newJ - offset - m != 0 && TTT[newJ - m - 1][newJ - offset - m - 1].chess == "_") {
                                        disperse--;
                                    }
                                    if (TTT[newJ - m + 1][newJ - offset - m + 1].chess == "_") {
                                        disperse--;
                                    }
                                }
                                //将该窗口的独立weight值加入总体weight值
                                totalWeight += WeightReCal(weight, obsStart, obsEnd, disperse);
                            } else {
                                weightCount++;
                                if (weightCount == m) totalWeight += WeightReCal(weight, obsStart, obsEnd, disperse);
                            }
                            if (weight >= m - 1 && obsStart == 0 && obsEnd == 0 && disperse == 2) {
                                System.out.println("ax: " + newI + " y: " + (newI - offset));
                                System.out.println("rD");
                                System.out.println("weight: " + weight);
                                System.exit(1);
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
                            //计算独立weight值与离散程度
                            if (TTT[newJ][sumset - newJ].chess != "_") {
                                weight += 1;
                                if (newJ != n - 1 && sumset - newJ != 0 && TTT[newJ + 1][sumset - newJ - 1].chess == "_") {
                                    disperse++;
                                }
                                if (newJ != 0 && sumset - newJ != n - 1 && TTT[newJ - 1][sumset - newJ + 1].chess == "_") {
                                    disperse++;
                                }
                            }
                            //计算开头或结尾与敌方相接的情况
                            if (j == ai.TTT[newI][sumset - newI].lDStart[type] && TTT[newJ][sumset - newJ].chess != "_") {
                                obsStart = 1;
                            }
                            if (j == ai.TTT[newI][sumset - newI].lDStart[type] + m) {
                                obsStart = 0;
                            }
                            if (j >= jEnd - m && TTT[newJ - (jEnd - j)][sumset - newJ + (jEnd - j)].chess != "_") {
                                obsEnd = 1;
                            }
                            //计算总体weight值
                            if (weightCount == m) {
                                //达到窗口长度M时剪掉尾巴的disperse和weight
                                if (TTT[newJ + m][sumset - newJ - m].chess != "_") {
                                    weight -= 1;
                                    if (newJ + m != n - 1 && sumset - newJ - m != 0 && TTT[newJ + m + 1][sumset - newJ - m - 1].chess == "_") {
                                        disperse--;
                                    }
                                    if (TTT[newJ + m - 1][sumset - newJ - m + 1].chess == "_") {
                                        disperse--;
                                    }
                                }
                                //将该窗口的独立weight值加入总体weight值
                                totalWeight += WeightReCal(weight, obsStart, obsEnd, disperse);
                            } else {
                                weightCount++;
                                if (weightCount == m) totalWeight += WeightReCal(weight, obsStart, obsEnd, disperse);
                            }
                            if (weight >= m - 1 && obsStart == 0 && obsEnd == 0 && disperse == 2) {
                                System.out.println("ax: " + newI + " y: " + (sumset - newI));
                                System.out.println("lD");
                                System.out.println("weight: " + weight);
                                System.exit(1);
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

    //sum of SE
    //注意这里同时加入了当点为子时的权重增加
    int CalculateStartEnd(int x, int y, int type, AI ai) {
        int sumOfSE = 0;
        if (!ai.TTT[x][y].chess.equals("_")) sumOfSE += pointP;
        sumOfSE += ai.TTT[x][y].rowEnd[type] - ai.TTT[x][y].rowStart[type];
        sumOfSE += ai.TTT[x][y].colEnd[type] - ai.TTT[x][y].colStart[type];
        sumOfSE += ai.TTT[x][y].rDEnd[type] - ai.TTT[x][y].rDStart[type];
        sumOfSE += ai.TTT[x][y].lDEnd[type] - ai.TTT[x][y].lDStart[type];
        return sumOfSE;
    }

    //calculate new potential of points in line,row,rd,ld of (x,y) after set(x,y)
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

    //给(x,y)每行每列每对角线上的点的直接潜力值进行重算，并对他们的总潜力值进行重算。
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

    //找出总潜力值最大的点落子(无minmax下启用)
    void FindOptimalXY(AI ai) {
        double totalPotential = -999999;
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                if (ai.TTT[x][y].chess.equals("_")) {
                    if (ai.TTT[x][y].totalPotential > totalPotential) {
                        totalPotential = ai.TTT[x][y].totalPotential;
                        optX = x;
                        optY = y;
                    }
                }
            }
        }
    }

    //找到最大的20个potential点(minmax下启用)
    void FindOptimalXYList(AI ai) {
        optimalLists = new ArrayList<OptimalList>();
        OptimalList minOptimal = new OptimalList(-1, -1, -9999999);
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                if (ai.TTT[x][y].chess.equals("_")) {
                    if (optimalLists.size() < 20) {
                        optimalLists.add(new OptimalList(x, y, ai.TTT[x][y].totalPotential));
                    } else if(ai.TTT[x][y].totalPotential>minOptimal.pointPotential) {
                        minOptimal = new OptimalList(x, y, ai.TTT[x][y].totalPotential);
                        optimalLists.add(minOptimal);
                        for (int i = 0; i < 21; i++) {
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

    void PotentialReset(int x, int y, AI ai, int type) {
        //评估函数：E = sigema(totalP(Xi,Yi,0) - totalP(Xi,Yi,1))
        //只对被影响点进行重置
        //ai.evaluationValue -= ai.evaluationIndi[x][y];
        //Row
        int i = Math.min(ai.TTT[x][y].rowStart[0], ai.TTT[x][y].rowStart[1]);
        int end = Math.max(ai.TTT[x][y].rowEnd[0], ai.TTT[x][y].rowEnd[1]);
        while (i <= end) {
            if (ai.TTT[x][i].chess == "_") {
                ai.TTT[x][i].totalPotential(type);
                //如果不需要每级评估，请关闭下面三条和函数的第一行
                //ai.evaluationValue -= ai.evaluationIndi[x][i];
                //ai.evaluationIndi[x][i] = (ai.TTT[x][i].AIWeight() - ai.TTT[x][i].MyWeight());
                //ai.evaluationValue += ai.evaluationIndi[x][i];
            }
            i++;
        }
        //Col
        i = Math.min(ai.TTT[x][y].colStart[0], ai.TTT[x][y].colStart[1]);
        end = Math.max(ai.TTT[x][y].colEnd[0], ai.TTT[x][y].colEnd[1]);
        while (i <= end) {
            if (ai.TTT[i][y].chess == "_") {
                ai.TTT[i][y].totalPotential(type);
                //ai.evaluationValue -= ai.evaluationIndi[i][y];
                //ai.evaluationIndi[i][y] = (ai.TTT[i][y].AIWeight() - ai.TTT[i][y].MyWeight());
                //ai.evaluationValue += ai.evaluationIndi[i][y];
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
                //ai.evaluationValue -= ai.evaluationIndi[newI][newI - offset];
                //ai.evaluationIndi[newI][newI - offset] = (ai.TTT[newI][newI - offset].AIWeight() - ai.TTT[newI][newI - offset].MyWeight());
                //ai.evaluationValue += ai.evaluationIndi[newI][newI - offset];
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
                ai.evaluationValue -= ai.evaluationIndi[newI][sumset - newI];
                ai.evaluationIndi[newI][sumset - newI] = (ai.TTT[newI][sumset - newI].AIWeight() - ai.TTT[newI][sumset - newI].MyWeight());
                ai.evaluationValue += ai.evaluationIndi[newI][sumset - newI];
            }
            i++;
            newI--;
        }
    }

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


    int WeightReCal(int weight, int obsStart, int obsEnd, int disperse) {
        if (obsEnd == 1 && obsStart == 1 && weight != m - 1) return 1;
        else if (weight <= m - 4) return weight * 3 + disperse - obsStart - obsEnd;
        else if (weight == m - 3) return 20 + disperse * 2 - (obsStart + obsEnd) * 10;
        else if (weight == m - 2) {
            if (obsStart == 1 || obsEnd == 1) {
                return 40 + disperse * 10;
            } else return 10000 + disperse * 2000;
        } else if (weight == m - 1) {
            if (obsStart == 1 || obsEnd == 1) {
                return 40000 + disperse * 4000;
            } else return 100000 + disperse * 20000;
        }
        return weight;
    }
}
