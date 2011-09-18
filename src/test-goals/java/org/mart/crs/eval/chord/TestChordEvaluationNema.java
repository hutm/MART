package org.mart.crs.eval.chord;

import org.mart.crs.exec.operation.eval.AbstractCRSEvaluator;
import org.mart.crs.exec.operation.eval.chord.ChordEvaluatorNema;
import junit.framework.TestCase;


/**
 * @version 1.0 5/5/11 2:29 PM
 * @author: Hut
 */
public class TestChordEvaluationNema extends TestCase {


    public void testChordEvaluatorNema() {
        AbstractCRSEvaluator evaluatorNema = new ChordEvaluatorNema();
        evaluatorNema.initializeDirectories("/home/hut/temp/3/fold/summary/640251202/data/1004564717/temp/lmWeight_9.00#acWeight_1.00#wip_-3.00_extended", "/home/hut/Beatles/labels", "/home/hut/temp/testMauchVsKhadkevich/khadkevich_extended.txt");
        evaluatorNema.evaluate();

    }


    public void testMIREX2011() {
        AbstractCRSEvaluator evaluatorNema = new ChordEvaluatorNema();
        evaluatorNema.initializeDirectories("/home/hut/PhD/read/MIREX/2011/testtrain", "/home/hut/Beatles/labels", "/home/hut/PhD/read/MIREX/2011/testtrain.txt");
        evaluatorNema.evaluate();
    }


    public void testMIREX2011Final() {
        AbstractCRSEvaluator evaluatorNema = new ChordEvaluatorNema();
        evaluatorNema.initializeDirectories("/home/hut/temp/!finalMIREX", "/home/hut/Beatles/labels", "/home/hut/temp/!finalMIREX.txt");
        evaluatorNema.evaluate();
    }


    public void testMIREX2011FinalFolds() {
        AbstractCRSEvaluator evaluatorNema = new ChordEvaluatorNema();
        evaluatorNema.initializeDirectories("/home/hut/PhD/read/MIREX/2011Our/testResults/CHORDSPRETRAINED/labels_out_pretrained_final", "/home/hut/Beatles/labels", "/home/hut/PhD/read/MIREX/2011Our/testResults/CHORDSPRETRAINED.txt");
        evaluatorNema.evaluate();


//        evaluatorNema = new ChordEvaluatorNema();
//        evaluatorNema.initializeDirectories("/home/hut/temp/!finalMIREXFolds/labels_0", "/home/hut/Beatles/labels", "/home/hut/temp/!finalMIREXFolds/labels_0.txt");
//        evaluatorNema.evaluate();
    }


}
