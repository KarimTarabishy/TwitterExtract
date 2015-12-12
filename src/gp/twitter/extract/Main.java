package gp.twitter.extract;


import java.io.File;
import java.io.IOException;


import twitter4j.*;
import twitter4j.auth.AccessToken;


public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        AccessToken accessToken = new AccessToken("425782963-onTpAu5ayVW8jeuAGzxiPYbWCqm7eWWdBC2sQaaU",
                "2ZHfxkmyvwCXEYrfSRbyGViCrwKLJP5K2QgZHbDyGzpET");
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.setOAuthConsumer("LIpR6PCEF8IJkre7Bq8RqKfKN", "XrsuBpMGTrR7vfnWuxoiDsO8J39wlDE6q3nUX7m4HkHpqLKKAb");
        twitterStream.setOAuthAccessToken(accessToken);

        StreamListener listener = new StreamListener(twitterStream);
        twitterStream.addListener(listener);
        String [] track = {"political"};
        String [] language = {"en"};
        twitterStream.filter(new FilterQuery(0,null,track,null,language ));


    }

}
