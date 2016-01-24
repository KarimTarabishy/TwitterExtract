package com.gp.extract.twitter.pipeline.taggers;


import com.gp.extract.twitter.Configuration;
import com.gp.extract.twitter.labeler.models.MaximumEntropyMarkovModel;
import com.gp.extract.twitter.labeler.models.SequenceModel;
import com.gp.extract.twitter.labeler.features.FeatureExtractor;
import com.gp.extract.twitter.labeler.features.WordClusters;
import com.gp.extract.twitter.labeler.features.WordsFeatures;
import com.gp.extract.twitter.labeler.sequence.Tags;

import java.io.IOException;
import java.util.ArrayList;

public class POSTaggerMEMM extends Tagger{

    public POSTaggerMEMM(String saveDirectory)
    {
        super(saveDirectory);
    }

    @Override
    public String getLoggerId() {
        return "POS Tagger MEMM";
    }

    @Override
    public Tags createTags() {
        return new Tags(saveDirectory, "POS_START_SYMBOL", "POS_END_SYMBOL");
    }

    @Override
    public SequenceModel createModel(Tags tags) throws Exception {
        return new MaximumEntropyMarkovModel(Configuration.Task.POS, tags, setupFeatures(), saveDirectory);
    }

    @Override
    public Configuration.Task getTask() {
        return Configuration.Task.POS;
    }

    protected   ArrayList<FeatureExtractor> setupFeatures() throws Exception
    {
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

        return extractors;
    }

    @Override
    protected int getCrossValidationFolds() {
        return 0;
    }

}
