package com.gp.extract.twitter.labeler.features;

import com.gp.extract.twitter.labeler.sequence.Sentence;
import com.gp.extract.twitter.util.ArrayUtil;
import com.gp.extract.twitter.util.IOUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class Features implements IOUtil.Logger, IOUtil.Loadable{

    private ArrayList<FeatureExtractor> extractors;
    private int nextOffset = 0;
    private static final String LOGGER_ID = Features.class.getName();
    private ArrayList<String> featureIndexToSymbol;
    private boolean locked = false;
    private String saveDirectory;


    private static final String DATA_FILE_NAME = "features.txt"; //contains the reverse mapping


    public Features(ArrayList<FeatureExtractor> extractors, String saveDirectory)
    {
        this.extractors = extractors;
        this.saveDirectory = saveDirectory;

        for(FeatureExtractor extractor: extractors)
        {
            extractor.setSaveDirectory(saveDirectory);
        }
    }

    public void extractFeatures(Sentence sentence)
    {
        ArrayList<FeatureArray> positionalFeatures = new ArrayList<>(sentence.getSize());
        //initialize the feature array
        for(int position = 0; position < sentence.getSize(); position++)
        {
            positionalFeatures.add(position, new FeatureArray());
        }

        //loop over each extractor
        for(int extractor_index = 0; extractor_index < extractors.size(); extractor_index++)
        {
            //get the current extractor
            FeatureExtractor extractor = extractors.get(extractor_index);
            //extract the features
            extractor.extract(sentence, positionalFeatures, false);
        }

        //attach to sentence
        sentence.setFeatures(positionalFeatures);
    }


    /**
     * Used once for training, each extractor save all features it saw and map it to integers.
     * Sentences are then filled with the appropriate feature values.
     * @param sentences training sentences
     */
    public boolean extractTrainingFeatures(ArrayList<Sentence> sentences)
    {
        if(locked)
        {
            IOUtil.showError(this, "Can not extract training features from locked features.");
            return false;
        }


        if(extractors == null)
        {
            IOUtil.showError(this, "extractors must be set before training");
            return false;
        }


        ArrayList<ArrayList<FeatureArray>> sentencesPositionalFeatures = new ArrayList<>(sentences.size());
        //initialize the extractorFeatures
        for(int sentence_index = 0; sentence_index < sentences.size(); sentence_index++) {
            Sentence sentence = sentences.get(sentence_index);
            ArrayList<FeatureArray> featuresArray = new ArrayList<>(sentence.getSize());
            for(int position = 0; position < sentence.getSize(); position++)
            {
                featuresArray.add(new FeatureArray());
            }
            sentencesPositionalFeatures.add(sentence_index, featuresArray);
        }


        //loop over each extractor
        for(int extractor_index = 0; extractor_index < extractors.size(); extractor_index++)
        {
            //get the current extractor
            FeatureExtractor extractor = extractors.get(extractor_index);
            extractor.setFeatureIndexOffset(nextOffset);
            //loop over each sentence to extract features
            for(int sentence_index = 0; sentence_index < sentences.size(); sentence_index++) {
                //get the current sentence
                Sentence sentence = sentences.get(sentence_index);
                //extract the features in training mode
                extractor.extract(sentence, sentencesPositionalFeatures.get(sentence_index), true);
            }

            nextOffset += extractor.getFeaturesSize();
        }

        //make a reverse mapping
        featureIndexToSymbol = new ArrayList<>(nextOffset);
        ArrayUtil.fill(featureIndexToSymbol, nextOffset);
        for(int extractor_index = 0; extractor_index < extractors.size(); extractor_index++)
        {
            FeatureExtractor extractor = extractors.get(extractor_index);
            Map<String, Integer> mapping = extractor.getFeatureMapping();
            for(Map.Entry<String, Integer> entry : mapping.entrySet())
            {
                featureIndexToSymbol.set(entry.getValue(),entry.getKey());
            }
        }

        //attach to sentence
        for(int sentence_index = 0; sentence_index < sentences.size(); sentence_index++) {
            //list which contain positional features of the sentence
            sentences.get(sentence_index).setFeatures(sentencesPositionalFeatures.get(sentence_index));
        }

        return true;
    }


    public String getFeatureName(int index)
    {
        if(featureIndexToSymbol != null)
        {
            return featureIndexToSymbol.get(index);
        }
        return null;
    }

    public int getDimensions()
    {
        return nextOffset;
    }

    @Override
    public String getLogId() {
        return LOGGER_ID;
    }

    @Override
    public void load() throws IOException {
        String file = saveDirectory + DATA_FILE_NAME;
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(IOUtil.getUTF8FileReader(file, false));
            Scanner scanner = new Scanner(bufferedReader);
            String line;
            //read next offset
            nextOffset = scanner.nextInt();
            scanner.nextLine();
            featureIndexToSymbol = new ArrayList<>(nextOffset);
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                line = line.trim();
                if(line.isEmpty())
                    continue;

                String[] data = line.split("\\s");
                int index = Integer.parseInt(data[0]);
                featureIndexToSymbol.add(index, data[1]);
            }

        }
        catch (IOException e)
        {
            throw e;
        }
        finally {
            if(bufferedReader != null)
                bufferedReader.close();
        }

        //load other extractors
        for(FeatureExtractor extractor: extractors)
        {
            extractor.load();
        }

        lock();
    }

    @Override
    public void save() throws IOException {

        String file = saveDirectory + DATA_FILE_NAME;
        BufferedWriter bufferedWriter = null;
        try
        {
            new File(saveDirectory).mkdirs();
            bufferedWriter = new BufferedWriter(IOUtil.getUTF8FileWriter(file, true));
            PrintWriter printWriter = new PrintWriter(bufferedWriter);
            //write offset
            printWriter.println(nextOffset);
            //start writing map data
            for(int i = 0; i < featureIndexToSymbol.size(); i++)
            {
                printWriter.printf("%d\t%s",i, featureIndexToSymbol.get(i));
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

        //save others
        //load other extractors
        for(FeatureExtractor extractor: extractors)
        {
            extractor.save();
        }
    }

    public void lock()
    {
        locked = true;
    }

}
