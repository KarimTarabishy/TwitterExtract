package com.gp.extract.twitter.pipeline;

import com.gp.extract.twitter.Configuration;
import com.gp.extract.twitter.labeler.sequence.Sentence;
import com.gp.extract.twitter.pipeline.taggers.Chunker;
import com.gp.extract.twitter.pipeline.taggers.POSTaggerMEMM;
import com.gp.extract.twitter.pipeline.taggers.Tagger;
import com.gp.extract.twitter.util.IOUtil;
import edu.stanford.nlp.util.StringUtils;

import java.util.ArrayList;
import java.util.EnumMap;


public class Pipeline implements IOUtil.Logger{
    private static Pipeline pipeline;
    private  EnumMap<Configuration.Task, Tagger> taggers = new EnumMap<Configuration.Task, Tagger>
            (Configuration.Task.class);

    private Pipeline(){}

    public static Pipeline getPipeline()
    {
        if(pipeline == null)
        {
            pipeline = new Pipeline();
            pipeline.setup();
        }
        return pipeline;
    }


    public void setup()
    {
        taggers.put(Configuration.Task.POS,new POSTaggerMEMM(Configuration.getTaggerFolder(Configuration.Task.POS)));
        taggers.put(Configuration.Task.CHUNKER,new Chunker(Configuration.getTaggerFolder(Configuration.Task.CHUNKER)));
    }

    public double train(Configuration.Task task)
    {
        double accuracy = 0;
        Tagger tagger = taggers.get(task);
        //Train and test the POS model
        tagger.train();
        accuracy = tagger.test();
        IOUtil.showInfo(tagger,"accuracy: "+ accuracy*100 + "%\n");

        return accuracy;
    }


    public double load(Configuration.Task task,boolean getResults)
    {
        Tagger tagger = taggers.get(task);
        tagger.load();
        if(getResults)
        {
            double accuracy = tagger.test();
            IOUtil.showInfo(tagger," accuracy: "+ accuracy*100 + "%\n");
            return accuracy;
        }
        return 0;

    }
    public void process(String text)
    {
        ArrayList<String> words = (ArrayList<String>) Twokenize.tokenizeRawTweetText(text);
        Sentence sentence = new Sentence(getPOSTagger().getTask(), words, null, null);

        //POS
        getPOSTagger().predict(sentence);
        //CHUNK
        getChunker().predict(sentence);
        //CAP

        for(int position = 0; position < sentence.getSize(); position++)
        {
            System.out.println(StringUtils.pad(sentence.getWord(position),30) + " \t -> " +
                    StringUtils.pad(getPOSTagger().getTagSymbol(sentence.getTag(getPOSTagger().getTask(),
                            position, null)),10) +  " \t -> " +
                    getChunker().getTagSymbol(sentence.getTag(getChunker().getTask(), position, null)));
        }

        System.out.println("\n\n\n");
    }

    public Tagger getPOSTagger(){return taggers.get(Configuration.Task.POS);}
    public  Tagger getChunker(){return taggers.get(Configuration.Task.CHUNKER);}

    @Override
    public String getLogId() {
        return "Pipeline";
    }
}
