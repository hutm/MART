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

import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.OperationType;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.helper.HelperFile;
import org.apache.log4j.Logger;
import org.mart.crs.config.ExecParams;
import org.mart.crs.exec.scenario.stage.*;

/**
 * @version 1.0 11-Oct-2010 16:41:52
 * @author: Hut
 */
public class MIREXTestScenario {

    protected static Logger logger = CRSLogger.getLogger(MIREXTestScenario.class);


    public MIREXTestScenario() {
        Settings.setOperationType(OperationType.fromString(Settings.operationDomain));

        Settings.stagesToRun = new int[]{4, 5, 6};

        String parentRootDirPath = HelperFile.getFile(ExecParams._initialExecParameters._workingDir.replace("*", "")).getParent();
        PersistenceManager.setPersistentFileDirPath(parentRootDirPath);

        ExecParams execParams = ExecParams._initialExecParameters.getClone();

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

//
//        try {
//            HelperFile.copyDirectory(String.format("%s/results/results_2048_-20.0_lm_9.00_ac_1.00_p_-3.00_factored_false", execParams._workingDir), ConfigSettings.outPathExternal);
//        } catch (IOException e) {
//            logger.error(String.format("Cannot copy output labels directory to %s", ConfigSettings.outPathExternal));
//            logger.error(Helper.getStackTrace(e));
//        }
    }
}
