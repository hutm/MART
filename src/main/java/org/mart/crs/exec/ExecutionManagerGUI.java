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

package org.mart.crs.exec;

import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.models.test.chord.RecognizeOperation;
import org.mart.crs.exec.scenario.CRSScenario;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.util.Map;

/**
 * @version 1.0 06-Jul-2010 16:46:50
 * @author: Hut
 */
public class ExecutionManagerGUI {

    public static void recognizeNewSongs(Map<String, String> fileCorrespondanceMap) {
        ExecParams._initialExecParameters._workingDir = "work";
        CRSScenario scenario = new CRSScenario(false);
        StageParameters stageParameters = new StageParameters();
        stageParameters.setWorkingDir(ExecParams._initialExecParameters._workingDir);
//TODO fix this problem
//        scenario.addOperation(new FeaturesExtractionOperation(stageParameters, ExecParams._initialExecParameters, ExecParams._initialExecParameters._waveFilesTestFileList, false, 1));
        scenario.addOperation(new RecognizeOperation(stageParameters, ExecParams._initialExecParameters));

        scenario.run();
        String outLabelsDir = ExecParams._initialExecParameters._workingDir + File.separator + "labels" + File.separator;
        String outLabelsDirLM = ExecParams._initialExecParameters._workingDir + File.separator + "labelsLM" + File.separator;

        HelperFile.getFile(outLabelsDir).mkdir();
        HelperFile.getFile(outLabelsDirLM).mkdir();

        String resultsDir = ExecParams._initialExecParameters._workingDir + "/results/results_1024_-20.0" + File.separator;
        String resultsDirLM = ExecParams._initialExecParameters._workingDir + "/results/results_1024_-20.0_lm_9.00_ac_1.00_p_-3.00_factored_false" + File.separator;
        for (String s : fileCorrespondanceMap.keySet()) {
            HelperFile.copyFile(resultsDir + HelperFile.getShortFileNameWithoutExt(fileCorrespondanceMap.get(s)) + Settings.LABEL_EXT, outLabelsDir + HelperFile.getShortFileNameWithoutExt(s) + Settings.LABEL_EXT);
            HelperFile.copyFile(resultsDirLM + HelperFile.getShortFileNameWithoutExt(fileCorrespondanceMap.get(s)) + Settings.LABEL_EXT, outLabelsDirLM + HelperFile.getShortFileNameWithoutExt(s) + Settings.LABEL_EXT);
        }

    }

}
