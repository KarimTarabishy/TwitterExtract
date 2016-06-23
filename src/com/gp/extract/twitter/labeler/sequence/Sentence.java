package com.gp.extract.twitter.labeler.sequence;

import com.gp.extract.twitter.Configuration;
import com.gp.extract.twitter.labeler.features.FeatureArray;

import java.util.ArrayList;
import java.util.EnumMap;

public class Sentence {
    private ArrayList<String> words;
    private EnumMap<Configuration.Task, ArrayList<Integer>> tags;
    private ArrayList<FeatureArray> observationalFeatures;
    private double capitalizeFeature = 0;

    public Sentence(Configuration.Task task, ArrayList<String> words, ArrayList<String> tagSymbols, Tags tagsSet)
    {
        tags = new EnumMap<Configuration.Task, ArrayList<Integer>>(Configuration.Task.class);
        this.words = words;

        for(Configuration.Task t : Configuration.Task.values())
        {
            ArrayList<Integer> tt = new ArrayList<>(words.size());
            for(int i = 0; i < words.size(); i++)
            {
                tt.add(null);
            }
            tags.put(t,tt);
        }

        ArrayList<Integer> _tags = tags.get(task);
        if(tagSymbols!= null)
        {
            for(int i = 0; i < words.size(); i++)
            {
               _tags.set(i,tagsSet.getTagIDBySymbol(tagSymbols.get(i)));
            }
        }



    }


    public int getSize()
    {
        return words.size();
    }

    public void setCapitalizeFeature(double value)
    {
        capitalizeFeature = value;
    }
    public double getCapitalizeFeature()
    {
        return capitalizeFeature;
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

    public void setFeatures(ArrayList<FeatureArray> features)
    {
        concatFeatures(features, true);
    }
    public int getTag(Configuration.Task task, int position, Tags tagSet)
    {
        if(position < 0)
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
