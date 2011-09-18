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
import org.mart.crs.config.Settings;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.helper.HelperFile;
import org.apache.log4j.Logger;

import java.io.File;

import static org.mart.crs.config.ConfigSettings.CONFIG_FILE_PATH;
import static org.mart.crs.utils.helper.HelperFile.*;
import static org.mart.crs.exec.scenario.stage.StageParameters.*;

/**
 * @version 1.0 11-Jun-2010 15:40:07
 * @author: Hut
 */
public abstract class AbstractCRSOperation extends Operation {

    protected static Logger logger = CRSLogger.getLogger(AbstractCRSOperation.class);

    protected OperationDomain operationDomain;

    protected StageParameters stageParameters;
    protected ExecParams execParams;


    /**
     * This directory contains location of output files for this operation
     */
    protected String dataDirPath;

    /**
     * This folder contains location of temporary files for this operation
     */
    protected String tempDirPath;

    //Temp directory files and dirs
    protected String mlfFilePath;
    protected String wordListTrainPath;
    protected String wordListTestPath;
    protected String wordListLMPath;
    protected String configPath;
    protected String dictFilePath;



    protected String[] wordArrayForTrain;


    public AbstractCRSOperation(StageParameters stageParameters, ExecParams execParams) {
        super(stageParameters.getWorkingDir());
        this.stageParameters = stageParameters;
        this.execParams = execParams;
        this.operationDomain = Settings.operationType.getOperationDomain(this);
    }


    protected void setDataDirPath(String dataDirPath) {
        this.dataDirPath = dataDirPath;
    }

    protected void setTempDirPath(String tempDirPath_) {
        HelperFile.createDir(tempDirPath_);
        tempDirPath = tempDirPath_;
        mlfFilePath = String.format("%s/%s", tempDirPath, MLF_FILE);
        wordListTrainPath = String.format("%s/%s", tempDirPath, WORD_LIST_TRAIN);
        wordListTestPath = String.format("%s/%s", tempDirPath, WORD_LIST_TEST);
        wordListLMPath = String.format("%s/%s", tempDirPath, WORD_LIST_LM);
        configPath = String.format("%s/%s", tempDirPath, HTK_CONFIG);
        dictFilePath = String.format("%s/%s", tempDirPath, DICT_FILE);
    }


    public void initialize() {
        String prefix = workingDir + File.separator;
        setDataDirPath(prefix + DATA_DIR_NAME);
        setTempDirPath(prefix + TEMP_DIR_NAME);
        createDir(dataDirPath);
        createDir(tempDirPath);
        copyConfigFile(); //TODO refctor this
        operationDomain.createWordLists();
        operationDomain.createMLF();
        operationDomain.createConfig();
    }




    protected void copyConfigFile() {
        //TODO refctor this
        File config = getFile(CONFIG_FILE_PATH);
        File configDest = getFile(tempDirPath + File.separator + ".." + File.separator + config.getName());
        copyFile(config.getAbsolutePath(), configDest.getAbsolutePath());
    }


    public ExecParams getExecParams() {
        return execParams;
    }
}
