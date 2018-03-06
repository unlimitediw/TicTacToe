import java.lang.Object;

public class DisplayBoard {
    public static void displayBoard(Point[][] TTT) {
        for (Point x[] : TTT) {
            for (Point y : x) {
                //String str = String.format("%04d",(int)(y.totalPotential()));
                //System.out.printf(str + " ");
            }
            System.out.println();
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
