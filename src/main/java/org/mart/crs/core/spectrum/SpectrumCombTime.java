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

package org.mart.crs.core.spectrum;

import org.mart.crs.core.AudioReader;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;

import static org.mart.crs.core.spectrum.HarmonicFrofileVector.NUMBER_OF_HARMONICS;

/**
 * @version 1.0 Dec 16, 2009 12:57:27 PM
 * @author: Maksim Khadkevich
 */
public class SpectrumCombTime extends SpectrumImplMatrixData {


    private HarmonicFrofileVector[] harmonicProfileVectors;
    private float[] harmonicPVScore;


    private float fundamentalFreqCenter;

    public SpectrumCombTime(AudioReader audioReader, int windowLength, float overLapping, float fundamentalFreqCenter) {
        super(audioReader, windowLength, 0, overLapping);
        this.fundamentalFreqCenter = fundamentalFreqCenter;
    }

    public SpectrumCombTime(float[] samples, float sampleRate, int windowLength, float overLapping, float fundamentalFreqCenter) {
        super(samples, sampleRate, windowLength, 0, overLapping);
        this.fundamentalFreqCenter = fundamentalFreqCenter;
    }

    public SpectrumCombTime(SpectrumImpl spectrum, float fundamentalFreqCenter){
        super(spectrum.getMagSpec(), spectrum.getSampleRate(), spectrum.getSampleRateSpectrum(), spectrum.getWindowLength(), spectrum.getWindowType(), spectrum.getOverlapping());
        this.fundamentalFreqCenter = fundamentalFreqCenter;
    }


    public void calculateHarmonicProfileVectors() {
        harmonicProfileVectors = new HarmonicFrofileVector[getMagSpec().length];
        harmonicPVScore = new float[getMagSpec().length];
        for (int i = 0; i < getMagSpec().length; i++) {
            harmonicProfileVectors[i] = getHPV(getMagSpec()[i]);
            harmonicPVScore[i] = harmonicProfileVectors[i].getScore();
        }
    }


    private HarmonicFrofileVector getHPV(float[] spectrumFrame) {
        float[] magnitudes = new float[NUMBER_OF_HARMONICS];
        float[] frequencies = new float[NUMBER_OF_HARMONICS];

        int[] peakIndexes = HelperArrays.searchPeakIndexes(spectrumFrame, freq2index(fundamentalFreqCenter / 2), NUMBER_OF_HARMONICS + 4, 1);

        //Now assign weights to HPV bins
        for (int i = 1; i <= NUMBER_OF_HARMONICS; i++) {
            for (int j = 0; j < peakIndexes.length; j++) {
                if (Helper.getSemitoneDistanceAbs(index2freq(peakIndexes[j]), i * fundamentalFreqCenter) < 1) {
                    magnitudes[i - 1] = spectrumFrame[peakIndexes[j]];
                    frequencies[i - 1] = index2freq(peakIndexes[j]);
                    break;
                }
            }
        }

        HarmonicFrofileVector outVector = new HarmonicFrofileVector(magnitudes, frequencies);
        return outVector;
    }


    public HarmonicFrofileVector[] getHarmonicProfileVectors() {
        return harmonicProfileVectors;
    }

    public float[] getHarmonicPVScore() {
        return harmonicPVScore;
    }
}

