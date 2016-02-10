package com.gp.extract.twitter.labeler.features;


import com.gp.extract.twitter.Configuration;
import com.gp.extract.twitter.labeler.sequence.Sentence;
import com.gp.extract.twitter.labeler.sequence.Tags;
import com.gp.extract.twitter.pipeline.Pipeline;
import com.gp.extract.twitter.pipeline.taggers.Tagger;

import java.util.ArrayList;

public class POSExtractor extends FeatureExtractor {
    public static final String FILE_NAME = POSExtractor.class.getSimpleName() + ".txt";
    @Override
    public void extract(Sentence sentence, ArrayList<FeatureArray> output, boolean isTraining) {
        for(int position = 0; position< sentence.getSize(); position++)
        {
            Tagger tagger = Pipeline.getPipeline().getPOSTagger();
            Tags tags = tagger.getTags();
            String curTag = tagger.getTagSymbol(sentence.getTag(Configuration.Task.POS, position,tags));
            String prevTag = tagger.getTagSymbol(sentence.getTag(Configuration.Task.POS, position-1,tags));
            String prevPrevTag = tagger.getTagSymbol(sentence.getTag(Configuration.Task.POS, position-2,tags));
            FeatureArray array = output.get(position);

            array.add(getFeatureIndex("UT|"+curTag, isTraining));
            array.add(getFeatureIndex("BT|"+prevTag+"|"+curTag, isTraining));
            array.add(getFeatureIndex("TT|"+prevPrevTag+"|"+prevTag+"|"+curTag, isTraining));
        }
    }

    @Override
    public String getDataFileName() {
        return FILE_NAME;
    }
}
