package gp.twitter.extract.util;

import java.util.ArrayList;

public class SparseArray {
	private ArrayList<Integer> data = new ArrayList<Integer>() ; 
	private SparseArray next=null;
	public  double dotProduct(double[] v){
    	  double result=0;
    	  SparseArray current=this;
    	  do 
    	  { 
    		  for(int i=0;i<data.size();i++)
    		  { 
    			  result+=v[data.get(i)];
    		  }
    		  
    		  current=current.next;
    	  }while(current!=null);
    	  return result;
       }
      public  void concat(SparseArray other)
      {   
    	  SparseArray current = this;
    	  while(current.next != null)
    	  {
    	  	current = current.next;
    	  }
    	  current.next = other;
      }
      
      
      
      public void add(int value ){
    	  data.add(value);
      }

}
