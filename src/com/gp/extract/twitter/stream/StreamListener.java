package com.gp.extract.twitter.stream;


import com.gp.extract.twitter.Main;
import com.gp.extract.twitter.util.IOUtil;
import twitter4j.*;

import java.io.BufferedWriter;
import java.io.IOException;


public class StreamListener implements StatusListener, IOUtil.Logger {
    private TwitterStream twitterStream;
    private int counter = 0;
    private String currentTweetsFile = "tweets_file_";
    private static  final String LOGGER_ID = StreamListener.class.getName();
    private int currentFileIndex = 0;
    private int maxTweetsCountPerFile = 1000;
    private BufferedWriter tweetsFileIO;
    private int limit;

    public StreamListener(TwitterStream twitterStream, int limit) throws IOException {
        this.twitterStream = twitterStream;
        this.limit = limit;
        //tweetsFileIO = new BufferedWriter(IOUtil.getUTF8FileWriter(getCurrentTweetsFileName(), true));
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

        counter++;
        if(counter == limit)
        {
            twitterStream.shutdown();
            return;
        }
        Main.pipeline(text);

//        try {
//            tweetsFileIO.write(text);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        counter++;
//        if(counter == maxTweetsCountPerFile ) {
//            try {
//                tweetsFileIO.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            twitterStream.shutdown();
//        }
    }

   
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        IOUtil.showError(this,"Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
    }

    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
        IOUtil.showError(this,"Got track limitation notice:" + numberOfLimitedStatuses);
    }

   
    public void onScrubGeo(long userId, long upToStatusId) {
        IOUtil.showError(this,"Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
    }

    
    public void onStallWarning(StallWarning warning) {
        IOUtil.showError(this,"Got stall warning:" + warning);
    }

    public void onException(Exception ex) {
        ex.printStackTrace();
    }


    private String getCurrentTweetsFileName()
    {
        return currentTweetsFile+Integer.toString(currentFileIndex) + ".txt";
    }


    @Override
    public String getLogId() {
        return LOGGER_ID;
    }
}
