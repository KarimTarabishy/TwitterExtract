package gp.twitter.extract.util;

import java.io.*;
import java.nio.charset.Charset;

public class IOUtil {

    static String errorFile = "errorFile";

    public  static File ensureFileExist(String filename) throws IOException {
        File yourFile = new File(filename);
        if(!yourFile.exists()) {
            yourFile.createNewFile();
        }
        return yourFile;
    }

    public static OutputStreamWriter getUTF8FileWriter(String file_name) throws IOException {
        return new OutputStreamWriter(
                new FileOutputStream(
                        IOUtil.ensureFileExist(file_name)
                ),  Charset.forName("UTF-8").newEncoder() );
    }

    public static InputStreamReader getUTF8FileReader(String file_name) throws IOException {
        return new InputStreamReader(
                new FileInputStream(
                        IOUtil.ensureFileExist(file_name)
                ),"UTF-8" );
    }
}
