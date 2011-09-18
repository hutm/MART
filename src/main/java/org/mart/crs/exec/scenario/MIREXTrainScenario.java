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

import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.OperationType;
import org.mart.crs.exec.scenario.stage.*;
import org.mart.crs.utils.helper.HelperFile;

/**
 * @version 1.0 11-Oct-2010 16:41:42
 * @author: Hut
 */
public class MIREXTrainScenario {

    public MIREXTrainScenario() {
        Settings.setOperationType(OperationType.fromString(Settings.operationDomain));

        String parentRootDirPath = HelperFile.getFile(ExecParams._initialExecParameters._workingDir.replace("*", "")).getParent();
        PersistenceManager.setPersistentFileDirPath(parentRootDirPath);

        ExecParams execParams = ExecParams._initialExecParameters.getClone();

//        TestRecognizeStage testRecognizeStage = new TestRecognizeStage("_TEST_RECOGNIZE_", null);
//        TestFeaturesStage testFeaturesStage = new TestFeaturesStage("_TEST_FEATURES_", testRecognizeStage);
//        TestPreprocessingStage testPreprocessingStage = new TestPreprocessingStage("_TEST_PRE_", testFeaturesStage);

        TrainModelsStage trainModelsStage = new TrainModelsStage("_TRAIN_MODELS_", null);
        TrainFeaturesStage trainFeatureExtractionStage = new TrainFeaturesStage("_TRAIN_FEATURES_", trainModelsStage);
        TrainPreprocessingStage trainPreprocessingStage = new TrainPreprocessingStage("_TRAIN_PRE_", trainFeatureExtractionStage);


        StageParameters rootStageParameters = new StageParameters();
        rootStageParameters.setRootDirectory(execParams._workingDir);
        rootStageParameters.setPreviousStageConfigurationStringRepresentation("");

        trainPreprocessingStage.execute(rootStageParameters, execParams);

    }
}
