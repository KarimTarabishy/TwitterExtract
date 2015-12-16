package gp.twitter.extract.labeler.sparse.array;

public abstract class SparseArray {
    protected  SparseArray next=null;
    protected abstract double _dotProduct(double[] v);

    public  void concat(SparseArray other)
    {
        SparseArray current = this;
        while(current.next != null)
        {
            current = current.next;
        }
        current.next = other;
    }

    public double dotProduct(double[] v){
        double result=0;
        SparseArray current=this;
        do
        {
            result += current._dotProduct(v);

            current=current.next;
        }while(current!=null);
        return result;
    }

}
