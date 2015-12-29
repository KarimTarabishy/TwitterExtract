package com.gp.extract.twitter;

import com.gp.extract.twitter.labeler.sequence.Sentence;
import com.gp.extract.twitter.pipeline.POSTaggerMEMM;
import com.gp.extract.twitter.pipeline.Twokenize;
import com.gp.extract.twitter.stream.Streamer;
import edu.stanford.nlp.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;


public class Main {

    public static final boolean train = true;
    private static POSTaggerMEMM tagger = new POSTaggerMEMM("pos_model/");
    public static void main(String[] args) throws IOException {
        if(train)
        {
            tagger.train("data/oct27.conll");
            double accuracy = tagger.testTagger("data/daily547.conll");
            System.out.printf("accuracy: %2.4f%%", accuracy*100);
        }
        else
        {
            tagger.load();
            double accuracy = tagger.testTagger("data/daily547.conll");
            System.out.printf("accuracy: %2.4f%%", accuracy*100);
        }

        Streamer.start_stream();

    }

    public static void pipeline(String txt)
    {
        ArrayList<String> words = (ArrayList<String>) Twokenize.tokenizeRawTweetText(txt);
        Sentence sentence = new Sentence(words, null, null);
        tagger.predict(sentence);

        for(int position = 0; position < sentence.getSize(); position++)
        {
            System.out.println(StringUtils.pad(sentence.getWord(position),30) + " \t -> " +
                    tagger.getTagSymbol(sentence.getTag(position, null)));
        }

        System.out.println("\n\n\n");

    }
}
