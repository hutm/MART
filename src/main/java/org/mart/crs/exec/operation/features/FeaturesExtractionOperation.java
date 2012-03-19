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
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.audio.ReferenceFreqManager;
import org.mart.crs.management.features.FeaturesManager;
import org.mart.crs.utils.CRSThreadPoolExecutor;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static org.mart.crs.utils.helper.HelperFile.getFile;

/**
 * @version 1.0 11-Jun-2010 14:53:33
 * @author: Hut
 */
public class FeaturesExtractionOperation extends AbstractCRSOperation {

    protected static Logger logger = CRSLogger.getLogger(FeaturesExtractionOperation.class);

    protected String fileListPath;
    protected String outDirPath;
    protected boolean isForTraining;
    protected int numberOfParralelThreads;

    protected String refFreqFilePath;


    protected List<String> fileListCollection;


    public FeaturesExtractionOperation(StageParameters stageParameters, ExecParams execParams) {
        super(stageParameters, execParams);
        ITrainTest currentStage = (ITrainTest)stageParameters.getCurrentStage();

        this.fileListPath = currentStage.getFileListToProcess();
        this.isForTraining = currentStage.isFeaturesForTrain();
        this.numberOfParralelThreads = Settings.threadsNumberForFeatureExtraction;

        outDirPath = currentStage.getFeaturesDirPath();
        refFreqFilePath = currentStage.getRefFreqFilePath();
    }
    
    
    public FeaturesExtractionOperation(String fileListPath, boolean isForTraining, String outDirPath, String refFreqFilePath){
        this.fileListPath = fileListPath;
        this.isForTraining = isForTraining;
        this.numberOfParralelThreads = Settings.threadsNumberForFeatureExtraction;

        this.outDirPath = outDirPath;
        this.refFreqFilePath = refFreqFilePath;
    }


    @Override
    public void initialize() {
        super.initialize();
        this.fileListCollection = HelperFile.readLinesFromTextFile(this.fileListPath);

        File fileList = getFile(fileListPath);
        logger.info("FileList to process: " + fileList.getPath());
        if (!fileList.exists() || fileList.isDirectory()) {
            logger.error("File " + fileListPath + " does not exist");
            logger.error("Feature extraction was aborted...");
            return;
        }


        if (Settings.isToDeleteTrainFeaturesAfterTraining && !isForTraining) {
            HelperFile.deleteDirectory(outDirPath);
        }

        prepareOutputDirs();
    }

    //TODO remove this method
    public void initializeSphinx() {
        this.operationDomain = Settings.operationType.getOperationDomain(this);
        this.execParams = ExecParams._initialExecParameters;
        this.fileListCollection = HelperFile.readLinesFromTextFile(this.fileListPath);

        File fileList = getFile(fileListPath);
        logger.info("FileList to process: " + fileList.getPath());
        if (!fileList.exists() || fileList.isDirectory()) {
            logger.error("File " + fileListPath + " does not exist");
            logger.error("Feature extraction was aborted...");
            return;
        }

        prepareOutputDirs();
    }


    @Override
    public void operate() {
        CRSThreadPoolExecutor poolExecutor = new CRSThreadPoolExecutor(Settings.threadsNumberForFeatureExtraction);

        for (String song : fileListCollection) {
            Runnable runnable = new Task(song, refFreqFilePath);
            poolExecutor.runTask(runnable);
        }
        poolExecutor.waitCompletedAndshutDown();
    }


    protected class Task implements Runnable {

        protected String song;
        protected String refFreqFilePath;


        public Task(String song, String refFreqFilePath) {
            this.song = song;
            this.refFreqFilePath = refFreqFilePath;
        }

        public void run() {
            try {
                FeaturesManager featuresManager =  operationDomain.getFeaturesManager(song, outDirPath, isForTraining, execParams);
                featuresManager.extractFeaturesForSong(ReferenceFreqManager.getReferenceFreqManager(refFreqFilePath));

            } catch (Exception e) {
                logger.error("Problem with extracting features from song " + song);
                logger.error(Helper.getStackTrace(e));
            }
        }
    }


    protected void prepareOutputDirs() {
        //create output dir
        File outDir = getFile(outDirPath);
        if (!outDir.exists()) {
            logger.debug("Creating Directory " + outDirPath);
            outDir.mkdirs();
        } else {
            logger.debug("Directory " + outDirPath + " exists");
        }


        //Create inner directories
        try {
            createOutputDirs(fileListCollection, outDir);
        } catch (Exception e) {
            logger.fatal("Cannnot write to folder " + outDirPath + ". Exiting...", e);
            return;
        }
        logger.info("Directories for output data were created successfully");
    }


    /**
     * Creates output directories
     *
     * @param songs  Set of songs for which dirs will be created
     * @param outDir output directory to make folders inside
     */
    public static void createOutputDirs(Collection<String> songs, File outDir) {
        File dir;
        String dirName;
        for (String song : songs) {
            dirName = outDir + File.separator + HelperFile.getNameWithoutExtension(song);
            dir = getFile(dirName);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
    }


}
