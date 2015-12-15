package gp.twitter.extract.sequence.labeler;

import java.util.ArrayList;

import gp.twitter.extract.features.*;
import gp.twitter.extract.util.SparseArray;


public class MEMM {
	private Features features = new Features();
	private double [] feature_weights;
	private String model_data_file_name;
	private static MEMM trainingMEMM;
	private boolean training = false;
	
	
	/**
	 * 
	 * @param model_data_file_name the file name containing the model data
	 */
	public MEMM(String model_data_file_name)
	{
		this.model_data_file_name = model_data_file_name;
	}
	
	/**
	 * Loads the model parameters from file
	 */
	public void loadFromFile()
	{
		
	}
	
	/**
	 * Trains the MEMM on the given training data set(s) 
	 * @param files file(s) containing the data set(s)
	 */
   static public MEMM getTrainingInstance(){
	
    	if(trainingMEMM==null)
    	{	
    		trainingMEMM = new MEMM(null);
    		trainingMEMM.training = true;
		}
		
       return trainingMEMM;
	}
	/**
	 *  
	 * @param sentence
	 * @param position
	 * @return
	 */
    public ArrayList<SparseArray>computeAllPossibleTransitionFeatures(Sentence sentence ,int position){
    	
    	Observation observation = sentence.getObsernation(position);
		//get previous tag of this observation, if it is the starting observation make the tag the 
		//default start tag
		Tag p_tag = (position==0) ? Tag.getStartTag() : sentence.getObsernation(position-1).getTag();
		// calculate the features that does not depend on the current tag
		SparseArray independent_features =features.getfeatures_independent(null, p_tag, sentence, position);
    	ArrayList<SparseArray> all_transition_feature = new ArrayList<>();
		for(int i =0; i < Tag.getTagLength(); i++)
		{  
			//calculate the feature that depend on the current tag
			SparseArray feature=features.getfeatures_dependent(Tag.getTag(i), p_tag,sentence, position);
			//concatenate it with the independent feature
			feature.concat(independent_features);
			//then save it
			all_transition_feature.add(feature);
		}
    	
    	return all_transition_feature;
    }
	
	
	
	public  Sentence  savefeature(Sentence sentence,int position)
	
	{  //get current observation from sentence
		Observation observation = sentence.getObsernation(position);
		//get previous tag of this observation, if it is the starting observation make the tag the 
		//default start tag
		Tag c_tag = sentence.getObsernation(position).getTag();
		
		//start saving the features of all possible tags in current position
		ArrayList <SparseArray> all_transition_feature=computeAllPossibleTransitionFeatures(sentence, position);
		observation.setAll_transition_feature(all_transition_feature);
		
		//save the feature of the current tag in the observation too
		observation.setCurrentTransitionFeature(all_transition_feature.get(c_tag.id));
		
		return sentence;
	}
	
	public  double transition_probability(double[] v, Sentence sentence ,int position){
		
		//make sure we only use this in training
		if(!training)
		{
			throw new RuntimeException("Can not get transition_probability while not training");
		}
		
		//get the current observation
		Observation obsrevation =sentence.getObsernation(position);
		//get features corresponding to all possible transitions in current position
		ArrayList <SparseArray> all_transition_feature =obsrevation.getAll_transition_feature();
		
		//calculate numerator
		double numirator = Math.exp(obsrevation.getCurrentTransitionFeature().dotProduct(v));
		
		//calculate denominator
		double denominator = 0;
		for(int i =0; i <all_transition_feature.size(); i++)
		{  
			denominator += Math.exp(all_transition_feature.get(i).dotProduct(v));
		}
		return numirator/denominator;
	}
	public  double Likelihood(ArrayList<Sentence>sentence,double []v,double landa_l1,double landa_l2)
	{
		 if(!training)
		 {
			 throw new RuntimeException("Can not get Likelihood while not training");
		 }
		 
		 double result =0;
		 double l1_sum=0;
		 double l2_sum=0;
		 for(int i=0;i<sentence.size();i++)
		 {
			 Sentence currentSentence = sentence.get(i);
			 for(int k=1;k< currentSentence.getSize();k++)
			 {
				 result+=Math.log(transition_probability( v,currentSentence, k));
			 }
		 }
		 result = -result;
		 for( int i=0;i<v.length;i++)
		 {
			 l1_sum += Math.abs(v[i]);
			 l2_sum += v[i]*v[i];
		 }
		 l1_sum*=landa_l1;
		 l2_sum*=((landa_l2)/2);
		 result+=l1_sum+l2_sum;
		 return result;
	 }
	 
	 public double derivativeLikelihood()
	 {
		 
		 return 0;
	 }
	 
	 public Sentence decode_greedy(Sentence sentence, double [] v)
	 {
		 if(training)
		 {
			 throw new RuntimeException("Can not decode while training");
		 }
		 
		
		// move over each word in tweet
		for(int i = 0; i < sentence.getSize(); i++)
		{   ArrayList<SparseArray> all_possible_transition_features=computeAllPossibleTransitionFeatures(sentence, i);
		
			//check all possible tags in this position and get the maximum one
			double max_transition_score = Double.MIN_VALUE;
			Tag max_tag = null;
			for(int j = 1; j < all_possible_transition_features.size(); j++)
			{  
				double score = Math.exp(all_possible_transition_features.get(j).dotProduct(v));
				if(score > max_transition_score)
				{
					max_transition_score = score;
					max_tag = Tag.getTag(j);
				}
			}
			//set the maximum tag of the current word
			sentence.getObsernation(i).setTag(max_tag); 
		}
		return sentence;
	 }
	
}
