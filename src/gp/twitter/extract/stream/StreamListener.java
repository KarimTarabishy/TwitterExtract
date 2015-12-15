package gp.twitter.extract.stream;


import gp.twitter.extract.util.IOUtil;
import twitter4j.*;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Scanner;


public class StreamListener implements StatusListener {
    private TwitterStream twitterStream;
    int counter = 0;
    String currentTweetsFile = "tweets_file_";
    int currentFileIndex = 0;
    int maxTweetsCountPerFile = 1000;
    BufferedWriter tweetsFileIO;

    public StreamListener(TwitterStream twitterStream) throws IOException {
        this.twitterStream = twitterStream;
        tweetsFileIO = new BufferedWriter(createTweetsFile());
    }

    /**
     * Called when a tweet arrives, we write maxTweetsCountPerFile in each file.
     * @param status
     */
    public void onStatus(Status status) {
        String text = status.getText();
        /* Todo:
            when reached maximum make new thread to process the current file
            then create a new file to put the new tweets in
         */
        try {
            tweetsFileIO.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
        counter++;
        if(counter == maxTweetsCountPerFile )
            twitterStream.shutdown();
    }

   
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
    }

    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
        System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
    }

   
    public void onScrubGeo(long userId, long upToStatusId) {
        System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
    }

    
    public void onStallWarning(StallWarning warning) {
        System.out.println("Got stall warning:" + warning);
    }

    public void onException(Exception ex) {
        ex.printStackTrace();
    }

    private OutputStreamWriter createTweetsFile() throws IOException {
        return new OutputStreamWriter(
                new FileOutputStream(
                        IOUtil.ensureFileExist(currentTweetsFile + Integer.toString(currentFileIndex) + ".txt")
                ),  Charset.forName("UTF-8").newEncoder() );
    }





}
