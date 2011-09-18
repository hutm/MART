package org.mart.crs.lm;

import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.models.lm.TextForLMCreator;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;
import junit.framework.TestCase;

import java.io.*;
import java.util.*;

/**
 * @version 1.0 May 11, 2009 11:29:24 AM
 * @author: Maksim Khadkevich
 */
public class TestDurationInBeatsStat extends TestCase {

    public static final String labelFileList = "D:\\Beatles\\wav\\listLabelsAll";
    public static final String text = "D:\\Beatles\\text";

    public static final String statisticFilePAth = "D:\\Beatles\\statistic";

    public static Map<Integer, Integer> statMap = new HashMap<Integer, Integer>();

    public void testDurationInBeats(){

        try {
            Settings.IS_QUANTIZED_DURATION = false;
            BufferedWriter writer = new BufferedWriter(new FileWriter(statisticFilePAth));
            List<String> labels = HelperFile.readTokensFromTextFile(labelFileList, 1);

            TextForLMCreator.process(labelFileList, text, true);

            BufferedReader reader = new BufferedReader(new FileReader(text));
            String line, token;
            int value;
            while((line=reader.readLine()) != null){
                StringTokenizer tokenizer = new StringTokenizer(line);
                while(tokenizer.hasMoreTokens()){
                    token = tokenizer.nextToken();
                    if(token.indexOf(":") > 0){
                        value = Helper.parseInt(token.substring(token.lastIndexOf("-") + 1));
                        if(statMap.containsKey(value)){
                            Integer count = statMap.get(value);
                            count = count + 1;
                            statMap.remove(value);
                            statMap.put(value, count);
                        } else {
                            statMap.put(value, 1);
                        }
                    }
                }

            }

            reader.close();
            Set<Integer> keys = statMap.keySet();
            List<Integer> keysList = new ArrayList<Integer>();
            for (Integer key:keys){
                keysList.add(key);
            }


            Collections.sort(keysList);
            for(Integer key:keysList){
                writer.write(key + " \t" + statMap.get(key) + "\n");
            }

            writer.close();


        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
