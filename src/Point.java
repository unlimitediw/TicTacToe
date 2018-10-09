import java.lang.Math;

public class Point {
    //To all of the following code
    //0 means you "X" 0
    //1 means opponent "O"

    //Weight value
    int[] rowWeight = new int[2];
    int[] colWeight = new int[2];
    int[] rDWeight = new int[2];
    int[] lDWeight = new int[2];
    int x;
    int y;

    //Start and End
    //E.G. To "O"
    //_ X _ _ _ O _ _ O _  _  _  X  _  _
    //1 2 3 4 5 6 7 8 9 11 12 13 14 15 16
    //The row start is 3, row end is 13
    int[] rowStart = new int[2];
    int[] rowEnd = new int[2];
    int[] colStart = new int[2];
    int[] colEnd = new int[2];
    int[] rDStart = new int[2];
    int[] rDEnd = new int[2];
    int[] lDStart = new int[2];
    int[] lDEnd = new int[2];

    //Five key parameter
    //With our experience
    //weightP = 5-20, potentialP = 0.02-0.2, biasP = 1 is workable
    double weightP;
    double potentialP;
    double biasP;
    //Use to judge attack of difference (ourWeight/oppWeight) positive correlated to attack willing
    double ourWeight;
    double oppWeight;

    //Potential value(as we said before, the "space" of empty)
    int[] potential = new int[2];

    //Bias is positive correlated to the distance from center
    double bias;

    //The final value of point judgement
    double totalPotential;

    //"X"'s value
    double aiWeight;

    //"O"'s value
    double myWeight;

    //Chess type
    String chess;

    Point(int x,int y,double w,double p, double b,int n,double ourWeight,double oppWeight){
        totalPotential = 0;
        this.x =x ;
        this.y = y;
        int rDLength = n-1-Math.abs(x-y);
        int lDLenght;
        if(x+y<=(n-1)) lDLenght = x+y;
        else lDLenght = 2*n-2 - x -y;
        chess = "_";
        rowWeight[0] = 0;
        colWeight[0] = 0;
        rDWeight[0] = 0;
        lDWeight[0] = 0;
        rowWeight[1] = 0;
        colWeight[1] = 0;
        rDWeight[1] = 0;
        lDWeight[1] = 0;
        rowStart[0] = 0;
        rowEnd[0] = n-1;
        colStart[0] = 0;
        colEnd[0] = n-1;
        rowStart[1] = 0;
        rowEnd[1] = n-1;
        colStart[1] = 0;
        colEnd[1] = n-1;
        rDStart[0] = 0;
        rDEnd[0] = rDLength;
        lDStart[0] = 0;
        lDEnd[0] = lDLenght;
        rDStart[1] = 0;
        rDEnd[1] = rDLength;
        lDStart[1] = 0;
        lDEnd[1] = lDLenght;
        potential[0] = 0;
        potential[1] = 0;
        weightP = w;
        potentialP = p;
        biasP = b;

        this.ourWeight = ourWeight;
        this.oppWeight = oppWeight;
    }

    //Calculate the point judgement value: totalPotential
    double totalPotential(int type){
        if(chess=="X") totalPotential = -999;
        else if(chess =="O") totalPotential =-999;
        else{
            if(type == 0) {
                totalPotential = ourWeight * AIWeight() + oppWeight * MyWeight() - biasP * bias;
            }
            else {
                totalPotential = oppWeight * AIWeight() + ourWeight * MyWeight() - biasP * bias;
            }
        }
        return totalPotential;
    }

    //Calculate the point judgement value in the part of "O"
    double AIWeight(){
        aiWeight = weightP*(rowWeight[0]+colWeight[0]+rDWeight[0]+lDWeight[0])+potentialP*potential[0];
        return aiWeight;
    }
    //Calculate the point judgement value in the part of "X"
    double MyWeight(){
        myWeight = weightP*(rowWeight[1]+colWeight[1]+rDWeight[1]+lDWeight[1])+potentialP*potential[1];
        return myWeight;
    }
}
