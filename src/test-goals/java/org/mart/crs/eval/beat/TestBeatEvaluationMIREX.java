package org.mart.crs.eval.beat;

import org.mart.crs.exec.operation.eval.beat.BeatEvaluator;
import org.mart.crs.exec.operation.eval.beat.BeatEvaluatorNema;
import org.mart.crs.labelling.beat.BeatLabelsConverter;
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

    public void testBeatEvaluatorNemaICASSP2012Hains() throws Exception {
        BeatEvaluatorNema evaluatorNema = new BeatEvaluatorNema();
        evaluatorNema.initializeDirectories("/home/hut/PhD/experiments/beatForICASSP/hains1/output", "/home/hut/PhD/experiments/beatForICASSP/hains1/gt", "/home/hut/PhD/experiments/beatForICASSP/hains1/results.txt");
        evaluatorNema.evaluate();
    }

    public void testBeatEvaluatorNemaICASSP2012Enst2010() throws Exception {
        BeatEvaluatorNema.isOnlyDownBeatEvaluation = true;
        BeatEvaluatorNema evaluatorNema = new BeatEvaluatorNema();
        evaluatorNema.initializeDirectories("/home/hut/PhD/experiments/beatForICASSP/2010/output", "/home/hut/PhD/experiments/beatForICASSP/2010/gt", "/home/hut/PhD/experiments/beatForICASSP/2010/resultsDownbeats.txt");
        evaluatorNema.evaluate();

//        BeatLabelsConverter.createListForMatlabBeatEvaluations("/home/hut/PhD/experiments/beatForICASSP/2010/gt", "/home/hut/PhD/experiments/beatForICASSP/2010/output/1dim", "/home/hut/PhD/experiments/beatForICASSP/2010/list1dim.txt");
//        BeatLabelsConverter.createListForMatlabBeatEvaluations("/home/hut/PhD/experiments/beatForICASSP/2010/gt", "/home/hut/PhD/experiments/beatForICASSP/2010/output/2dim", "/home/hut/PhD/experiments/beatForICASSP/2010/list2dim.txt");
//        BeatLabelsConverter.createListForMatlabBeatEvaluations("/home/hut/PhD/experiments/beatForICASSP/2010/gt", "/home/hut/PhD/experiments/beatForICASSP/2010/output/3dim", "/home/hut/PhD/experiments/beatForICASSP/2010/list3dim.txt");
//        BeatLabelsConverter.createListForMatlabBeatEvaluations("/home/hut/PhD/experiments/beatForICASSP/2010/gt", "/home/hut/PhD/experiments/beatForICASSP/2010/output/DAVIES", "/home/hut/PhD/experiments/beatForICASSP/2010/listDAVIES.txt");

    }

    public void testBeatEvaluatorNema() throws Exception {
        BeatEvaluator evaluatorNema = new BeatEvaluator();
        evaluatorNema.initializeDirectories("/home/hut/PhD/experiments/beatForICASSP/hains1/output/1dims", "/home/hut/PhD/experiments/beatForICASSP/hains1/gt", "/home/hut/PhD/experiments/beatForICASSP/hains1/results1dims.txt");
        evaluatorNema.evaluate();

        BeatLabelsConverter.createListForMatlabBeatEvaluations("/home/hut/PhD/experiments/beatForICASSP/hains1/gt", "/home/hut/PhD/experiments/beatForICASSP/hains1/output/1dims", "/home/hut/PhD/experiments/beatForICASSP/hains1/list.txt");
    }


    public void testBeatEvaluatorNemaBeatles() throws Exception {
        BeatEvaluatorNema.isOnlyDownBeatEvaluation = true;
        BeatEvaluatorNema evaluatorNema = new BeatEvaluatorNema();
        evaluatorNema.initializeDirectories("/home/hut/PhD/experiments/beatBeatles/results/output", "/home/hut/PhD/experiments/beatBeatles/results/gt", "/home/hut/PhD/experiments/beatBeatles/results/resultsDownbeats.txt");
        evaluatorNema.evaluate();
    }

}
