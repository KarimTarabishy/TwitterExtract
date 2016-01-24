package com.gp.extract.twitter.labeler.sequence;

import com.gp.extract.twitter.Configuration;
import com.gp.extract.twitter.labeler.features.FeatureArray;

import java.util.ArrayList;
import java.util.EnumMap;

public class Sentence {
    private ArrayList<String> words;
    private EnumMap<Configuration.Task, ArrayList<Integer>> tags;
    private ArrayList<FeatureArray> observationalFeatures;

    public Sentence(Configuration.Task task, ArrayList<String> words, ArrayList<String> tagSymbols, Tags tagsSet)
    {
        tags = new EnumMap<Configuration.Task, ArrayList<Integer>>(Configuration.Task.class);
        this.words = words;

        for(Configuration.Task t : Configuration.Task.values())
        {
            tags.put(t,new ArrayList<>(words.size()));
        }

        ArrayList<Integer> _tags = tags.get(task);
        if(tagSymbols!= null)
        {
            for(String tagSymbol : tagSymbols)
            {
               _tags.add(tagsSet.getTagIDBySymbol(tagSymbol));
            }
        }
        else
        {
            for(int i = 0; i < words.size(); i++)
            {
                _tags.add(i, -1);
            }
        }


    }


    public int getSize()
    {
        return words.size();
    }


    public void concatFeatures(ArrayList<FeatureArray> features, boolean starting)
    {
        if(starting)
        {
            observationalFeatures = features;
        }
        else
        {
            for(int i = 0 ; i < observationalFeatures.size(); i++)
            {
                observationalFeatures.get(i).concat(features.get(i));
            }
        }
    }

    public int getTag(Configuration.Task task, int position, Tags tagSet)
    {
        if(position == -1)
        {
            return tagSet.getStartTagId();
        }
        return tags.get(task).get(position);
    }

    public FeatureArray getObservationalFeatureArray(int position)
    {
        return observationalFeatures.get(position);
    }

    public ArrayList<FeatureArray> getAllObservationalFeatureArrays(){
        return observationalFeatures;
    }
    public String getWord(int position)
    {
        return words.get(position);
    }

    public ArrayList<Integer> getTags(Configuration.Task task)
    {
        return tags.get(task);
    }

    public void setTag(Configuration.Task task,int position, int tag_index)
    {
        tags.get(task).set(position, tag_index);
    }

}
