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
        ArrayList<ArrayList<FeatureArray>> sentencePositionalFeatures = new ArrayList<>(extractors.size());
        //initialize the extractorFeatures
        for(int extractor_index = 0; extractor_index < extractors.size(); extractor_index++)
        {
            sentencePositionalFeatures.add(extractor_index, new ArrayList<>(sentence.getSize()));
        }

        //This part is specifically made like that to be easy to parallelize later
        //loop over each extractor
        for(int extractor_index = 0; extractor_index < extractors.size(); extractor_index++)
        {
            //get the current extractor
            FeatureExtractor extractor = extractors.get(extractor_index);
            //list which contain positional features of the sentence
            ArrayList<FeatureArray> positionalFeatures = sentencePositionalFeatures.get(extractor_index);
            //extract the features
            extractor.extract(sentence, positionalFeatures, false);
        }

        //attach to sentence
        for(int extractor_index = 0; extractor_index < extractors.size(); extractor_index++)
        {
            sentence.concatFeatures(sentencePositionalFeatures.get(extractor_index),
                        extractor_index==0);
        }

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


        ArrayList<ArrayList<ArrayList<FeatureArray>>> extractorFeatures = new ArrayList<>(extractors.size());
        //initialize the extractorFeatures
        for(int extractor_index = 0; extractor_index < extractors.size(); extractor_index++)
        {
            extractorFeatures.add(extractor_index, new ArrayList<>(sentences.size()));
        }


        //This part is specifically made like that to be easy to parallelize later
        //loop over each extractor
        for(int extractor_index = 0; extractor_index < extractors.size(); extractor_index++)
        {
            //get the current extractor
            FeatureExtractor extractor = extractors.get(extractor_index);
            //get the list whose elements contain the positional features of each sentence for the current extractor
            ArrayList<ArrayList<FeatureArray>> sentencesPositionalFeatures = extractorFeatures.get(extractor_index);
            //loop over each sentence to extract features
            for(int sentence_index = 0; sentence_index < sentences.size(); sentence_index++) {
                //get the current sentence
                Sentence sentence = sentences.get(sentence_index);
                //list which contain positional features of the sentence
                ArrayList<FeatureArray> positionalFeatures = new ArrayList<>(sentence.getSize());
                //extract the features in training mode
                extractor.extract(sentence, positionalFeatures, true);
                //add the positional feature of this sentence
                sentencesPositionalFeatures.add(sentence_index, positionalFeatures);

            }
        }

        //now its time to sequentially update the offset of each feature extractor
        for(int extractor_index = 0; extractor_index < extractors.size(); extractor_index++)
        {
            FeatureExtractor extractor = extractors.get(extractor_index);
            extractor.setFeatureIndexOffset(nextOffset);
            nextOffset += extractor.getFeaturesSize();

        }

        //make a reverse mapping
        featureIndexToSymbol = new ArrayList<>(nextOffset);
        ArrayUtil.fill(featureIndexToSymbol, nextOffset);
        for(int extractor_index = 0; extractor_index < extractors.size(); extractor_index++)
        {
            FeatureExtractor extractor = extractors.get(extractor_index);
            int offset = extractor.getFeatureIndexOffset();
            Map<String, Integer> mapping = extractor.getFeatureMapping();
            for(Map.Entry<String, Integer> entry : mapping.entrySet())
            {
                featureIndexToSymbol.set(entry.getValue()+offset,entry.getKey());
            }
        }


        //start fixing the ids produced before offset was set
        for(int extractor_index = 0; extractor_index < extractors.size(); extractor_index++)
        {
            FeatureExtractor extractor = extractors.get(extractor_index);
            if(extractor.getFeatureIndexOffset() == 0)
                continue;

            //get the list whose elements contain the positional features of each sentence for the current extractor
            ArrayList<ArrayList<FeatureArray>> sentencesPositionalFeatures = extractorFeatures.get(extractor_index);
            //loop over each sentence
            for(int sentence_index = 0; sentence_index < sentences.size(); sentence_index++) {
                ArrayList<FeatureArray> features = sentencesPositionalFeatures.get(sentence_index);
                for (FeatureArray array : features)
                {
                    array.addOffset(extractor.getFeatureIndexOffset());
                }
            }
        }

        //attach to sentence
        for(int extractor_index = 0; extractor_index < extractors.size(); extractor_index++)
        {
            //get the list whose elements contain the positional features of each sentence for the current extractor
            ArrayList<ArrayList<FeatureArray>> sentencesPositionalFeatures = extractorFeatures.get(extractor_index);
            //loop over each sentence
            for(int sentence_index = 0; sentence_index < sentences.size(); sentence_index++) {
                //list which contain positional features of the sentence
                sentences.get(sentence_index).concatFeatures(sentencesPositionalFeatures.get(sentence_index),
                        extractor_index==0);
            }
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
