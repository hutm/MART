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


import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.eval.AbstractCRSEvaluator;
import org.mart.crs.management.beat.BeatStructure;
import org.mart.crs.management.label.LabelsSource;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;
import org.mart.crs.utils.metrics.FMeasure;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BeatEvaluator extends AbstractCRSEvaluator {

    public static final String BEAT_PRECISION_NAME = "beatPrecision";
    public static final String BEAT_RECALL_NAME = "beatRecall";
    public static final String BEAT_FMEASURE_NAME = "beatFMeasure";
    public static final String DOWNBEAT_PRECISION_NAME = "downBeatPrecision";
    public static final String DOWNBEAT_RECALL_NAME = "downBeatRecall";
    public static final String DOWNBEAT_FMEASURE_NAME = "downBeatFMeasure";


    public static final float DEFAULT_PRECISION_WINDOW = 0.1f;  //in seconds

    protected String recognizedLabelsDir;
    protected String groundTruthLabelsDir;
    protected String outputFilePath;

    protected List<BeatEvalResult> beatEvalResults;

    protected float beatPrecision;
    protected float beatRecall;
    protected float beatFMeasure;

    protected float downBeatPrecision;
    protected float downBeatRecall;
    protected float downBeatFMeasure;



    public void initializeDirectories(String recognizedLabelsDir, String groundTruthLabelsDir, String outputFilePath) {
        this.recognizedLabelsDir = recognizedLabelsDir;
        this.groundTruthLabelsDir = groundTruthLabelsDir;
        this.outputFilePath = outputFilePath;
        beatEvalResults = new ArrayList<BeatEvalResult>();

        beatPrecision = 0;
        beatRecall = 0;
        beatFMeasure = 0;
        downBeatPrecision = 0;
        downBeatRecall = 0;
        downBeatFMeasure = 0;

        File outputDirectory = HelperFile.getFile(outputFilePath).getParentFile();
        outputDirectory.mkdirs();
    }


    public void evaluate() {
        File recognizedDir = HelperFile.getFile(recognizedLabelsDir);
        File[] recognizedFileList = recognizedDir.listFiles(new ExtensionFileFilter(Settings.BEAT_EXT));
        LabelsSource labelsSource = new LabelsSource(groundTruthLabelsDir, true, "gt", Settings.BEAT_EXTENSIONS);
        for (File recognizedSongFilePath : recognizedFileList) {
            String gtSongFilePath = labelsSource.getFilePathForSong(recognizedSongFilePath.getName());
            BeatStructure recognizedBeatStructure = BeatStructure.getBeatStructure(recognizedSongFilePath.getPath());

            BeatStructure gtBeatStructure = BeatStructure.getBeatStructure(gtSongFilePath);

            BeatEvalResult beatEvalResult = new BeatEvalResult(recognizedBeatStructure, gtBeatStructure, DEFAULT_PRECISION_WINDOW);
            beatEvalResults.add(beatEvalResult);
        }

        try {
            FileWriter writer = new FileWriter(outputFilePath);
            int length = beatEvalResults.size();
            if (Settings.numberOfTestMaterial > 0) {
                length = Settings.numberOfTestMaterial;     //Sometimes output does not contain labels for all the songs. In this way the score will be lower.
            }
            Collections.sort(beatEvalResults);
            for (BeatEvalResult evalResult : beatEvalResults) {
                FMeasure beatFMeasureEvalResult = evalResult.getBeatMeasure();
                FMeasure downBeatFMeasureEvalResult = evalResult.getDownBeatMeasure();

                beatPrecision += beatFMeasureEvalResult.getPrecision();
                beatRecall += beatFMeasureEvalResult.getRecall();
                beatFMeasure += beatFMeasureEvalResult.getFmeasure();

                downBeatPrecision += downBeatFMeasureEvalResult.getPrecision();
                downBeatRecall += downBeatFMeasureEvalResult.getRecall();
                downBeatFMeasure += downBeatFMeasureEvalResult.getFmeasure();


                String songName = Helper.getStringPadded(evalResult.getSongName(), 90);
                writer.write(String.format("%s %5.3f %5.3f %5.3f %5.3f %5.3f %5.3f\r\n", songName,
                        beatFMeasureEvalResult.getPrecision(), beatFMeasureEvalResult.getRecall(), beatFMeasureEvalResult.getFmeasure(),
                        downBeatFMeasureEvalResult.getPrecision(), downBeatFMeasureEvalResult.getRecall(), downBeatFMeasureEvalResult.getFmeasure()));
            }

            writer.write("----------------------------------------\r\n");
            writer.write(String.format("%5.3f %5.3f %5.3f %5.3f %5.3f %5.3f\r\n",
                    beatPrecision /= length, beatRecall /= length, beatFMeasure /= length, downBeatPrecision /= length, downBeatRecall /= length, downBeatFMeasure /= length));

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public String getResultsHeadersCommaSeparated() {
        return String.format("%s,%s,%s,%s,%s,%s,", BEAT_PRECISION_NAME, BEAT_RECALL_NAME, BEAT_FMEASURE_NAME, DOWNBEAT_PRECISION_NAME, DOWNBEAT_RECALL_NAME, DOWNBEAT_FMEASURE_NAME);
    }

    public String getResultsValuesCommaSeparated() {
        return String.format("%5.3f,%5.3f,%5.3f,%5.3f,%5.3f,%5.3f,", beatPrecision, beatRecall, beatFMeasure, downBeatPrecision, downBeatRecall, downBeatFMeasure);
    }


    public static void main(String[] args) {                                                       //results_4_7.0_lm_10.00_ac_1.00_p_-1.00_factored_false
        BeatEvaluator evaluator = new BeatEvaluator();
        evaluator.initializeDirectories("/home/hut/work/test_beat/DAVIESLABELS/results/ENST_DATA_4dims", "/home/hut/prg/BEAT/data/labels",
                "/home/hut/work/test_beat/DAVIESLABELS/results/ENST_DATA_4dims.txt");
        evaluator.evaluate();
    }
}
