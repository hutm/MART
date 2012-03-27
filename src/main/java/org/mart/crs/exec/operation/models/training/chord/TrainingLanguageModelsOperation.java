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

package org.mart.crs.exec.operation.models.training.chord;

import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.AbstractCRSOperation;
import org.mart.crs.exec.operation.models.lm.LanguageModelChord;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.exec.scenario.stage.TrainModelsStage;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.io.IOException;

import static org.mart.crs.config.Settings.IS_TO_CREATE_LM;

/**
 * @version 1.0 11-Jun-2010 16:00:18
 * @author: Hut
 */
public class TrainingLanguageModelsOperation extends AbstractCRSOperation {

    protected String wavFileList;
    protected String lmDir;

    protected String netHViteFilePath;


    public TrainingLanguageModelsOperation(StageParameters stageParameters, ExecParams execParams) {
        super(stageParameters, execParams);


        TrainModelsStage trainModelsStage = (TrainModelsStage)stageParameters.getStage(TrainModelsStage.class);


        this.wavFileList = execParams._waveFilesTrainFileList;
        this.lmDir = trainModelsStage.getLanguageModelsDirPath();
        this.netHViteFilePath = trainModelsStage.getNetHViteFilePath();
    }


    @Override
    public void operate() {
        if (IS_TO_CREATE_LM) {
            try {
                createLanguageModels();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    protected void createLanguageModels() throws IOException {

        HelperFile.createDir(lmDir);

        //First create text for standard language model
        String textFilePath = lmDir + File.separator + "text_lan_model_standard";
        LanguageModelChord languageModel = new LanguageModelChord(Settings.chordLabelsGroundTruthDir, wavFileList, textFilePath);
        languageModel.process();
        languageModel.createLanguageModel(execParams.standardLmOrder, String.format("%s/%s", lmDir, StageParameters.LMModelStandardVersion));




    }

}
