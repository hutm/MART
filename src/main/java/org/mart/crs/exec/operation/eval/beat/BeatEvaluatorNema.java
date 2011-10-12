/*
 * This file is part of MART.
 * MART is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2, as published
 * by the Free Software Foundation.
 *
 * MART is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MART; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.mart.crs.exec.operation.eval.beat;

import org.mart.crs.management.beat.BeatStructure;
import org.mart.crs.utils.helper.HelperFile;
import org.imirsel.nema.analytics.evaluation.Evaluator;
import org.imirsel.nema.analytics.evaluation.EvaluatorFactory;
import org.imirsel.nema.analytics.evaluation.ResultRenderer;
import org.imirsel.nema.analytics.evaluation.ResultRendererFactory;
import org.imirsel.nema.model.*;
import org.imirsel.nema.model.fileTypes.BeatTextFile;
import org.imirsel.nema.model.fileTypes.SingleTrackEvalFileType;
import org.imirsel.nema.model.util.PathAndTagCleaner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 5/15/11 11:50 AM
 * @author: Hut
 */
public class BeatEvaluatorNema {

    private NemaTask singleSetTask;
    private NemaDataset singleSetDataset;
    private List<NemaTrackList> singleTestSet;
    private static File workingDirectory;
    private static File outputDirectory;
    File groundTruthDirectory;


    public static String GT_PATH;
    public static String RESULT_PATH;

    public static boolean isOnlyDownBeatEvaluation;


    public void initializeDirectories(String recognizedDirPath, String groundTruthFolder, String outFolder) throws Exception {
        GT_PATH = String.format("%s_MIREX_Downbeat_%s",groundTruthFolder, String.valueOf(isOnlyDownBeatEvaluation));
        RESULT_PATH = String.format("%s_MIREX_Downbeat_%s",recognizedDirPath, String.valueOf(isOnlyDownBeatEvaluation));
        workingDirectory = new File(outFolder);
        outputDirectory = new File(workingDirectory, (System.currentTimeMillis()) + "");
        outputDirectory.mkdirs();
        prepareOutputLabels(groundTruthFolder, GT_PATH, recognizedDirPath, RESULT_PATH);
        setUp();
    }


    protected void prepareOutputLabels(String gtFolder, String outGTFolder, String recognizedFolder, String outRecognizedFolder) {
        BeatStructure.transfromLabelsToMIREXFormat(gtFolder, outGTFolder, true, isOnlyDownBeatEvaluation);
        File[] results = HelperFile.getFile(recognizedFolder).listFiles();
        for (File file : results) {
            if (!file.isDirectory()) {
                continue;
            }
            BeatStructure.transfromLabelsToMIREXFormat(file.getPath(), String.format("%s/%s", outRecognizedFolder, file.getName()), false, isOnlyDownBeatEvaluation);
        }
    }

    public void setUp() throws Exception {

        groundTruthDirectory = new File(GT_PATH);
        singleSetTask = new NemaTask();
        singleSetTask.setId(14);
        singleSetTask.setName("Audio Beat Tracking");
        singleSetTask.setDescription("Audio beat tracking of musical audio requiring participants to find the beat locations in a piece of music");
        singleSetTask.setDatasetId(16);
        singleSetTask.setSubjectTrackMetadataId(11);
        singleSetTask.setSubjectTrackMetadataName(NemaDataConstants.BEAT_TRACKING_DATA);

        singleSetDataset = new NemaDataset();
        singleSetDataset.setId(singleSetTask.getDatasetId());
        singleSetDataset.setName("MIREX 2005 Beat Tracking Dataset");
        singleSetDataset.setDescription("MIREX 2005 Beat dataset created and donated by Martin McKinney of Phillips");

        int idtrackListId = 0;
        {
            ArrayList<NemaTrack> trackList = new ArrayList<NemaTrack>(4);
            File[] files = groundTruthDirectory.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().endsWith(".txt")) {
                    String id = PathAndTagCleaner.convertFileToMIREX_ID(files[i]);
                    trackList.add(new NemaTrack(id));
                    System.out.println("got track: " + id);
                }
            }
//	        trackList.add(new NemaTrack("daisy1"));
//	        trackList.add(new NemaTrack("daisy2"));
//	        trackList.add(new NemaTrack("daisy3"));
//	        trackList.add(new NemaTrack("daisy4"));
            singleTestSet = new ArrayList<NemaTrackList>(1);
            singleTestSet.add(new NemaTrackList(idtrackListId, singleSetTask.getDatasetId(), 3, "test", idtrackListId, trackList));
            idtrackListId++;
        }

    }

    public void evaluate() throws IllegalArgumentException, IOException, InstantiationException, IllegalAccessException {
        File groundTruthDirectory = new File(GT_PATH);
        File resultsDirectory = new File(RESULT_PATH);

        List<File> systemDirs = new ArrayList<File>();
        List<String> systemNames = new ArrayList<String>();
        File[] files = resultsDirectory.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory() && !(files[i].getName().equals(".svn"))) {
                String systemName = files[i].getName();

                System.out.println("got system: " + systemName);
                systemDirs.add(files[i]);
                systemNames.add(systemName);
            }
        }

        Evaluator evaluator = null;
        ResultRenderer renderer = null;

        //evaluator = new MelodyEvaluator(task, dataset, outputDirectory, workingDirectory, testSets);
        evaluator = EvaluatorFactory.getEvaluator(singleSetTask.getSubjectTrackMetadataName(), singleSetTask, singleSetDataset, null, singleTestSet);
        renderer = ResultRendererFactory.getRenderer(singleSetTask.getSubjectTrackMetadataName(), outputDirectory, workingDirectory, false, null);
        SingleTrackEvalFileType reader = new BeatTextFile();

        List<NemaData> groundTruth = reader.readDirectory(groundTruthDirectory, ".txt");
        evaluator.setGroundTruth(groundTruth);

        for (int i = 0; i < systemNames.size(); i++) {
            List<NemaData> resultsForAllTracks = reader.readDirectory(systemDirs.get(i), null);
            evaluator.addResults(systemNames.get(i), systemNames.get(i), singleTestSet.get(0), resultsForAllTracks);
        }


        NemaEvaluationResultSet results = evaluator.evaluate();

        //test rendering
        renderer.renderResults(results);

        //File resultFile = new File("src/test/resources/classification/evaluation/GT1/report.txt");
        //File outputFile = new File(outputDirectory,systemName+System.getProperty("file.separator")+"report.txt");

        //assertThat(resultFile, fileContentEquals(outputFile));

    }


    public static void main(String[] args) throws Exception {
        BeatEvaluatorNema evaluatorNema;
        String ROOTDIR = "/home/hut/prg/CHORDS3/TOOLS/nema-analytics/src/test/resources/beat/MIREX_RESULTS_BEATLES/";
        ROOTDIR = "/home/hut/work/test_beat/DAVIESLABELS/";

        evaluatorNema = new BeatEvaluatorNema();
        evaluatorNema.initializeDirectories(ROOTDIR + "results", ROOTDIR + "GT", ROOTDIR + "out");
        evaluatorNema.evaluate();

        isOnlyDownBeatEvaluation = true;

        evaluatorNema = new BeatEvaluatorNema();
        evaluatorNema.initializeDirectories(ROOTDIR + "results", ROOTDIR + "GT", ROOTDIR + "out");
        evaluatorNema.evaluate();
    }


}
