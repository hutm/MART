package org.mart.crs.eval;

import org.junit.Test;
import org.mart.crs.exec.operation.eval.chord.confusion.ConfusionChordManager;

/**
 * @version 1.0 11-Mar-2010 15:45:39
 * @author: Maksim Khadkevich
 */
public class ConfusionMatrixTest {


    @Test
    public void testExtractConfusions(){
        String resultsDir = "/home/hut/PhD/newPapers/mypapers/7_REAS_PAPER2010/experiments/confusionMatrix/ConfudionsBest/lmWeight_9.00#acWeight_1.00#wip_-3.00";
        String outTxtFile = "/home/hut/PhD/newPapers/mypapers/7_REAS_PAPER2010/experiments/confusionMatrix/ConfudionsBest/chordConfusionMatrix.txt";

        String labelsDir = "/home/hut/mirdata/chords/labels"; //Settings.chordLabelsGroundTruthDir;

        ConfusionChordManager confusionChordMatrix = new ConfusionChordManager();
        confusionChordMatrix.extractConfusion(resultsDir, labelsDir, outTxtFile);
    }


     @Test
    public void testExtractConfusionsOld(){
        String resultsDir = "/home/hut/PhD/newPapers/mypapers/7_REAS_PAPER2010/experiments/confusionMatrix/ConfusionBaseline/lmWeight_9.00#acWeight_1.00#wip_-3.00";
        String outTxtFile = "/home/hut/PhD/newPapers/mypapers/7_REAS_PAPER2010/experiments/confusionMatrix/ConfusionBaseline/chordConfusionMatrix.txt";

        String labelsDir = "/home/hut/mirdata/chords/labels"; //Settings.chordLabelsGroundTruthDir;

        ConfusionChordManager confusionChordMatrix = new ConfusionChordManager();
        confusionChordMatrix.extractConfusion(resultsDir, labelsDir, outTxtFile);
    }



}
