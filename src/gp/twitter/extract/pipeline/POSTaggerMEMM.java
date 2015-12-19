package gp.twitter.extract.pipeline;


import gp.twitter.extract.features.Features;
import gp.twitter.extract.labeler.MEMM;
import gp.twitter.extract.labeler.Trainer;
import gp.twitter.extract.labeler.tags.POSTags;

import java.io.IOException;

public class POSTaggerMEMM {

    private static POSTaggerMEMM tagger;
    private MEMM model;
    private String tagsFile;
    private Features features;
    private POSTags tags;
    private POSTaggerMEMM()
    {

    }

    public static POSTaggerMEMM getTagger()
    {
        if(tagger == null)
        {
            tagger = new POSTaggerMEMM();
            setupFeatures(tagger);
        }
        return tagger;
    }


    /**
     * Trainer memm on pos and report accuracy on test data.
     * @param training_file file containing training data, format "word \t tag",
     *                      and tweets are separated by an empty line
     * @param testing_file  file conaining the testing data,same format
     * @param tags_file     file containing tags, each in its own line (null means use the previous tags)
     * @return accuracy of the trained POS tagger on the given testing set
     */
    public double trainTagger(String training_file, String testing_file, String tags_file)
    {
        //check the tags file
        if(tags_file == null)
        {
            //this means user assumes that the tagger already has its tags object created
            //we need to check that
            if(tags == null)
            {
                throw new RuntimeException("POS tagger was supplied with a null tag file while it has not setup" +
                        " the tagging set before.");
            }
        }
        else
        {
            try {
                tags = new POSTags(tags_file);
            } catch (IOException e) {
                throw new RuntimeException("Could not read the tagging set file.");
            }
        }

        // ensure we have a new model to train on
        if(model != null)
        {
            model = new MEMM(tags, features);
        }

        Trainer trainer = new Trainer(model);
        try {
            trainer.trainModel(training_file);
        } catch (IOException e) {
            throw new RuntimeException("Could not read the training set file.");
        }
        double accuracy = 0;
        try {
            accuracy = trainer.testModel(testing_file);
        } catch (IOException e) {
            throw new RuntimeException("Could not read the testing set file.");
        }
        return accuracy;

    }

    private static void setupFeatures(POSTaggerMEMM tagger)
    {

    }


}
