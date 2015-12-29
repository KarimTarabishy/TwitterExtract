package com.gp.extract.twitter.pipeline;

import com.gp.extract.twitter.labeler.sequence.Sentence;

/**
 * Created by tarab on 12/25/2015.
 */
public class Pipeline {
    private static Pipeline pipeline;

    private Pipeline(){}

    public static Pipeline getPipeline()
    {
        if(pipeline == null)
        {
            pipeline = new Pipeline();
        }
        return pipeline;
    }



    public void process(String text)
    {

    }
}
