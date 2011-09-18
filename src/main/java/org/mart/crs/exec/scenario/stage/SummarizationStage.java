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

import org.mart.crs.config.ConfigSettings;
import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.eval.EvaluationOperation;
import org.mart.crs.utils.helper.HelperFile;
import org.mart.crs.config.ExecParams;

import java.io.File;

import static org.mart.crs.exec.scenario.stage.StageParameters.DATA_DIR_NAME;
import static org.mart.crs.exec.scenario.stage.StageParameters.TEMP_DIR_NAME;

/**
 * @version 1.0 3/3/11 2:10 AM
 * @author: Hut
 */
public class SummarizationStage extends AbstractStage {


    public SummarizationStage(String parameterPrefix, AbstractStage childStage) {
        super(parameterPrefix, childStage);
        this.stageName = SUMMaRY_STAGE_NAME;
        this.stageNumber = SUMMARY_STAGE_NUMBER;
    }

    @Override
    public String getStageName() {
        return "summary";
    }

    @Override
    public void runStage(StageParameters stageParameters, ExecParams execParams) {
        //Copy output data from folds
        for(int fold = 0; fold < Settings.numberOfFolds; fold++){
            String foldDirectory = ExecParams._initialExecParameters._workingDir.replace("*", String.valueOf(fold));
            String recognizeStageOutuputDirectory = String.format("%s/%d-%s", foldDirectory, TEST_RECOGNITION_STAGE_NUMBER, TEST_RECOGNITION_STAGE_NAME);
            File[] recognizedDirectories = HelperFile.getFile(recognizeStageOutuputDirectory).listFiles();
            for(File directory:recognizedDirectories){
                String directoryToCopyFilePath =  String.format("%s/%s", getFilePathInDataDirectory(directory.getName()), TEMP_DIR_NAME);
                String dirFrom = String.format("%s/%s", directory.getPath(), DATA_DIR_NAME);
                HelperFile.copyDirectory(dirFrom, directoryToCopyFilePath);
            }
        }
        scenario.addOperation(new EvaluationOperation(stageParameters, execParams));

        //Save them to the output folder if necessary
        if (ConfigSettings.outPathExternal != null && !ConfigSettings.outPathExternal.equals("")) {
            String foldDirectory = ExecParams._initialExecParameters._workingDir.replace("*", "");
            HelperFile.copyDirectory(foldDirectory, ConfigSettings.outPathExternal);

        }
    }
}
