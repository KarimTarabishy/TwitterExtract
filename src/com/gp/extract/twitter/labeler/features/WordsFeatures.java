package com.gp.extract.twitter.labeler.features;

import com.gp.extract.twitter.labeler.sequence.Sentence;
import com.gp.extract.twitter.pipeline.Twokenize;
import com.twitter.Regex;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tarab on 12/21/2015.
 */
public class WordsFeatures {
    public static Pattern URL = Pattern.compile(Twokenize.OR(Twokenize.url, Twokenize.Email));
    public static Pattern justbase = Pattern.compile("(?!www\\.|ww\\.|w\\.|@)[a-zA-Z0-9]+\\.[A-Za-z0-9\\.]+");


    public static class WordFormFeatures extends FeatureExtractor{
        public static final String FILE_NAME = WordFormFeatures.class.getSimpleName() + ".txt";
        @Override
        public void extract(Sentence sentence, ArrayList<FeatureArray> output, boolean isTraining) {

            for(int position = 0; position< sentence.getSize();position++)
            {
                FeatureArray array = new FeatureArray();
                String word = sentence.getWord(position);
                String quotationFixed = WordsFeatures.fixQuotations(word);
                array.add(getFeatureIndex("word|"+quotationFixed, isTraining));
                array.add(getFeatureIndex("lower|"+WordsFeatures.lowerNormalizeString(quotationFixed), isTraining));
                array.add(getFeatureIndex("xxdShape|" + Xxdshape(quotationFixed), isTraining), 0.5);
                array.add(getFeatureIndex("charclass|" + charclassshape(word), isTraining), 0.5);

                output.add(position, array);
            }
        }

        @Override
        public String getDataFileName() {
            return FILE_NAME;
        }

        private String Xxdshape(String tok) {
            String s=tok.replaceAll("[a-z]", "x").replaceAll("[0-9]", "d").replaceAll("[A-Z]","X");
            return s;
        }

        private String charclassshape(String tok) {
            StringBuilder sb = new StringBuilder(3 * tok.length());
            for(int i=0; i<tok.length(); i++){
                sb.append(Character.getType(tok.codePointAt(i))).append(',');
            }
            return sb.toString();
        }
    }


    public static class NgramPrefix extends FeatureExtractor {
        private int ngram = 3;
        public static final String FILE_NAME = NgramPrefix.class.getSimpleName() + ".txt";
        public NgramPrefix(int ngram)
        {
            this.ngram = ngram;
        }

        @Override
        public void extract(Sentence sentence, ArrayList<FeatureArray> output, boolean isTraining) {
            for(int position = 0; position< sentence.getSize();position++)
            {
                FeatureArray array = new FeatureArray();
                String norm_word = lowerNormalizeString(WordsFeatures.fixQuotations(sentence.getWord(position)));
                int word_length = norm_word.length();
                for(int prefix_len = 1; prefix_len <= ngram; prefix_len++)
                {
                    if(word_length >= prefix_len)
                    {
                        array.add(getFeatureIndex(prefix_len+"glramPref|" + norm_word.substring(0,prefix_len),
                                isTraining));
                    }
                    else break;
                }
                output.add(position, array);
            }
        }

        @Override
        public String getDataFileName() {
            return FILE_NAME;
        }
    }

    public static class NgramSuffix extends FeatureExtractor {
        private int ngram = 3;
        public static final String FILE_NAME = NgramSuffix.class.getSimpleName() + ".txt";
        public NgramSuffix(int ngram)
        {
            this.ngram = ngram;
        }

        @Override
        public void extract(Sentence sentence, ArrayList<FeatureArray> output, boolean isTraining) {
            for(int position = 0; position< sentence.getSize();position++)
            {
                FeatureArray array = new FeatureArray();
                String norm_word = lowerNormalizeString(WordsFeatures.fixQuotations(sentence.getWord(position)));
                int word_length = norm_word.length();
                for(int prefix_len = 1; prefix_len <= ngram; prefix_len++)
                {
                    if(word_length >= prefix_len)
                    {
                        array.add(getFeatureIndex(prefix_len+"glramPref|" +
                                norm_word.substring(word_length-prefix_len,word_length),
                                isTraining));
                    }
                    else break;
                }
                output.add(position, array);
            }
        }

        @Override
        public String getDataFileName() {
            return FILE_NAME;
        }
    }


    public static class NextWord extends FeatureExtractor {
        public static final String FILE_NAME = NextWord.class.getSimpleName() + ".txt";

