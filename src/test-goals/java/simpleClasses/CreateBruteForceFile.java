package simpleClasses;

import org.mart.crs.utils.helper.HelperFile;
import junit.framework.TestCase;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @version 1.0 04-Jul-2010 20:44:12
 * @author: Hut
 */
public class CreateBruteForceFile extends TestCase {


    public void testCreateFile() throws IOException {
//        for (char start = 65; start <= 90; start++) {
        FileWriter writer = new FileWriter(String.format("script0.txt"));

        for (char i = 65; i <= 90; i++) {
            for (char j = 65; j <= 90; j++) {
                writer.write(String.format("ls %s1\n", "" + i + j));
            }
        }


//            for (char i = 65; i <= 90; i++) {
//                for (char j = 65; j <= 90; j++) {
//                    for (char k = 65; k <= 90; k++) {
//                        writer.write(String.format("ls %s1\n", "" + i + j + k));
//                    }
//                }
//            }
        writer.close();
//        }
    }


    public void testParseResults() throws IOException {
        String inFile = "c:/Documents and Settings/Administrator/Desktop/out.txt";
        String outFile = "D:/brute/data.txt";
        FileWriter writer = new FileWriter(outFile);
        List<String> lines = HelperFile.readLinesFromTextFile(inFile);
        for (String s : lines) {
            if (s.startsWith("sftp>") || s.startsWith("ls") || s.startsWith("Can't ls") || s.startsWith("Couldn't") || s.startsWith("lCouldn't")) {

//                String dirName = s.substring(s.indexOf(" "));
//                if (dirName.indexOf("p") > 0) {
//                    dirName = dirName.substring(0, dirName.indexOf("p") + 1);
//                    writer.write("get " + dirName + "\n");
//                }
            } else {

                writer.write("get " + s + "\n");
            }
        }

        writer.close();
    }

    public void testParseResultsnew() throws IOException {
        String inFile = "c:\\cygwin\\bin\\3.log";
        String outFile = "D:/brute/data.txt";
        FileWriter writer = new FileWriter(outFile);
        List<String> lines = HelperFile.readLinesFromTextFile(inFile);
        for (String s : lines) {
            if (!s.startsWith("sftp>")) {
                writer.write("get " + s + "\n");
            }

        }

        writer.close();
    }


}
