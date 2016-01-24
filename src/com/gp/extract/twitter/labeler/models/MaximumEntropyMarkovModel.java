package com.gp.extract.twitter.labeler.models;

import com.gp.extract.twitter.Configuration;
import com.gp.extract.twitter.labeler.features.Feature;
import com.gp.extract.twitter.labeler.features.FeatureArray;
import com.gp.extract.twitter.labeler.features.FeatureExtractor;
import com.gp.extract.twitter.labeler.sequence.Sentence;
import com.gp.extract.twitter.labeler.sequence.Tags;
import com.gp.extract.twitter.util.ArrayUtil;
import com.gp.extract.twitter.util.IOUtil;
import edu.stanford.nlp.math.ArrayMath;

import java.util.ArrayList;

public class MaximumEntropyMarkovModel extends SequenceModel implements IOUtil.Logger, IOUtil.Loadable{

    private int transitionWeightsOffset, transitionWeightsColLength, observationalWeightsOffset,
            observationalWeightsColLength;

    private static final String LOGGER_ID = MaximumEntropyMarkovModel.class.getName();


    @Override
    public String getLoggerId() {
        return "Maximum Entropy Model";
    }

    public MaximumEntropyMarkovModel(Configuration.Task task, Tags tags,
                                     ArrayList<FeatureExtractor> extractors, String saveDirectory) {
       super(task, tags, extractors, saveDirectory);
    }


    @Override
    public String getFeatureName(int index)
    {
        //check if label index
        if(index < transitionWeightsOffset)
        {
            return tags.getTagSymbolById(index);
        }

        //check if transition index
        if(index < observationalWeightsOffset)
        {
            index -= transitionWeightsOffset;
            int row = index / transitionWeightsColLength;
            int col = index - (row*transitionWeightsColLength);
            return tags.getTagSymbolById(row) + " < " + tags.getTagSymbolById(col);
        }

        index -= observationalWeightsOffset;
        int row = index / observationalWeightsColLength;
        int col = index - (row*observationalWeightsColLength);

        return tags.getTagSymbolById(col) + " < " + features.getFeatureName(row);

    }

    @Override
    public void setupWeights() {

        if(weights != null)
        {
            IOUtil.showError(this, "Weights are already setup.");
            return;
        }

        //tags + tags*tags+1 + tags*obs_feat
        //we have a weight for each tag excluding the start tag
        int tags_size = tags.getSize();
        //weight for each possible tag transition
        int transitional_size = (tags.getSize()) * (tags.getSize()+1);
        //weights for having the observation features with each tag excluding the start tag
        int observational_size= features.getDimensions() * (tags.getSize());

        weights = new double[tags_size + transitional_size+ observational_size];

        //set offsets
        transitionWeightsOffset = tags_size;
        observationalWeightsOffset = tags_size + transitional_size;

        //set row sizes
        transitionWeightsColLength = (tags.getSize()+1);
        observationalWeightsColLength = (tags.getSize());
    }

    private int getTagWeightIndex(int tag_index)
    {
        return tag_index;
    }
    private int getTransitionTagWeightIndex(int tag_current_index, int tag_previous_index)
    {
        return transitionWeightsOffset + ((tag_current_index*(transitionWeightsColLength))
                + tag_previous_index);
    }
    private int getObservationalTagWeightIndex(int tag_index, int feature_index)
    {
        return observationalWeightsOffset + ((feature_index*(observationalWeightsColLength))
                + tag_index);
    }

    private double getTagWeight(int tag_index)
    {
        return weights[getTagWeightIndex(tag_index)];
    }


    private double getTransitionTagWeight(int tag_current_index, int tag_previous_index)
    {
        return weights[getTransitionTagWeightIndex(tag_current_index, tag_previous_index)];
    }

    private double getObservationalTagWeight(int tag_index, int feature_index)
    {
        return weights[getObservationalTagWeightIndex(tag_index, feature_index)];
    }

