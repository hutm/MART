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

package org.mart.crs.exec.scenario;

import org.mart.crs.config.ConfigSettings;
import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.OperationType;
import org.mart.crs.exec.scenario.stage.*;
import org.mart.crs.utils.helper.HelperFile;
import org.mart.crs.config.ExecParams;

import static org.mart.crs.config.Settings.readProperties;


/**
 * @version 1.0 3/1/11 11:27 AM
 * @author: Hut
 */
public class RunAllScenario {


    public RunAllScenario() {

        Settings.setOperationType(OperationType.fromString(Settings.operationDomain));

        String parentRootDirPath = HelperFile.getFile(ExecParams._initialExecParameters._workingDir.replace("*", "")).getParent();
        PersistenceManager.setPersistentFileDirPath(parentRootDirPath);

        int startFold = 0;
        int endFold = Settings.numberOfFolds;

        if(System.getProperty("fold") != null){
            startFold = Integer.valueOf(System.getProperty("fold"));
            endFold = startFold + 1;
        }

        for (int fold = startFold; fold < endFold; fold++) {
            //Read parameter configuration
            readProperties(ConfigSettings.CONFIG_FILE_PATH);

            ExecParams execParams = ExecParams._initialExecParameters.getClone();
            execParams._workingDir = ExecParams._initialExecParameters._workingDir.replace("*", String.valueOf(fold));
            execParams._waveFilesTrainFileList = ExecParams._initialExecParameters._waveFilesTrainFileList.replace("*", String.valueOf(fold));
            execParams._waveFilesTestFileList = ExecParams._initialExecParameters._waveFilesTestFileList.replace("*", String.valueOf(fold));



            TestRecognizeStage testRecognizeStage = new TestRecognizeStage("_TEST_RECOGNIZE_", null);
            TestFeaturesStage testFeaturesStage = new TestFeaturesStage("_TEST_FEATURES_", testRecognizeStage);
            TestPreprocessingStage testPreprocessingStage = new TestPreprocessingStage("_TEST_PRE_", testFeaturesStage);

            TrainModelsStage trainModelsStage = new TrainModelsStage("_TRAIN_MODELS_", testPreprocessingStage);
            TrainFeaturesStage trainFeatureExtractionStage = new TrainFeaturesStage("_TRAIN_FEATURES_", trainModelsStage);
            TrainPreprocessingStage trainPreprocessingStage = new TrainPreprocessingStage("_TRAIN_PRE_", trainFeatureExtractionStage);


            StageParameters rootStageParameters = new StageParameters();
            rootStageParameters.setRootDirectory(execParams._workingDir);
            rootStageParameters.setPreviousStageConfigurationStringRepresentation("");

            trainPreprocessingStage.execute(rootStageParameters, execParams);
        }


        SummarizationStage summarizationStage = new SummarizationStage("_SUMMARY_", null);
        ExecParams execParams = ExecParams._initialExecParameters.getClone();
        execParams._workingDir = ExecParams._initialExecParameters._workingDir.replace("*", "");

        StageParameters rootStageParameters = new StageParameters();
        rootStageParameters.setRootDirectory(execParams._workingDir);
        rootStageParameters.setPreviousStageConfigurationStringRepresentation("");

        summarizationStage.execute(rootStageParameters, execParams);

    }


}
