package misc;

import org.mart.crs.utils.helper.Helper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 29.01.2009
 * Time: 14:13:39
 * To change this template use File | Settings | File Templates.
 */
public class DictTrainer {

    public static void main(String[] args) {
        String filenameIn = args[0];
        String filenameOut = args[1];
        List<String> words = new ArrayList<String>();
        List<String> translations = new ArrayList<String>();

        Object[] wordsO, translationsO;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filenameIn));
            String line;
            while ((line = reader.readLine()) != null && line.length() > 1) {
                words.add(line.substring(0, line.lastIndexOf("-")));
                translations.add(line.substring(line.lastIndexOf("-") + 1));
            }

            if (Helper.parseBoolean(args[2])) {
                wordsO = words.toArray();
                translationsO = translations.toArray();
            } else {
                wordsO = translations.toArray();
                translationsO = words.toArray();
            }

            mixUpArray(wordsO, translationsO);


            BufferedWriter writer = new BufferedWriter(new FileWriter(filenameOut));

            for (int i = 0; i < wordsO.length; i++) {
                System.out.print("" + i + ". " + wordsO[i] + " - ");
                readInput();
                System.out.println(translationsO[i]);
                if (readInput().length() > 0) {
                    writer.write(wordsO[i] + " - " + translationsO[i] + "\n");
                    writer.flush();
                }
            }
            writer.close();
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String readInput() throws IOException {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        String line = br.readLine();
        return line;
    }


    public static void mixUpArray(Object inarray1[], Object inarray2[]) {
        Object temp;
        int index1, index2;
        for (int i = 0; i < 30000; i++) {
            index1 = (int) Math.floor(Math.random() * (inarray1.length - 1));
            index2 = (int) Math.floor(Math.random() * (inarray1.length - 1));
            temp = inarray1[index1];
            inarray1[index1] = inarray1[index2];
            inarray1[index2] = temp;
            temp = null;
            temp = inarray2[index1];
            inarray2[index1] = inarray2[index2];
            inarray2[index2] = temp;
            temp = null;
        }
    } // method MixUpArray
}
