package org.mart.crs.lm;

import junit.framework.TestCase;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.management.label.chord.ChordStructure;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @version 1.0 03.05.2009 21:20:50
 * @author: Maksim Khadkevich
 */
public class TestPercentageOfKnownChords extends TestCase {


    public static void testPercentageOfKnownChords(){
        String outDir = "D:\\Beatles\\final\\ham_2048\\results\\results_0.0\\";

//        List<String> labelFiles = Helper.readTokensFromTextFile("D:\\Beatles\\wav\\list1test_labels", 1);

        String dirName = "D:\\Beatles\\final\\ham_2048\\results\\results";
        File[] labelFiles = (new File(dirName)).listFiles();


        List<ChordSegment> segments;
        for (File labelFile: labelFiles){
            segments = (new ChordStructure(labelFile.getAbsolutePath())).getChordSegments();
            for(ChordSegment segment : segments){
                segment.setOnset((float) Math.floor(segment.getOnset() * 10) / 10.0f);
                segment.setOffset((float) Math.floor(segment.getOffset() * 10) / 10.0f);
            }
            try {
                FileWriter writer = new FileWriter(new File(outDir + (new File(labelFile.getAbsolutePath())).getName()));
                for (ChordSegment cs : segments) {
                    writer.write(cs + "\n");
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        EvaluatorOld.makeEvaluation(  labelsGroundTruthDir, outDir, "D:\\Beatles\\percentage.txt");
    }
}
