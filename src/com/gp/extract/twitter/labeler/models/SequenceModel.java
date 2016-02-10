package com.gp.extract.twitter.labeler.models;

import com.gp.extract.twitter.Configuration;
import com.gp.extract.twitter.labeler.features.FeatureExtractor;
import com.gp.extract.twitter.labeler.features.Features;
import com.gp.extract.twitter.labeler.sequence.Sentence;
import com.gp.extract.twitter.labeler.sequence.Tags;
import com.gp.extract.twitter.util.IOUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public abstract class SequenceModel implements IOUtil.Logger, IOUtil.Loadable{

    private static final String DATA_FILE = "model.txt";
    private static final String FEATURES_DIR = "features/";


    protected int transitionWeightsOffset, transitionWeightsColLength, observationalWeightsOffset,
            observationalWeightsColLength;


    protected Tags tags;
    protected Configuration.Task task;
    protected Features features;
    protected double [] weights;
    protected boolean locked = false;
    protected String saveDirectory;


    public abstract String getLoggerId();

    public SequenceModel(Configuration.Task task, Tags tags, ArrayList<FeatureExtractor> extractors,
                         String saveDirectory) {
        this.tags = tags;
        this.features = new Features(extractors, saveDirectory + FEATURES_DIR);
        this.saveDirectory = saveDirectory;
        this.task = task;
    }

    public Features getFeatures()
    {
        return features;
    }

    public Configuration.Task getTask() {
        return task;
    }
    public abstract String getFeatureName(int index);
    public void setupWeights() {

        if(weights != null)
        {
            IOUtil.showError(this, "Weights are already setup.");
            return;
        }

        //tags + tags*tags+1 + tags*obs_feat
        //we have a weight for each tag excluding the start tag
        int tags_size = tags.getSize();
        //weight for each possible tag transition
        int transitional_size = (tags.getSize()) * (tags.getSize()+1);
        //weights for having the observation features with each tag excluding the start tag
        int observational_size= features.getDimensions() * (tags.getSize());

        weights = new double[tags_size + transitional_size+ observational_size];

        //set offsets
        transitionWeightsOffset = tags_size;
        observationalWeightsOffset = tags_size + transitional_size;

        //set row sizes
        transitionWeightsColLength = (tags.getSize()+1);
        observationalWeightsColLength = (tags.getSize());
    }

    public double [] getWeights()
    {
        return weights;
    }

    public void setWeights(double [] weights)
    {
        if(locked)
        {
            IOUtil.showError(this, "Can not set weights in locked mode.");
            return;
        }
        this.weights = weights;
    }

    public void lock(){
        locked = true;
        features.lock();
    }

    public abstract double getLogLikelihood(ArrayList<Sentence> trainingSentences);
    public abstract double[] computeGradient(ArrayList<Sentence> trainingSentences);

    public Tags getTags() {
        return tags;
    }


    public abstract void decode(Sentence sentence);


    @Override
    public String getLogId() {
        return getLoggerId();
    }

    @Override
    public void load() throws IOException {
        features.load();
        setupWeights();

        String file = saveDirectory + DATA_FILE;
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(IOUtil.getUTF8FileReader(file, false));
            Scanner scanner = new Scanner(bufferedReader);
            String line = "";
            for (int i = 0; i < weights.length; i++){
                line = scanner.nextLine();
                String [] data = line.split("\\t");
                weights[i] = Double.parseDouble(data[1]);
            }
        }
        catch (IOException e)
        {
            throw e;
        }
        finally {
            if (bufferedReader != null)
                bufferedReader.close();
        }
        lock();
    }

    @Override
    public void save() throws IOException {
        features.save();

        if(weights.length == 0) {
            IOUtil.showError(this, "Model should be trained before it can be saved.");
            return;
        }

        String file = saveDirectory + DATA_FILE;
        BufferedWriter bufferedWriter = null;
        try
        {
            bufferedWriter = new BufferedWriter(IOUtil.getUTF8FileWriter(file, true));
            PrintWriter printWriter = new PrintWriter(bufferedWriter);
            for(int i = 0 ; i < weights.length; i++)
            {
                printWriter.printf("%s\t%s",getFeatureName(i), Double.toString(weights[i]));
                printWriter.println();
            }
        }
        catch (IOException e)
        {
            throw e;
        }
        finally {
            if(bufferedWriter != null)
            {
                bufferedWriter.close();
            }
        }


    }


    protected int getTagWeightIndex(int tag_index)
    {
        return tag_index;
    }
    protected int getTransitionTagWeightIndex(int tag_current_index, int tag_previous_index)
    {
        return transitionWeightsOffset + ((tag_current_index*(transitionWeightsColLength))
                + tag_previous_index);
    }
    protected int getObservationalTagWeightIndex(int tag_index, int feature_index)
    {
        return observationalWeightsOffset + ((feature_index*(observationalWeightsColLength))
                + tag_index);
    }

    protected double getTagWeight(int tag_index)
    {
        return weights[getTagWeightIndex(tag_index)];
    }


    protected double getTransitionTagWeight(int tag_current_index, int tag_previous_index)
    {
        return weights[getTransitionTagWeightIndex(tag_current_index, tag_previous_index)];
    }

    protected double getObservationalTagWeight(int tag_index, int feature_index)
    {
        return weights[getObservationalTagWeightIndex(tag_index, feature_index)];
    }
}
