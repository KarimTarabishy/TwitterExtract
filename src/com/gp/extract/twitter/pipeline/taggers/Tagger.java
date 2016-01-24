package com.gp.extract.twitter.pipeline.taggers;


import com.gp.extract.twitter.Configuration;
import com.gp.extract.twitter.labeler.models.SequenceModel;
import com.gp.extract.twitter.labeler.Trainer;
import com.gp.extract.twitter.labeler.features.FeatureExtractor;
import com.gp.extract.twitter.labeler.sequence.Sentence;
import com.gp.extract.twitter.labeler.sequence.Tags;
import com.gp.extract.twitter.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public abstract class Tagger implements IOUtil.Logger{
    protected SequenceModel model;
    protected String saveDirectory;


    public Tags getTags()
    {
        return model.getTags();
    }
    public String getTagSymbol(int index)
    {
        return model.getTags().getTagSymbolById(index);
    }
    public Tagger(String saveDirectory)
    {
        this.saveDirectory = "taggers_data/"+saveDirectory;
    }
    public abstract String getLoggerId();
    public abstract Tags createTags();
    public  abstract SequenceModel createModel(Tags tags) throws Exception;
    public abstract Configuration.Task getTask();
    public boolean load()
    {
        IOUtil.showInfo(this, "Loading Model...");
        Tags tags = createTags();
        try {
            tags.load();
        } catch (IOException e) {
            IOUtil.showError(this, "Couldn't load tags: " + e.getMessage());
            return false;
        }

        try {
            // ensure we have a new model to train on
            model = createModel(tags);
        }
        catch (Exception e)
        {
            IOUtil.showError(this, e.getMessage());
            return false;
        }

        try {
            model.load();
        } catch (IOException e) {
            IOUtil.showError(this, "Couldn't load model: " + e.getMessage());
            return false;
        }
        return true;
    }



    /**
     * Trainer memm on pos and report accuracy on test data.
     * @param training_file file containing training data, format "word \t tag",
     *                      and tweets are separated by an empty line
     */
    public void train(String training_file)
    {
        Tags tags = createTags();

        ArrayList<Sentence> training_sentences;
        try {
            training_sentences = Trainer.readAnnotatedSentences(getTask(), training_file, tags);
        } catch (IOException e) {
            IOUtil.showError(this, e.getMessage());
            return;
        }
        IOUtil.showInfo(this, "Completed reading training data");

        //do any preprocess like pre tagging
        preprocess(training_sentences);
        try {
            // ensure we have a new model to train on
            model = createModel(tags);
        }
        catch (Exception e)
        {
            IOUtil.showError(this, e.getMessage());
            return;
        }

        IOUtil.showInfo(this, "Training...");
        Trainer trainer = new Trainer(model);
        trainer.trainModel(training_sentences);

        try {
            new File(saveDirectory).mkdirs();
            model.save();
        } catch (IOException e) {
            IOUtil.showError(this, "Could not save model: " + e.getMessage());
            return;
        }

        try {
            tags.save();
        } catch (IOException e) {
            IOUtil.showError(this, "Could not save POSTags: " + e.getMessage());
            return;
        }

    }


    public void train()
    {
        train(Configuration.getTrainingFile(getTask()));
    }
    /**
     * Test the trained model
     * @param testing_file  file conaining the testing data,format "word \t tag",
     *                      and tweets are separated by an empty line
     * @return
     */
    public double test(String testing_file)
    {
        if(model == null && getCrossValidationFolds() == 0)
        {
            IOUtil.showError(this,"Can't find model to test.");
            return 0;
        }

        ArrayList<Sentence> testing_sentences;
        try {
            testing_sentences = Trainer.readAnnotatedSentences(getTask(), testing_file, model.getTags());
        } catch (IOException e) {
            IOUtil.showError(this, e.getMessage());
            return 0;
        }
        IOUtil.showInfo(this, "Completed reading testing data");

        //do any preprocess like pre tagging
        preprocess(testing_sentences);
        double precision = 0;
        if(getCrossValidationFolds() == 0) {
            precision = testModel(testing_sentences);
        }
        else
        {
            precision = testModelWithCrossValidation(testing_sentences);
        }

        return precision;
    }

    public double test()
    {
        return test(Configuration.getTestingFile(getTask()));
    }

    private double testModel(ArrayList<Sentence> sentences) {
        IOUtil.showInfo(this, "Testing...");
        int total = 0;
        int correct = 0;

        for(int i = 0; i < sentences.size(); i++)
        {
            Sentence sentence = sentences.get(i);
            ArrayList<Integer> actualTags = new ArrayList<>();
            for(int j = 0 ; j < sentence.getSize(); j++)
            {
                actualTags.add(j, sentence.getTag(getTask(), j, model.getTags()));
            }
            model.decode(sentences.get(i));

            total += sentence.getSize();
            //compare
            for(int position = 0; position < sentence.getSize(); position++)
            {
                if(actualTags.get(position).equals(sentence.getTag(getTask(), position, model.getTags())))
                {
                    correct++;
                }
            }
        }

        return (double)correct/(double)total;

    }

    private double testModelWithCrossValidation(ArrayList<Sentence> sentences) {
        int folds = getCrossValidationFolds();


        return 0;
    }

    public void predict(Sentence sentence)
    {
        model.decode(sentence);
    }

    protected void preprocess(ArrayList<Sentence> sentences)
    {

    }
    protected abstract ArrayList<FeatureExtractor> setupFeatures() throws Exception;

    protected abstract int getCrossValidationFolds();
    @Override
    public String getLogId() {
        return getLoggerId();
    }
}
