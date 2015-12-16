package gp.twitter.extract.features;


import gp.twitter.extract.labeler.sequence.Sentence;
import gp.twitter.extract.labeler.sparse.array.BooleanSparseArray;
import gp.twitter.extract.labeler.sparse.array.DoubleSparseArray;
import gp.twitter.extract.labeler.tags.Tag;
import gp.twitter.extract.labeler.sparse.array.SparseArray;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public  abstract class FeatureExtractor {
	private int size_of_features, start_id;
	protected Map<String,Integer> history = new HashMap<>();
	public int getSizeofFeature() {
		return size_of_features;
	}

	
	public void setSizeofFeature(int size_of_features){
		 this.size_of_features=size_of_features;
	}
	 
	public  abstract SparseArray getFeatures(Tag c_tag , Tag p_tag , Sentence sentence, int position,
			boolean is_training ) ;
		
	
	public void setBeginId(int next_start_feature_id){
        start_id =	next_start_feature_id;

	}
	
	public int getBeginId(){
		
		return start_id ;
	} 
	
	public abstract boolean isCurrentTagDependent();

    protected int addFeatureToHistory(String name){
        Integer id = history.get(name);
        if(id==null){
            id = start_id++;
            history.put(name,id);

        }
        return id;


    }
	protected int getFeatureId(String name){

        Integer id = history.get(name);
        if(id==null){
            return  -1;

        }
        return id;

    }
    protected void attemptAddingFeature(String feature, BooleanSparseArray featuresVector,boolean isTraining){
        int feature_id = -1;
        if(isTraining){
            feature_id = addFeatureToHistory(feature);
        }
        else
        {
            feature_id = getFeatureId(feature);
        }

        if(feature_id != -1) {
            featuresVector.add(feature_id);
        }
    }
    protected void attemptAddingFeature(String feature,double value, DoubleSparseArray featuresVector, boolean isTraining){
        int feature_id = -1;
        if(isTraining){
            feature_id = addFeatureToHistory(feature);
        }
        else
        {
            feature_id = getFeatureId(feature);
        }

        if(feature_id != -1) {
            featuresVector.add(feature_id,value);
        }
    }
}
