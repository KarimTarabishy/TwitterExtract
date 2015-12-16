package gp.twitter.extract.features;

import com.sun.org.apache.xalan.internal.utils.FeatureManager;
import gp.twitter.extract.labeler.sequence.Sentence;
import gp.twitter.extract.labeler.tags.Tag;
import gp.twitter.extract.labeler.sparse.array.SparseArray;

import java.util.ArrayList;

public class Features {
	private ArrayList<FeatureExtractor> current_tag_independent_extractors;
    private int next_start_feature_id=0;
    private  ArrayList<FeatureExtractor> current_tag_dependent_extractors;
    private boolean training;
    private String featureFileName;
    public Features(String featureFileName){

        this.featureFileName=featureFileName;
        training=true;
    }
    public Features(){

        training=false;
    }
    public void finishTraining(){
        training=false;


    }


	public void addExtractor(FeatureExtractor fe)
	{
        fe.setBeginId(next_start_feature_id);
        next_start_feature_id += fe.getSizeofFeature();
        if(fe.isCurrentTagDependent())
        {
            current_tag_dependent_extractors.add(fe);
        }
        else
        {
            current_tag_independent_extractors.add(fe);
        }
	}


	public SparseArray getFeatures(Tag c_tag , Tag p_tag , Sentence sentence, int position)
    {
        SparseArray featureVector = getDependentFeatures( c_tag ,p_tag , sentence, position);
        featureVector.concat(getIndependentFeatures(c_tag ,p_tag , sentence, position));

        return featureVector;
    }

    private SparseArray extract(ArrayList<FeatureExtractor> extractors, Tag c_tag , Tag ptag,
        Sentence sentence, int position)
    {
        SparseArray featureVector = null;
        for(int i=0;i<extractors.size();i++)
        {
            if(i == 0)
            {
                featureVector = extractors.get(i).getFeatures(c_tag, ptag, sentence, position,training);
            }
            else
            {
                featureVector.concat(extractors.get(i).getFeatures(c_tag, ptag, sentence, position,training));
            }
        }

        return featureVector;
	}


	public SparseArray getDependentFeatures(Tag c_tag , Tag ptag , Sentence sentence, int position)
	{
	    return extract(current_tag_dependent_extractors, c_tag , ptag , sentence, position);
	}

	public SparseArray getIndependentFeatures(Tag c_tag , Tag ptag , Sentence sentence, int position)
	{
        return extract(current_tag_independent_extractors, c_tag , ptag , sentence, position);
	}

}
	 
	 
	 
	 

