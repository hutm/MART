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

import org.mart.crs.config.ExecParams;
import org.mart.crs.core.AudioReader;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrum;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.windowing.WindowFunction;
import org.mart.crs.utils.windowing.WindowOption;
import org.mart.crs.utils.windowing.WindowType;
import org.mart.crs.utils.helper.HelperArrays;

/**
 * @version 1.0 13-Sep-2010 12:56:51
 * @author: Hut
 */
public class ReassignedSpectrumPhaseChange extends ReassignedSpectrum {

    public static final int shiftInSamples = 1;

    public ReassignedSpectrumPhaseChange(AudioReader audioReader, int startSampleIndex, int endSampleIndex, int windowLength, int windowType, float overlapping, ExecParams execParams) {
        super(audioReader, startSampleIndex, endSampleIndex, windowLength, windowType, overlapping, execParams);
    }

    public ReassignedSpectrumPhaseChange(AudioReader audioReader, int windowLength, int windowType, float overlapping, ExecParams execParams) {
        super(audioReader, windowLength, windowType, overlapping, execParams);
    }

    public ReassignedSpectrumPhaseChange(float[] samples, int startSampleIndex, int endSampleIndex, float sampleRate, ExecParams execParams) {
        super(samples, startSampleIndex, endSampleIndex, sampleRate, execParams);
    }

    public ReassignedSpectrumPhaseChange(float[] samples, float sampleRate, ExecParams execParams) {
        super(samples, sampleRate, execParams);
    }

    public ReassignedSpectrumPhaseChange(AudioReader audioReader, int startSampleIndex, int endSampleIndex, ExecParams execParams) {
        super(audioReader, startSampleIndex, endSampleIndex, execParams);
    }

    public ReassignedSpectrumPhaseChange(AudioReader audioReader, ExecParams execParams) {
        super(audioReader, execParams);
    }

    protected float[][] processFrame(float centerFrameSampleIndex) {
        int centerFrameSampleIndexInt = Math.round(centerFrameSampleIndex);


        float[] inputFrame = new float[windowLength];
        int destStartIndex = -1 * Math.min(0, centerFrameSampleIndexInt - windowLength / 2);
        int sourceStartIndex = Math.max(0, centerFrameSampleIndexInt - windowLength / 2);
        int actualLength = Math.min((inputFrame.length - destStartIndex), samples.length - sourceStartIndex);
        System.arraycopy(samples, sourceStartIndex, inputFrame, destStartIndex, actualLength);

        centerFrameSampleIndexInt += shiftInSamples;

        float[] inputFrame2 = new float[windowLength];
        int destStartIndex2 = -1 * Math.min(0, centerFrameSampleIndexInt - windowLength / 2);
        int sourceStartIndex2 = Math.max(0, centerFrameSampleIndexInt - windowLength / 2);
        int actualLength2 = Math.min((inputFrame2.length - destStartIndex2), samples.length - sourceStartIndex2);

        System.arraycopy(samples, sourceStartIndex2, inputFrame2, destStartIndex2, actualLength2);

        float[][] spectrum = calculateSpectrumOfFrame(inputFrame, inputFrame2, centerFrameSampleIndex);


        return spectrum;
    }


    protected float[][] calculateSpectrumOfFrame(float[] samples, float[] samplesShifted, float centerFrameSample) {

        WindowFunction.apply(samples, 0, windowLength, WindowType.values()[windowType], WindowOption.WINDOW);
        WindowFunction.apply(samplesShifted, 0, windowLength, WindowType.values()[windowType], WindowOption.WINDOW);


        double[] samples_d = getSamplesWithProperLengthAndType(samples, 0);
        double[] samples_d_Shifted = getSamplesWithProperLengthAndType(samplesShifted, 0);


        float[][] out = new float[2][numberOfFreqBinsInTheOutputSpectrogram];
        try {

            fft.calcReal(samples_d, -1);
            float[] magSpectrum = SpectrumImpl.extractMagSpec(samples_d);
            float[] phaseSpectrum = SpectrumImpl.extractPhaseSpec(samples_d);


            fft.calcReal(samples_d_Shifted, -1);
            float[] phaseSpectrumShifted = SpectrumImpl.extractPhaseSpec(samples_d_Shifted);

            float[] phaseChange = HelperArrays.subtractPhasesAbs(phaseSpectrumShifted, phaseSpectrum);
            phaseChange[0] = 0;
            for (int i = 1; i < phaseChange.length; i++) {
                phaseChange[i] /= 2 * Math.PI * shiftInSamples / sampleRate;
            }

            float reassignedFreq;
            int newBinIndex;
            for (int i = 0; i < magSpectrum.length; i++) {

                //Frequency reassignment
                reassignedFreq = phaseChange[i];
                newBinIndex = SpectrumImpl.freq2index(reassignedFreq, (0.5f * sampleRate) / numberOfFreqBinsInTheOutputSpectrogram);

                int frameIndex = getIndexForSample(centerFrameSample);


                if (newBinIndex >= 0 && newBinIndex < out[0].length && frameIndex >= 0 && frameIndex < this.magSpec.length) {
                    this.magSpec[frameIndex][newBinIndex] += magSpectrum[i];
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            SpectrumImpl.logger.error("Error while calculating spectrum ");
            SpectrumImpl.logger.error(Helper.getStackTrace(e));
        }
        return out;
    }
}