        @Override
        public void extract(Sentence sentence, ArrayList<FeatureArray> output, boolean isTraining) {
            for(int position = 0; position< sentence.getSize()-1;position++)
            {
                String nextWord = sentence.getWord(position+1);
                String nextNormWord = lowerNormalizeString(nextWord);
                String curNormWord = lowerNormalizeString(sentence.getWord(position));
                FeatureArray array = new FeatureArray();
                array.add(getFeatureIndex("nextword|"+nextWord,isTraining), 0.5);
                array.add(getFeatureIndex("nextword|"+nextNormWord,isTraining), 0.5);
                array.add(getFeatureIndex("curnext|"+curNormWord+"|"+nextNormWord,isTraining));
                output.add(position, array);
            }
            FeatureArray array = new FeatureArray();
            array.add(getFeatureIndex("curnext|"+lowerNormalizeString(sentence.getWord(sentence.getSize()-1))
                    +"|<END>",isTraining));
            array.add(getFeatureIndex("nextword|<END>",isTraining));
            output.add(array);
        }

        @Override
        public String getDataFileName() {
            return FILE_NAME;
        }
    }

    public static class PrevWord extends FeatureExtractor {
        public static final String FILE_NAME = PrevWord.class.getSimpleName() + ".txt";

        @Override
        public void extract(Sentence sentence, ArrayList<FeatureArray> output, boolean isTraining) {
            FeatureArray array = new FeatureArray();
            array.add(getFeatureIndex("prevCur|<START>|"+lowerNormalizeString(sentence.getWord(0))
                    ,isTraining));
            array.add(getFeatureIndex("prevword|<START>",isTraining));
            output.add(0,array);
            for(int position = 1; position < sentence.getSize();position++)
            {
                String prevWord = sentence.getWord(position-1);
                String prevNormWord = lowerNormalizeString(prevWord);
                String curNormWord = lowerNormalizeString(sentence.getWord(position));
                array = new FeatureArray();
                array.add(getFeatureIndex("prevword|"+prevWord,isTraining));
                array.add(getFeatureIndex("prevword|"+prevNormWord,isTraining));
                array.add(getFeatureIndex("curnext|"+prevNormWord+"|"+curNormWord,isTraining));
                output.add(position, array);
            }

        }

        @Override
        public String getDataFileName() {
            return FILE_NAME;
        }
    }


    public static class Positional extends FeatureExtractor {
        public static final String FILE_NAME = Positional.class.getSimpleName() + ".txt";

        @Override
        public void extract(Sentence sentence, ArrayList<FeatureArray> output, boolean isTraining) {

            //fill output
            for(int i = 0; i < sentence.getSize(); i++)
            {
                output.add(new FeatureArray());
            }

            for(int position = 0; position < Math.min(sentence.getSize(),4);position++)
            {
                FeatureArray array = output.get(position);
                array.add(getFeatureIndex("t="+position,isTraining));
            }
            for (int position=sentence.getSize()-1; position > Math.max(sentence.getSize()-4, -1); position--) {
                FeatureArray array = output.get(position);
                array.add(getFeatureIndex("t=-"+position,isTraining));
            }
        }

        @Override
        public String getDataFileName() {
            return FILE_NAME;
        }
    }


    public static class Orthographical extends FeatureExtractor {
        public static final String FILE_NAME = Orthographical.class.getSimpleName() + ".txt";
        private static Pattern hasDigit = Pattern.compile("[0-9]");
        private static Pattern allPunct = Pattern.compile("^\\W*$");
        private static Pattern emoticon = Pattern.compile(Twokenize.emoticon);
        @Override
        public void extract(Sentence sentence, ArrayList<FeatureArray> output, boolean isTraining) {

            for(int position = 0; position < sentence.getSize();position++)
            {
                FeatureArray array = new FeatureArray();
                String word = sentence.getWord(position);

                if (hasDigit.matcher(word).find())
                    array.add(getFeatureIndex("ortho|HasDigit",isTraining));

                if (word.charAt(0) == '@')
                    array.add(getFeatureIndex("ortho|InitAt",isTraining));

                if (word.charAt(0) == '#')
                    array.add(getFeatureIndex("ortho|InitHash",isTraining));

                if (emoticon.matcher(word).matches()){
                    array.add(getFeatureIndex("ortho|Emoticon",isTraining));
                }
                if (word.contains("-")){
                    array.add(getFeatureIndex("ortho|Hyphenated",isTraining));
                    String[] splithyph = lowerNormalizeString(word).split("-", 2);
                    for (String part:splithyph){
                        array.add(getFeatureIndex("ortho|hyph|" + part,isTraining));
                    }
                }
                output.add(position, array);
            }

        }

