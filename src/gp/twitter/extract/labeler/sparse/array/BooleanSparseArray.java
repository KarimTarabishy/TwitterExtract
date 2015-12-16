package gp.twitter.extract.labeler.sparse.array;

import gp.twitter.extract.labeler.sparse.array.SparseArray;

import java.util.ArrayList;

/**
 * Created by stc on 12/16/2015.
 */

public class BooleanSparseArray extends SparseArray {

    ArrayList<Integer> data= new ArrayList<>();

    public  double _dotProduct(double[] v){
        double result=0;
        for(int i=0;i<data.size();i++)
        {
            result+=v[data.get(i)];
        }
        return result;
    }
    public void add(int index){

        data.add(index);

    }
}
