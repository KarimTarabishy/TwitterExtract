package com.gp.extract.twitter.labeler.features;

import java.util.ArrayList;
import java.util.Iterator;

public class FeatureArray implements Iterable<Feature>{

    private ArrayList<Feature> features = new ArrayList<>();
    private FeatureArray next = null;
    private FeatureArray last = this;
    private int size = 0;


    public int getSize()
    {
        return size;
    }

    public Feature getFeature(int index)
    {
        return features.get(index);
    }
    public void add(int index)
    {
        if(index == -1)
            return;
        size++;
        features.add(new Feature(index,1));
    }


    public void add(int index, double value)
    {
        if(index == -1)
            return;
        size++;
        features.add(new Feature(index,value));
    }

    public void addOffset(int offset)
    {
        for(Feature feature : features)
        {
            feature.index += offset;
        }
    }

    public void concat(FeatureArray other)
    {
        if(other.getSize() == 0)
        {
            return;
        }
        last.next = other;
        last = last.next;
        size += other.getSize();
    }


    @Override
    public Iterator<Feature> iterator() {
        Iterator<Feature> it = new Iterator<Feature>() {

            private int currentIndex = 0;
            private FeatureArray current = FeatureArray.this;

            @Override
            public boolean hasNext() {
                //if we reached the end of current array
                if(currentIndex == current.features.size())
                {
                    //check if exist another one
                    if(current.next == null)
                        return false;

                    current = current.next;
                    currentIndex = 0;
                }
                //we have a feature with none empty features
                return true;

            }

            @Override
            public Feature next() {
                return current.features.get(currentIndex++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
        return it;
    }
}
