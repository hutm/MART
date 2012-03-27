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

package org.mart.crs.config;

import org.mart.crs.exec.operation.OperationType;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.ReflectUtils;
import org.mart.crs.utils.helper.HelperFile;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * User: Hut
 * Date: 11.05.2008
 * Class containing all constants
 */
public class Settings {

    protected static Logger logger = CRSLogger.getLogger(Settings.class);

    private static Properties properties;


    //Files
    public static String DYNAMIC_LIBRARY_EXTENSION;
    public static String EXECUTABLE_EXTENSION;
    public static String SCRIPT_EXTENSION;


    //Extensions
    public static final String WAV_EXT = ".wav";
    public static final String MP3_EXT = ".mp3";
    public static final String CHROMA_EXT = ".chr";
    public static final String CHROMA_SEC_PASS_EXT = ".chr2";
    public static String LABEL_EXT = ".lab";
    public static final String BEAT_EXT = ".xml";
    public static final String LATTICE_EXT = ".lattice";
    public static final String LATTICE_SEC_PASS_EXT = ".lattice2";
    public static final String CHANNELS_STORED_EXT = ".chan";
    public static final String MAT_EXT = ".mat";
    public static final String CFG_EXT = ".cfg";
    public static final String TXT_EXT = ".txt";
    public static final String ONSET_EXT = ".txt";
    public static final String BEAT_TXT_EXT = ".beattxt";
    public static final String BEAT_MAZURKA_EXT = ".tap";
    public static final String[] BEAT_EXTENSIONS = new String[]{Settings.BEAT_EXT, Settings.BEAT_MAZURKA_EXT, Settings.BEAT_TXT_EXT, Settings.ONSET_EXT};


    public static final String FIELD_SEPARATOR = "#";


    //Chord extraction settings
    public static String[] chordDictionary;

    public static final boolean isToUseChordWrappersToTrainChordChildren = true;

    //-----------------------------------------------------------------------------
    //Execution
    public static int[] stagesToRun;

    public static String operationDomain;
    public static OperationType operationType;


    public static String scenario;
    public static int numberOfFolds;

    //If set to true, PCP is computed segment by segment that decreases memory consumption drastically
    public static boolean initializationByParts;
    public static int threadsNumberForFeatureExtraction;


    public static String labelsGroundTruthDir;
    public static String chordLabelsGroundTruthDir;
    public static String beatLabelsGroundTruthDir;
    public static String onsetLabelsGroundTruthDir;
    public static String keyLabelsGroundTruthDir;


    public static String tuningsGroudTruthFilePath;
    public static String keysGroudTruthFilePath;

    public static String chordRecognizedDirectory;


    public static boolean isToTrainModels;
    public static boolean isToExtractFeaturesForTrain;
    public static boolean isToTestModels;
    public static boolean isToExtractFeaturesForTest;
    public static boolean isToUseRefFreq;
    public static boolean forceReExtractRefFreq;
    public static boolean isToDetectKeys;
    public static boolean forceReDetectKeys;
    public static boolean isToDeleteTrainFeaturesAfterTraining;

    public static boolean saveReassignmentStatistics = false;
    public static boolean saveHPSRelatedInformation = false;
    public static boolean savePhaseSpectrum = false;
    public static boolean saveMagSpectrum = true;
    public static boolean saveNoResolutionRepresentation = false;

    public static boolean isBeatSynchronousDecoding;
    public static boolean downbeatGranulation;
    public static int maxSegmentInBeats = 4;
    public static int maxNumberOfHypos = 3;

    public static boolean isToUseLMs;
    public static boolean isToUseBigramDuringHVite;

    public static boolean isToPostProcessLabelsToOutputComplicatedChords;

    public static int numberOfTestMaterial;

    public static int minNimberOfFramesForBeatSegment = 20;
    public static int maxNimberOfFramesForBeatSegment = 100;

    public static float stretchCoeffForBeatLMBuildingStart = 0.1f;
    public static float stretchCoeffForBeatLMBuildingEnd = 2.0f;

    //=================================NEED to be refactored=================BeatsBeat====================


    public static boolean isMIREX;
    public static boolean isSphinx;
    public static boolean isQUAERO;


    public static String melodyGroundTruthFolder = "/home/hut/Beatles/labelsMelisody/long";
    public static float melodyWindowSize = 6;


    //Language modelling
    public static boolean IS_FACTORED_LM = false;

