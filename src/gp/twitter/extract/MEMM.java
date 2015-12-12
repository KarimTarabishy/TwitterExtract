package gp.twitter.extract;

import java.beans.FeatureDescriptor;
import java.io.*;
import java.util.*;


public class MEMM {
	public static Tag[] TAGS;
	private Features features = new Features();
	private double [] feature_weights;
	private String model_data_file_name;
	
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
	public void train(String [] files)
	{
		
	}
	
	
	public  double transition_probability(double[] v, Tag c_tag,String[] words ,Tag p_tag, int position){
		SparseArray independent_features =features.getfeatures_independent(c_tag, p_tag, words, position);
		SparseArray dependent_features =features.getfeatures_dependent(c_tag, p_tag, words, position);
		
		double denominator = 0;
		SparseArray numirator_feature =new SparseArray();
		numirator_feature.concat(independent_features);
		numirator_feature.concat(dependent_features);

		double numirator = Math.exp(numirator_feature.dotProduct(v));
		for(int i =0; i < TAGS.length; i++)
		{  
			SparseArray feature=features.getfeatures_dependent(TAGS[i], p_tag, words, position);
			feature.concat(independent_features);
			denominator += Math.exp(feature.dotProduct(v));
		}
			
			
		return numirator/denominator;
			
	}

	 public  double Likelihood(String words [][] ,Tag tags [][],double []v,double landa_l1,double landa_l2)
	 {
		 double result =0;
		 double l1_sum=0;
		 double l2_sum=0;
		 for(int i=0;i<words.length;i++)
		 {
			 for(int k=1;k<words[i].length;k++)
			 {
				 result+=Math.log(transition_probability( v, tags[i][k], words[i] ,tags[i][k-1], k));
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
	 
	 public Tag[] decode_greedy(String[] words, double [] v)
	 {
		Tag tags[] = new Tag[words.length + 1];
		tags[0] = TAGS[0];
		// move over each word in tweet
		for(int i = 1; i < words.length; i++)
		{
			//check all possible tags in this position and get the maximum one
			double max_transition_probability = Double.MIN_VALUE;
			Tag max_tag = null;
			for(int j = 1; j < tags.length; j++)
			{
				double probability = transition_probability(v, tags[j], words, tags[j-1], i);
				if(probability > max_transition_probability)
				{
					max_transition_probability = probability;
					max_tag = TAGS[j];
				}
			}
			//set the maximum tag of the current word
			tags[i] = max_tag;
		}
		return tags;
	 }
	
}
