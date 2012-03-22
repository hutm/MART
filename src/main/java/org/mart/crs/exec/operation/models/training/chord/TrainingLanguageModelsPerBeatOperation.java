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
import org.mart.crs.exec.operation.models.lm.TextForLMCreator;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.io.IOException;

/**
 * @version 1.0 3/20/12 7:02 PM
 * @author: Hut
 */
public class TrainingLanguageModelsPerBeatOperation extends TrainingLanguageModelsOperation {

    public TrainingLanguageModelsPerBeatOperation(StageParameters stageParameters, ExecParams execParams) {
        super(stageParameters, execParams);
    }


    @Override
    protected void createLanguageModels() throws IOException {

        HelperFile.createDir(lmDir);

        //First create text for standard language model
        String textFilePath = lmDir + File.separator + "text_lan_model_standard";
        (new TextForLMCreator()).process(wavFileList, textFilePath, false);

        String command = String.format("%s/ngram-count -text %s -order %d -wbdiscount -lm %s/%s", StageParameters.binariesDir, textFilePath, execParams.standardLmOrder,  lmDir, StageParameters.LMModelStandardVersion);
        Helper.execCmd(command);
    }
}
