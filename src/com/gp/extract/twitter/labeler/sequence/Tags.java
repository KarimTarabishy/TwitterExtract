package com.gp.extract.twitter.labeler.sequence;

import com.gp.extract.twitter.util.IOUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Tags implements IOUtil.Logger, IOUtil.Loadable{
    private Map<String,Integer> symbolToTag = new HashMap<>() ;
    private ArrayList<String> idToTag = new ArrayList<>();

    private String startTagSymbol, endTagSymbol;
    private int startTagIndex, endTagIndex;
    private static final String DATA_FILE = "tags.txt";
    private boolean isInitialized = false;
    private static final String LOGGER_ID = Tags.class.getName();
    private String saveDirectory;
    private int sizeReduction = 0;


    public Tags(String saveDirectory, String startTagSymbol, String endTagSymbol)
    {
        this.saveDirectory = saveDirectory;
        this.startTagSymbol = startTagSymbol;
        this.endTagSymbol = endTagSymbol;
    }

    public void addTags(HashSet<String> tagSet)
    {
        fillTags(tagSet);
    }

    private void fillTags(Collection<String> collection)
    {
        if(isInitialized)
        {
            IOUtil.showError(this, "Tags are already initialized.");
            return;
        }
        int index = 0;
        for(String tag : collection)
        {
            idToTag.add(index, tag);
            symbolToTag.put(tag, index++);
        }

        //check if the start tag symbol exists in the tag set
        //if so then adjust its index, otherwise add it and adjust its index
        Integer start_index = symbolToTag.get(startTagSymbol);
        if(start_index != null)
        {
            startTagIndex = start_index;
        }
        else
        {
            startTagIndex = index;
            //add the start tag
            idToTag.add(index, startTagSymbol);
            symbolToTag.put(startTagSymbol, index++);
            sizeReduction++;
        }

        //check if the end tag symbol exists in the tag set
        //if so then adjust its index, otherwise add it and adjust its index
        Integer endIndex = symbolToTag.get(endTagSymbol);
        if(endIndex != null)
        {
            endTagIndex = endIndex;
            idToTag.add(index, endTagSymbol);
        }
        else
        {
            endTagIndex = index;
            //add the start tag
            idToTag.add(index, endTagSymbol);
            symbolToTag.put(endTagSymbol, index++);
            sizeReduction++;
        }


        isInitialized = true;
    }

    public int getTagIDBySymbol(String symbol) {
        return symbolToTag.get(symbol);
    }

    public String getTagSymbolById(int id) {
        return idToTag.get(id);
    }

    public int getStartTagId() {
        return startTagIndex;
    }


    public String getStartTagSymbol() {
        return startTagSymbol;
    }

    public String getEndTagSymbol() {
        return endTagSymbol;
    }

    public int getStartTagIndex() {
        return startTagIndex;
    }

    public int getEndTagIndex() {
        return endTagIndex;
    }


    /**
     * Get tags size.
     * @return the size f the tags without the start tag
     */
    public int getSize()
    {
        return idToTag.size()- sizeReduction;
    }

    public boolean isInitialized()
    {
        return isInitialized;
    }

    @Override
    public String getLogId() {
        return LOGGER_ID;
    }

    @Override
    public void load() throws IOException {
        String file = saveDirectory + DATA_FILE;
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(IOUtil.getUTF8FileReader(file, false));
            Scanner scanner = new Scanner(bufferedReader);
            String line;
            //read list
            ArrayList<String> tags = new ArrayList<>();
            while (scanner.hasNext()) {
                line = scanner.next().trim();
                if(line.isEmpty())
                    continue;
                tags.add(line);
            }
            //fill tags
            fillTags(tags);
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

    @Override
    public void save() throws IOException {

        String file = saveDirectory + DATA_FILE;
        BufferedWriter bufferedWriter = null;
        try
        {
            bufferedWriter = new BufferedWriter(IOUtil.getUTF8FileWriter(file, true));
            PrintWriter printWriter = new PrintWriter(bufferedWriter);
            //start writing list
            for(int i = 0; i < getSize(); i++)
            {
                printWriter.println(idToTag.get(i));
            }

        }
        catch (IOException e)
        {
            throw e;
        }
        finally {
            if(bufferedWriter != null)
            {
                bufferedWriter.close();
            }
        }
    }
}
