package gp.twitter.extract.features;

import gp.twitter.extract.preprocess.Twokenize;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * Created by stc on 12/16/2015.
 */
public class FeatureUtil {
    public static Pattern URL = Pattern.compile(Twokenize.OR(Twokenize.url, Twokenize.Email));
    public static Pattern justbase = Pattern.compile("(?!www\\.|ww\\.|w\\.|@)[a-zA-Z0-9]+\\.[A-Za-z0-9\\.]+");

    public static String normalize(String str) {
        str = str.toLowerCase();
        if (URL.matcher(str).matches()){
            String base = "";
            Matcher m = justbase.matcher(str);
            if (m.find())
                base=m.group().toLowerCase();
            return "<URL-"+base+">";
        }
        if (Regex.VALID_MENTION_OR_LIST.matcher(str).matches())
            return "<@MENTION>";
        return str;
    }



}
