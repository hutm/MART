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

package org.mart.crs.exec.operation;

import org.mart.crs.config.ExecParams;
import org.mart.crs.exec.operation.eval.AbstractCRSEvaluator;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.features.FeaturesManager;
import org.mart.crs.utils.helper.Helper;
import org.apache.log4j.Logger;
import org.mart.crs.utils.helper.HelperFile;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @version 1.0 2/22/11 11:30 PM
 * @author: Hut
 */
//------------------Interface for Domain---------------
public abstract class OperationDomain {

    protected static Logger logger = CRSLogger.getLogger(OperationDomain.class);


    protected AbstractCRSOperation crsOperation;


    public void setCrsOperation(AbstractCRSOperation crsOperation) {
        this.crsOperation = crsOperation;
    }

    public void createConfig() {
        try {
            FileWriter fileWriter = new FileWriter(HelperFile.getFile(crsOperation.configPath));
            fileWriter.write("TARGETKIND = USER\n" +
                    "NATURALREADORDER = TRUE \n");
            fileWriter.close();
        } catch (IOException e) {
            logger.error("Problems: ");
            logger.error(Helper.getStackTrace(e));
        }
    }

    public abstract void createWordLists();

    public abstract void createMLF();

    public abstract void createDictionaryFile();

    public abstract List<AbstractCRSEvaluator> getEvaluators();

    public abstract AbstractCRSOperation getRecognizeOperation(StageParameters stageParameters, ExecParams execParams);

    public abstract AbstractCRSOperation getRecognizeLanguageModelOperation(StageParameters stageParameters, ExecParams execParams);

    public abstract AbstractCRSOperation getTrainLanguageModelsOperation(StageParameters stageParameters, ExecParams execParams);

    public abstract FeaturesManager getFeaturesManager(String songFilePath, String outDirPath, boolean isForTraining, ExecParams execParams);
}
