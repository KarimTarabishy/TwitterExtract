package com.gp.extract.twitter.pipeline;

import com.gp.extract.twitter.Configuration;
import com.gp.extract.twitter.labeler.sequence.Sentence;
import com.gp.extract.twitter.pipeline.taggers.POSTaggerMEMM;
import com.gp.extract.twitter.pipeline.taggers.Tagger;
import com.gp.extract.twitter.util.IOUtil;
import edu.stanford.nlp.util.StringUtils;

import java.util.ArrayList;
import java.util.EnumMap;


public class Pipeline implements IOUtil.Logger{
    private static Pipeline pipeline;
    private Tagger POSTagger;
    private Tagger chunker;

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
        POSTagger = new POSTaggerMEMM(Configuration.getTaggerFolder(Configuration.Task.POS));
    }

    public EnumMap<Configuration.Task, Double> train()
    {
        EnumMap<Configuration.Task, Double> results = new
                EnumMap<Configuration.Task, Double>(Configuration.Task.class);

        //Train and test the POS model
        POSTagger.train();
        double accuracy = POSTagger.test();
        IOUtil.showInfo(this,"accuracy: "+ accuracy*100 + "%\n");
        results.put(Configuration.Task.POS, accuracy);

        //TODO: Chunker

        return results;
    }


    public EnumMap<Configuration.Task, Double> load(boolean getResults)
    {
        EnumMap<Configuration.Task, Double> results = new
                EnumMap<Configuration.Task, Double>(Configuration.Task.class);
        POSTagger.load();
        if(getResults)
        {
            double accuracy = POSTagger.test();
            IOUtil.showInfo(this,"POS accuracy: "+ accuracy*100 + "%\n");
            results.put(Configuration.Task.POS, accuracy);
        }

        return results;

    }
    public void process(String text)
    {
        ArrayList<String> words = (ArrayList<String>) Twokenize.tokenizeRawTweetText(text);
        Sentence sentence = new Sentence(POSTagger.getTask(), words, null, null);
        POSTagger.predict(sentence);

        for(int position = 0; position < sentence.getSize(); position++)
        {
            System.out.println(StringUtils.pad(sentence.getWord(position),30) + " \t -> " +
                    POSTagger.getTagSymbol(sentence.getTag(POSTagger.getTask(), position, null)));
        }

        System.out.println("\n\n\n");
    }

    public Tagger getPOSTagger(){return POSTagger;}

    @Override
    public String getLogId() {
        return "Pipeline";
    }
}
