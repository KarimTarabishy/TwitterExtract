package gp.twitter.extract.labeler.sparse.array;

import java.util.ArrayList;

/**
 * Created by stc on 12/16/2015.
 */
public class DoubleSparseArray {
    ArrayList<FeaturePair> data= new ArrayList<>();

    public  double _dotProduct(double[] v){
        double result=0;
        for(int i=0;i<data.size();i++)
        {
            FeaturePair f =data.get(i);
            result+=v[f.index]*f.value;
        }
        return result;
    }
    public void add(int index,double value){

        data.add(new FeaturePair(index,value));

    }
    private static class FeaturePair  {
        int index;
        double value;

        public FeaturePair(int index, double value) {
            this.index = index;
            this.value = value;
        }
    }
}

