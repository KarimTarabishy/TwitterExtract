package gp.twitter.extract.util;

import java.io.File;
import java.io.IOException;

public class IOUtil {

    static String errorFile = "errorFile";

    public  static File ensureFileExist(String filename) throws IOException {
        File yourFile = new File(filename);
        if(!yourFile.exists()) {
            yourFile.createNewFile();
        }
        return yourFile;
    }
}
