/*
 * Copyright (c) 2008-2013 Maksim Khadkevich and Fondazione Bruno Kessler.
 *
 * This file is part of MART.
 * MART is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2, as published
 * by the Free Software Foundation.
 *
 * MART is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with MART; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.mart.crs.core.spectrum;

import org.mart.crs.core.AudioReader;
import org.mart.crs.utils.helper.HelperArrays;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 24.03.2009 14:51:04
 * @author: Maksim Khadkevich
 */
public class SpectrumCrossCorrelationBasedImpl extends SpectrumImpl {

    protected int crossCorrSpectrNumberOfBins;

    protected boolean isKullbackLeibner = false;

    private double sum, absLeft, absRight;


    public SpectrumCrossCorrelationBasedImpl(AudioReader audioReader, int crossCorrSpectrNumberOfBins) {
        this(audioReader.getSamples(), audioReader.getSampleRate(), crossCorrSpectrNumberOfBins);
    }

    /**
     * Constructor
     *
     * @param samples    samples
     * @param sampleRate samplerate
     */
    public SpectrumCrossCorrelationBasedImpl(float[] samples, float sampleRate, int crossCorrSpectrNumberOfBins) {
//        super(samples, sampleRate);  //TODO: fix it. Did not tested this class after refactoring
        this.crossCorrSpectrNumberOfBins = crossCorrSpectrNumberOfBins;
        this.sampleRate = sampleRate;
        this.sampleRateSpectrum = sampleRate;
        this.sampleNumber = samples.length;
    }


    protected void initialize() {
//        logger.debug("Creating SpectrumCrossCorrelationBased object...");

        logger.debug("Number of samples to process: " + sampleNumber);
        magSpec = new float[sampleNumber][crossCorrSpectrNumberOfBins];

        //If we have very little number of samples processSamples() throws an exception
        if (samples.length < 2 * crossCorrSpectrNumberOfBins) {
            crossCorrSpectrNumberOfBins = samples.length / 2;
        }

        processSamples();
    }


    private void processSamples() {
        float[] samples = audioReader.getSamples();
        for (int i = 2; i < crossCorrSpectrNumberOfBins; i++) {
            int samplePosition = crossCorrSpectrNumberOfBins;
            sum = 0;
            absLeft = 0;
            absRight = 0;

            for (int k = samplePosition - i; k <= samplePosition; k++) {
                if (isKullbackLeibner) {
                    sum += samples[k] * Math.log(samples[k] / samples[k + i]);
                } else {
                    sum += samples[k] * samples[k + i];
                    absLeft += samples[k] * samples[k];
                    absRight += samples[k + i] * samples[k + i];
                }
            }
            magSpec[samplePosition][i] = calculateSpecValue();
            for (int position = samplePosition + 1; position + crossCorrSpectrNumberOfBins < samples.length; position++) {
                calculateSample(i, position);
            }
        }
    }

    private void calculateSample(int i, int samplePosition) {
        float[] samples = audioReader.getSamples();
        if (isKullbackLeibner) {
            sum += (-1) * samples[samplePosition - 1 - i] * Math.log(samples[samplePosition - 1 - i] / samples[samplePosition - 1]) +
                    samples[samplePosition] * Math.log(samples[samplePosition] /samples[samplePosition + i]);
        } else{
            sum += (-1) * samples[samplePosition - 1 - i] * samples[samplePosition - 1] +
                    samples[samplePosition] * samples[samplePosition + i];
            absLeft += (-1) * samples[samplePosition - 1 - i] * samples[samplePosition - 1 - i] +
                    samples[samplePosition] * samples[samplePosition];
            absRight += (-1) * samples[samplePosition - 1] * samples[samplePosition - 1] +
                    samples[samplePosition + i] * samples[samplePosition + i];

        }
        magSpec[samplePosition][i] = calculateSpecValue();
    }


    private float calculateSpecValue() {
        if (!isKullbackLeibner) {
            if (absLeft > 0 && absRight > 0) {
                float value = 0.5f + (float) (sum / (2 * Math.sqrt(absLeft) * Math.sqrt(absRight)));
                //TODO fix the problem of buffer overfilling
                if (value <= 1 && value >= 0) {
                    return value;
                }
            }
            return 0;
        }
        else{
            return (float)sum;
        }
    }


    public void setCrossCorrSpectrNumberOfBins(int crossCorrSpectrNumberOfBins) {
        this.crossCorrSpectrNumberOfBins = crossCorrSpectrNumberOfBins;
    }


