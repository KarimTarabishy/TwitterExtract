package com.gp.extract.twitter.util;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class IOUtil {

    public static interface Logger{
        String getLogId();
    }
    public static interface Loadable{
        void load() throws IOException;
        void save() throws IOException;
    }
    public static void showError(Logger logger, String msg)
    {
        //TODO: make this output to the GUI
        System.err.println("Error ["+logger.getLogId()+"]: " + msg + ".\n\n");
    }

    public static void showInfo(Logger logger, String msg)
    {
        //TODO: make this output to the GUI
        System.out.println("Info ["+logger.getLogId()+"]: " + msg + ".\n\n");
    }

    public  static File ensureFileExist(String filename) throws IOException {
        File yourFile = new File(filename);
        if(!yourFile.exists()) {
            yourFile.createNewFile();
        }
        return yourFile;
    }

    public static OutputStreamWriter getUTF8FileWriter(String file_name, boolean createIfNotExist) throws IOException {
        File s;
        if(createIfNotExist)
        {
            s = ensureFileExist(file_name);
        }
        else
        {
            s = new File(file_name);
        }
        return new OutputStreamWriter(
                new FileOutputStream(
                        s
                ),  Charset.forName("UTF-8").newEncoder() );
    }

    public static InputStreamReader getUTF8FileReader(String file_name, boolean createIfNotExist) throws IOException {
        File s;
        if(createIfNotExist)
        {
            s = ensureFileExist(file_name);
        }
        else
        {
            s = new File(file_name);
        }
        return new InputStreamReader(
                new FileInputStream(
                        s
                ),"UTF-8" );
    }

    public static class DatasetReader{
        private String fileName;
        private BufferedReader bufferedReader;

        public DatasetReader(String fileName) throws IOException {
            fileName = fileName;
            bufferedReader = new BufferedReader(getUTF8FileReader(fileName, false));


        }

        /**
         * Read a single line from file, and put the found tag and words in the given arguments.
         * If current tweet finished it sets the word argument to null.
         * If the file finished it return false.
         * @param data
         * @return boolean indicating whether file ended or not
         * @throws IOException
         */
        public boolean read(ArrayList<String> data) throws IOException {
            try
            {
                String line = bufferedReader.readLine();
                if(line == null)
                    return false;

                line.trim();
                if(line.isEmpty())
                {
                    data.set(0,null);
                }
                else
                {
                    String [] split = line.split("\\s");
                    data.set(0,split[0]);
                    data.set(1,split[1]);
                }
            }
            catch (IOException e)
            {
                bufferedReader.close();
                throw e;
            }
            return true;
        }
    }
}
