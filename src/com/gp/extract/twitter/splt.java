package com.gp.extract.twitter;

import com.gp.extract.twitter.labeler.Trainer;
import com.gp.extract.twitter.labeler.sequence.Sentence;
import com.gp.extract.twitter.labeler.sequence.Tags;
import com.gp.extract.twitter.util.IOUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by tarab on 1/27/2016.
 */
public class splt {
    public static void main(String[] args) throws IOException {
        Tags tags = new Tags("","o","o");
        ArrayList<Sentence> sentences1  = Trainer.readAnnotatedSentences(Configuration.Task.CHUNKER,
                "data/chunk_train.txt", tags);
        ArrayList<Sentence> sentences2  = Trainer.readAnnotatedSentences(Configuration.Task.CHUNKER,
                "data/chunk_test.txt", tags);

//        int training_size = (int)Math.ceil(sentences.size() * 0.75);
//
//        String [] files = {"data/chunk_train.txt","data/chunk_test.txt"};
//        int [][] range = {{0,training_size},{training_size,sentences.size()}};
//        int [] sizes = {0,0};
//        for(int job = 0; job < 2; job++)
//        {
//            BufferedWriter bufferedWriter = null;
//            try
//            {
//                bufferedWriter = new BufferedWriter(IOUtil.getUTF8FileWriter(files[job], true));
//                PrintWriter printWriter = new PrintWriter(bufferedWriter);
//                for(int i = range[job][0] ; i < range[job][1]; i++)
//                {
//                    Sentence sentence = sentences.get(i);
//                    for(int position = 0; position< sentence.getSize();position++)
//                    {
//                        sizes[job]++;
//                        printWriter.printf("%s\t%s",sentence.getWord(position),
//                                tags.getTagSymbolById(sentence.getTag(Configuration.Task.CHUNKER, position, tags)));
//                        printWriter.println();
//                    }
//                    printWriter.println();
//                }
//            }
//            catch (IOException e)
//            {
//                throw e;
//            }
//            finally {
//                if(bufferedWriter != null)
//                {
//                    bufferedWriter.close();
//                }
//            }
//        }


//        System.out.println("Training: "+ sizes[0] + " tokens and " + training_size + " sentences");
//        System.out.println("Testing: "+ sizes[1] + " tokens and " + (sentences.size() - training_size)+ " sentences");
        System.out.println();
    }
}
