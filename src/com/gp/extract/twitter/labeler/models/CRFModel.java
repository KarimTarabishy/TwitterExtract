package com.gp.extract.twitter.labeler.models;

import com.gp.extract.twitter.Configuration;
import com.gp.extract.twitter.labeler.features.FeatureExtractor;
import com.gp.extract.twitter.labeler.sequence.Sentence;
import com.gp.extract.twitter.labeler.sequence.Tags;

import java.util.ArrayList;


public class CRFModel extends SequenceModel {
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
    public void setupWeights() {

    }

    @Override
    public double getLogLikelihood(ArrayList<Sentence> trainingSentences) {

        return 0;
    }

    @Override
    public double[] computeGradient(ArrayList<Sentence> trainingSentences) {

        return new double[0];
    }

    @Override
    public void decode(Sentence sentence) {

    }
}
