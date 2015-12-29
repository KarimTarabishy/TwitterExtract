package com.gp.extract.twitter.labeler.features;

import com.gp.extract.twitter.labeler.sequence.Sentence;
import com.gp.extract.twitter.util.IOUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public abstract class FeatureExtractor implements IOUtil.Loadable{

    protected int featureIndexOffset = 0;
    protected Map<String, Integer> featureMapping = new HashMap<>();
    private String saveDirectory;

    /**
     * Extract observational features from the sentence.
     * @param sentence the sentence to extract its features
     * @param output list of feature array for each position in the sentence
     * @param isTraining indicate if in training mode
     */
    public abstract void extract(Sentence sentence, ArrayList<FeatureArray> output, boolean isTraining);


    public void setSaveDirectory(String saveDirectory)
    {
        this.saveDirectory = saveDirectory;
    }

    public abstract String getDataFileName();

    public int getFeatureIndex(String feature, boolean isTraining)
    {
        //try to get the feature from the saved mapping
        Integer index = featureMapping.get(feature);
        //if it is not found
        if(index == null)
        {
            //then if we are not training
            if(!isTraining)
            {
                //make it -1 indicating not found
                index = -1;

            }
            else // we are training
            {
                //add the feature to the mapping
                index = featureMapping.size();
                //update the returned index
                featureMapping.put(feature, index);
            }
        }
        else
        {
            //in non training mode the offset should be used
            if(!isTraining)
                index += featureIndexOffset;
        }
        return index;

    }
    public int getFeaturesSize()
    {
        return featureMapping.size();
    }

    public int getFeatureIndexOffset() {
        return featureIndexOffset;
    }

    public void setFeatureIndexOffset(int offset)
    {
        this.featureIndexOffset = offset;
    }

    Map<String, Integer> getFeatureMapping(){return featureMapping;}

    public void disableFeature(String name)
    {
        featureMapping.remove(name);
    }

    @Override
    public void load() throws IOException {
        String file = saveDirectory + getDataFileName();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(IOUtil.getUTF8FileReader(file, false));
            Scanner scanner = new Scanner(bufferedReader);
            String line;
            //read feature index offset
            featureIndexOffset = scanner.nextInt();
            scanner.nextLine();
            //read map
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                line = line.trim();
                if(line.isEmpty())
                    continue;

                String[] data = line.split("\\s");
                featureMapping.put(data[0], Integer.parseInt(data[1]));
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

    }

    @Override
    public void save() throws IOException {
        String file = saveDirectory + getDataFileName();
        BufferedWriter bufferedWriter = null;
        try
        {
            bufferedWriter = new BufferedWriter(IOUtil.getUTF8FileWriter(file, true));
            PrintWriter printWriter = new PrintWriter(bufferedWriter);
            //write offset
            printWriter.println(featureIndexOffset);
            //start writing map data
            for(Map.Entry<String,Integer> entry : featureMapping.entrySet())
            {
                printWriter.printf("%s\t%d",entry.getKey(), entry.getValue());
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
}
