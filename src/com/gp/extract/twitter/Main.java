package com.gp.extract.twitter;

import com.gp.extract.twitter.pipeline.Pipeline;
import com.gp.extract.twitter.stream.Streamer;

import java.io.IOException;


public class Main {

    public static final boolean train = true;
    public static void main(String[] args) throws IOException {
        Pipeline pipeline = Pipeline.getPipeline();
        pipeline.load(Configuration.Task.POS, true);
        pipeline.load(Configuration.Task.CHUNKER,true);

        System.out.println();
        Streamer.start_stream();
    }
}