    public static boolean IS_QUANTIZED_DURATION = true;

    public static boolean IS_TO_CREATE_LM = true;
    public static boolean IS_INCLUDE_OOV_INTO_LM = false;


    public static String SAVED_FEATURES_ALTERNATIVE_FOLDER = "fake";

    //=================================END_NEED to be refactored=====================================


    //----------Some constants---------------------------------------------------------------------------------
    public static final float REFERENCE_FREQUENCY = 440.0f;//261.6256f;
    public static final int REFERENCE_FREQUENCY_MIDI_NOTE = 69; //with respect to the REFERENCE_FREQUENCY
    public static final int NUMBER_OF_SEMITONES_IN_OCTAVE = 12;

    public static final int START_NOTE_FOR_PCP_UNWRAPPED = 24; //C1 midi note
    public static final int END_NOTE_FOR_PCP_UNWRAPPED = 107; //B7 midi note


    //F0 extraction  and tuner
    public static String PITCH_EXTRACTOR = "PitchExtractorReassignedFrequency"; //todo read this parameter from the config.
    public static String REF_FREQ_TUNER = "F0PeaksTuner";

    //FilterBanks
    public static String FILTERBANK_CONFIG_PATH = "cfg/filters/PQQMFconfig.xml";
    public static String FILTERBANK_CONFIG_QMF_PATH = "cfg/filters/QMFconfig.cfg";
    public static final String FILTERBANK_CONFIG_PQMF_PATH = "cfg/filters/PQMFConfig.xml";
    public static final String FILTERBANK_CONFIG_ELLIPTIC_PATH = "cfg/ellipticConfig.xml";
    public static boolean IS_TO_UPSAMPLE = true;  //Used for more effective NCC

    //Silence detection
    public static final float SILENCE_DETECTION_STEP = 0.02f; //In sec //TODO read this parameter from the config
    public static final float SILENCE_DETECTION_THRESHOLD = 0.04f; //TODO read this parameter from the config
    public static final float SILENCE_SEGMENT_MIN_DURATION = 0.4f; //in sec //TODO read this parameter from the config


    //Color display
    public static boolean IS_MONOCHROME = false;

    //WEB
    public static String urlRoot;

    public static int NumberOfParallelThreadsForConfigListenerService = 1;


    //-----------------------initialization part---------------------------

    static {
        initialize();
    }


    public static void initialize() {
        String osName = System.getProperty("os.name");
        if (osName.contains("Windows")) {
            EXECUTABLE_EXTENSION = ".exe";
            DYNAMIC_LIBRARY_EXTENSION = ".dll";
            SCRIPT_EXTENSION = ".bat";
        } else if (osName.contains("Mac")) {
            EXECUTABLE_EXTENSION = "";
            DYNAMIC_LIBRARY_EXTENSION = ".so";
            SCRIPT_EXTENSION = ".sh";
        } else if (osName.contains("Linux")) {
            EXECUTABLE_EXTENSION = "";
            DYNAMIC_LIBRARY_EXTENSION = ".so";
            SCRIPT_EXTENSION = ".sh";
        }
        if (HelperFile.getFile(ConfigSettings.CONFIG_FILE_PATH).exists()) {
            readProperties(ConfigSettings.CONFIG_FILE_PATH);
        } else {
            throw new IllegalArgumentException(String.format("Configuration file '%s' is not found", ConfigSettings.CONFIG_FILE_PATH));
        }
        setOperationType(OperationType.fromString(Settings.operationDomain));
        operationType.getOperationDomain();
    }

    public static void readProperties(String fileName) {
        properties = new Properties();
        try {
            properties.load(new FileInputStream(fileName));
            ReflectUtils.fillInVariables(Settings.class, properties);

            ExecParams._initialExecParameters = new ExecParams();
            ReflectUtils.fillInVariables(ExecParams._initialExecParameters, properties);
        } catch (IOException e) {
            logger.warn("Error while reading settings file: " + fileName, e);
        }
    }

    public static void readProperties(StringBuffer stringBuffer) {
        //At first read properties
        properties = new Properties();
        try {
            properties.load(new ByteArrayInputStream(stringBuffer.toString().getBytes("UTF-8")));
            ReflectUtils.fillInVariables(Settings.class, properties);
        } catch (IOException e) {
            logger.warn("Error while reading settings file from StringBuffer", e);
        }
    }


    public static void setOperationType(OperationType operationType_) {
        operationType = operationType_;
    }
}
