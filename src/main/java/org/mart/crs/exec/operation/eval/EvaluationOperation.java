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

package org.mart.crs.exec.operation.eval;

import org.apache.log4j.Logger;
import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.AbstractCRSOperation;
import org.mart.crs.exec.scenario.stage.PersistenceManager;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mart.crs.exec.scenario.stage.StageParameters.*;
/**
 * @version 1.0 3/3/11 2:36 PM
 * @author: Hut
 */
public class EvaluationOperation extends AbstractCRSOperation {


    protected static Logger logger = CRSLogger.getLogger(EvaluationOperation.class);

    protected Map<String, FileWriter> summaryFiles;

    public EvaluationOperation(StageParameters stageParameters, ExecParams execParams) {
        super(stageParameters, execParams);
        this.summaryFiles = new HashMap<String, FileWriter>();

    }

    @Override
    public void operate() {

        String resultsRootDir = this.dataDirPath;
        String outFilePath = String.format("%s/%s", stageParameters.getRootDirectory(), SUMMARY_FILENAME);
        List<AbstractCRSEvaluator> evaluators = this.operationDomain.getEvaluators();

        boolean isEvaluationresultsHeaderWritten = false;
        for (AbstractCRSEvaluator evaluator : evaluators) {
            evaluator.addSummaryData(ExecParams._initialExecParameters.getParamNamesCommaSeparated());
        }

        File[] resultDirs = HelperFile.getFile(resultsRootDir).listFiles();
        for (File resultDir : resultDirs) {
            if (!resultDir.isDirectory()) {
                continue;
            }
            File resultDirWithInnerFiles = HelperFile.getFile(String.format("%s/%s", resultDir, TEMP_DIR_NAME));
            File[] childDirList = resultDirWithInnerFiles.listFiles();
            if (childDirList == null) {
                continue;
            }
            for (File childDir : childDirList) {
                if (!childDir.isDirectory()) {
                    continue;
                }
                String outDirFilePath = String.format("%s/%s/%s%s", childDir.getParentFile().getParentFile().getPath(), DATA_DIR_NAME, childDir.getName(), Settings.TXT_EXT);

                for (AbstractCRSEvaluator evaluator : evaluators) {
                    evaluator.initializeDirectories(childDir.getPath(), Settings.labelsGroundTruthDir, outDirFilePath);
                    evaluator.evaluate();
                    if (!isEvaluationresultsHeaderWritten) {
                        evaluator.addSummaryData(evaluator.getResultsHeadersCommaSeparated() + "\r\n");
                    }

                    String configurationString = String.format("%s%s%s", PersistenceManager.getInstance().getMappedDirectoryNameFromNumber(resultDir.getName()), Settings.FIELD_SEPARATOR, childDir.getName());
                    evaluator.addSummaryData((ExecParams._initialExecParameters.parseExecParamsConfigurationFromString(configurationString)).getParamValuesCommaSeparated());
                    evaluator.addSummaryData(evaluator.getResultsValuesCommaSeparated());
                    evaluator.addSummaryData("\r\n");
                }
                isEvaluationresultsHeaderWritten = true;
            }
        }

        for (AbstractCRSEvaluator evaluator : evaluators) {
            evaluator.exportSummary(outFilePath);
        }


    }



}
