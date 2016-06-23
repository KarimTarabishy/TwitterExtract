package com.gp.extract.twitter.labeler.features;


import com.gp.extract.twitter.Configuration;
import com.gp.extract.twitter.labeler.sequence.Sentence;
import com.gp.extract.twitter.labeler.sequence.Tags;
import com.gp.extract.twitter.pipeline.Pipeline;
import com.gp.extract.twitter.pipeline.taggers.Tagger;

import java.util.ArrayList;

public class CapExtractor extends FeatureExtractor {
    public static final String FILE_NAME = CapExtractor.class.getSimpleName() + ".txt";
    @Override
    public void extract(Sentence sentence, ArrayList<FeatureArray> output, boolean isTraining) {
        for(int position = 0; position< sentence.getSize(); position++)
        {

        }
    }

    @Override
    public String getDataFileName() {
        return FILE_NAME;
    }
}
