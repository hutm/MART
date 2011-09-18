package misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 29.01.2009
 * Time: 14:13:39
 * To change this template use File | Settings | File Templates.
 */
public class UniqueWordsCounter {

    public static void main(String[] args) {
        String filenameIn = args[0];
        String filenameOut = args[1];
        List<String> lines = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filenameIn));
            String line;
            while (true) {
                if ((line = reader.readLine()) != null && line.length() > 1) {
                    String[] result = line.split("\\W");
                    for (int i = 0 ; i < result.length; i++) {
                        lines.add(result[i]);
                    }
                } else {
                    break;
                }
            }

            Collections.sort(lines);

            //Remove duplicates
            for(int i = 0; i < lines.size(); i++){
                String word = lines.get(i);
                boolean flag = true;
                while (flag) {
                    for(int j =0; j < lines.size(); j++){
                        if (i!=j && word.equals(lines.get(j))){
                            lines.remove(j);
                            flag = true;
                            break;
                        }
                        flag = false;
                    }
                }
            }


            BufferedWriter writer = new BufferedWriter(new FileWriter(filenameOut));
            for (int i = 0; i < lines.size(); i++){
                writer.write((String)lines.get(i) + "\n");
            }
            writer.close();
            reader.close();

        } catch (Exception e) {
           e.printStackTrace();
        }

    }


}
