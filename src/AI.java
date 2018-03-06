import java.lang.Math;

//严重警告 由于围棋下多了[捂脸] 全部X Y请反着看 也就是 row是col.... col是row...

public class AI {
    //Parameters

    //Initialize chess
    String yourChess;//yourChess
    String opponentChess;//OpponentChess

    //Size of board
    int n = 8;

    //Size of win
    int m = 6;

    //Weight parameter
    double w;

    //一次迭代下的潜力权重参数 easy potential parameter
    double p;

    //Distance from center parameter
    double b;

    //Memory maxPotential(a order heap)
    double maxPotential;

    //CalculateSE中的为子系数
    int pointP;

    //Initialize board;
    Point[][] TTT = new Point[n][n];

    int optX,optY;

    AI(double w,double p,double b,int pointP,String yourChess,String opponentChess){
        this.w = w;
        this.p = p;
        this.b = b;
        this.pointP = pointP;
        this.yourChess = yourChess;
        this.opponentChess = opponentChess;
        TTTIni(TTT,w,p,b);
    }

    //Initialize x&y, bias and potential for each point
    void TTTIni(Point[][] TTT,double w,double p,double b){
        for(int x = 0; x< TTT.length;x++){
            for(int y = 0; y < TTT[0].length;y++){
                TTT[x][y] = new Point(x,y,w,p,b,n,m);
                TTT[x][y].x = x;//Initialize x&y position
                TTT[x][y].y = y;
                double center = ((double)n-1.0)/2.0;//9.5 when n = 20.
                TTT[x][y].bias = Math.pow((double)x-center,2) +Math.pow((double)y-center,2);//Initialize bias
                //Potential is constructed by 5 components
                //Considering the time complexity, we only use one iteration of potential
                //Potential parts：
                //1: row potential:(rowStart to rowEnd) sum of weights * w and length from start to end in 4 directions
                //Prow = sum(from start to end): w*distance(距离计算点的offset)*(rW+cW+rDW+lDW)+rL+cL+rDL+lDL(+p*(P)(not affect one) optional!!!)
                //这个地方有两个方向:1(我现在的方向）延伸点上的potential只是用该点的weight和潜在范围做
                //2.（待研究）每次更新完点后，全局potential变动。比如说，更新延时方向potential时，给延时方向的点一个较大的potential*系数，然后对延伸点的延时依次做根据延伸点P值变化的变化，复杂度会比较高
                //2方法 落子后只更新局部potential 供给给未来的potential计算 potential要做到增加而不是减少，potential不能归零，主要是系数调整比较麻烦，感觉可行（一种伪最优化解）
                //2,3,4: same as 1
                //bias:-b*bias
                //Final version:(row,col,rD,lD)sum of direction(rowStart to rowEnd) sum of weights * w and length from start to end in 4 directions
                //In the situation of initialization, we don't need to consider the part of weight

            }
        }
    }

    void PotentialIni(AI ai){
        for(int x = 0; x< ai.TTT.length;x++) {
            for (int y = 0; y < ai.TTT[0].length; y++) {
                CalculatePotential(x,y,0,ai);
                CalculatePotential(x,y,1,ai);
            }
        }
    }

    void Play(int x, int y, int turn,AI ai){

        //(x,y)落点后TTT[x][y].weight增加 potential归零

        //The elements in col[x] reset value, only for start to end
        if(turn == 1){
            FindOptimalXY(ai);
            ai.TTT[optX][optY].chess ="O";
            ResetStartEnd(optX,optY,1,ai);
            CalculateWeight(optX,optY,0,ai);
            CalculateWeight(optX,optY,1,ai);
            AdjustPotential(optX,optY,ai);
            DisplayBoard.displayBoard(ai.TTT);
            System.out.println("x: "+optY+" y: " +optX);
        }
            //对手CRRL的SE重置，范围内weight重置
            //对手
            //SE值

            //对手范围内weight重置
            //己方CRRL的SE重置，范围内weight重置
        else {
            ai.TTT[y][x].chess = "X";
            ResetStartEnd(y, x, 0, ai);
            CalculateWeight(y, x, 0, ai);
            CalculateWeight(y, x, 1, ai);
            AdjustPotential(y, x, ai);
            DisplayBoard.displayBoard(ai.TTT);
            System.out.println();

            //己方范围内weight重置
            FindOptimalXY(ai);
            ai.TTT[optX][optY].chess = "O";
            ResetStartEnd(optX, optY, 1, ai);
            CalculateWeight(optX, optY, 0, ai);
            CalculateWeight(optX, optY, 1, ai);
            AdjustPotential(optX, optY, ai);
            DisplayBoard.displayBoard(ai.TTT);
            System.out.println("x: " + optY + " y: " + optX);
        }
        //The elements in row[y] reset value, only for start to end
        //The right diagonal rD[19-y+x] reset value(p.s. element.x = element.y + offset(offset = x - y)), only for start to end
        //The left diagonal lD[x+y] rest value(p.s. element.x = 19 + offset - element.y(offset = x-y)), only for start to end
    }

