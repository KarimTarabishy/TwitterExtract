package gp.twitter.extract.stream;

import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;

public class Streamer {
    private static AccessToken accessToken = new AccessToken("425782963-onTpAu5ayVW8jeuAGzxiPYbWCqm7eWWdBC2sQaaU",
            "2ZHfxkmyvwCXEYrfSRbyGViCrwKLJP5K2QgZHbDyGzpET");

    /**
     * Starts a twitter streaming thread
     */
    public static void start_stream() {
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.setOAuthConsumer("LIpR6PCEF8IJkre7Bq8RqKfKN", "XrsuBpMGTrR7vfnWuxoiDsO8J39wlDE6q3nUX7m4HkHpqLKKAb");
        twitterStream.setOAuthAccessToken(accessToken);

        StreamListener listener = new StreamListener(twitterStream);
        twitterStream.addListener(listener);
        twitterStream.sample("en"); //sample 1% from the Firehose continuously
    }
}
