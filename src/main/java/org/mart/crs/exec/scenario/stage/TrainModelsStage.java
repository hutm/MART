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
import org.mart.crs.exec.operation.ChordFullTrainingOperationDomain;
import org.mart.crs.exec.operation.OperationType;
import org.mart.crs.exec.operation.models.training.chord.TrainingAcousticModelsOperation;
import org.mart.crs.utils.helper.HelperFile;

import static org.mart.crs.exec.scenario.stage.StageParameters.*;
/**
 * @version 1.0 2/15/11 7:00 PM
 * @author: Hut
 */
public class TrainModelsStage extends AbstractStage {


    public TrainModelsStage(String parameterPrefix, AbstractStage childStage) {
        super(parameterPrefix, childStage);
        this.stageName = TRAIN_MODELS_STAGE_NAME;
        this.stageNumber = TRAIN_MODELS_STAGE_NUMBER;
    }


    @Override
    public void runStage(StageParameters stageParameters, ExecParams execParams) {
        if (Settings.isToTrainModels) {
            if (Settings.IS_TO_CREATE_LM) {
                scenario.addOperation(Settings.operationType.getOperationDomain(null).getTrainLanguageModelsOperation(stageParameters, execParams));
            }
            if (Settings.operationType.equals(OperationType.CHORD_OPERATION_FULL_TRAIN)) {
                String[] tails = ChordFullTrainingOperationDomain.getFileTails();
                for(int i = 0; i < tails.length; i++){
                    scenario.addOperation(new TrainingAcousticModelsOperation(stageParameters, execParams, tails[i], i));
                }
            } else{
                scenario.addOperation(new TrainingAcousticModelsOperation(stageParameters, execParams));
            }
        }
    }

    @Override
    public boolean isDataOutputFound() {
        return HelperFile.getFile(getLanguageModelsDirPath()).exists() &&
                HelperFile.getFile(getFilePathInDataDirectory(String.format("%s_1", hmmFolder))).exists();
    }

    public String getHMMsDirPath() {
        return getFilePathInDataDirectory("");
    }

    public String getLanguageModelsDirPath() {
        return getFilePathInDataDirectory(LM_DIR);
    }

    public String getNetHViteFilePath() {
        return getFilePathInDataDirectory(NET_HVITE_FILE);
    }

}
