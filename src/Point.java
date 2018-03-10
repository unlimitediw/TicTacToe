import java.lang.Math;

public class Point {
    //0 means you "X" 0 is always us
    //1 means opponent "O"
    int[] rowWeight = new int[2];
    int[] colWeight = new int[2];
    int[] rDWeight = new int[2];
    int[] lDWeight = new int[2];
    int x;
    int y;

    int[] rowStart = new int[2];//row[rowStart]
    int[] rowEnd = new int[2];//row[rowEnd]
    int[] colStart = new int[2];
    int[] colEnd = new int[2];
    int[] rDStart = new int[2];
    int[] rDEnd = new int[2];
    int[] lDStart = new int[2];
    int[] lDEnd = new int[2];

    double weightP;
    double potentialP;
    double biasP;

    int[] potential = new int[2];
    double bias;
    double totalPotential;
    double aiWeight;
    double myWeight;
    String chess;

    double ourWeight;
    double oppWeight;
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
        //System.out.println("x: " + y + " y: " + x+ " rDEnd: "+rDEnd[0]);
    }

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

    double AIWeight(){
        aiWeight = weightP*(rowWeight[0]+colWeight[0]+rDWeight[0]+lDWeight[0])+potentialP*potential[0];
        return aiWeight;
    }
    double MyWeight(){
        myWeight = weightP*(rowWeight[1]+colWeight[1]+rDWeight[1]+lDWeight[1])+potentialP*potential[1];
        return myWeight;
    }
}
