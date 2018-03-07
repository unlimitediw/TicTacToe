public class MinMaxState {
    AI ai;
    int type;
    int index;
    double alpha;
    double beta;
    double evaluateValue;

    MinMaxState(AI ai,int type,int index,double alpha,double beta,double evaluateValue){
        this.ai = ai;
        this.type = type;
        this.index = index;
        this.alpha = alpha;
        this.beta = beta;
        this.evaluateValue = evaluateValue;
    }
}
