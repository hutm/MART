package org.mart.crs.eval;

import junit.framework.TestCase;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @version 1.0 May 17, 2009 8:11:49 PM
 * @author: Maksim Khadkevich
 */
public class TestResultsCompactRepresentation extends TestCase {

    public static String KEY = "timeunit:"; //TODO when working with the first token e.g. "Average: 0.7111152"  it does not work

    public static String rootFilePAth = "D:\\Beatles\\secPass";

    public static String directoryPath = rootFilePAth + "\\number\\results";
    public static String outputFile = rootFilePAth + "\\number\\CompactResults.txt";

    public static List<List<String>> data;
    public static int numberOfFOlds = 5;

    public void testProduceCompactRepresentation(){
        data = new ArrayList<List<String>>();


        for (int i = 0; i <= numberOfFOlds - 1; i++) {


            String directoryPath_ = directoryPath.replaceAll("number", String.valueOf(i));
            String outputFile_ = outputFile.replaceAll("number", String.valueOf(i));

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile_));
                File dir = new File(directoryPath_);

                List<String> buffer = new ArrayList<String>();
                int counter = 0;

                for (File file:dir.listFiles()){
                    if(file.isDirectory()){
                        continue;
                    }
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String line, token, accuracy;
                    while((line = reader.readLine())!=null){
                        StringTokenizer tokenizer = new StringTokenizer(line);
                        if (tokenizer.hasMoreTokens()) {
                            tokenizer.nextToken();
                        }
                        if (tokenizer.hasMoreTokens() && tokenizer.nextToken().equals(KEY)){
                            accuracy = tokenizer.nextToken();
                            writer.write(file.getName() + "\t" + accuracy + "\n");
                            buffer.add(accuracy);
                        }
                    }
                    reader.close();
                }
                data.add(buffer);
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        //Now write summaru data over all folds
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(rootFilePAth + File.separator + "AlResults"));
            for(int i = 0; i < data.get(0).size(); i++){
                for(List<String> s:data){
                    writer.write(s.get(i) + "\t");
                }
                writer.write("\n");
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
