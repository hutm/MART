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
import org.mart.crs.utils.helper.HelperArrays;

import java.util.Arrays;

/**
 * @version 1.0 3/28/12 5:07 PM
 * @author: Hut
 */
public class SpectrumImplPhaseChange extends SpectrumImplPhaseComponents {

    protected float[][] phaseChangeSpec;
    protected int shiftInSamples;

    private float[][] buffer;


    public SpectrumImplPhaseChange(AudioReader audioReader, int startSampleIndex, int endSampleIndex, int windowLength, int windowType, float overlapping) {
        super(audioReader, startSampleIndex, endSampleIndex, windowLength, windowType, overlapping);
    }

    public SpectrumImplPhaseChange(AudioReader audioReader, int windowLength, int windowType, float overlapping) {
        super(audioReader, windowLength, windowType, overlapping);
    }

    public SpectrumImplPhaseChange(float[] samples, float sampleRate, int windowLength, int windowType, float overlapping) {
        super(samples, sampleRate, windowLength, windowType, overlapping);
    }

    public SpectrumImplPhaseChange(float[] samples, int startSampleIndex, int endSampleIndex, float sampleRate, int windowLength, int windowType, float overlapping) {
        super(samples, startSampleIndex, endSampleIndex, sampleRate, windowLength, windowType, overlapping);
    }


    @Override
    protected void initializeSpectrumDataArrays(int startIndex, int endIndex) {
        super.initializeSpectrumDataArrays(startIndex, endIndex);
        phaseChangeSpec = new float[numberOfFrames][];
        this.buffer = new float[2][];
    }


    @Override
    protected void processFrame(float centerFrameSampleIndex) {
        super.processFrame(centerFrameSampleIndex);
        super.processFrame(centerFrameSampleIndex + 1);

        float[] phaseChange = HelperArrays.subtractPhasesAbs(buffer[1], buffer[0]);
        phaseChange[0] = 0;
        for (int i = 1; i < phaseChange.length; i++) {
            phaseChange[i] /= 2 * Math.PI * shiftInSamples / sampleRate;
        }
        this.phaseChangeSpec[getIndexForSample(centerFrameSampleIndex)] = phaseChange;
        this.buffer = new float[2][];
    }

    @Override
    protected void innerSpectralDataExtraction(int frameIndex, double[] fftTransformedSamples) {
        super.innerSpectralDataExtraction(frameIndex, fftTransformedSamples);
        if(buffer[0] == null){
            buffer[0] = Arrays.copyOf(phaseSpec[frameIndex], phaseSpec[frameIndex].length);
        } else{
            buffer[1] = Arrays.copyOf(phaseSpec[frameIndex], phaseSpec[frameIndex].length);
        }
    }


}