        @Override
        public String getDataFileName() {
            return FILE_NAME;
        }
    }


    public static class URL extends FeatureExtractor {
        public static final String FILE_NAME = URL.class.getSimpleName() + ".txt";
        private static Pattern validURL = Pattern.compile(Twokenize.url);
        private static Pattern validEmail = Pattern.compile(Twokenize.Email);
        @Override
        public void extract(Sentence sentence, ArrayList<FeatureArray> output, boolean isTraining) {

            for(int position = 0; position < sentence.getSize();position++)
            {
                FeatureArray array = new FeatureArray();
                String word = sentence.getWord(position);

                if (validURL.matcher(word).matches() || validEmail.matcher(word).matches()){
                    array.add(getFeatureIndex("URL|validURL" ,isTraining));
                }
                output.add(position, array);
            }

        }

        @Override
        public String getDataFileName() {
            return FILE_NAME;
        }
    }



    public static class PrevNext extends FeatureExtractor {
        public static final String FILE_NAME = PrevNext.class.getSimpleName() + ".txt";

        @Override
        public void extract(Sentence sentence, ArrayList<FeatureArray> output, boolean isTraining) {
            //fill output
            for(int i = 0; i < sentence.getSize(); i++)
            {
                output.add(new FeatureArray());
            }

            if (sentence.getSize()>1){
                output.get(0).add(getFeatureIndex("prevnext|<START>|"+
                        lowerNormalizeString(sentence.getWord(1)),isTraining));
                int position=1;
                for (; position < sentence.getSize()-1; position++) {
                    output.get(position).add(getFeatureIndex("prevnext|"+lowerNormalizeString(sentence.getWord(position-1))
                            + "|" + lowerNormalizeString(sentence.getWord(position+1)),isTraining));
                }
                output.get(position).add(getFeatureIndex("prevnext|"+lowerNormalizeString(sentence.getWord(position-1))
                        + "|<END>",isTraining));

            }

        }

        @Override
        public String getDataFileName() {
            return FILE_NAME;
        }
    }

    public static class Capitalization extends FeatureExtractor {
        public static final String FILE_NAME = Capitalization.class.getSimpleName() + ".txt";

        @Override
        public void extract(Sentence sentence, ArrayList<FeatureArray> output, boolean isTraining) {
            //fill output
            for(int i = 0; i < sentence.getSize(); i++)
            {
                output.add(new FeatureArray());
            }


            for (int position = 0; position < sentence.getSize()-1; position++) {
                String word = sentence.getWord(position);
                int numChar = 0;
                int numCap = 0;
                for (int i=0; i < word.length(); i++) {
                    numChar += Character.isLetter(word.charAt(i)) ? 1 : 0;
                    numCap += Character.isUpperCase(word.charAt(i)) ? 1 : 0;
                }

                // A     => shortcap
                // HELLO => longcap
                // Hello => initcap
                // HeLLo => mixcap

                boolean allCap = numChar==numCap;
                boolean shortCap = allCap && numChar <= 1;
                boolean longCap  = allCap && numChar >= 2;
                boolean initCap = !allCap && numChar >= 2 && Character.isUpperCase(word.charAt(0)) && numCap==1;
                boolean mixCap = numCap>=1 && numChar >= 2 && (word.charAt(0) != '@') && !(word.startsWith("http://"));

                String caplabel = shortCap ? "shortcap" : longCap ? "longcap" : initCap ? "initcap"
                        : mixCap ? "mixcap" : "nocap";
                if (caplabel != null){
                    if (numChar >= 1) {
                        if (word.endsWith("'s"))
                            caplabel = "pos-" + caplabel;
                        else if (position==0)
                            caplabel = "first-" + caplabel;
                        output.get(position).add(getFeatureIndex(caplabel+"",isTraining));
                    }
                }
            }


            }

        @Override
        public String getDataFileName() {
            return FILE_NAME;
        }
    }

    public static String fixQuotations(String string)
    {
        return string.replaceAll("[‘’´`]", "'").replaceAll("[“”]", "\"");
    }

    public static String lowerNormalizeString(String string)
    {
        string = string.toLowerCase();
        if (URL.matcher(string).matches()){
            String base = "";
            Matcher m = justbase.matcher(string);
            if (m.find())
                base=m.group().toLowerCase();
            return "<URL-"+base+">";
        }
        if (Regex.VALID_MENTION_OR_LIST.matcher(string).matches())
            return "<@MENTION>";
        return string;
    }
}
