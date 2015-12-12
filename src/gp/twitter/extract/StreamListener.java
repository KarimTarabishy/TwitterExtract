package gp.twitter.extract;


import twitter4j.*;

import java.util.List;



public class StreamListener implements StatusListener {
    private TwitterStream twitterStream;
    int counter = 0;
    private Twokenize twokenize;

    public StreamListener(TwitterStream twitterStream)
    {
        this.twitterStream = twitterStream;
        twokenize  = new Twokenize();
    }

 
    public void onStatus(Status status) {
        String text = status.getText();
        System.out.println(status.getText());
        System.out.println("\nTOKENS:\n");
        List<String> tokens = twokenize.tokenizeRawTweetText(text);

        for (int i=0; i<tokens.size(); i++) {
            System.out.println(tokens.get(i));
        }

        System.out.println("----------------------------------------------------------\n");

        counter++;
        if(counter ==20 )
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






}
