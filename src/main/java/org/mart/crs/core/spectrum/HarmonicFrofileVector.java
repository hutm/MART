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

import org.mart.crs.utils.helper.HelperArrays;

public class HarmonicFrofileVector {

    public static final int NUMBER_OF_HARMONICS = 7;
    public static final float harmonicDecayFactor = 0.6f;

    private float[] scores;


    float C1 = 1.0f;
    float C2 = 10.0f;
    float C3 = 1.0f;


    private float[] magnitudes;
    private float[] frequencies;

    private static float[] harmonicTemplate;

    static {
        harmonicTemplate = new float[NUMBER_OF_HARMONICS];
        for (int i = 0; i < NUMBER_OF_HARMONICS; i++) {
            harmonicTemplate[i] = (float) Math.pow(harmonicDecayFactor, i);
//            harmonicTemplate[i] = 1.0f;
        }
    }

    public HarmonicFrofileVector(float[] magnitudes, float[] frequencies) {
        this.magnitudes = magnitudes;
        this.frequencies = frequencies;
    }

    public float getScore() {

        float distance = calculateDistance(magnitudes, harmonicTemplate);
//        float distanceScore = C1 * (float)Math.log(1 / distance);
        float distanceScore = C1 * (1 / distance);


        float inharmonicity = calculateInharmonicity();
        float inharmonicityScore = -C2 * inharmonicity;

        float odd2EvenHarmonicEnergyRatio = calculateOdd2EvenHarmonicEnergyRatio();
        float odd2EvenHarmonicEnergyRatioScore;
        if(odd2EvenHarmonicEnergyRatio > 1){
            odd2EvenHarmonicEnergyRatioScore = 1 / odd2EvenHarmonicEnergyRatio;
        } else{
            odd2EvenHarmonicEnergyRatioScore = odd2EvenHarmonicEnergyRatio;
        }
        odd2EvenHarmonicEnergyRatioScore *= C3;

        scores = new float[7];
        scores[0] = distance;
        scores[1] = distanceScore;
        scores[2] = inharmonicity;
        scores[3] = inharmonicityScore;
        scores[4] = odd2EvenHarmonicEnergyRatio;
        scores[5] = odd2EvenHarmonicEnergyRatioScore;




        float score =  (distanceScore + inharmonicityScore + odd2EvenHarmonicEnergyRatioScore);
        if (score < 0) {
            score = 0;
        }
        scores[6] = score;

        return score * magnitudes[0];
//        return score ;
    }


    public float calculateDistance(float[] magnitudes, float[] harmonicTemplate){
        float distance = 0;

//        float sumEnergy = sum(magnitudes);
//        distance = sumEnergy * calculateDistance(magnitudes, harmonicTemplate);
//        distance = calculateDistanceNoNormalization(magnitudes, harmonicTemplate);

        distance =  HelperArrays.calculateDistance(magnitudes, harmonicTemplate);

//        distance =  KullbackLeibler(normalizeVector(magnitudes), harmonicTemplate);

//        distance = module(difference(normalizeVector(magnitudes), normalizeVector(harmonicTemplate)));

        return (distance);
    }


    public float calculateInharmonicity() {
        if (frequencies[0] == 0) {
            return Integer.MAX_VALUE;
        }

        float sumDenom = 0;
        float sumNom = 0;
        for (int i = 1; i < NUMBER_OF_HARMONICS; i++) {
            float energy = magnitudes[i] * magnitudes[i];
            sumDenom += energy;

            float freqDiff = Math.abs(frequencies[0] * (i + 1) - frequencies[i]);
            sumNom += freqDiff * energy;
        }
        return 2 / frequencies[0] * sumNom / sumDenom;
    }


    public float calculateOdd2EvenHarmonicEnergyRatio() {
        float sumOdd = 0;
        float sumEven = 0;
        for (int i = 1; i < NUMBER_OF_HARMONICS; i = i + 2) {
            sumOdd += magnitudes[i] * magnitudes[i];
        }
        for (int i = 2; i < NUMBER_OF_HARMONICS; i = i + 2) {
            sumEven += magnitudes[i] * magnitudes[i];
        }
        if (sumEven > 0) {
            return sumOdd / sumEven;
        } else {
            return Integer.MAX_VALUE;
        }
    }


    public float[] getMagnitudes() {
        return magnitudes;
    }

    public float[] getFrequencies() {
        return frequencies;
    }

    public float[] getScores() {
        return scores;
    }
}
