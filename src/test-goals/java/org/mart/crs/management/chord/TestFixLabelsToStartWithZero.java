package org.mart.crs.management.chord;

import org.mart.crs.utils.helper.HelperFile;
import junit.framework.TestCase;

import java.io.*;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @version 1.0 4/29/11 6:32 PM
 * @author: Hut
 */
public class TestFixLabelsToStartWithZero extends TestCase {


    public void testAllLabelsToStartWithZero() throws IOException {
        String inDir = "/home/hut/Beatles/labels";
        String outDir = "/home/hut/Beatles/labels1";
        File[] files = HelperFile.getFile(inDir).listFiles();
        File outDirFile = HelperFile.getFile(outDir);
        outDirFile.mkdirs();
        for(File file:files){
            List<String> lines = HelperFile.readLinesFromTextFile(file.getPath());
            String firstLine = lines.get(0);
            String firstLineOut = "";
            StringTokenizer tokenizer = new StringTokenizer(firstLine);
            String startTime = tokenizer.nextToken();
            if(Float.valueOf(startTime) > 0){
                firstLineOut = "0.0 ";
                while(tokenizer.hasMoreTokens()){
                    firstLineOut = firstLineOut + tokenizer.nextToken() + " ";
                }
            }else{
                firstLineOut = firstLine;
            }
            lines.remove(0);
            lines.add(0, firstLineOut);
            HelperFile.saveCollectionInFile(lines, String.format("%s/%s", outDir, file.getName()), false);
        }
    }

}
