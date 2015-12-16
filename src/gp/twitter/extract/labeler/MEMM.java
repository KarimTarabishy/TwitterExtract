package gp.twitter.extract.labeler;

import gp.twitter.extract.features.Features;
import gp.twitter.extract.labeler.sequence.Observation;
import gp.twitter.extract.labeler.sequence.Sentence;
import gp.twitter.extract.labeler.tags.Tag;
import gp.twitter.extract.labeler.tags.Tags;
import gp.twitter.extract.util.SparseArray;

import java.io.IOException;
import java.util.ArrayList;


public class MEMM {
	private Features features = new Features();
	private double [] feature_weights;
	private String model_data_file_name;
	private Tags tags;
	
	/**
	 * Construct a MEMM model from file, usually used when already trained memm model.
	 * @param model_data_file_name the file name containing the model data
     * @param tags Specific tags used
	 */
	public MEMM(String model_data_file_name, Tags tags) throws IOException {
		this.model_data_file_name = model_data_file_name;
		loadFromFile();
        this.tags = tags;
	}

    /**
     * Construct MEMM model. Usually used when training
     * @param tags Specific tags used
     */
    public MEMM(Tags tags)
    {
        this.tags = tags;
    }
	
	/**
	 * Loads the model parameters from file
	 */
	public void loadFromFile() throws IOException
	{
		if(model_data_file_name != null)
		{
			/*
				TODO: load feature weights from file
			 */
		}
	}

    public Tags getTags()
    {
        return tags;
    }

	/**
	 * Compute all possible tag transition features from the given position.
	 * @param sentence the sentence
	 * @param position the position
	 * @return array list of the features
	 */
    public ArrayList<SparseArray>computeAllPossibleTransitionFeatures(Sentence sentence , int position){
    	
    	Observation observation = sentence.getObservation(position);
		//get previous tag of this observation, if it is the starting observation make the tag the 
		//default start tag
		Tag p_tag = (position == 0) ? tags.getStartTag() : sentence.getObservationTag(position-1);

		// calculate the features that does not depend on the current tag
		SparseArray independent_features =features.getIndependentFeatures(null, p_tag, sentence, position);

        //start calculating features corresponding to all the possible transitions
    	ArrayList<SparseArray> all_transition_feature = new ArrayList<>();
		for(int i =0; i < tags.getSize(); i++)
		{  
			//calculate the feature that depend on the current tag
			SparseArray feature=features.getDependentFeatures(tags.getTagById(i), p_tag, sentence, position);
			//concatenate it with the independent feature
			feature.concat(independent_features);
			//then save it
			all_transition_feature.add(feature);
		}
    	return all_transition_feature;
    }


    /**
     * Calculates and save all the features associated with the current position in sentence, all of them
     * means that we calculate a feature for each possible tag in this position
     * @param sentence the senetence
     * @param position the position
     */
	public void saveFeature(Sentence sentence, int position)
	{
        //get current observation from sentence
		Observation observation = sentence.getObservation(position);
		//get current tag
		Tag c_tag = sentence.getObservation(position).getTag();
		
		//start saving the features of all possible tags in current position
		ArrayList <SparseArray> all_transition_feature = computeAllPossibleTransitionFeatures(sentence, position);
		observation.setAll_transition_feature(all_transition_feature);
		
		//save the feature of the current tag in the observation too
		observation.setCurrentTransitionFeature(all_transition_feature.get(c_tag.getId()));
		
	}

    /**
     * Calculate the transition probability for the given tag in the current observation, used in
     * training which assume that the sentence come with all its features and tags saved in it.
     * @param v the feature weights
     * @param sentence the sentence
     * @param position the position
     * @return probability of this transition
     */
	public  double transitionProbability(double[] v, Sentence sentence, int position){
		
		//get the current observation
		Observation obsrevation =sentence.getObservation(position);
		//get features corresponding to all possible transitions in current position
		ArrayList <SparseArray> all_transition_feature =obsrevation.getAll_transition_feature();
		
		//calculate numerator
		double numerator = Math.exp(obsrevation.getCurrentTransitionFeature().dotProduct(v));
		
		//calculate denominator
		double denominator = 0;
		for(int i =0; i <all_transition_feature.size(); i++)
		{  
			denominator += Math.exp(all_transition_feature.get(i).dotProduct(v));
		}
		return numerator/denominator;
	}

    /**
     * Calculates the likelihood function with the regularization. Used only in training.
     * @param sentence all training sentences
     * @param v the features weights
     * @param lambda_l1 the L1 regularization weights
     * @param lambda_l2 the L2 regularization weights
     * @return a score indicating how the given parameter v of the model fits the training data
     */
	public  double Likelihood(ArrayList<Sentence>sentence,double []v,double lambda_l1,double lambda_l2)
	{
        double result =0;
        double l1_sum=0;
        double l2_sum=0;

        //loop over each sentence
        for(int i = 0; i < sentence.size(); i++)
        {
            //get current sentence
            Sentence currentSentence = sentence.get(i);
            //loop over each word in the sentence
            for(int k = 1; k < currentSentence.getSize(); k++)
            {
                //get the log probability of this position in the sentence
                result += Math.log(transitionProbability(v, currentSentence, k));
            }
        }
        // take the negative of that as we will minimize
        result = -result;

        //calculate the L1 & L2 norm of the v parameter
        for( int i=0;i<v.length;i++)
        {
            l1_sum += Math.abs(v[i]);
            l2_sum += v[i]*v[i];
        }

        l1_sum *= lambda_l1;
        l2_sum *= lambda_l2 / 2;
        result += l1_sum + l2_sum;

        return result;
    }
	 
    public double derivativeLikelihood()
    {

        return 0;
    }
	 
    public Sentence greedy_decode(Sentence sentence, double [] v)
    {
        // move over each word in tweet
        for(int i = 0; i < sentence.getSize(); i++)
        {
            //get features for all possible tags in current position
            ArrayList<SparseArray> all_possible_transition_features = computeAllPossibleTransitionFeatures(sentence, i);

            //get the tag with the greatest transition score
            double max_transition_score = Double.MIN_VALUE;
            Tag max_tag = null;
            //move over all tags
            for(int j = 1; j < all_possible_transition_features.size(); j++)
            {
                double score = Math.exp(all_possible_transition_features.get(j).dotProduct(v));
                if(score > max_transition_score)
                {
                    max_transition_score = score;
                    max_tag = tags.getTagById(j);
                }
            }
            //set the maximum tag of the current word
            sentence.getObservation(i).setTag(max_tag);
        }
        return sentence;
    }
	
}
