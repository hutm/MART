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
 * @version 1.0 3/28/12 6:19 PM
 * @author: Hut
 */
public class ReassignmentSpectrumStatistics extends ReassignedSpectrum {

    protected float[][] timeReasStatistics;
    protected float[][] frequencyReasStatistics;

    public ReassignmentSpectrumStatistics(AudioReader audioReader, int startSampleIndex, int endSampleIndex, int windowLength, int windowType, float overlapping) {
        super(audioReader, startSampleIndex, endSampleIndex, windowLength, windowType, overlapping);
    }

    public ReassignmentSpectrumStatistics(AudioReader audioReader, int windowLength, int windowType, float overlapping) {
        super(audioReader, windowLength, windowType, overlapping);
    }

    public ReassignmentSpectrumStatistics(float[] samples, int startSampleIndex, int endSampleIndex, float sampleRate, int windowLength, int windowType, float overlapping) {
        super(samples, startSampleIndex, endSampleIndex, sampleRate, windowLength, windowType, overlapping);
    }

    public ReassignmentSpectrumStatistics(float[] samples, float sampleRate, int windowLength, int windowType, float overlapping) {
        super(samples, sampleRate, windowLength, windowType, overlapping);
    }


    protected void initializeSpectrumDataArrays(int startFrame, int endFrame) {
        timeReasStatistics = new float[numberOfFrames][windowLength / 2];
        frequencyReasStatistics = new float[numberOfFrames][windowLength / 2];
    }


    @Override
    protected void additionalCalculations(ReassignedFrame frame) {
        int currentFrameIndex = frame.getCurrentFrameIndex();
        int currentFreqIndex = frame.getCurrentFreqIndex();
        frequencyReasStatistics[currentFrameIndex][currentFreqIndex] = -1 * frame.getFreqDelta();
        timeReasStatistics[currentFrameIndex][currentFreqIndex] = frame.getTimeDelta() * sampleRate;
    }

    public float[][] getTimeReasStatistics() {
        return timeReasStatistics;
    }

    public float[][] getFrequencyReasStatistics() {
        return frequencyReasStatistics;
    }

}
