package gp.twitter.extract.labeler.tags;

import gp.twitter.extract.util.IOUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class POSTags implements Tags {
    private  Map<String,Tag> symbolToTag ;
    private ArrayList<Tag> idToTag;
    private static String startTagSymbol = "POS_START_TAG";
    private static Tag startTag = new Tag(-1, startTagSymbol);

    /**
     * Construct POS tags using the given file. File is assumed to have a separate  tag symbol in each line
     * @param tags_file
     */
    public POSTags(String tags_file) throws IOException {
        //get reader to the tags file
        BufferedReader bufferedReader = new BufferedReader(IOUtil.getUTF8FileReader(tags_file));

        //read line by line
        String line;
        int index = 0;
        while ((line = bufferedReader.readLine()) != null)
        {
            //trim the line
            line.trim();
            //create a tag object for this symbol
            Tag tag = new Tag(index, line);
            //then add to the tags dictionary
            symbolToTag.put(tag.getSymbol(), tag);
            //and add to tags array
            idToTag.add(index, tag);

            //increment index
            index++;
        }
    }

    @Override
    public Tag getTagBySymbol(String symbol) {
        //check if it is the start tag as it is not put in the tags dictionary
        if(symbol.equals(startTagSymbol)) {
            return startTag;
        }
        //try to get the tag from the tags dictionary
        Tag tag = symbolToTag.get(symbol);

        //if tag not found then throw an exception
        if(tag == null) {
            throw new RuntimeException("Tag with symbol " + symbol + " was not found.");
        }
        return tag;
    }

    @Override
    public Tag getTagById(int id) {
        //check for the start tag as it is not saved in the tags arraylist
        if(id == -1) {
            return startTag;
        }

        //check if out of bound
        if(id < 0 || id >= idToTag.size()){
            throw new RuntimeException("Tag with id " + Integer.toString(id) + " was not found.");
        }

        return idToTag.get(id);
    }

    @Override
    public Tag getStartTag() {
        return startTag;
    }

    @Override
    public int getSize()
    {
        return idToTag.size();
    }
}
