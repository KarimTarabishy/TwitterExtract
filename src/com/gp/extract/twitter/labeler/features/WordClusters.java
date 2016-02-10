package com.gp.extract.twitter.labeler.features;

import com.gp.extract.twitter.labeler.sequence.Sentence;
import com.gp.extract.twitter.util.IOUtil;
import edu.stanford.nlp.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by tarab on 12/24/2015.
 */
public class WordClusters  extends FeatureExtractor{

    public static String DATA_FILE = "gazeteer/50mpaths2";
    public static final String FILE_NAME = WordClusters.class.getSimpleName() + ".txt";
    public static HashMap<String,String> wordToPath;

    public WordClusters() throws IOException {

        if(wordToPath == null)
        {
            BufferedReader bufferedReader = null;
            try
            {
                bufferedReader= new BufferedReader(IOUtil.getUTF8FileReader(DATA_FILE, false));
                String line = "";
                wordToPath = new HashMap<String,String>();
                while((line = bufferedReader.readLine()) != null){
                    String[] data = line.split("\\t");
                    wordToPath.put(data[1], data[0]);
                }
            }
            catch (IOException e)
            {
                throw e;
            }
            finally {
                if(bufferedReader != null)
                    bufferedReader.close();
            }
        }


    }

    @Override
    public void extract(Sentence sentence, ArrayList<FeatureArray> output, boolean isTraining) {
        //start extraction
        for(int position = 0; position< sentence.getSize();position++)
        {
            String word = WordsFeatures.lowerNormalizeString(sentence.getWord(position));
            String path = wordToPath.get(word);

            //check if we need to fuzzify the input word (normalize it in several ways)
            if(path == null)
            {
                //get all fuzzy forms
                ArrayList<String> fuzzyForms = fuzztoken(word, true);
                //search to see if any of the fuzzy forms exist in the clusters
                for(String fuzzyForm : fuzzyForms)
                {
                    //if exist break
                    if((path = wordToPath.get(fuzzyForm)) != null)
                        break;
                }
            }


            //lets populate the features if the path exist
            if(path != null)
            {
                path = StringUtils.pad(path, 16).replace(' ','0');
                FeatureArray current = output.get(position);
                //add all parent clusters too
                for(int i = 2; i <= 16; i+=2)
                {
                    current.add(getFeatureIndex("CurCluster|"+path.substring(0,i), isTraining));
                }

                //if this is not the first position
                if(position > 0)
                {
                    //add this cluster path as the next cluster path for the previous position
                    FeatureArray prev = output.get(position-1);
                    for(int i = 4; i<=12; i+= 4)
                    {
                        prev.add(getFeatureIndex("NextCluster|" + path.substring(0,i),isTraining));
                    }
                }

                //if not the last position
                if(position < sentence.getSize()-1)
                {
                    //add this cluster path as the previous cluster path for the next position
                    FeatureArray next = output.get(position+1);
                    for(int i = 4; i<=12; i+= 4)
                    {
                        next.add(getFeatureIndex("PrevCluster|" + path.substring(0,i),isTraining));
                    }
                }
            }
        }
    }

    static Pattern repeatchar = Pattern.compile("([\\w])\\1{1,}");
    static Pattern repeatvowel = Pattern.compile("(a|e|i|o|u)\\1+");
    public static ArrayList<String> fuzztoken(String tok, boolean apos) {
        ArrayList<String> fuzz = new ArrayList<String>();
        fuzz.add(tok.replaceAll("[‘’´`]", "'").replaceAll("[“”]", "\""));
        fuzz.add(tok);
        fuzz.add(repeatchar.matcher(tok).replaceAll("$1"));//omggggggg->omg
        fuzz.add(repeatchar.matcher(tok).replaceAll("$1$1"));//omggggggg->omgg
        fuzz.add(repeatvowel.matcher(tok).replaceAll("$1"));//heeellloooo->helllo
        if (apos && !(tok.startsWith("<URL"))){
            fuzz.add(tok.replaceAll("\\p{Punct}", ""));//t-swift->tswift
            //maybe a bad idea (bello's->bello, re-enable->re, croplife's->'s)
            fuzz.addAll(Arrays.asList(tok.split("\\p{Punct}")));
        }
        return fuzz;
    }

    @Override
    public String getDataFileName() {
        return FILE_NAME;
    }
}
