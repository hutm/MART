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

package org.mart.tools.tuning;

import org.mart.crs.core.AudioReader;
import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;
import org.mart.crs.utils.windowing.WindowType;
import org.apache.log4j.Logger;
import org.mart.crs.config.ExecParams;

import java.util.ArrayList;
import java.util.List;

import static org.mart.crs.config.Settings.NUMBER_OF_SEMITONES_IN_OCTAVE;
import static org.mart.crs.config.Settings.REFERENCE_FREQUENCY;

/**
 * If there are some deviations from standart frequences in a song, it will find out the freq shift
 *
 * @version 1.0 01.05.2009 13:13:46
 * @author: Maksim Khadkevich
 */
public class PhaseVocoderBasedTuner implements Tuner {

    protected static Logger logger = CRSLogger.getLogger(PhaseVocoderBasedTuner.class);

    public static final boolean isToSaveDebugInfo = false;


    public static int TIME_SHIFT_IN_SAMPLES = 2;
    public static float MIN_DISTANCE_BETWEEN_PEAKS = 0.9f; //in semitone scale

    public static float startFreq = 240;
    public static float endFreq = 540;

    public static float e = 0.5f;

    public static float[] scale = createSemitoneScale(startFreq, endFreq);

    protected String filePath;

    public PhaseVocoderBasedTuner(String filePath) {
        this.filePath = filePath;
    }

    public float[] calculateCandidates(int windowSize, float overlapping, int windowType, int numberOfCandidatesPerFrame) {

        AudioReader audioReader = new AudioReader(filePath, ExecParams._initialExecParameters.samplingRate);

        SpectrumImpl spectrum = new SpectrumImpl(audioReader, windowSize, windowType, overlapping, ExecParams._initialExecParameters);

        List<Float> outList = new ArrayList<Float>();

        int startFreqIndex = spectrum.freq2index(startFreq, audioReader.getSampleRate() / windowSize);
        int endFreqIndex = spectrum.freq2index(endFreq, audioReader.getSampleRate() / windowSize);

        for (int i = 0; i < audioReader.getSamples().length - windowSize; i += windowSize * overlapping) {
            float timeInstance = i / audioReader.getSampleRate();
            float[] magSpec = spectrum.getMagSpecValue(timeInstance);
            float[] phaseChange = spectrum.getPhaseChange(timeInstance, TIME_SHIFT_IN_SAMPLES);


            //leave only part of the magnitude spectrum
            for (int j = 0; j < magSpec.length; j++) {
                if (j < startFreqIndex || j > endFreqIndex) {
                    magSpec[j] = 0;
                }
            }

            int[] indexes = HelperArrays.searchPeakIndexes(magSpec, startFreqIndex, numberOfCandidatesPerFrame, MIN_DISTANCE_BETWEEN_PEAKS);
            for (int k = 0; k < indexes.length; k++) {
                //To be sure that the phase is constant
                if (phaseChange[indexes[k]] != 0 && indexes[k] != 0 && Math.abs(phaseChange[indexes[k] - 1] - phaseChange[indexes[k]]) < e && Math.abs(phaseChange[indexes[k] + 1] - phaseChange[indexes[k]]) < e) {
//                        Math.abs(phaseChange[indexes[k] - 2] - phaseChange[indexes[k]]) < e && Math.abs(phaseChange[indexes[k]+2] - phaseChange[indexes[k]]) < e) {
                    outList.add(phaseChange[indexes[k]]);
                }
            }
        }

        float[] out = new float[outList.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = outList.get(i);
        }

        return out;
    }


    public static float[] createSemitoneScale(float startFreq, float endFreq) {
        int startSemitone = (int) Math.floor(NUMBER_OF_SEMITONES_IN_OCTAVE * Math.log(startFreq / REFERENCE_FREQUENCY) / Math.log(2));
        int endSemitone = (int) Math.floor(NUMBER_OF_SEMITONES_IN_OCTAVE * Math.log(endFreq / REFERENCE_FREQUENCY) / Math.log(2)) + 1;

        float[] out = new float[endSemitone - startSemitone + 1];
        for (int i = 0; i < out.length; i++) {
            out[i] = (float) (REFERENCE_FREQUENCY * Math.pow(2, (i + startSemitone) / 12f));
        }
        return out;
    }

