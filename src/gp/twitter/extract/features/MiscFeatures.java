package gp.twitter.extract.features;

import gp.twitter.extract.labeler.sequence.Observation;
import gp.twitter.extract.labeler.sequence.Sentence;
import gp.twitter.extract.labeler.sparse.array.BooleanSparseArray;
import gp.twitter.extract.labeler.tags.Tag;
import gp.twitter.extract.labeler.sparse.array.SparseArray;

import java.util.List;

/**
 * Created by stc on 12/16/2015.
 */
public class MiscFeatures  {

    public static class WordIdentityFeature extends FeatureExtractor{
        public SparseArray getFeatures(Tag c_tag, Tag p_tag, Sentence sentence, int position, boolean isTraining) {
            BooleanSparseArray featuresVector = new BooleanSparseArray();
            //get the feature
            String word = sentence.getObservation(position).getValue();
            //normalize quotations
            word = word.replaceAll("[‘’´`]", "'").replaceAll("[“”]", "\"");
            attemptAddingFeature(word,featuresVector,isTraining);
            return null;
        }

        @Override
        public boolean isCurrentTagDependent() {
            return false;
        }
    }

}
