package gp.twitter.extract.sequence.labeler;

import java.util.ArrayList;

import javax.management.RuntimeErrorException;

import gp.twitter.extract.util.SparseArray;

public class Sentences {
	private ArrayList <Observation> observations;
	private boolean locked = false ;
	
	public void addObseration(Observation observation){
		if(locked==false)
		{ 
			observations.add(observation);
		}
		
		else{
			throw new RuntimeException("adding to locked sentences");
		} 
			
		
	}
 
	
	
}
