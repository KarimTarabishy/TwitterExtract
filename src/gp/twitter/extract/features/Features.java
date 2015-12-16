package gp.twitter.extract.features;

import gp.twitter.extract.labeler.sequence.Sentence;
import gp.twitter.extract.labeler.tags.Tag;
import gp.twitter.extract.util.SparseArray;

import java.util.ArrayList;

public class Features {
	ArrayList<FeatureExtractor> current_tag_independent_extractors;
	ArrayList<FeatureExtractor> current_tag_dependent_extractors;
	int next_start_feature_id=0;

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


	public SparseArray getFeatures(Tag c_tag , Tag ptag , Sentence sentence, int position)
    {
        SparseArray featureVector = getDependentFeatures( c_tag ,ptag , sentence, position);
        featureVector.concat(getIndependentFeatures(c_tag ,ptag , sentence, position));

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
                featureVector = extractors.get(i).getfeatures(c_tag, ptag, sentence, position);
            }
            else
            {
                featureVector.concat(extractors.get(i).getfeatures(c_tag, ptag, sentence, position));
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
	 
	 
	 
	 

