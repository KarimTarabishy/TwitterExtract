package gp.twitter.extract.features;


import gp.twitter.extract.labeler.sequence.Sentence;
import gp.twitter.extract.labeler.tags.Tag;
import gp.twitter.extract.util.SparseArray;

public  abstract class FeatureExtractor {
	private int size_of_features, start_id;
	
	public int getSizeofFeature() {
		return size_of_features;
	}
	
	public void setSizeofFeature(int size_of_features){
		 
		 this.size_of_features=size_of_features;
		 
		 
	 }
	 
	public  abstract SparseArray getfeatures(Tag c_tag , Tag ptag , Sentence sentence, int position) ;
		
	
	public void setBeginId(int next_start_feature_id){
		 start_id =	next_start_feature_id;
	}
	
	public int getBeginId(){
		
		return start_id ;
	} 
	
	public abstract boolean isCurrentTagDependent();
	

}
