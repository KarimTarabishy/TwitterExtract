package com.gp.extract.twitter.pipeline.taggers;

import com.gp.extract.twitter.Configuration;
import com.gp.extract.twitter.labeler.models.SequenceModel;
import com.gp.extract.twitter.labeler.features.FeatureExtractor;
import com.gp.extract.twitter.labeler.sequence.Sentence;
import com.gp.extract.twitter.labeler.sequence.Tags;
import com.gp.extract.twitter.pipeline.Pipeline;

import java.util.ArrayList;

public class Chunker extends Tagger {
    public Chunker(String saveDirectory) {
        super(saveDirectory);
    }

    @Override
    public String getLoggerId() {
        return "Chunker";
    }

    @Override
    public Tags createTags() {
        return new Tags(saveDirectory, "o","o");
    }

    @Override
    public SequenceModel createModel(Tags tags) throws Exception {
        return null;
    }

    @Override
    public Configuration.Task getTask() {
        return Configuration.Task.CHUNKER;
    }

    @Override
    protected ArrayList<FeatureExtractor> setupFeatures() throws Exception {
        return null;
    }

    @Override
    protected int getCrossValidationFolds() {
        return 4;
    }

    @Override
    protected void preprocess(ArrayList<Sentence> sentences) {
        Tagger tagger = Pipeline.getPipeline().getPOSTagger();
        sentences.forEach(tagger::predict);
    }
}
