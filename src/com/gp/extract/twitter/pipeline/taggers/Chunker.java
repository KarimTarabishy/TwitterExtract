package com.gp.extract.twitter.pipeline.taggers;

import com.gp.extract.twitter.Configuration;
import com.gp.extract.twitter.labeler.features.FeatureExtractor;
import com.gp.extract.twitter.labeler.features.POSExtractor;
import com.gp.extract.twitter.labeler.features.WordClusters;
import com.gp.extract.twitter.labeler.features.WordsFeatures;
import com.gp.extract.twitter.labeler.models.CRFModel;
import com.gp.extract.twitter.labeler.models.MaximumEntropyMarkovModel;
import com.gp.extract.twitter.labeler.models.SequenceModel;
import com.gp.extract.twitter.labeler.sequence.Sentence;
import com.gp.extract.twitter.labeler.sequence.Tags;
import com.gp.extract.twitter.pipeline.Pipeline;

import java.io.IOException;
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
        return new Tags(saveDirectory, "CHUNKER_START","CHUNKER_END");
    }

    @Override
    public SequenceModel createModel(Tags tags) throws Exception {
        return new MaximumEntropyMarkovModel(Configuration.Task.CHUNKER, tags, setupFeatures(), saveDirectory);
    }

    @Override
    public Configuration.Task getTask() {
        return Configuration.Task.CHUNKER;
    }

    @Override
    protected ArrayList<FeatureExtractor> setupFeatures() throws Exception {
        ArrayList<FeatureExtractor> extractors = new ArrayList<>();

        extractors.add(new WordsFeatures.WordFormFeatures());
        extractors.add(new WordsFeatures.NgramPrefix(20));
        extractors.add(new WordsFeatures.NgramSuffix(20));
        extractors.add(new WordsFeatures.NextWord());
        extractors.add(new WordsFeatures.PrevWord());
        extractors.add(new WordsFeatures.Positional());
        extractors.add(new WordsFeatures.Orthographical());
        extractors.add(new WordsFeatures.PrevNext());
        extractors.add(new WordsFeatures.Capitalization());

        try {
            extractors.add(new WordClusters());
        } catch (IOException e) {
            throw new Exception("Couldn't initialize world clusters feature: " + e.getMessage());
        }

        extractors.add(new POSExtractor());

        return extractors;
    }



    @Override
    protected void preprocess(ArrayList<Sentence> sentences) {
        Tagger tagger = Pipeline.getPipeline().getPOSTagger();
        sentences.forEach(tagger::predict);
    }
}
