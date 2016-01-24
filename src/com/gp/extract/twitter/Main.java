package com.gp.extract.twitter;

import com.gp.extract.twitter.pipeline.Pipeline;
import com.gp.extract.twitter.pipeline.taggers.POSTaggerMEMM;
import com.gp.extract.twitter.pipeline.taggers.Tagger;
import com.gp.extract.twitter.stream.Streamer;

import java.io.IOException;


public class Main {

    public static final boolean train = true;
    public static void main(String[] args) throws IOException {
        if(train)
        {
            Pipeline.getPipeline().train();
        }
        else
        {
            Pipeline.getPipeline().load(true);
        }

        System.out.println();
        Streamer.start_stream();

    }
}
