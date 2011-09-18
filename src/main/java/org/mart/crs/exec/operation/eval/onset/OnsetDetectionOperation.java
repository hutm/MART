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

package org.mart.crs.exec.operation.eval.onset;

import org.mart.crs.config.Settings;
import org.mart.crs.core.AudioReader;
import org.mart.crs.core.onset.OnsetDetectionFunction;
import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrumPercussivePart;
import org.mart.crs.exec.operation.Operation;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.utils.helper.HelperArrays;
import org.mart.crs.utils.helper.HelperFile;
import org.mart.crs.config.ConfigSettings;
import org.mart.crs.config.ExecParams;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 3/11/11 1:34 AM
 * @author: Hut
 */
public class OnsetDetectionOperation extends Operation {

    protected static  float onsetDetectionThreshold = 1.0f;

    protected ExecParams execParams;

    protected String filePathWavList;

    protected String resultsDir;



    public OnsetDetectionOperation(ExecParams execParams) {
        super(execParams._workingDir);
        this.execParams = execParams;
    }

    @Override
    public void initialize() {
        this.filePathWavList = execParams._waveFilesTestFileList;
        this.resultsDir = String.format("%s/%s", workingDir, StageParameters.RESULTS_DIR_NAME);
        HelperFile.createDir(this.resultsDir);
    }

    @Override
    public void operate() {
        List<String> testFiles = HelperFile.readLinesFromTextFile(this.filePathWavList);
        for(String testFile:testFiles){
            AudioReader audioReader = new AudioReader(testFile);
            SpectrumImpl spectrum = new ReassignedSpectrumPercussivePart(audioReader, execParams.beatReasPercussivePartThreshold, execParams);
            OnsetDetectionFunction onsetDetectionFunction = new OnsetDetectionFunction(spectrum);
            float[] onsetDetectionFunctionData = onsetDetectionFunction.getDetectionFunction();
            float average = HelperArrays.calculateMean(onsetDetectionFunctionData);
            onsetDetectionFunctionData = HelperArrays.subtract(onsetDetectionFunctionData, onsetDetectionThreshold * average);

            int[] onsets = HelperArrays.searchPeakIndexes(onsetDetectionFunctionData, 0, onsetDetectionFunctionData.length, onsetDetectionFunctionData.length, 10, false);
            List<Double> outList = new ArrayList<Double>();
            for(int onset:onsets){
                outList.add(spectrum.getTimeMomentForIndex(onset));
            }

            String outFilePath = String.format("%s/%s%s", this.resultsDir, HelperFile.getNameWithoutExtension(testFile), Settings.ONSET_EXT);
            HelperFile.saveDoubleDataInTextFile(outList, outFilePath);
        }
    }


    public static void main(String[] args) {
        ConfigSettings.CONFIG_FILE_PATH = "./cfg/configOnset.cfg";
        Settings.initialize();
        OnsetDetectionOperation onsetDetectionOperation = new OnsetDetectionOperation(ExecParams._initialExecParameters);
        onsetDetectionOperation.initialize();
        onsetDetectionOperation.operate();
    }
}
