package org.mart.crs.eval.beat;

import org.mart.crs.exec.operation.eval.beat.BeatEvaluator;
import org.mart.crs.management.beat.BeatStructure;
import org.mart.crs.utils.helper.HelperFile;
import junit.framework.TestCase;

import java.io.File;
import java.util.List;

/**
 * @version 1.0 9/6/11 2:34 PM
 * @author: Hut
 */
public class TestBeatEvaluationMIREX extends TestCase {

    String beatOutFolder = "/home/hut/PhD/read/MIREX/2011Our/testResults/MIREXBEEATS2_FOLD2";
    String gtFolder = "/home/hut/prg/BEAT/data/labels";
    String tempLabelsFolder = "/home/hut/PhD/read/MIREX/2011Our/testResults/MIREXBEEATS2_FOLD2ResultsTemp";
    String resultsFinalFolder = "/home/hut/PhD/read/MIREX/2011Our/testResults/MIREXBEEATS2_FOLD2FINALRESULTS";


    public void testChordEvaluatorNema() throws Exception {


        HelperFile.createDir(tempLabelsFolder);
        File[] files = HelperFile.getFile(gtFolder).listFiles();

        for (File gtFile : files) {

            List<String> filePaths = HelperFile.findFilesWithName(beatOutFolder, HelperFile.getNameWithoutExtension(gtFile.getName()));
            if (filePaths != null && filePaths.size() > 0) {

                String beatOutput = filePaths.get(0);

                BeatStructure beatStructure = BeatStructure.getBeatStructure(beatOutput);
                String outFilePath = String.format("%s/%s", tempLabelsFolder, gtFile.getName());
                beatStructure.serializeIntoXML(outFilePath);
            }

        }


        BeatEvaluator evaluatorNema = new BeatEvaluator();
        evaluatorNema.initializeDirectories(tempLabelsFolder, gtFolder, resultsFinalFolder + ".txt");
        evaluatorNema.evaluate();

    }


}