    public static float[] getSemitoneDistance(float[] data, float[] semitoneScale) {
        float[] out = new float[data.length];
        //The last bin is the average value
        for (int i = 0; i < data.length; i++) {
            int index = Helper.getClosestLogDistanceIndex(data[i], semitoneScale);
            out[i] = (float) (NUMBER_OF_SEMITONES_IN_OCTAVE * (Math.log(data[i]) - Math.log(semitoneScale[index])) / Math.log(2));
        }

        return out;
    }


    public float getReferenceFrequency() {
        ExecParams._initialExecParameters.samplingRate = 11025f;

        float[] candidates = calculateCandidates(4096, ExecParams._initialExecParameters.overlapping, WindowType.HANNING_WINDOW.ordinal(), 10);
        float[] distance = getSemitoneDistance(candidates, scale);

        float negvalue, posvalue;
        float negsum = 0;
        float possum = 0;
        int negCounter = 0;
        int posCounter = 0;
        for (int i = 0; i < distance.length; i++) {
            if (distance[i] < 0) {
                negsum += distance[i];
                negCounter++;
            } else {
                possum += distance[i];
                posCounter++;
            }
        }
        negvalue = negsum / negCounter;
        posvalue = possum / posCounter;

        float factor = 2.3f;//Needs to be at least {factor} times greater
        float reffreq;
        float[] meandev = HelperArrays.calculateMeanAndStandardDeviation(distance);
        if (factor * negCounter < posCounter && posvalue > -1 && posvalue < 1) {
            reffreq = (float) (REFERENCE_FREQUENCY * Math.pow(2, posvalue / NUMBER_OF_SEMITONES_IN_OCTAVE));
        } else {

            if (factor * posCounter < negCounter && negvalue > -1 && negvalue < 1) {
                reffreq = (float) (REFERENCE_FREQUENCY * Math.pow(2, negvalue / NUMBER_OF_SEMITONES_IN_OCTAVE));
            } else {
                reffreq = REFERENCE_FREQUENCY;
            }
        }

        logger.debug(String.format("ref=%5.2f pos=%5.2f neg=%5.2f posNum=%d negNum=%d", reffreq, posvalue, negvalue, posCounter, negCounter));

        return reffreq;
    }


    //----------------------------------Main part------------------------------
//
//    public static List<String> fileListCollection;
//
//
//    /**
//     * Runs feature extraction in threads
//     */
//    public static void processFileList(String fileListPath) {
//
//        CRSThreadPoolExecutor threadPoolExecutor = new CRSThreadPoolExecutor(Settings.threadsNumberForFeatureExtraction);
//
//        fileListCollection = HelperFile.readTokensFromTextFile(fileListPath, 1);
//
//        for (final String song : fileListCollection) {
//            Runnable featureExtractor = new Runnable() {
//                public void run() {
//                    logger.info(String.format("Tuning %s", song));
//                    processSong(song);
//                }
//            };
//            threadPoolExecutor.runTask(featureExtractor);
//        }
//        threadPoolExecutor.waitCompletedAndshutDown();
//
//        (new ReferenceFreqManager("")).saveRefFreqs();
//    }
//
//
//    public static void processSong(String songFilePath) {
//        if (!Settings.forceReExtractRefFreq && (new ReferenceFreqManager("")).containSong(songFilePath)) {
//            logger.debug(String.format("Song %s is already in the list: no need for refFreq Extraction", songFilePath));
//        }
//
//        try {
//            processFile(songFilePath);
//        } catch (IOException e1) {
//            logger.warn("File " + songFilePath + " was processed with errors");
//            logger.debug(Helper.getStackTrace(e1));
//        }
//    }




    public static void main(String[] args) {
        PhaseVocoderBasedTuner tuner = new PhaseVocoderBasedTuner("/home/hut/Beatles/data/wav/44100/09_-_It's_Only_Love.wav");
        float frequency = tuner.getReferenceFrequency();
        System.out.println(frequency);
    }


}
