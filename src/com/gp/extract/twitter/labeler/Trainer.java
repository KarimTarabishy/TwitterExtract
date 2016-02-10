package com.gp.extract.twitter.labeler;

import com.gp.extract.twitter.Configuration;
import com.gp.extract.twitter.labeler.models.SequenceModel;
import com.gp.extract.twitter.labeler.sequence.Sentence;
import com.gp.extract.twitter.labeler.sequence.Tags;
import com.gp.extract.twitter.util.IOUtil;
import edu.stanford.nlp.math.ArrayMath;
import edu.stanford.nlp.optimization.DiffFunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class Trainer implements IOUtil.Logger {


    private static final String LOGGER_ID = Trainer.class.getName();
    private ArrayList<Sentence> trainingSentences;
    private SequenceModel model;

    private double l1, l2;
    private static final int maxIter = 500;
    private static double tol = 1e-7;

    public Trainer(SequenceModel model) {
        this.model = model;
        l1 = Configuration.getL1(model.getTask());
        l2 = Configuration.getL2(model.getTask());;
    }

    public void trainModel(ArrayList<Sentence> sentences)
    {
        trainingSentences = sentences;
        IOUtil.showInfo(this, "Extracting features....");
        //get all observational features
        model.getFeatures().extractTrainingFeatures(trainingSentences);

        IOUtil.showInfo(this, "Setting up weights");
        //setup the weight
        model.setupWeights();


        IOUtil.showInfo(this, "Optimizing");
        optimize();

        model.lock();
    }

    private void optimize()
    {
        OWLQN minimizer = new OWLQN();
        minimizer.setMaxIters(maxIter);
        minimizer.setQuiet(false);
        minimizer.setWeightsPrinting(new MyWeightsPrinter());
        double[] initialWeights = model.getWeights();

        double[] finalWeights = minimizer.minimize(
                new GradientCalculator(),
                initialWeights, l1, tol, 5);

        model.setWeights(finalWeights);
    }

    @Override
    public String getLogId() {
        return LOGGER_ID;
    }

    private class GradientCalculator implements DiffFunction {

        public int domainDimension() {
            return model.getWeights().length;
        }


        public double valueAt(double[] flatCoefs) {
            model.setWeights(flatCoefs);

            return -model.getLogLikelihood(trainingSentences) + (0.5*l2*getL2Norm(model.getWeights()));
        }


        public double[] derivativeAt(double[] flatCoefs) {
            model.setWeights(flatCoefs);
            double [] gradients = model.computeGradient(trainingSentences);

            ArrayMath.multiplyInPlace(gradients, -1);

            //regularize
            for(int i = 0; i < gradients.length; i++)
            {
                gradients[i] += (l2*flatCoefs[i]);
            }
            return gradients;
        }
    }

    public class MyWeightsPrinter implements OWLQN.WeightsPrinter {

        public void printWeights() {

            System.out.printf("\t LL %.6f\t", model.getLogLikelihood(trainingSentences));
        }
    }


    public void setL1(double l1) {
        this.l1 = l1;
    }

    public void setL2(double l2) {
        this.l2 = l2;
    }

    private double getL2Norm(double [] weights)
    {
        double norm = 0;
        for(int i = 0; i < weights.length; i++)
        {
            norm += weights[i]*weights[i];
        }
        return norm;
    }

    /**
     * Read sentences from the given data set file.
     * @param task Task for this annotated data set
     * @param file_name the data set file
     * @param tags when supplied filled with all the found tags
     * @return list of all sentences read, contains tag and value for each observation in a sentence
     * @throws IOException
     */
    public  static ArrayList<Sentence> readAnnotatedSentences(Configuration.Task task,
                                                              String file_name, Tags tags) throws IOException
    {
        IOUtil.DatasetReader reader = new IOUtil.DatasetReader(file_name) ;
        ArrayList<Sentence> sentences =new ArrayList<Sentence>();
        ArrayList<ArrayList<String>> words = new ArrayList<>(), tagSymbols = new ArrayList<>();
        HashSet<String> foundTagSymbols = new HashSet<>();

        ArrayList<String> data = new ArrayList<>(2);
        data.add(0, "");
        data.add(1,"");
        int sentence_index = 0;
        //initialize first sentence
        words.add(0, new ArrayList<>());
        tagSymbols.add(0, new ArrayList<>());
        // read while we have not reached the end of file
        while(reader.read(data))
        {
            String word = data.get(0), tag = data.get(1);
            // check if we reached the end of the current sentence
            if(word == null){
                sentence_index++;
                //create arraylist for the new sentence
                words.add(sentence_index, new ArrayList<>());
                tagSymbols.add(sentence_index, new ArrayList<>());

            }
            else
            {
                //we are in the sentence
                words.get(sentence_index).add(word);
                tagSymbols.get(sentence_index).add(tag);

                //add to found tags
                foundTagSymbols.add(tag);
            }
        }
        //creates tag mapper if not filled
        if(!tags.isInitialized())
        {
            tags.addTags(foundTagSymbols);
        }

        //start creating sentences
        for(int i = 0; i < words.size(); i++)
        {
            if(!words.get(i).isEmpty())
                sentences.add(new Sentence(task, words.get(i), tagSymbols.get(i), tags));
        }

        return sentences;
    }
}
