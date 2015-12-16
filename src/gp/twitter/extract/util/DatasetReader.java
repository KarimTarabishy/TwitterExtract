package gp.twitter.extract.util;

import java.io.BufferedReader;
import java.io.IOException;

public class DatasetReader {
    private String fileName;
    private BufferedReader bufferedReader;

    public DatasetReader(String fileName) throws IOException {
        fileName = fileName;
        bufferedReader = new BufferedReader(IOUtil.getUTF8FileReader(fileName));
    }

    /**
     * Read a single line from file, and put the found tag and words in the given arguments.
     * If current tweet finished it sets the word argument to null.
     * If the file finished it return false.
     * @param word  holder to put the found word in it
     * @param tag  holder to put the found tag in
     * @return boolean indicating whether file ended or not
     * @throws IOException
     */
    public boolean read(String word, String tag) throws IOException {
        String line = bufferedReader.readLine();
        if(line == null)
            return false;

        line.trim();
        if(line.isEmpty())
        {
            word = null;
        }
        else
        {
            String [] split = line.split("\\s");
            word = split[0];
            tag = split[1];
        }
        return true;
    }

}