    public float[] analyzeSCCBFrame(float startFreq, float endFreq, boolean isToInterpolate, boolean isToConsiderHigerPeaks) {
        float[] output = new float[getMagSpec().length];
        for (int i = 0; i < output.length; i++) {
            output[i] = analyzeSCCBFrame(i, startFreq, endFreq, isToInterpolate, isToConsiderHigerPeaks);
        }
        return output;
    }


    public float index2freq(float precisePeakIndex) {
        float timePeriod = precisePeakIndex / getSampleRate();
        float resultFrequency = 1 / timePeriod;
        return resultFrequency;
    }

    public static float index2freq(float precisePeakIndex, float sampleRate) {
        float timePeriod = precisePeakIndex / sampleRate;
        float resultFrequency = 1 / timePeriod;
        return resultFrequency;
    }


    public int freq2index(float freq) {
        float timePeriod = 1 / freq;
        float index = timePeriod * getSampleRate();
        return Math.round(index);
    }

    public static int freq2index(float freq, float sampleRate) {
        float timePeriod = 1 / freq;
        float index = timePeriod * sampleRate;
        return Math.round(index);
    }


    /**
     * Finds periodicity in crossCorrelation based spectrum
     *
     * @param currentSample                  currentSample
     * @param startFreq                      start freq to search
     * @param endFreq                        end Freq to search
     * @param isToInterpolate                isToInterpolate
     * @param isToTakeIntoAccountHigherPeaks isToTakeIntoAccountHigherPeaks
     * @return detected frequency
     */
    public float analyzeSCCBFrame(int currentSample, float startFreq, float endFreq, boolean isToInterpolate, boolean isToTakeIntoAccountHigherPeaks) {

        float resultFrequency;
        float[] data = getMagSpec()[currentSample];

        List<Integer> peaks = new ArrayList<Integer>();
        findSignificantPeaksForNormalizedCrossCorrelation(data, 0, peaks);

        if (peaks.size() == 0) {
            return -1;
        }

        float precisePeakValue;

        if (isToInterpolate) {
            precisePeakValue = HelperArrays.findInterpolatedMax(data, peaks.get(0));
            if (isToTakeIntoAccountHigherPeaks) {

                float higherIndex;
                boolean isOk = false;
                for (int i = 2; i <= 3; i++) {
                    higherIndex = precisePeakValue * i;
                    isOk = false;
                    for (Integer peak : peaks) {
                        if (Math.abs(higherIndex - peak) < 0.2 * precisePeakValue) {
                            isOk = true;
                        }
                        if (peak > 2 * higherIndex) {
                            break;
                        }
                    }
                    if (!isOk) {
                        break;
                    }
                }
                if (!isOk) {
                    precisePeakValue = -1;
                }
//                float averageFreq = 0;
//                for (Integer index : peaks) {
//                    timePeriod = (index / (peaks.indexOf(index) + 1)) / spectrumCCB.getSampleRate();
//                    resultFrequency = 1 / timePeriod;
//                    averageFreq += resultFrequency;
//                }
//                averageFreq /= peaks.size();
//
//                if (averageFreq > (spectrumCCB.getSampleRate() / 2)) {
//                    return -1;
//                }

            }

        } else {
            precisePeakValue = peaks.get(0);
        }
        resultFrequency = index2freq(precisePeakValue);

        if (resultFrequency < endFreq && resultFrequency > startFreq) {
            return resultFrequency;
        }
        return -1;

    }


    /**
     * This method is used for finding the first peak (in the normalized cross-correlation function)
     *
     * @param array data array
     * @return index
     */
    public static void findSignificantPeaksForNormalizedCrossCorrelation(float[] array, int startIndex, List<Integer> significantPeaks) {


        if (startIndex >= array.length - 3) {
            return;
        }

        int stepsDown = 0;
        int stepsUp = 0;

        int startPositionIndex = 0;
        int endPositionIndex = 0;

        int i = startIndex;

        while (stepsUp < 3 && i < array.length - 1) {
            if (array[i + 1] - array[i] < 0) {
                stepsUp = 0;
            } else {
                stepsUp++;
            }
            i++;
        }

        //start searching peak from this position
        startPositionIndex = i;
        stepsDown = 0;
        stepsUp = 0;

        while (stepsDown < 3 && i < array.length - 1) {
            if (array[i + 1] - array[i] < 0) {
                stepsDown++;
            } else {
                stepsDown = 0;
            }
            i++;
        }

        endPositionIndex = i;

        int maxIndex = HelperArrays.findIndexWithMaxValue(array, startPositionIndex, endPositionIndex);

        if (array[maxIndex] > 0.95f) {
            significantPeaks.add(maxIndex);
        }
        findSignificantPeaksForNormalizedCrossCorrelation(array, endPositionIndex, significantPeaks);
    }


}
