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

package org.mart.crs.exec.operation.features;

import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.AbstractCRSOperation;
import org.mart.crs.exec.scenario.stage.ITrainTest;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.management.audio.ReferenceFreqManager;
import org.mart.crs.utils.helper.HelperFile;
import org.mart.tools.tuning.Tuner;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.mart.crs.utils.helper.HelperFile.getFile;

/**
 * @version 1.0 25-Jun-2010 14:39:16
 * @author: Hut
 */
public class ReferenceFrequencyExtractionOperation extends AbstractCRSOperation {

    protected String refFreqFilePath;

    protected List<String> fileListCollection;


    public ReferenceFrequencyExtractionOperation(StageParameters stageParameters, ExecParams execParams) {
        super(stageParameters, execParams);
        refFreqFilePath = ((ITrainTest)stageParameters.getCurrentStage()).getRefFreqFilePath();
    }




    @Override
    public void initialize() {
        super.initialize();

        String fileListPath = ((ITrainTest)stageParameters.getCurrentStage()).getFileListToProcess();
        this.fileListCollection = HelperFile.readLinesFromTextFile(fileListPath);

        File fileList = getFile(fileListPath);
        logger.info("FileList to process: " + fileList.getPath());
        if (!fileList.exists() || fileList.isDirectory()) {
            logger.error("File " + fileListPath + " does not exist");
            logger.error("Reference frequency extraction was aborted...");
        }


        if(!Settings.forceReExtractRefFreq){
            HelperFile.copyFile(Settings.tuningsGroudTruthFilePath, this.refFreqFilePath);
        }

    }

    @Override
    public void operate() {
        ReferenceFreqManager manager = new ReferenceFreqManager(this.refFreqFilePath);
        for(String filePath:fileListCollection){
            if(filePath.trim().length() == 0){
                continue;
            }
            if(!manager.containSong(filePath) || Settings.forceReExtractRefFreq){
                Tuner tuner = null;
                try {
                    logger.info(String.format("Extracting reference frequency from file %s", HelperFile.getFile(filePath).getName()));
                    Constructor c = Class.forName(Settings.REF_FREQ_TUNER).getConstructor(new Class[]{String.class});
                    tuner = (Tuner) c.newInstance(filePath);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                float refFreq = tuner.getReferenceFrequency();
                manager.addExtractedRefFreq(filePath, refFreq);
            }
        }
        manager.saveRefFreqs();
    }


}
