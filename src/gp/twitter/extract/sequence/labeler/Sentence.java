package gp.twitter.extract.sequence.labeler;

import java.util.ArrayList;

import javax.management.RuntimeErrorException;

import gp.twitter.extract.util.SparseArray;

public class Sentence {
	private ArrayList <Observation> observations;
	private boolean locked = false ;
	
	public Sentence() {
		
		
		
	}
	public void addObseration(Observation observation){
		if(locked==false)
		{ 
			observations.add(observation);
		}
		
		else{
			throw new RuntimeException("adding to locked sentences");
		} 
	}
   
	public void lock(){
		locked=true;
	}
	
	public Observation getObsernation(int index){
		
		return observations.get(index);
	}
	
	public int getSize(){
		
		return observations.size();
		
		
		
		
	}

}
