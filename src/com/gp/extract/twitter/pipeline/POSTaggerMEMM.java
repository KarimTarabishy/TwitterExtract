package com.gp.extract.twitter.pipeline;


import com.gp.extract.twitter.labeler.MaximumEntropyMarkovModel;
import com.gp.extract.twitter.labeler.Trainer;
import com.gp.extract.twitter.labeler.features.FeatureExtractor;
import com.gp.extract.twitter.labeler.features.Features;
import com.gp.extract.twitter.labeler.features.WordClusters;
import com.gp.extract.twitter.labeler.features.WordsFeatures;
import com.gp.extract.twitter.labeler.sequence.Sentence;
import com.gp.extract.twitter.labeler.sequence.Tags;
import com.gp.extract.twitter.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class POSTaggerMEMM implements IOUtil.Logger{

    private final String LOGGER_ID = POSTaggerMEMM.class.getName();
    private MaximumEntropyMarkovModel model;
    private String saveDirectory;

    public POSTaggerMEMM(String saveDirectory)
    {
        this.saveDirectory = saveDirectory;
    }

    public Tags getPOSTags()
    {
        return model.getTags();
    }

    public String getTagSymbol(int index)
    {
        return model.getTags().getTagSymbolById(index);
    }
    public boolean load()
    {
        IOUtil.showInfo(this, "Loading Model...");
        Tags POSTags = new Tags(saveDirectory);
        try {
            POSTags.load();
        } catch (IOException e) {
            IOUtil.showError(this, "Couldn't load tags: " + e.getMessage());
            return false;
        }

        try {
            // ensure we have a new model to train on
            model = new MaximumEntropyMarkovModel(POSTags, setupFeatures(), saveDirectory);
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
     * Test the trained model
     * @param testing_file  file conaining the testing data,format "word \t tag",
     *                      and tweets are separated by an empty line
     * @return
     */
    public double testTagger(String testing_file)
    {
        if(model == null)
        {
            IOUtil.showError(this,"Can't find model to test.");
            return 0;
        }

        ArrayList<Sentence> testing_sentences;
        try {
            testing_sentences = Trainer.readAnnotatedSentences(testing_file, model.getTags());
        } catch (IOException e) {
            IOUtil.showError(this, e.getMessage());
            return 0;
        }
        IOUtil.showInfo(this, "Completed reading testing data");

        return testModel(testing_sentences);
    }

    /**
     * Trainer memm on pos and report accuracy on test data.
     * @param training_file file containing training data, format "word \t tag",
     *                      and tweets are separated by an empty line
     */
    public void train(String training_file)
    {
        Tags POSTags = new Tags(saveDirectory);

        ArrayList<Sentence> training_sentences;
        ArrayList<Sentence> testing_sentences;
        try {
            training_sentences = Trainer.readAnnotatedSentences(training_file, POSTags);
        } catch (IOException e) {
            IOUtil.showError(this, e.getMessage());
            return;
        }
        IOUtil.showInfo(this, "Completed reading training data");

        try {
            // ensure we have a new model to train on
            model = new MaximumEntropyMarkovModel(POSTags, setupFeatures(), saveDirectory);
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
            POSTags.save();
        } catch (IOException e) {
            IOUtil.showError(this, "Could not save POSTags: " + e.getMessage());
            return;
        }

    }

    private static ArrayList<FeatureExtractor> setupFeatures() throws Exception
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

    private double testModel(ArrayList<Sentence> sentences) {
        if(model == null)
        {
            IOUtil.showError(this, "No model found to test.");
            return 0;
        }

        IOUtil.showInfo(this, "Testing...");
        int total = 0;
        int correct = 0;
        for(int i = 0; i < sentences.size(); i++)
        {
            Sentence sentence = sentences.get(i);
            ArrayList<Integer> actualTags = new ArrayList<>();
            for(int j = 0 ; j < sentence.getSize(); j++)
            {
                actualTags.add(j, sentence.getTag(j, model.getTags()));
            }
            model.greedy_decode(sentences.get(i));

            total += sentence.getSize();
            //compare
            for(int position = 0; position < sentence.getSize(); position++)
            {
                if(actualTags.get(position).equals(sentence.getTag(position, model.getTags())))
                {
                    correct++;
                }
            }
        }

        return (double)correct/(double)total;

    }

    public void predict(Sentence sentence)
    {
        model.greedy_decode(sentence);
    }

    @Override
    public String getLogId() {
        return LOGGER_ID;
    }
}
