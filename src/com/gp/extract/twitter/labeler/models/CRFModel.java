package com.gp.extract.twitter.labeler.models;

import com.gp.extract.twitter.Configuration;
import com.gp.extract.twitter.labeler.features.Feature;
import com.gp.extract.twitter.labeler.features.FeatureArray;
import com.gp.extract.twitter.labeler.features.FeatureExtractor;
import com.gp.extract.twitter.labeler.sequence.Sentence;
import com.gp.extract.twitter.labeler.sequence.Tags;
import com.gp.extract.twitter.util.IOUtil;
import edu.stanford.nlp.math.ArrayMath;

import java.util.ArrayList;


public class CRFModel extends SequenceModel {
    private double [][][][] trainingForwardBackwardProbabilities;
    private double [] Z;
    private double [][][][] Q;
    private static final int FORWARD = 0, BACKWARD = 1;



    public CRFModel(Configuration.Task task, Tags tags,
                    ArrayList<FeatureExtractor> extractors, String saveDirectory) {
        super(task, tags, extractors, saveDirectory);
    }

    @Override
    public String getLoggerId() {
        return "CRF Model";
    }

    @Override
    public String getFeatureName(int index) {
        return null;
    }



    @Override
    public double getLogLikelihood(ArrayList<Sentence> trainingSentences) {
        computeForwardBackward(trainingSentences);

        if(locked)
        {
            IOUtil.showError(this, "Can not calculate likelihood in locked mode.");
            return 0;
        }

        double lik = 0;

        for(int sentence_index = 0; sentence_index < trainingSentences.size(); sentence_index++)
        {
            Sentence sentence = trainingSentences.get(sentence_index);
            for(int position = 0; position < sentence.getSize(); position++)
            {
                lik += Math.log(calculateModelProbability(sentence_index,sentence));
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

        for(int sentence_index = 0; sentence_index < trainingSentences.size(); sentence_index++)
        {
            Sentence sentence =  trainingSentences.get(sentence_index);
            double [][][] sentenceScores = Q[sentence_index];
            for(int position = 0; position < sentence.getSize(); position++)
            {
                double [][] positionScores = sentenceScores[position];
                int current_tag_index = sentence.getTag(task, position, tags);
                int previous_tag_index = sentence.getTag(task, position-1, tags);
                //loop over all labels
                //add empirical values - model expected values
                for(int prev_tag = 0; prev_tag < tags.getSize(); prev_tag++)
                {
                    double [] transitionScore = positionScores[prev_tag];
                    for(int cur_tag = 0; cur_tag < tags.getSize();cur_tag++)
                    {
                        double expectedProbability = transitionScore[cur_tag];
                        int empiricalValue = (current_tag_index == cur_tag && previous_tag_index == prev_tag) ? 1 : 0;
                        //update current label gradient
                        gradient[getTagWeightIndex(cur_tag)] += empiricalValue - expectedProbability;
                        //update current transition gradient
                        gradient[getTransitionTagWeightIndex(cur_tag, prev_tag)] += empiricalValue -
                                expectedProbability;


                        //update the observation features gradient
                        FeatureArray featureArray = sentence.getObservationalFeatureArray(position);
                        for(Feature feature : featureArray)
                        {
                            gradient[getObservationalTagWeightIndex(current_tag_index, feature.index)] += (empiricalValue
                                    -  expectedProbability) * feature.value;
                        }
                    }
                }
            }
        }
        return gradient;
    }

    @Override
    public void decode(Sentence sentence) {
        double [][] scores = new double[sentence.getSize()][tags.getSize()];
        int [][] maximum = new int[sentence.getSize()-1][tags.getSize()];

        //initialize
        for(int tag_id = 0; tag_id < sentence.getSize(); tag_id++)
        {
            scores[0][tag_id] = calculateTransitionScore(0, tags.getStartTagId(), tag_id, sentence);
        }

        //complete table
        for(int position = 1; position < sentence.getSize(); position++)
        {
            for(int cur_tag_id = 0; cur_tag_id < tags.getSize(); cur_tag_id++)
            {
                //get maximum transition
                double transitionScore = calculateTransitionScore(position,0,cur_tag_id, sentence);
                double max_value = transitionScore * scores[position-1][0];
                int max_arg = 0;
                for(int prev_tag_id = 1; prev_tag_id < tags.getSize(); prev_tag_id++)
                {
                    //update transition score
                    transitionScore -= getTransitionTagWeight(cur_tag_id, prev_tag_id-1);
                    transitionScore += getTransitionTagWeight(cur_tag_id, prev_tag_id);

                    double score = scores[position-1][prev_tag_id] * transitionScore;
                    if(score > max_value)
                    {
                        max_value = score;
                        max_arg = prev_tag_id;
                    }
                }

                //save the maximum transition score
                scores[position][cur_tag_id] = max_value;
                maximum[position-1][cur_tag_id] = max_arg;

            }

        }

        //save tags
        int last_max_tag_id = ArrayMath.argmax(scores[sentence.getSize()-1]);
        sentence.setTag(getTask(), sentence.getSize()-1,last_max_tag_id);

        //restore highest from table
        for(int position = maximum.length-1; position >= 0; position++)
        {
            last_max_tag_id = maximum[position][last_max_tag_id];
            sentence.setTag(getTask(), sentence.getSize()-1,last_max_tag_id);
        }


    }

    private double calculateModelProbability(int sentence_index, Sentence sentence)
    {
        double total = 0;
        for(int position = 0; position < sentence.getSize(); position++)
        {
            total += calculateTransitionScore(position, sentence.getTag(getTask(),position-1, tags),
                    sentence.getTag(getTask(), position, tags), sentence);
        }

        return Math.exp(total)/Z[sentence_index];
    }

    private double calculateTransitionScore(int position, int prev_tag, int cur_tag, Sentence sentence)
    {
        double total = 0;
        //calculate the score of having the current tag = tag weight * 1
        total += getTagWeight(cur_tag);
        //calculate the score of being in this tag given the previous tag
        total += getTransitionTagWeight(cur_tag, prev_tag);
        //calculate the score of seeing the observational features with this tag
        FeatureArray featureArray = sentence.getObservationalFeatureArray(position);
        for(Feature feature: featureArray)
        {
            total += getObservationalTagWeight(cur_tag, feature.index) * feature.value;
        }
        return total;
    }
    private double calculateTransitionScoreExp(int position, int prev_tag, int cur_tag, Sentence sentence)
    {
        return Math.exp(calculateTransitionScore(position,prev_tag,cur_tag,sentence));
    }
    private void computeForwardBackward(ArrayList<Sentence> sentences)
    {
        //check if not initialized
        if(trainingForwardBackwardProbabilities == null)
        {
            trainingForwardBackwardProbabilities = new double[sentences.size()][][][];
            for(int sentence_index = 0; sentence_index < sentences.size(); sentence_index++)
            {
                double [][][] sentenceData = new
                        double[sentences.get(sentence_index).getSize()][][];
                for(int position = 0; position < sentenceData.length; position++)
                {
                    double [][] positionData = new double[tags.getSize()][];
                    for(int tag_id = 0; tag_id < tags.getSize(); tag_id++)
                    {
                        positionData[tag_id] = new double[2];
                        if(position == sentenceData.length-1)
                        {
                            positionData[tag_id][BACKWARD] = 1;
                        }
                    }
                    sentenceData[position] = positionData;
                }
                trainingForwardBackwardProbabilities[sentence_index] = sentenceData;
            }

            Q = new double[sentences.size()][][][];
            for(int sentence_index = 0; sentence_index < sentences.size(); sentence_index++)
            {
                double [][][] sentenceData = new
                        double[sentences.get(sentence_index).getSize()][][];
                for(int position = 0; position < sentenceData.length-1; position++)
                {
                    double [][] positionData = new double[tags.getSize()][];
                    for(int tag_id = 0; tag_id < tags.getSize(); tag_id++)
                    {
                        positionData[tag_id] = new double[tags.getSize()];

                    }
                    sentenceData[position] = positionData;
                }
                Q[sentence_index] = sentenceData;
            }

            Z = new double[sentences.size()];
        }

        //calculate the forward
        for(int sentence_index = 0; sentence_index < sentences.size(); sentence_index++)
        {
            double [][][] sentenceData = trainingForwardBackwardProbabilities[sentence_index];
            double [][] score = sentenceData[0];
            Sentence curSentence = sentences.get(sentence_index);
            double totalZ = 0;
            //add for the first position
            for(int tag_id = 0; tag_id < tags.getSize(); tag_id++)
            {
                score[tag_id][FORWARD] = calculateTransitionScoreExp(0,tags.getStartTagId(), tag_id,
                        curSentence);
            }

            //go over each position and calculate the forward score for each tag
            for(int position = 1; position < curSentence.getSize(); position++ )
            {
                //previous scores
                score = sentenceData[position-1];
                //loop over all possible current tags
                for(int cur_tag_id = 0; cur_tag_id < tags.getSize(); cur_tag_id++)
                {
                    double total = score[0][FORWARD];
                    double transitionScore = calculateTransitionScoreExp(position, 0,cur_tag_id,curSentence);
                    total *= transitionScore;
                    for(int prev_tag_id = 1; prev_tag_id < tags.getSize(); prev_tag_id++)
                    {
                        //update transition score
                        transitionScore -= getTransitionTagWeight(cur_tag_id, prev_tag_id-1);
                        transitionScore += getTransitionTagWeight(cur_tag_id, prev_tag_id);

                        total += (score[prev_tag_id][FORWARD] * transitionScore);

                    }

                    sentenceData[position][cur_tag_id][FORWARD] = total;

                    //if last position add to totalZ
                    if(position == curSentence.getSize()-1)
                    {
                        totalZ += total;
                    }
                }
            }
            Z[sentence_index] = totalZ;
        }


        //calculate the backward
        for(int sentence_index = 0; sentence_index < sentences.size(); sentence_index++)
        {
            double [][][] sentenceData = trainingForwardBackwardProbabilities[sentence_index];
            double [][] score;
            Sentence curSentence = sentences.get(sentence_index);

            //go over each position and calculate the backward score for each tag
            for(int position = curSentence.getSize()-2; position >= 0 ; position--)
            {
                //previous scores
                score = sentenceData[position+1];
                //loop over all possible current tags
                for(int cur_tag_id = 0; cur_tag_id < tags.getSize(); cur_tag_id++)
                {
                    double total = 0;
                    for(int next_tag_id = 0; next_tag_id < tags.getSize(); next_tag_id++)
                    {
                        //update transition score
                        double transitionScore = calculateTransitionScoreExp(position+1, cur_tag_id, next_tag_id,
                                curSentence);

                        total += (score[next_tag_id][BACKWARD] * transitionScore);

                    }

                    sentenceData[position][cur_tag_id][BACKWARD] = total;
                }
            }
        }

        //calculate Q
        for(int sentence_index = 0; sentence_index < sentences.size(); sentence_index++)
        {
            double [][][] sentenceDataFB = trainingForwardBackwardProbabilities[sentence_index];
            double [][][] sentenceDataQ = Q[sentence_index];
            double [][]  scoreQ;
            double z = Z[sentence_index];
            Sentence curSentence = sentences.get(sentence_index);
            for(int position = 0; position < curSentence.getSize()-1 ; position++)
            {
                scoreQ = sentenceDataQ[position];
                //loop over all possible current tags
                for(int prev_tag_id = 0; prev_tag_id < tags.getSize(); prev_tag_id++)
                {
                    double [] transitionScore = scoreQ[prev_tag_id];

                    for(int cur_tag_id = 0; cur_tag_id < tags.getSize(); cur_tag_id++)
                    {
                        transitionScore[cur_tag_id] = (sentenceDataFB[position][prev_tag_id][FORWARD] *
                                calculateTransitionScoreExp(position+1,prev_tag_id, cur_tag_id, curSentence) *
                                sentenceDataFB[position+1][cur_tag_id][BACKWARD])/z;
                    }

                }
            }
        }

    }

}
