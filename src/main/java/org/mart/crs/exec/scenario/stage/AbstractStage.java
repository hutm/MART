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
import org.mart.crs.exec.scenario.CRSScenario;
import org.mart.crs.utils.Combinator;
import org.mart.crs.utils.CRSThreadPoolExecutor;
import org.mart.crs.utils.ReflectUtils;
import org.mart.crs.utils.helper.HelperFile;

import static org.mart.crs.exec.scenario.stage.StageParameters.DATA_DIR_NAME;

/**
 * @version 1.0 2/15/11 5:51 PM
 * @author: Hut
 * <p/>
 * This class defines a stage in the experimental evaluation
 */
public abstract class AbstractStage {

    /**
     * Working directory of this stage
     */
    protected String workingDir;


    /**
     * Prefix that corresponds to the parameters relevant to the stage
     */
    protected String parameterPrefix;


    /**
     * Child stage that is needed to be run for each parameter configuration for the given stage
     */
    protected AbstractStage childStage;

    protected CRSScenario scenario;

    protected ExecParams execParams;

    protected StageParameters stageParameters;

    protected String stageName;
    protected int stageNumber;


    public static final String TRAIN_PREPROCESSING_STAGE_NAME = "trPr";
    public static final String TRAIN_FEATURES_STAGE_NAME = "trainFeat";
    public static final String TRAIN_MODELS_STAGE_NAME = "trainMod";
    public static final String TEST_PREPROCESSING_STAGE_NAME = "tsPr";
    public static final String TEST_FEATURES_STAGE_NAME = "testFeat";
    public static final String TEST_RECOGNITION_STAGE_NAME = "testRec";
    public static final String SUMMaRY_STAGE_NAME = "summary";


    public static final int TRAIN_PREPROCESSING_STAGE_NUMBER = 1;
    public static final int TRAIN_FEATURES_STAGE_NUMBER = 2;
    public static final int TRAIN_MODELS_STAGE_NUMBER = 3;
    public static final int TEST_PREPROCESSING_STAGE_NUMBER = 4;
    public static final int TEST_FEATURES_STAGE_NUMBER = 5;
    public static final int TEST_RECOGNITION_STAGE_NUMBER = 6;
    public static final int SUMMARY_STAGE_NUMBER = 7;


    public AbstractStage(String parameterPrefix, AbstractStage childStage) {
        this.parameterPrefix = parameterPrefix;
        this.childStage = childStage;
        scenario = new CRSScenario(true);
    }


    public void execute(StageParameters stageParameters_, ExecParams execParams) {
        this.stageParameters = stageParameters_.getClone();
        stageParameters.setStage(this);
        Combinator stageCombinator = new Combinator(ReflectUtils.getSettingsVariables(execParams, parameterPrefix));

        String previousStageParameterString = stageParameters.getPreviousStageConfigurationStringRepresentation();

        PersistenceManager persistenceManager = PersistenceManager.getInstance();

        CRSThreadPoolExecutor poolExecutor = new CRSThreadPoolExecutor(getNumberOfThreads());

        do {

            this.execParams = execParams.getClone();

            String combinationStringRepresentation = stageCombinator.setNextConfiguration(this.execParams);
            String currentConfigurationStringRepresentation = String.format("%s#%s%s", previousStageParameterString, getStageName(), combinationStringRepresentation);
            stageParameters.setPreviousStageConfigurationStringRepresentation(currentConfigurationStringRepresentation);

            String workingDirName = persistenceManager.getMappingOfWorkingDirectory(currentConfigurationStringRepresentation);
            this.workingDir = String.format("%s/%s/%s", stageParameters.getRootDirectory(), getStageName(), workingDirName);
            stageParameters.setWorkingDir(workingDir);

            if (isRunNeeded()) {
                runStage(stageParameters.getClone(), AbstractStage.this.execParams.getClone());
            }
            if (childStage != null) {
                childStage.execute(stageParameters.getClone(), AbstractStage.this.execParams.getClone());
            }


        } while (stageCombinator.hasMoreCombinations());

        poolExecutor.waitCompletedAndshutDown();

    }


    public String getFilePathInDataDirectory(String relativePath) {
        return String.format("%s/%s/%s", workingDir, DATA_DIR_NAME, relativePath);
    }


    public boolean isRunNeeded() {
        boolean contains = false;
        for (int i = 0; i < Settings.stagesToRun.length; i++) {
            if (stageNumber == Settings.stagesToRun[i]) {
                contains = true;
                break;
            }
        }
        return contains || !isDataOutputFound();
    }


    /**
     * This method is supposed to be overridden
     *
     * @return
     */
    public boolean isDataOutputFound() {
        String dataDir = String.format("%s/%s", workingDir, DATA_DIR_NAME);
        return HelperFile.getFile(dataDir).exists();
    }

    /**
     * Max number of possible parallel threads for this stage
     *
     * @return number of threads
     */
    public int getNumberOfThreads() {
        return 1;
    }

    public String getStageName() {
        return String.format("%d-%s", stageNumber, stageName);
    }

    public abstract void runStage(StageParameters stageParameters, ExecParams execParams);

}
