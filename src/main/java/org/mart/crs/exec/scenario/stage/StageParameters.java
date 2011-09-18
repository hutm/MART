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

import org.mart.crs.utils.helper.Helper;

import java.util.HashMap;
import java.util.Map;


/**
 * This class represents a structure to be passed to a child stage. Contains parameters needed to run child stages.
 *
 * @version 1.0 2/18/11 1:47 PM
 * @author: Hut
 */
public class StageParameters implements Cloneable{

    /**
     * Root directory for the given set of the experiments
     */
    protected String rootDirectory;

    /**
     * Working directory for the current stage
     */
    protected String workingDir;

    /**
     * String prefix containing all the configuration parameters for the given stage
     */
    protected String previousStageConfigurationStringRepresentation;


    protected Map<Class, AbstractStage> stages;

    protected AbstractStage currentStage;


    public StageParameters() {
        this.stages = new HashMap<Class, AbstractStage>();

        stages.put(TrainPreprocessingStage.class, null);
        stages.put(TrainFeaturesStage.class, null);
        stages.put(TrainModelsStage.class, null);
        stages.put(TestPreprocessingStage.class, null);
        stages.put(TestFeaturesStage.class, null);
        stages.put(TestRecognizeStage.class, null);
        stages.put(SummarizationStage.class, null);
    }

    public void setStage(AbstractStage stage){
        Class stageClass = null;
        for(Class clazz:stages.keySet()){
            if(clazz.isInstance(stage)){
                stageClass = clazz;
            }
        }
        if(stageClass != null){
            stages.put(stageClass, stage);
        }
        currentStage = stage;
    }

    public AbstractStage getStage(Class clazz){
        return stages.get(clazz);
    }



    public String getRootDirectory() {
        return rootDirectory;
    }

    public void setRootDirectory(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public String getPreviousStageConfigurationStringRepresentation() {
        return previousStageConfigurationStringRepresentation;
    }

    public void setPreviousStageConfigurationStringRepresentation(String previousStageConfigurationStringRepresentation) {
        this.previousStageConfigurationStringRepresentation = previousStageConfigurationStringRepresentation;
    }


    public AbstractStage getCurrentStage() {
        return currentStage;
    }



    public StageParameters getClone() {
        try {
            return (StageParameters) this.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException(Helper.getStackTrace(e));
        }
    }


    public static final String binariesDir = "bin";
    public static final String hmmFolder = "hmm";
    public static final String FEATURES_DIR_NAME = "features";
    public static final String latticesDirName = "lattices";
    public static final String SUMMARY_FILENAME = "summary.txt";
    public static final String REF_FREQ_FILE_NAME = "ref_freq.txt";


    public static final String BEAT_SYMBOL = "beat";
    public static final String DOWNBEAT_SYMBOL = "downbeat";
    public static final String MEASURE_SYMBOL = "measure";
    public static final String NOTHING_SYMBOL = "nothing";

    public static final String BEAT_START = "beatStart";
    public static final String BEAT_END = "beatEnd";
    public static final String DOWNBEAT_START = "downbeatStart";
    public static final String DOWNBEAT_END = "downbeatEnd";
    public static final String NO_BEAT = "noBeat";


    public static final String[] chordArrayForTrain = {"C", "Cm"};
    public static final String[] beatArrayForTrain = {BEAT_SYMBOL, DOWNBEAT_SYMBOL};

    public static String[] beatLMArrayForTrain;
    public static final int maxNumberOBeatsInMeasure = 4;

    public static final int minNimberOfFramesForBeatSegment = 20;
    public static final int maxNimberOfFramesForBeatSegment = 100;

    public static String[] beatJumpArrayForTrain = {BEAT_START, BEAT_END, DOWNBEAT_START, DOWNBEAT_END, NO_BEAT};
    public static String[] beatOnlyArrayForTrain = {BEAT_START, BEAT_END, NO_BEAT};


    public static final String hmmDefs = "hmmdefs";
    public static final String macros = "macros";

    public static final String LMSpecFileName = "spec_w.flm";
    public static final String LMSpecFactoredFileName = "spec_wd.flm";
    public static final String LMTemplateFilePath = "LM/spec_w_template_3.flm";
    public static final String LMTemplateFactoredFilePath = "LM/spec_wd_template_2.flm";
    public static final String LMCountsFile = "flm_w.count.gz";
    public static final String LMFactoredCountsFile = "flm_wd.count.gz";
    public static final String LMModelFile = "flm_w.lm.gz";
    public static final String LMFactoredModelFile = "flm_wd.lm.gz";
    public static final String LMModelStandardVersion = "language_model_stnd.lm";


    public static final String TEMP_DIR_NAME = "temp";
    public static final String DATA_DIR_NAME = "data";
    public static final String TRAINED_DIR_NAME = "trained";
    public static final String RESULTS_DIR_NAME = "results";



    public static final String MLF_FILE = "chordlabel.mlf";
    public static final String WORD_LIST_TRAIN = "wordListTrain";
    public static final String WORD_LIST_TEST = "wordListTest";
    public static final String WORD_LIST_LM = "wordListLM";
    public static final String HTK_CONFIG = "htk_config.cfg";
    public static final String PROTOTYPE = "proto";
    public static final String HMM_DIR = "train_hmm";
    public static final String HED_FILE = "hed";
    public static final String GRAM_FILE = "gram";
    public static final String DICT_FILE = "dict";
    public static final String NET_FILE = "net";
    public static final String NET_HVITE_FILE = "netHVite";
    public static final String DECODED_OUT = "out";
    public static final String FEATURE_FILELIST_TRAIN = "featureListTrain.lst";
    public static final String FEATURE_FILELIST_TEST = "featureListTest.lst";
    public static final String LM_DIR = "LM";


}
