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

import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.core.AudioReader;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrum;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrumHarmonicPart;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.mart.crs.config.Settings.NUMBER_OF_SEMITONES_IN_OCTAVE;
import static org.mart.crs.config.Settings.REFERENCE_FREQUENCY;
import static org.mart.crs.utils.helper.HelperArrays.sum;

/**
 * @version 1.0 23-Jun-2010 14:55:23
 * @author: Hut
 */
public class F0PeaksTuner implements Tuner {

    protected static Logger logger = CRSLogger.getLogger(F0PeaksTuner.class);

    protected float[] semitoneDeviations;
    protected float referenceFreq;
    protected float[] semitoneScale;

    public static float MIN_DISTANCE_BETWEEN_PEAKS_HARMONIC_SEARCH = 0.3f; //In semitone scale. (When searching for peaks)
    public static float MAX_AMP_RELATION_BETWEEN_PEAKS_HARMONIC_SEARCH = 0.5f;
    public static float MIN_AMP_WITH_RESPECT_TO_TOTAL_FRAME_ENERGY = 0.05f;

    public static int NUMBER_OF_PEAKS = 10;

    public static float BIN_STEP = 0.1f;


    protected String audioFilePath;

    protected ReassignedSpectrum spectrum;
    protected float[][] magSpec;


    public F0PeaksTuner(String audioFilePath) {
        this.audioFilePath = audioFilePath;
        initialize();
    }


    protected void initialize() {
        //TODO migrate to noresolution approach
        boolean temp1 = Settings.initializationByParts;
        boolean temp2 = Settings.saveMagSpectrum;
        int temp3 = ExecParams._initialExecParameters.numberOfFreqBinsInTheOutputSpectrogram;
        int windowLengthtemp = ExecParams._initialExecParameters.windowLength;

        Settings.initializationByParts = true;
        Settings.saveMagSpectrum = true;
        ExecParams._initialExecParameters.numberOfFreqBinsInTheOutputSpectrogram = 8192;
        ExecParams._initialExecParameters.windowLength = 1024;

        AudioReader audioReader = new AudioReader(audioFilePath, ExecParams._initialExecParameters.samplingRate);
        spectrum = new ReassignedSpectrumHarmonicPart(audioReader,  ExecParams._initialExecParameters.reassignedSpectrogramThreshold, ExecParams._initialExecParameters);
        magSpec = spectrum.getMagSpec();

        Settings.initializationByParts = temp1;
        Settings.saveMagSpectrum = temp2;
        ExecParams._initialExecParameters.numberOfFreqBinsInTheOutputSpectrogram = temp3;
        ExecParams._initialExecParameters.windowLength = windowLengthtemp;

    }


