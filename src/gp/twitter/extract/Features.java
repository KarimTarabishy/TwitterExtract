package gp.twitter.extract;

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
	
	
	 public SparseArray  getfeatures(Tag c_tag ,Tag ptag ,String [] word, int position){
		 
		SparseArray featureVector =getfeatures_dependent( c_tag ,
				ptag , word, position);
		featureVector.concat(getfeatures_independent (c_tag ,
				ptag , word, position));
		 return featureVector;
	 }
	 
	 private SparseArray extract(ArrayList<FeatureExtractor> extractors,Tag c_tag ,Tag ptag,
			                                       String [] word, int position)
	 {
		SparseArray featureVector = null;
		for(int i=0;i<extractors.size();i++)
		{
			if(i == 0)
			{
				featureVector = extractors.get(i).getfeatures(c_tag, ptag, word, position);
			}
			else
			{
				featureVector.concat(extractors.get(i).getfeatures(c_tag, ptag, word, position));
			}
		}
			 
		 return featureVector;
	 }
		
	 
	public SparseArray getfeatures_dependent(Tag c_tag ,Tag ptag ,String [] word, int position)
	{
		return extract(current_tag_dependent_extractors, c_tag ,
				ptag , word, position);
		 
		 
		 
	 }
	
	public SparseArray getfeatures_independent(Tag c_tag ,Tag ptag ,String [] word, int position)
	{
		return extract(current_tag_independent_extractors, c_tag ,
				ptag , word, position);
		 
	 }
   		 
		 
		 
}
	 
	 
	 
	 