    private double[] calculateAllTagScores(int position, Sentence sentence)
    {
        double [] tagScores = new double[tags.getSize()];
        //dot product weights times features
        for(int tag_index = 0; tag_index < tags.getSize(); tag_index++)
        {
            double total = 0;
            //calculate the score of having the current tag = tag weight * 1
            total += getTagWeight(tag_index);
            //calculate the score of being in this tag given the previous tag
            total += getTransitionTagWeight(tag_index, sentence.getTag(task,position-1, tags));
            //calculate the score of seeing the observational features with this tag
            FeatureArray featureArray = sentence.getObservationalFeatureArray(position);
            for(Feature feature: featureArray)
            {
                total += getObservationalTagWeight(tag_index, feature.index) * feature.value;
            }

            tagScores[tag_index] = total;
        }
        return tagScores;
    }

    private double calculateTagTransitionProbability(int position, int tag_index, Sentence sentence)
    {
        double [] tagsScore = calculateAllTagScores(position, sentence);
        ArrayUtil.expInPlace(tagsScore);

        return tagsScore[tag_index] / ArrayUtil.sum(tagsScore);
    }


    private double [] calculateAllTagTransitionProbability(int position, Sentence sentence)
    {
        double [] tagsScore = calculateAllTagScores(position, sentence);
        ArrayUtil.expInPlace(tagsScore);

        double denominator = ArrayUtil.sum(tagsScore);
        for(int i = 0; i < tagsScore.length; i++)
        {
            tagsScore[i] /= denominator;
        }
        return tagsScore;
    }

    @Override
    public double getLogLikelihood(ArrayList<Sentence> trainingSentences) {
        if(locked)
        {
            IOUtil.showError(this, "Can not calculate likelihood in locked mode.");
            return 0;
        }

        double lik = 0;

        for(Sentence sentence : trainingSentences)
        {
            for(int position = 0; position < sentence.getSize(); position++)
            {
                lik += Math.log(calculateTagTransitionProbability(position,sentence.getTag(task, position,tags),
                        sentence));
            }
        }

        return lik;
    }

    @Override
    public double[] computeGradient(ArrayList<Sentence> trainingSentences) {
        if(locked)
        {
            IOUtil.showError(this, "Can not calculate Gradient likelihood in locked mode.");
            return null;
        }

        double [] gradient = new double[weights.length];

        for(Sentence sentence : trainingSentences)
        {
            for(int position = 0; position < sentence.getSize(); position++)
            {
                double [] expectedFeatureProbability = calculateAllTagTransitionProbability(position,
                        sentence);
                int current_tag_index = sentence.getTag(task, position, tags);
                int previous_tag_index = sentence.getTag(task, position-1, tags);
                //loop over all labels
                //add empirical values - model expected values
                for(int i = 0; i < tags.getSize(); i++)
                {
                    int empiricalValue = current_tag_index == i ? 1 : 0;
                    //update current label gradient
                    gradient[getTagWeightIndex(i)] += empiricalValue - expectedFeatureProbability[i];
                    //update current transition gradient
                    gradient[getTransitionTagWeightIndex(i, previous_tag_index)] += empiricalValue -
                            expectedFeatureProbability[i];


                    //update the observation features gradient
                    FeatureArray featureArray = sentence.getObservationalFeatureArray(position);
                    for(Feature feature : featureArray)
                    {
                        gradient[getObservationalTagWeightIndex(i, feature.index)] += (empiricalValue
                                -  expectedFeatureProbability[i]) * feature.value;
                    }
                }
            }
        }
        return gradient;
    }


    @Override
    public void decode(Sentence sentence) {
        features.extractFeatures(sentence);
        for(int position = 0; position < sentence.getSize(); position++)
        {
            double [] tagsScore = calculateAllTagScores(position, sentence);
            sentence.setTag(task, position, ArrayMath.argmax(tagsScore));
        }
    }
}