    public float getReferenceFrequency() {

        List<Float> extractedPeaks = new ArrayList<Float>();
        List<Float> extractedAmps = new ArrayList<Float>();
        for (float[] spectralFrame : magSpec) {
            int[] peaks = HelperArrays.searchPeakIndexes(spectralFrame, spectrum.freq2index(Helper.getFreqForMIDINote(Settings.START_NOTE_FOR_PCP_UNWRAPPED)),
                    spectrum.freq2index(Helper.getFreqForMIDINote(ExecParams._initialExecParameters.endMidiNote)), NUMBER_OF_PEAKS, MIN_DISTANCE_BETWEEN_PEAKS_HARMONIC_SEARCH, true);
            float spectralEnergy = HelperArrays.sum(spectralFrame);

            for (int peakCandidate : peaks) {


                if (spectralFrame[peakCandidate] < spectralEnergy * MIN_AMP_WITH_RESPECT_TO_TOTAL_FRAME_ENERGY) {
                    continue;
                }

                boolean isHarmonic = false;
                for (int i = 2; i <= 4; i++) {
                    for (int peak : peaks) {
                        if (peakCandidate != peak && Helper.getSemitoneDistanceAbs((peakCandidate + 0.0f) / i, peak) < MIN_DISTANCE_BETWEEN_PEAKS_HARMONIC_SEARCH) {
                            float relation = spectralFrame[peak] / spectralFrame[peakCandidate];
                            if (relation > MAX_AMP_RELATION_BETWEEN_PEAKS_HARMONIC_SEARCH || (1 / relation) < MAX_AMP_RELATION_BETWEEN_PEAKS_HARMONIC_SEARCH) {
                                isHarmonic = true;
                                break;
                            }
                        }
                    }
                    if (isHarmonic) {
                        break;
                    }
                }
                if (!isHarmonic) {
                    extractedPeaks.add(spectrum.index2freq(peakCandidate));
                    extractedAmps.add(spectralFrame[peakCandidate]);
                }
            }
        }

        float[] arrayData = new float[extractedPeaks.size()];
        float[] arrayDataAmp = new float[extractedPeaks.size()];

        for (int i = 0; i < arrayData.length; i++) {
            arrayData[i] = extractedPeaks.get(i);
            arrayDataAmp[i] = extractedAmps.get(i);
        }

        if (semitoneScale == null) {
            semitoneScale = createSemitoneScale(Helper.getFreqForMIDINote(Settings.START_NOTE_FOR_PCP_UNWRAPPED),
                    Helper.getFreqForMIDINote(ExecParams._initialExecParameters.endMidiNote));
        }

        semitoneDeviations = getSemitoneDistance(arrayData, semitoneScale);

        int[] hist = new int[Math.round(1 / BIN_STEP)];
        for (int i = 0; i < semitoneDeviations.length; i++) {
            int value = Math.round(semitoneDeviations[i] / BIN_STEP);
            if (value >= 0) {
                hist[value] += arrayData[i];
            } else {
                hist[hist.length + value] += arrayData[i];
            }
        }
        int maxindex = HelperArrays.findIndexWithMaxValue(hist);
        float mistuning = maxindex * BIN_STEP;
        if (mistuning > 0.5) {
            mistuning--;
        }

        referenceFreq = Helper.getFreqForMIDINote(69 + mistuning);
        logger.debug(String.format("Found reference freq %5.2f", referenceFreq));
        return referenceFreq;
    }

    protected float[] createSemitoneScale(float startFreq, float endFreq) {
        int startSemitone = (int) Math.floor(NUMBER_OF_SEMITONES_IN_OCTAVE * Math.log(startFreq / REFERENCE_FREQUENCY) / Math.log(2));
        int endSemitone = (int) Math.floor(NUMBER_OF_SEMITONES_IN_OCTAVE * Math.log(endFreq / REFERENCE_FREQUENCY) / Math.log(2)) + 1;

        float[] semitoneScale = new float[endSemitone - startSemitone + 1];
        for (int i = 0; i < semitoneScale.length; i++) {
            semitoneScale[i] = (float) (REFERENCE_FREQUENCY * Math.pow(2, (i + startSemitone) / 12f));
        }
        return semitoneScale;
    }

    protected float[] getSemitoneDistance(float[] data, float[] semitoneScale) {
        float[] out = new float[data.length];
        //The last bin is the average value
        for (int i = 0; i < data.length; i++) {
            int index = Helper.getClosestLogDistanceIndex(data[i], semitoneScale);
            out[i] = (float) (NUMBER_OF_SEMITONES_IN_OCTAVE * (Math.log(data[i]) - Math.log(semitoneScale[index])) / Math.log(2));
            if(!(out[i] <= 0.5 && out[i] >= -0.5)){
                out[i] = Float.NEGATIVE_INFINITY;
            }
        }

        return out;
    }


    public static void main(String[] args) {
        F0PeaksTuner tuner = new F0PeaksTuner("/home/hut/Beatles/data/wav/44100/16_-_The_End.wav");
        float frequency = tuner.getReferenceFrequency();
        System.out.println(frequency);
    }


}