    //After dropping at (x,y) reset SE value of the points in SE of (x,y)
    void ResetStartEnd(int x, int y,int type,AI ai) {
        //row direction calculate
        int i = ai.TTT[x][y].rowStart[type];
        int end = ai.TTT[x][y].rowEnd[type];
        while (i <= end) {
            if (i < y) ai.TTT[x][i].rowEnd[type] = y-1;
            if (i > y) ai.TTT[x][i].rowStart[type] = y+1;
            i++;
        }
        //col direction calculate
        i = ai.TTT[x][y].colStart[type];
        end = ai.TTT[x][y].colEnd[type];
        while (i <= end) {
            if (i < x) ai.TTT[i][y].colEnd[type] = x-1;
            if (i > x) ai.TTT[i][y].colStart[type] = x+1;
            i++;
        }
        //RD direction calculate
        i = ai.TTT[x][y].rDStart[type];
        end = ai.TTT[x][y].rDEnd[type];
        int offset = x - y;
        while (i <= end) {
            if (offset > 0) {
                if (i + offset < x) ai.TTT[i + offset][i].rDEnd[type] = x - offset-1;
                if (i + offset > x) ai.TTT[i + offset][i].rDStart[type] = x - offset+1;
            } else {
                if (i < x) ai.TTT[i][i - offset].rDEnd[type] = x-1;
                if (i > x) ai.TTT[i][i - offset].rDStart[type] = x+1;
            }
            i++;
        }
        //LD direction calculate
        i = ai.TTT[x][y].lDStart[type];
        end = ai.TTT[x][y].lDEnd[type];
        int sumset = x + y;
        while (i <= end) {
            if (sumset > n-1) {//19 when n = 20
                if (i + sumset - n + 1 < y)
                    ai.TTT[n - 1 - i][i + sumset - n + 1].lDEnd[type] = y - (sumset - n + 1) - 1;
                if (i + sumset - n + 1 > y)
                    ai.TTT[n - 1 - i][i + sumset - n + 1].lDStart[type] = y - (sumset - n + 1) + 1;
            }else {
                if (i < y) ai.TTT[sumset - i][i].lDEnd[type] = y - 1;
                if (i > y) ai.TTT[sumset - i][i].lDStart[type] = y + 1;
            }
            i++;
        }
    }
    //calculate new weight of points in line,row,rd,ld of (x,y) after set(x,y)
    //(x,y)will maximum affect 15 points in weight calculation, the close the more. Start and end will cutoff this effect
    //如果要改动权重重量与棋数关系 两种都要改
    void CalculateWeight(int x,int y,int type,AI ai){
        //row calculation
        int i = ai.TTT[x][y].rowStart[type];
        int end = ai.TTT[x][y].rowEnd[type];
        while(i<=end) {
            //if same type we only need to add the new weight on old one
            //8 when m = 8
            if(i>y-m&&i<y+m) {
                int j = ai.TTT[x][i].rowStart[type];
                int jEnd = ai.TTT[x][i].rowEnd[type];
                int weight = 0;
                int obsStart = 0;
                int obsEnd = 0;
                int disperse =0;
                int totalWeight = 0;
                int weightCount = 0;
                while (j <= jEnd) {
                    if (j > i - m && j < i + m) {
                        //计算独立weight值与离散程度
                        if(TTT[x][j].chess != "_") {
                            weight += 1;
                            if (j != 0 && TTT[x][j - 1].chess == "_") {
                                disperse++;
                            }
                            if (j != n - 1 && TTT[x][j + 1].chess == "_") {
                                disperse++;
                            }
                        }
                        //计算开头或结尾与敌方相接的情况
                        if(j == ai.TTT[x][i].rowStart[type]&&TTT[x][j].chess != "_"){
                            obsStart = 1;
                        }
                        if(j == ai.TTT[x][i].rowStart[type]+m){
                            obsStart = 0;
                        }
                        if(j >= jEnd-m&&TTT[x][jEnd].chess != "_"){
                            obsEnd = 1;
                        }
                        //计算总体weight值
                        if (weightCount == m) {
                            //达到窗口长度M时剪掉尾巴的disperse和weight
                            if(TTT[x][j-m].chess != "_") {
                                weight -= 1;
                                if (j-m!=0&&TTT[x][j - m - 1].chess == "_") {
                                    disperse--;
                                }
                                if (TTT[x][j - m + 1].chess == "_") {
                                    disperse--;
                                }
                            }
                            //将该窗口的独立weight值加入总体weight值
                            totalWeight += WeightReCal(weight,obsStart,obsEnd,disperse);
                        } else {
                            weightCount++;
                            if (weightCount == m) totalWeight += WeightReCal(weight,obsStart,obsEnd,disperse);
                        }
                    }
                    j++;
                }
                ai.TTT[x][i].rowWeight[type] = totalWeight;
            }
            i++;
        }
        //col calculation
        i = ai.TTT[x][y].colStart[type];
        end = ai.TTT[x][y].colEnd[type];
        while(i<=end) {
            if(i>x-m&&i<x+m) {
                int j = ai.TTT[i][y].colStart[type];
                int jEnd = ai.TTT[i][y].colEnd[type];
                int weight = 0;
                int obsStart = 0;
                int obsEnd = 0;
                int disperse =0;
                int totalWeight = 0;
                int weightCount = 0;
                while (j <= jEnd) {
                    if (j > i - m && j < i + m) {
                        //计算独立weight值与离散程度
                        if(TTT[j][y].chess != "_") {
                            weight += 1;
                            if (j != 0 && TTT[j - 1][y].chess == "_") {
                                disperse++;
                            }
                            if (j != n - 1 && TTT[j + 1][y].chess == "_") {
                                disperse++;
                            }
                        }
                        //计算开头或结尾与敌方相接的情况
                        if(j == ai.TTT[i][y].colStart[type]&&TTT[j][y].chess != "_"){
                            obsStart = 1;
                        }
                        if(j == ai.TTT[i][y].colStart[type]+m){
                            obsStart = 0;
                        }
                        if(j >= jEnd-m&&TTT[jEnd][y].chess != "_"){
                            obsEnd = 1;
                        }
                        //计算总体weight值
                        if (weightCount == m) {
                            //达到窗口长度M时剪掉尾巴的disperse和weight
                            if(TTT[j-m][y].chess != "_") {
                                weight -= 1;
                                if (j-m!=0&&TTT[j - m - 1][y].chess == "_") {
                                    disperse--;
                                }
                                if (TTT[j - m + 1][y].chess == "_") {
                                    disperse--;
                                }
                            }
                            //将该窗口的独立weight值加入总体weight值
                            totalWeight += WeightReCal(weight,obsStart,obsEnd,disperse);
                        } else {
                            weightCount++;
                            if (weightCount == m) totalWeight += WeightReCal(weight,obsStart,obsEnd,disperse);
                        }
                    }
                    j++;
                }
                ai.TTT[i][y].colWeight[type] = totalWeight;
            }
            i++;
        }
        //rightDiagonal calculation
        i = ai.TTT[x][y].rDStart[type];
        end = ai.TTT[x][y].rDEnd[type];
        int offset = x-y;
        int newI;
        if(offset>0) newI = i+offset;
        else newI = i;
        while(i<=end) {
            if(newI>x-m&&newI<x+m) {
                int j = ai.TTT[newI][newI - offset].rDStart[type];
                int jEnd = ai.TTT[newI][newI - offset].rDEnd[type];
                int weight = 0;
                int obsStart = 0;
                int obsEnd = 0;
                int disperse =0;
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
                    }
                    j++;
                    newJ++;
                }
                ai.TTT[newI][newI - offset].rDWeight[type] = totalWeight;
            }
            i++;
            newI++;
        }
        //leftDiagonal calculation
        i = ai.TTT[x][y].lDStart[type];
        end = ai.TTT[x][y].lDEnd[type];
        int sumset = x+y;
        if(sumset > n-1) newI = n-1-i;
        else newI = sumset-i;
        while(i<=end) {
            if(newI>x-m&&newI<x+m) {
                int j = ai.TTT[newI][sumset - newI].lDStart[type];
                int jEnd = ai.TTT[newI][sumset - newI].lDEnd[type];
                int weight = 0;
                int obsStart = 0;
                int obsEnd = 0;
                int disperse = 0;
                int totalWeight = 0;
                int weightCount = 0;
                int newJ;
                if (sumset > n - 1) newJ = j + sumset - n + 1;
                else newJ = j;
                while (j <= jEnd) {
                    if (j > i - m && j < i + m) {
                        //计算独立weight值与离散程度
                        if (TTT[newJ][sumset - newJ].chess != "_") {
                            weight += 1;
                            if (newJ != 0 && sumset - newJ != n-1 && TTT[newJ - 1][sumset-newJ + 1].chess == "_") {
                                disperse++;
                            }
                            if (newJ != n - 1 && sumset-newJ != 0 && TTT[newJ + 1][sumset-newJ -1].chess == "_") {
                                disperse++;
                            }
                        }
                        //计算开头或结尾与敌方相接的情况
                        if (j == ai.TTT[newI][sumset - newI].lDStart[type] && TTT[newJ][sumset-newJ].chess != "_") {
                            obsStart = 1;
                        }
                        if (j == ai.TTT[newI][sumset - newI].lDStart[type] + m) {
                            obsStart = 0;
                        }
                        if (j >= jEnd - m && TTT[newJ + jEnd - j][sumset-newJ - (jEnd - j)].chess != "_") {
                            obsEnd = 1;
                        }
                        //计算总体weight值
                        if (weightCount == m) {
                            //达到窗口长度M时剪掉尾巴的disperse和weight
                            if (TTT[newJ - m][sumset-newJ+m].chess != "_") {
                                weight -= 1;
                                if (newJ - m != 0 && sumset-newJ+m!= n-1 && TTT[newJ - m - 1][sumset-newJ+m+1].chess == "_") {
                                    disperse--;
                                }
                                if (TTT[newJ - m + 1][sumset-newJ+m-1].chess == "_") {
                                    disperse--;
                                }
                            }
                            //将该窗口的独立weight值加入总体weight值
                            totalWeight += WeightReCal(weight, obsStart, obsEnd, disperse);
                        } else {
                            weightCount++;
                            if (weightCount == m) totalWeight += WeightReCal(weight, obsStart, obsEnd, disperse);
                        }
                    }
                    j++;
                    newJ++;
                }
                ai.TTT[newI][sumset - newI].lDWeight[type] = totalWeight;
            }
            i++;
            newI--;
        }

    }

    //sum of SE
    //注意这里同时加入了当点为子时的权重增加
    int CalculateStartEnd(int x,int y,int type,AI ai){
        int sumOfSE = 0;
        if(ai.TTT[x][y].totalPotential == 0) sumOfSE += pointP;
        sumOfSE += ai.TTT[x][y].rowEnd[type]-ai.TTT[x][y].rowStart[type];
        sumOfSE += ai.TTT[x][y].colEnd[type]-ai.TTT[x][y].colStart[type];
        sumOfSE += ai.TTT[x][y].rDEnd[type]-ai.TTT[x][y].rDStart[type];
        sumOfSE += ai.TTT[x][y].lDEnd[type]-ai.TTT[x][y].lDStart[type];
        return sumOfSE;
    }

    //calculate new potential of points in line,row,rd,ld of (x,y) after set(x,y)
    void CalculatePotential(int x, int y,int type,AI ai) {

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
            if (sumset > n-1) ai.TTT[x][y].potential[type] += CalculateStartEnd(n-1-i, i + sumset - n+1, type, ai);
            else ai.TTT[x][y].potential[type] += CalculateStartEnd(sumset-i, i, type, ai);
            i++;
        }
    }

    //给(x,y)每行每列每对角线上的点的直接潜力值进行重算，并对他们的总潜力值进行重算。
    void AdjustPotential(int x, int y,AI ai){
        for(int type = 0;type<=1;type++) {
            int offset = x - y;
            int sumset = x + y;
            //row
            int i = ai.TTT[x][y].rowStart[type];
            int end = ai.TTT[x][y].rowEnd[type];
            while (i <= end) {
                CalculatePotential(x,i,type,ai);
                ai.TTT[x][i].totalPotential();
                i++;
            }
            //col
            i = ai.TTT[x][y].colStart[type];
            end = ai.TTT[x][y].colEnd[type];
            while (i <= end) {
                CalculatePotential(i,y,type,ai);
                ai.TTT[i][y].totalPotential();
                i++;
            }
            //rD
            i = ai.TTT[x][y].rDStart[type];
            end = ai.TTT[x][y].rDEnd[type];
            while (i <= end) {
                if (offset > 0){
                    CalculatePotential(i+offset,i,type,ai);
                    ai.TTT[i+offset][i].totalPotential();
                }
                else {
                    CalculatePotential(i, i - offset, type, ai);
                    ai.TTT[i][i - offset].totalPotential();
                }
                i++;
            }
            //lD
            i = ai.TTT[x][y].lDStart[type];
            end = ai.TTT[x][y].lDEnd[type];
            while(i<=end){
                if (sumset>n-1){
                    CalculatePotential(n-1-i,i + sumset - n+1,type,ai);
                    ai.TTT[n-1-i][i + sumset - n+1].totalPotential();
                }
                else{
                    CalculatePotential(sumset-i,i,type,ai);
                    ai.TTT[sumset-i][i].totalPotential();
                }
                i++;
            }
        }
    }

    //找出总潜力值最大的点落子
    void FindOptimalXY(AI ai){
        double totalPotential = -1;
        for(int x = 0; x< n;x++) {
            for (int y = 0; y < n; y++) {
                if(ai.TTT[x][y].totalPotential>totalPotential) {
                    totalPotential = ai.TTT[x][y].totalPotential;
                    optX = x;
                    optY = y;
                }
            }
        }
    }

    int WeightReCal(int weight,int obsStart,int obsEnd,int disperse){
        if(obsEnd==1&&obsStart==1) return 1;
        else if(weight <= m-4) return weight*3+disperse - obsStart-obsEnd;
        else if(weight == m-3) return 20+disperse*2 - (obsStart+obsEnd)*10;
        else if(weight == m-2){
            if(obsStart==1||obsEnd==1){
                return 40+disperse*10;
            }
            else return 10000 + disperse*2000;
        }
        else if(weight == m-1){
            if(obsStart==1||obsEnd==1){
                return 40 + disperse*10;
            }
            else return 100000+disperse*20000;
        }
        return weight;
    }
    //!!想加入线上同色棋子数对权重的影响，做完之后再加
    //之前关于每行每列最大连子的计算也要加入，最大连子应该给予较高权重

    /*void CalculateBias(int x,int y,AI ai){
        ai.TTT[x][y].bias = Math.pow(9.5-x,2)+Math.pow(9.5-y,2);
    }
                            if(y==11&& weight!= 0){
                            System.out.println("Jend: "+jEnd);
                            System.out.println("!!!"+weight);
                            System.out.println("total: "+totalWeight);
                            System.out.println("j: "+j);
                            System.out.println("i: "+i);
                            System.out.println("type: "+type);
                            System.out.println();
                        }

                System.out.println("x: "+(newI-offset)+" y: " +newI);
                System.out.println("rDStart: "+j);
                System.out.println("rDEnd: "+ jEnd);
                System.out.println("type: "+type);
                System.out.println();

    //这里只对X Y 进行了weight重置 应该是0,15全重置

    //loop according to i

        int weight = 0;
        int totalWeight = 0;
        int weightCount = 0;
            if(i>y-8&&i<y+8){
                weight += (TTT[x][i].chess == "_")?0:1;
                if(weightCount == 8){
                    weight -= (TTT[x][i-8].chess == "_")?0:1;
                    totalWeight += weight;
                }
                else{
                    weightCount++;
                    if(weightCount == 8) totalWeight+=weight;
                }
            }*/
    //留到明天来写 weight计算这里怎么简化还是比较烦 重点是，每次读到CRRL中（7,7）范围内的一个point都要计算一次新的对应CRRL的weight同时做到代码简化
    //思路memo：weight和SE重置完成后，可以直接生成新的P1，在新的CRRL.P1中，对对应的CRRL.Point先进行减P2（对应CRRL方向.P2）（之前预设的potential下一级互相影响值Potential2）再进行一次加通过新CRRL.P1计算出的P2值。热更新？
}
