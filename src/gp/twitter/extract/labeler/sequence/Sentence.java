package gp.twitter.extract.labeler.sequence;

import gp.twitter.extract.labeler.tags.Tag;

import java.util.ArrayList;

public class Sentence {
	private ArrayList <Observation> observations;
	private boolean locked = false ;

	public void addObservation(Observation observation){
		if(locked==false)
		{ 
			observations.add(observation);
		}
		else
		{
			throw new RuntimeException("adding to locked sentences");
		} 
	}
   
	public void lock(){
		locked=true;
	}
	
	public Observation getObservation(int index){
		return observations.get(index);
	}
	
	public int getSize(){
		return observations.size();
	}

    public Tag getObservationTag(int position)
    {
        return observations.get(position).getTag();
    }



}
