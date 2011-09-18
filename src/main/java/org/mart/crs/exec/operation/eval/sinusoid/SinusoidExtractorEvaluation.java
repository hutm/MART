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

package org.mart.crs.exec.operation.eval.sinusoid;

import org.mart.crs.analysis.filterbank.FilterBankManagerBandPass;
import org.mart.crs.exec.operation.eval.FMeasure;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.helper.Helper;
import org.apache.log4j.Logger;
import org.mart.crs.utils.helper.HelperArrays;
import org.mart.crs.utils.helper.HelperFile;

import java.util.Arrays;
import java.util.List;

/**
 * @version 1.0 26-Apr-2010 11:55:29
 * @author: Hut
 */
public class SinusoidExtractorEvaluation {

    protected static Logger logger = CRSLogger.getLogger(SinusoidExtractorEvaluation.class);

    private String gtFilePath;
    private String resultsFilePath;

    private double[][] gt;
    private FilterBankManagerBandPass filterBankManager;

    public SinusoidExtractorEvaluation(String gtFilePath, String resultsFilePath) {
        this.gtFilePath = gtFilePath;
        this.resultsFilePath = resultsFilePath;
        readGT();
        readTranscription();
    }

    public void readGT() {
        List<String> lines = HelperFile.readLinesFromTextFile(gtFilePath);
        gt = new double[lines.size()][];
        int i = 0;
        for (String line : lines) {
            try {
                gt[i] = Helper.getStringValuesAsDoubles(line);
                i++ ;
            } catch (Exception e) {
                logger.warn(String.format("Problems when parsing line :%s", line));
                logger.debug(Helper.getStackTrace(e));
            }
        }
    }


    public void readTranscription() {
        FilterBankManagerBandPass fnManager = FilterBankManagerBandPass.importDetectedPeriodicities(resultsFilePath);
        this.filterBankManager = fnManager;
        if (fnManager.getFrameSizeOfFeaturesInSecs() != gt[1][0]) {
            logger.error("Cannot evaluate since ground-truth and transcription have different frame rates");
        }
    }


    public FMeasure evaluate() {
        FMeasure fMeasure = new FMeasure(0.5f, true);
        float[][][] transcribed = filterBankManager.getDataForEvaluation();
        for (int i = 0; i < gt.length; i++) {
            double[] groundTruth = Arrays.copyOfRange(gt[i], 1, (gt[i].length-1) / 2 + 1);
            double[] groundTruthWeight = Arrays.copyOfRange(gt[i], (gt[i].length-1) / 2 + 1, gt[i].length);


            double[] transcription = HelperArrays.getFloatAsDouble(transcribed[i][0]);
            double[] transcriptionWeights = HelperArrays.getFloatAsDouble(transcribed[i][1]);

            fMeasure.processFrame(groundTruth, groundTruthWeight, transcription, transcriptionWeights);
        }

        return fMeasure;
    }





    public static void main(String[] args) {
        SinusoidExtractorEvaluation sinusoidExtractorEvaluation = new SinusoidExtractorEvaluation("d:\\dev\\Matlab\\chromaElliptic\\sines\\generatedWav.txt", "d:\\dev\\Matlab\\chromaElliptic\\sines\\generatedWav.spec");
        FMeasure fMeasure = sinusoidExtractorEvaluation.evaluate();
        fMeasure.calculateGlobalValues();
        System.out.println(fMeasure);
        System.out.println("End");

    }

}
