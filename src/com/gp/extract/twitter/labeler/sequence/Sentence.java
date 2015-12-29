package com.gp.extract.twitter.labeler.sequence;

import com.gp.extract.twitter.labeler.features.FeatureArray;

import java.util.ArrayList;

public class Sentence {
    private ArrayList<String> words;
    private ArrayList<Integer> tags;
    private ArrayList<FeatureArray> observationalFeatures;

    public Sentence(ArrayList<String> words, ArrayList<String> tagSymbols, Tags tagsSet)
    {
        this.words= words;
        tags = new ArrayList<>(words.size());

        if(tagSymbols!= null)
        {
            for(String tagSymbol : tagSymbols)
            {
                tags.add(tagsSet.getTagIDBySymbol(tagSymbol));
            }
        }
        else
        {
            for(int i = 0; i < words.size(); i++)
            {
                tags.add(i, -1);
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

    public int getTag(int position, Tags tagSet)
    {
        if(position == -1)
        {
            return tagSet.getStartTagId();
        }
        return tags.get(position);
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

    public ArrayList<Integer> getTags()
    {
        return tags;
    }

    public void setTag(int position, int tag_index)
    {
        tags.set(position, tag_index);
    }

}
