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

/**
 * @version 1.0 08-Jul-2010 19:38:37
 * @author: Hut
 */
public class SettingsString {

    public static String settingsStringVersion = "#-------------------------------------------------------\n" +
            "#Paths\n" +
            "#-------------------------------------------------------\n" +
            "\n" +
            "IS_BATCH_MODE=false\n" +
            "\n" +
            "_workingDir=work_/work_test/testmultiStreamWeighted_2ocateves/\n" +
            "\n" +
            "_waveFilesTrainFileList=D:/Beatles/list/short0.txt\n" +
            "_waveFilesTestFileList=D:/Beatles/list/short0.txt\n" +
            "\n" +
            "labelsGroundTruthDir=D:/Beatles/labels\n" +
            "\n" +
            "#-------------------------------------------------------\n" +
            "#Execution\n" +
            "#-------------------------------------------------------\n" +
            "\n" +
            "threadsNumberForFeatureExtraction=1\n" +
            "\n" +
            "isToTrainModels=true\n" +
            "isToExtractFeaturesForTrain=true\n" +
            "isToTestModels=true\n" +
            "isToExtractFeaturesForTest=true\n" +
            "\n" +
            "isToUseRefFreq=false\n" +
            "forceReExtractRefFreq=false\n" +
            "#F0PeaksTuner and PhaseVocoderBasedTuner - are the options\n" +
            "REF_FREQ_TUNER=PhaseVocoderBasedTuner\n" +
            "\n" +
            "isToDetectKeys=false\n" +
            "forceReDetectKeys=false\n" +
            "keysGroudTruthFilePath=D:/Beatles/keys.txt\n" +
            "\n" +
            "isToDetectBeats=false\n" +
            "forceReDetectBeats=false\n" +
            "\n" +
            "isToMakePostSynchronizationWithBeats=false\n" +
            "isToIncludeBeatDurationInOutputLabels=false\n" +
            "\n" +
            "\n" +
            "#-------------------------------------------------------\n" +
            "#Language Modelling\n" +
            "#-------------------------------------------------------\n" +
            "USE_BASE_KEY_TRANSFORM=false\n" +
            "IS_FACTORED_LM=false\n" +
            "IS_FACTORED_LM_FOR_STANDARD_VERSION=false\n" +
            "\n" +
            "IS_QUANTIZED_DURATION=true\n" +
            "\n" +
            "IS_TO_CREATE_LM=true\n" +
            "IS_INCLUDE_OOV_INTO_LM=false\n" +
            "\n" +
            "isToUseLMs=true\n" +
            "isVectorPerBeatVersion=false\n" +
            "\n" +
            "#-------------------------------------------------------\n" +
            "#HTK\n" +
            "#-------------------------------------------------------\n" +
            "states=3\n" +
            "FEATURES=12\n" +
            "isDiagonal=true\n" +
            "\n" +
            "gaussianNumber=2048\n" +
            "PRUNING=-t 250.0\n" +
            "numberOfIterationsHERest=6\n" +
            "\n" +
            "penalty=-20.0\n" +
            "\n" +
            "NBestCalculationLatticeOrder=3\n" +
            "latticeRescoringOrder=3\n" +
            "standardLmOrder = 3\n" +
            "\n" +
            "_lmWeights=9.0\n" +
            "_acWeights=1.0\n" +
            "_wips=-3.0\n" +
            "\n" +
            "\n" +
            "melodyGroundTruthFolder=D:/Beatles/labelsMelody/long\n" +
            "melodyWindowSize=6\n" +
            "\t\n" +
            "\n" +
            "#-------------------------------------------------------\n" +
            "#Acoustic model parameters\n" +
            "#-------------------------------------------------------\n" +
            "samplingRate=11025.0\n" +
            "windowLength=1024\n" +
            "overlapping=0.5\n" +
            "#RECTANGULAR_WINDOW=0 TRIANGULAR_WINDOW=1 HANNING_WINDOW=2 HAMMING_WINDOW=3 BLACKMAN_WINDOW=4 \n" +
            "windowType=2\n" +
            "startMidiNote=54\n" +
            "endMidiNote=96\n" +
            "\n" +
            "startMidiNoteBass=24\n" +
            "endMidiNoteBass=54\n" +
            "\n" +
            "#-------------------------------------------------------\n" +
            "#PCP\n" +
            "#-------------------------------------------------------\n" +
            "\n" +
            "#IF set to false, magnitude spectrum is used for PCP\n" +
            "IS_ENERGY_SPECTRUM_FOR_CHROMAGRAM=false\n" +
            "isToNormalizeFeatureVectors=true\n" +
            "\n" +
            "FILTERBANK_CONFIG_PATH=cfg/filters/semitoneFilterBank.xml\n" +
            "\n" +
            "#folder to save spectrograms and read from if features has already been extracted\n" +
            "SAVED_FEATURES_ALTERNATIVE_FOLDER=data_chroma_beatles\n" +
            "\n" +
            "PQMFBasedSpectrumFrameLength=0.003\n" +
            "isToConsiderHigerPeaks=false\n" +
            "\n" +
            "#-------------------------------------------------------\n" +
            "#Feature extractors\n" +
            "#-------------------------------------------------------\n" +
            "\n" +
            "#PCP_SPECTRUM_BASED = 0 PCP_FILTERBANK_NCC_BASED = 1 PCP_PITCH_FILTERING_BASED=2 PCP_REASSIGNED_SPECTRUM_BASED=3 PCP_BASS_SPECTRUM_BASED=4 MELODY_FEATURES=5 UNWRAPPED=6\n" +
            "\n" +
            "featureExtractors=3 4\n" +
            "featureExtractorsWeights=1 1\n" +
            "FEATURE_EXTRACTORS_FEATURE_BINS=12 12\n" +
            "\n" +
            "\n" +
            "urlRoot=http://chordsextraction.appspot.com\n" +
            "#-------------------------------------------------------\n" +
            "#BATCH Parameters\n" +
            "#-------------------------------------------------------\n" +
            "BATCH_IS_ENERGY_SPECTRUM_FOR_CHROMAGRAM=false\n" +
            "BATCH_PCP_NORMALIZATION=true\n" +
            "BATCH_WINDOW_TYPE=2\n" +
            "BATCH_WINDOW_LENGTHS=1024\n" +
            "BATCH_OVERLAPPING=0.5\n" +
            "\n" +
            "BATCH_PENALTIES=-20\n" +
            "BATCH_GAUSSIANS=2048 ";

}
