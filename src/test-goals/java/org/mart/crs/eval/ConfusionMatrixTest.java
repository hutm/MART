package org.mart.crs.eval;

import org.mart.crs.exec.operation.eval.chord.confusion.ConfusionChordManager;
import org.junit.Test;

/**
 * @version 1.0 11-Mar-2010 15:45:39
 * @author: Maksim Khadkevich
 */
public class ConfusionMatrixTest {


    @Test
    public void testExtractConfusions(){
        String resultsDir = "/home/hut/PhD/papers/mypapers/IEEEPaper/experiments/confusionMatrix/ConfudionsBest/lmWeight_9.00#acWeight_1.00#wip_-3.00";
//        String resultsDir = this.getClass().getResource("/chordConfusions/-").getPath();
        String outTxtFile = "temp/chordConfusionMatrix.txt";

        String labelsDir = "/home/hut/Beatles/labels"; //Settings.chordLabelsGroundTruthDir;

        ConfusionChordManager confusionChordMatrix = new ConfusionChordManager();
        confusionChordMatrix.extractConfusion(resultsDir, labelsDir, outTxtFile);
    }



}
