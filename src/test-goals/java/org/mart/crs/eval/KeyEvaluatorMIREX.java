package org.mart.crs.eval;

import junit.framework.TestCase;
import org.mart.crs.exec.operation.eval.AbstractCRSEvaluator;
import org.mart.crs.exec.operation.eval.chord.ChordEvaluatorNema;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 9/6/11 1:46 PM
 * @author: Hut
 */
public class KeyEvaluatorMIREX extends TestCase {


    public void testEvaluateKeyLabels(){
        String keyOutFolder = "/home/hut/PhD/read/MIREX/2011Our/testResults/MIREXKEYS2";
        String gtFolder = "/home/hut/Beatles/labelsKey";
        String tempLabelsFolder = "/home/hut/PhD/read/MIREX/2011Our/testResults/MIREXKEYS2Temp";
        String resultsFinalFolder = "/home/hut/PhD/read/MIREX/2011Our/testResults/MIREXKEYS2FINALRESULTS";

        HelperFile.createDir(tempLabelsFolder);
        File[] files = HelperFile.getFile(gtFolder).listFiles();

        for(File gtFile:files){
            String keyOutput = HelperFile.findFilesWithName(keyOutFolder, gtFile.getName()).get(0);
            String tonality = HelperFile.readLinesFromTextFile(keyOutput).get(0);
            String[] tokens = tonality.trim().split("\\s+");



            ChordStructure chordStructure = new ChordStructure(gtFile.getPath());
            double endTime = chordStructure.getChordSegments().get(chordStructure.getChordSegments().size() - 1).getOffset();

            String outputLine = String.format("0.0 %5.3f %s:%s", endTime, tokens[0], tokens[1].substring(0, 3));
            List<String> outputArrayList = new ArrayList<String>();
            outputArrayList.add(outputLine);

            String outFilePath = String.format("%s/%s", tempLabelsFolder,gtFile.getName());
            HelperFile.saveCollectionInFile(outputArrayList, outFilePath, false);
        }



        AbstractCRSEvaluator evaluatorNema = new ChordEvaluatorNema();
        evaluatorNema.initializeDirectories(tempLabelsFolder, gtFolder, resultsFinalFolder);
        evaluatorNema.evaluate();

    }


}
