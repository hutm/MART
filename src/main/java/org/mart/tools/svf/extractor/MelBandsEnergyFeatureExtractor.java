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

package org.mart.tools.svf.extractor;

import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.core.spectrum.SpectrumImplMatrixData;

/**
 * User: hut
 * Date: Nov 14, 2008
 * Time: 1:25:38 PM
 */
public class MelBandsEnergyFeatureExtractor implements SVFFeatureExtractorInterface {
    public static float MIN_FILTER_FREQUENCY_SVF = 133.3334f;   //lower freq taken into consideration. Only for MelBandsEnergyFeatureExtractor
    public static float MAX_FILTER_FREQUENCY_SVF = 16000f;      //higer freq taken into consideration. Only for MelBandsEnergyFeatureExtractor
    public static int NUMBER_OF_MEL_FILTERS = 23;               // Only for MelBandsEnergyFeatureExtractor
    public static int NUMBER_OF_MEL_FILTERS_USED_FOR_FEATURES = 23;     //Only for MelBandsEnergyFeatureExtractor


    protected float samplingRate;

    protected SpectrumImpl spectrum;


    public float[][] extract(float[][] data, float sampleRate){
        return extract(new SpectrumImplMatrixData(data, sampleRate, sampleRate));
    }

    public float[][] extract(SpectrumImpl spectrum) {

        samplingRate = spectrum.getSampleRate();
        this.spectrum = spectrum;

        float[][] MFCC = new float[spectrum.getMagSpec().length][NUMBER_OF_MEL_FILTERS_USED_FOR_FEATURES + 1];

        for (int k = 0; k < spectrum.getMagSpec().length; k++) {

            // Magnitude Spectrum
            float spec[] = spectrum.getMagSpec()[k];

            // Mel Filtering
            int cbin[] = fftBinIndices();

            // get Mel Filterbank
            float fbank[] = melFilter(spec, cbin);

            // Non-linear transformation
            float f[] = nonLinearTransformation(fbank);

            // Add resulting MFCC to array
            for (int i = 0; i < NUMBER_OF_MEL_FILTERS_USED_FOR_FEATURES; i++) {
                MFCC[k][i] = f[i];
            }

            //Add energy bin
            MFCC[k][NUMBER_OF_MEL_FILTERS_USED_FOR_FEATURES] = getEnergyBin(spec);
        }

        return MFCC;
    }

    private float getEnergyBin(float[] spec) {
        float sum = 0f;
        for (int i = 0; i < spec.length; i++) {
            sum += spec[i] * spec[i];
        }
        return (float) Math.log10(sum);
    }

    /**
     * calculates the FFT bin indices<br>
     * calls: none<br>
     * called by: featureExtraction
     *
     * @return array of FFT bin indices
     */
    private int[] fftBinIndices() {
        int cbin[] = new int[NUMBER_OF_MEL_FILTERS + 2];

        cbin[0] = (int) Math.round(MIN_FILTER_FREQUENCY_SVF / samplingRate * spectrum.getWindowLength());
        cbin[cbin.length - 1] = (spectrum.getWindowLength() / 2 - 1);

        for (int i = 1; i <= NUMBER_OF_MEL_FILTERS; i++) {
            double fc = centerFreq(i);
            cbin[i] = (int) Math.round(fc / samplingRate * spectrum.getWindowLength());
        }

        return cbin;
    }

    /**
     * Calculate the output of the mel filter<br>
     * calls: none
     * called by: featureExtraction
     */
    private float[] melFilter(float spec[], int cbin[]) {
        float temp[] = new float[NUMBER_OF_MEL_FILTERS + 2];

        for (int k = 1; k <= NUMBER_OF_MEL_FILTERS; k++) {
            float num1 = 0, num2 = 0;

            for (int i = cbin[k - 1]; i <= cbin[k]; i++) {
                num1 += ((i - cbin[k - 1] + 1) / (cbin[k] - cbin[k - 1] + 1)) * spec[i];
            }

            for (int i = cbin[k] + 1; i <= cbin[k + 1]; i++) {
                num2 += (1 - ((i - cbin[k]) / (cbin[k + 1] - cbin[k] + 1))) * spec[i];
            }

            temp[k] = num1 + num2;
        }

        float fbank[] = new float[NUMBER_OF_MEL_FILTERS];
        for (int i = 0; i < NUMBER_OF_MEL_FILTERS; i++) {
            fbank[i] = temp[i + 1];
        }

        return fbank;
    }


    /**
     * the output of mel filtering is subjected to a logarithm function (natural logarithm)<br>
     *
     * @param fbank Output of mel filtering
     * @return Natural log of the output of mel filtering
     */
    private static float[] nonLinearTransformation(float fbank[]) {
        float f[] = new float[fbank.length];
        final float FLOOR = -90;

        for (int i = 0; i < fbank.length; i++) {
            f[i] = (float) Math.log10(fbank[i]);

            // check if ln() returns a value less than the floor
            if (f[i] < FLOOR) f[i] = FLOOR;
        }

        return f;
    }

    /**
     * calculates center frequency<br>
     *
     * @param i Index of mel filters
     * @return Center Frequency
     */
    private double centerFreq(int i) {
        double mel[] = new double[2];
        mel[0] = freqToMel(MIN_FILTER_FREQUENCY_SVF);
//        mel[1] = freqToMel(Math.min(MAX_FILTER_FREQUENCY_SVF, samplingRate / 2));
        mel[1] = Math.min(freqToMel(MAX_FILTER_FREQUENCY_SVF), freqToMel(samplingRate / 2));

        // take inverse mel of:
        double temp = mel[0] + ((mel[1] - mel[0]) / (NUMBER_OF_MEL_FILTERS + 1)) * i;
        return inverseMel(temp);
    }


    /**
     * calculates the inverse of Mel Frequency<br>
     *
     * @param x x
     * @return inverseMel
     */
    private static double inverseMel(double x) {
        double temp = Math.pow(10, x / 1127) - 1;
        return 700 * (temp);
    }


    /**
     * convert frequency to mel-frequency<br>
     *
     * @param freq Frequency
     * @return Mel-Frequency
     */
    protected static double freqToMel(double freq) {
        return 1127 * Math.log10(1 + freq / 700);
    }

}
