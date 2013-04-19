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

package org.mart.crs.core.spectrum.reassigned;

import org.mart.crs.core.AudioReader;

/**
 * @version 1.0 3/28/12 6:09 PM
 * @author: Hut
 */
public class ReassignedSpectrumHPS extends ReassignedSpectrum {

    protected boolean[][] harmonicComponentsMatrix;
    protected boolean[][] percussiveComponentsMatrix;
    protected boolean[][] noiseComponentsMatrix;


    {
        this.saveComplexComponents = true;
        this.savePhaseSpectrum = false;
    }




    public ReassignedSpectrumHPS(AudioReader audioReader, int startSampleIndex, int endSampleIndex, int windowLength, int windowType, float overlapping) {
        super(audioReader, startSampleIndex, endSampleIndex, windowLength, windowType, overlapping);
    }

    public ReassignedSpectrumHPS(AudioReader audioReader, int windowLength, int windowType, float overlapping) {
        super(audioReader, windowLength, windowType, overlapping);
    }

    public ReassignedSpectrumHPS(float[] samples, int startSampleIndex, int endSampleIndex, float sampleRate, int windowLength, int windowType, float overlapping) {
        super(samples, startSampleIndex, endSampleIndex, sampleRate, windowLength, windowType, overlapping);
    }

    public ReassignedSpectrumHPS(float[] samples, float sampleRate, int windowLength, int windowType, float overlapping) {
        super(samples, sampleRate, windowLength, windowType, overlapping);
    }


    @Override
    protected void initializeSpectrumDataArrays(int startIndex, int endIndex) {
        super.initializeSpectrumDataArrays(startIndex, endIndex);
        complexSpectrumRealPart = new float[numberOfFrames][];
        complexSpectrumImagPart = new float[numberOfFrames][];
        harmonicComponentsMatrix = new boolean[numberOfFrames][];
        percussiveComponentsMatrix = new boolean[numberOfFrames][];
        noiseComponentsMatrix = new boolean[numberOfFrames][];
    }


    @Override
    protected void innerSpectralDataExtraction(int currentFrameIndex, double[] ffttransformedSamples) {
        super.innerSpectralDataExtraction(currentFrameIndex, ffttransformedSamples);
        complexSpectrumRealPart[currentFrameIndex] = extractComplexSpecRealPart(ffttransformedSamples);
        complexSpectrumImagPart[currentFrameIndex] = extractComplexSpecImagPart(ffttransformedSamples);
        harmonicComponentsMatrix[currentFrameIndex] = new boolean[complexSpectrumRealPart[currentFrameIndex].length];
        percussiveComponentsMatrix[currentFrameIndex] = new boolean[complexSpectrumRealPart[currentFrameIndex].length];
        noiseComponentsMatrix[currentFrameIndex] = new boolean[complexSpectrumRealPart[currentFrameIndex].length];
    }


    protected void additionalCalculations(ReassignedFrame frame) {
        float phaseDoubleDerivative = frame.getPhaseDoubleDerivative();
        int currentFrameIndex = frame.getCurrentFrameIndex();
        int currentFreqIndex = frame.getCurrentFreqIndex();
        if (Math.abs(1 + phaseDoubleDerivative) < threshold) {
            harmonicComponentsMatrix[currentFrameIndex][currentFreqIndex] = true;
        } else if (Math.abs(phaseDoubleDerivative) < threshold) {
            percussiveComponentsMatrix[currentFrameIndex][currentFreqIndex] = true;
        } else {
            noiseComponentsMatrix[currentFrameIndex][currentFreqIndex] = true;
        }
    }


    public boolean[][] getHarmonicComponentsMatrix() {
        initialize();
        return harmonicComponentsMatrix;
    }

    public boolean[][] getNoiseComponentsMatrix() {
        initialize();
        return noiseComponentsMatrix;
    }

    public boolean[][] getPercussiveComponentsMatrix() {
        initialize();
        return percussiveComponentsMatrix;
    }

}
