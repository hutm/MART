package misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 29.01.2009
 * Time: 14:13:39
 * To change this template use File | Settings | File Templates.
 */
public class RandomMixStrings {

    public static void main(String[] args) {
        String filenameIn = args[0];
        String filenameOut = args[1];
        List<String> lines = new ArrayList<String>();
        Object[] linesArray;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filenameIn));
            String line;
            while (true) {
                if ((line = reader.readLine()) != null && line.length() > 1) {
//                    lines.add(line.substring(0, line.indexOf("-")));
                    lines.add(line);
                } else {
                    break;
                }
            }

            linesArray = lines.toArray();
            linesArray = mixUpArray(linesArray);


            BufferedWriter writer = new BufferedWriter(new FileWriter(filenameOut));
            for (int i = 0; i < linesArray.length; i++){
                writer.write((String)linesArray[i] + "\n");
            }
            writer.close();
            reader.close();

        } catch (Exception e) {
           e.printStackTrace();
        }

    }

    public static Object[] mixUpArray(Object inarray[]) {
        Object temp;
        int index1, index2;
        for (int i = 0; i < 30000; i++) {
            index1 = (int) Math.floor(Math.random() * (inarray.length - 1));
            index2 = (int) Math.floor(Math.random() * (inarray.length - 1));
            temp = inarray[index1];
            inarray[index1] = inarray[index2];
            inarray[index2] = temp;
            temp = null;
        }
        return inarray;
    } // method MixUpArray
}
