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

import org.mart.crs.config.Settings;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.helper.Helper;
import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @version 1.0 3/31/11 9:45 AM
 * @author: Hut
 */
public abstract class AbstractCRSEvaluator {

    protected static Logger logger = CRSLogger.getLogger(AbstractCRSEvaluator.class);


    protected StringBuffer summaryData;


    protected AbstractCRSEvaluator() {
        summaryData = new StringBuffer();
    }

    public abstract void initializeDirectories(String recognizedLabelsDir, String groundTruthLabelsDir, String outputFilePath);

    public abstract void evaluate();

    public abstract String getResultsHeadersCommaSeparated();

    public abstract String getResultsValuesCommaSeparated();

    public void addSummaryData(String data){
        summaryData.append(data);
    }

    public void exportSummary(String outFileName){
        outFileName = outFileName.replaceAll(Settings.TXT_EXT, String.format("%s%s", this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".") + 1), Settings.TXT_EXT));
        try {
            FileWriter writer = new FileWriter(outFileName);
            writer.write(summaryData.toString());
            writer.close();
        } catch (IOException e) {
            logger.error("Could not export summary data to file");
            logger.error(Helper.getStackTrace(e));
        }
    }

}
