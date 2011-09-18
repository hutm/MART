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

import org.mart.crs.exec.operation.AbstractCRSOperation;

/**
 * @version 1.0 Dec 3, 2010 7:14:01 PM
 * @author: Hut
 */
public class SimpleScenarioBeats {

    public SimpleScenarioBeats() {
        CRSScenario scenario = new CRSScenario(false);
        AbstractCRSOperation operation;


//        if (Settings.IS_TO_CREATE_LM) {
//            operation = new TrainingBeatLanguageModelsOperation(Settings._workingDir, Settings._waveFilesTrainFileList);
//            operation.setOperationType(OperationType.ONSET_OPERATION);
//            scenario.addOperation(operation);
//        }
//
//        if (Settings.isToTrainModels) {
//
//            if (Settings.isToExtractFeaturesForTrain) {
//                operation = new FeaturesExtractionOperation(Settings._workingDir, Settings._waveFilesTrainFileList, true, 1);
//                operation.setOperationType(OperationType.ONSET_OPERATION);
//                scenario.addOperation(operation);
//            }
//
//            operation = new TrainingAcousticModelsOperation(Settings._workingDir);
//            operation.setOperationType(OperationType.ONSET_OPERATION);
//            scenario.addOperation(operation);
//        }
//
//        if (Settings.isToTestModels) {
//            if (Settings.isToExtractFeaturesForTest) {
//                operation = new FeaturesExtractionOperation(Settings._workingDir, Settings._waveFilesTestFileList, false, 1);
//                operation.setOperationType(OperationType.ONSET_OPERATION);
//                scenario.addOperation(operation);
//            }
//
//            operation = new RecognizeOnsetConventionalVersionWithLMOperation(Settings._workingDir);
//            operation.setOperationType(OperationType.ONSET_OPERATION);
//            scenario.addOperation(operation);
//        }
//
//        scenario.run();
    }
}
