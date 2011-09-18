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

package org.mart.crs.exec.scenario.stage;

import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.features.ReferenceFrequencyExtractionOperation;
import org.mart.crs.utils.helper.HelperFile;

import static org.mart.crs.exec.scenario.stage.StageParameters.REF_FREQ_FILE_NAME;

/**
 * @version 1.0 3/2/11 10:44 PM
 * @author: Hut
 */
public class TrainPreprocessingStage extends AbstractStage implements ITrainTest {

    public TrainPreprocessingStage(String parameterPrefix, AbstractStage childStage) {
        super(parameterPrefix, childStage);
        this.stageName = TRAIN_PREPROCESSING_STAGE_NAME;
        this.stageNumber = TRAIN_PREPROCESSING_STAGE_NUMBER;
    }


    @Override
    public void runStage(StageParameters stageParameters, ExecParams execParams) {
        if (Settings.isToUseRefFreq) {
            scenario.addOperation(new ReferenceFrequencyExtractionOperation(stageParameters, execParams));
        }
    }

    @Override
    public boolean isDataOutputFound() {
        return HelperFile.getFile(getRefFreqFilePath()).exists();
    }

    public String getFeaturesDirPath() {
        return getFilePathInDataDirectory("");
    }

    public String getFileListToProcess() {
        return execParams._waveFilesTrainFileList;
    }

    public boolean isFeaturesForTrain() {
        return true;
    }

    public String getRefFreqFilePath() {
        return getFilePathInDataDirectory(REF_FREQ_FILE_NAME);
    }

}
