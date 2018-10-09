import java.lang.Object;

//It is not meaningful
//You should not be care about it
//But it is really helpful to us to test and draw the board
//If you really want to see the board, use the function DisplayBoard.displayBoard.
//It works well, I promise.
public class DisplayBoard {
    public static void displayBoard(Point[][] TTT,AI ai) {
        int t = 0;
        int k = 0;
        /*
        for(int i = 0;i< ai.optimalLists.size();i++){
            System.out.println(ai.optimalLists.get(i).pointPotential+" x: "+ ai.optimalLists.get(i).x + " y: " + ai.optimalLists.get(i).y);
        }
        */

        System.out.println();
        System.out.printf("   ");
        for(Point x[]:TTT){
            System.out.printf("%04d",t);
            System.out.printf(" ");
            t++;
        }
        System.out.println();
        for (Point x[] : TTT) {
            System.out.printf("%02d",k);
            System.out.printf(" ");
            for (Point y : x) {
                double a = y.totalPotential;
                String str = "";
                if(y.chess == "_") {
                    str = String.format("%04d", (int) (y.totalPotential));
                }
                else if(y.chess == "X"){
                    str = String.format("%04d", (int) (0));
                }
                else{
                    str = String.format("%04d", (int) (0));
                }
                System.out.printf(str + " ");
            }
            System.out.println();
            k++;
        }
        System.out.println();

        int i =0;
        int j = 0;
        System.out.printf("   ");
        for(Point x[]:TTT){
            System.out.printf("%02d",j);
            System.out.printf(" ");
            j++;
        }
        System.out.println();
        for (Point x[] : TTT) {
            System.out.printf("%02d",i);
            System.out.printf(" ");
            for (Point y : x) {
                System.out.printf(y.chess+"  ");
            }
            System.out.println();
            i++;
        }
        System.out.println();
        /*
        for (Point x[] : TTT) {
            for (Point y : x) {
                String str = String.format("%04d",(int)(y.MyWeight()));
                System.out.printf(str + " ");
            }
            System.out.println();
        }
        System.out.println();
        for (Point x[] : TTT) {
            for (Point y : x) {
                String str = String.format("%04d",(int)(y.colWeight[1]));
                System.out.printf(str + " ");
            }
            System.out.println();
        }
        System.out.println();
        */
    }
}
