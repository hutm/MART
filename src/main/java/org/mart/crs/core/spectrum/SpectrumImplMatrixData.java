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

/**
 * @version 1.0 3/28/12 3:46 PM
 * @author: Hut
 */
public class SpectrumImplMatrixData extends SpectrumImpl {

    public SpectrumImplMatrixData(AudioReader audioReader, int startSampleIndex, int endSampleIndex, int windowLength, int windowType, float overlapping) {
        super(audioReader, startSampleIndex, endSampleIndex, windowLength, windowType, overlapping);
    }

    public SpectrumImplMatrixData(AudioReader audioReader, int windowLength, int windowType, float overlapping) {
        super(audioReader, windowLength, windowType, overlapping);
    }

    public SpectrumImplMatrixData(float[] samples, float sampleRate, int windowLength, int windowType, float overlapping) {
        super(samples, sampleRate, windowLength, windowType, overlapping);
    }

    public SpectrumImplMatrixData(float[] samples, int startSampleIndex, int endSampleIndex, float sampleRate, int windowLength, int windowType, float overlapping) {
        super(samples, startSampleIndex, endSampleIndex, sampleRate, windowLength, windowType, overlapping);
    }

    /**
     * Explicitly difine spectrum data
     *
     * @param spectrumData Matrix of spectral elements
     * @param spectrumSampleRate
     */
    public SpectrumImplMatrixData(float[][] spectrumData, float sampleRate, float spectrumSampleRate, int windowLength, int windowType, float overlapping) {
        this.magSpec = new float[spectrumData.length][spectrumData[0].length];
        for (int i = 0; i < spectrumData.length; i++) {
            System.arraycopy(spectrumData[i], 0, magSpec[i], 0, spectrumData[i].length);
        }
        this.sampleRate = sampleRate;
        this.sampleRateSpectrum = spectrumSampleRate;
        this.windowLength = windowLength;
        this.windowType = windowType;
        this.overlapping = overlapping;
        numberOfFrames = magSpec.length;
        initialized = true;
    }


    public SpectrumImplMatrixData(float[][] spectrumData, float sampleRate, float spectrumSampleRate) {
        this(spectrumData, sampleRate, spectrumSampleRate, 0, 0, 0);
    }



}
