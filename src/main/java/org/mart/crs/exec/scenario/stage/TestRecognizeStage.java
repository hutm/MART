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
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.helper.HelperFile;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * @version 1.0 2/15/11 7:02 PM
 * @author: Hut
 */
public class TestRecognizeStage extends AbstractStage {

    protected static Logger logger = CRSLogger.getLogger(TestRecognizeStage.class);

    public TestRecognizeStage(String parameterPrefix, AbstractStage childStage) {
        super(parameterPrefix, childStage);
        this.stageName = TEST_RECOGNITION_STAGE_NAME;
        this.stageNumber = TEST_RECOGNITION_STAGE_NUMBER;
    }


    @Override
    public void runStage(StageParameters stageParameters, ExecParams execParams) {
        if (!Settings.isToUseLMs) {
            scenario.addOperation(Settings.operationType.getOperationDomain(null).getRecognizeOperation(stageParameters, execParams));
        } else {
            scenario.addOperation(Settings.operationType.getOperationDomain(null).getRecognizeLanguageModelOperation(stageParameters, execParams));
        }
    }

    @Override
    public boolean isDataOutputFound() {
        File resultsDir = HelperFile.getFile(getResultsDirPath());
        return resultsDir.exists() && resultsDir.listFiles().length > 0;
    }

    public String getResultsDirPath() {
        return getFilePathInDataDirectory("");
    }

    @Override
    public int getNumberOfThreads() {
        return Settings.threadsNumberForFeatureExtraction;
    }
}
