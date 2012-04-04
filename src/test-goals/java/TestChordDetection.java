import junit.framework.TestCase;
import org.mart.crs.config.Settings;
import org.mart.crs.utils.helper.HelperFile;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static org.mart.crs.utils.helper.HelperFile.getFile;

//import HelperFile;

/**
 * @version 1.0 Jan 20, 2010 10:08:07 AM
 * @author: Maksim Khadkevich
 */
public class TestChordDetection extends TestCase {

    public void testMapNamesBack() {


        Map<String, String> correspondanceMap = HelperFile.readMapFromTextFile("D:\\build\\Songs");
        String dirPath = "D:\\build\\data\\beats";
        String ext = Settings.BEAT_EXT;


        String origName, numberName;
        for (String filePath : correspondanceMap.keySet()) {
            origName = HelperFile.getNameWithoutExtension(filePath);
            numberName = HelperFile.getNameWithoutExtension(correspondanceMap.get(filePath));

            String sourceFile = dirPath + File.separator + numberName + ext;
            String destFile = dirPath + File.separator + origName + ext;

            getFile(sourceFile).renameTo(getFile(destFile));


        }


        System.out.println("");

    }

    public void testMapNamesBackFromTextFiles() {
        Map<String, String> correspondanceMap = HelperFile.readMapFromTextFile("D:\\build\\Songs");
        String datafilePath = "D:\\build\\data\\out11\\keys.txt";


        String origName, numberName;
        Map<String, String> inversedMap = new HashMap<String, String>();

        for (String key : correspondanceMap.keySet()) {
            origName = HelperFile.getNameWithoutExtension(key);
            numberName = HelperFile.getNameWithoutExtension(correspondanceMap.get(key));


            inversedMap.put(numberName, origName);
        }


        try {
            BufferedReader reader = new BufferedReader(new FileReader(datafilePath));
            BufferedWriter writer = new BufferedWriter(new FileWriter(datafilePath + "_"));
            String line = "";
            while ((line = reader.readLine()) != null) {
                StringTokenizer tokenizer = new StringTokenizer(line);
                String token = tokenizer.nextToken();
                token = token.replaceAll("\"", "");
                line = line.replaceAll(token, inversedMap.get(token));
                writer.write(line + "\n");
            }
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("");
    }
}
