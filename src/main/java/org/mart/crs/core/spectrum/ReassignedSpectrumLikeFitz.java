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
import de.dfki.maths.Complex;

/**
 * @version 1.0 18-Oct-2010 18:39:15
 * @author: Hut
 */
public class ReassignedSpectrumLikeFitz extends ReassignedSpectrum {

    public ReassignedSpectrumLikeFitz(AudioReader audioReader, int startSampleIndex, int endSampleIndex, int windowLength, int windowType, float overlapping, ExecParams execParams) {
        super(audioReader, startSampleIndex, endSampleIndex, windowLength, windowType, overlapping, execParams);
    }

    public ReassignedSpectrumLikeFitz(AudioReader audioReader, int windowLength, int windowType, float overlapping, ExecParams execParams) {
        super(audioReader, windowLength, windowType, overlapping, execParams);
    }

    public ReassignedSpectrumLikeFitz(float[] samples, int startSampleIndex, int endSampleIndex, float sampleRate, ExecParams execParams) {
        super(samples, startSampleIndex, endSampleIndex, sampleRate, execParams);
    }

    public ReassignedSpectrumLikeFitz(float[] samples, float sampleRate, ExecParams execParams) {
        super(samples, sampleRate, execParams);
    }

    public ReassignedSpectrumLikeFitz(AudioReader audioReader, int startSampleIndex, int endSampleIndex, ExecParams execParams) {
        super(audioReader, startSampleIndex, endSampleIndex, execParams);
    }

    public ReassignedSpectrumLikeFitz(AudioReader audioReader, ExecParams execParams) {
        super(audioReader, execParams);
    }

    /**
     * Calculates magnitude and phase spectrum from samples
     *
     * @param samples samples
     * @return
     */
    protected float[][] calculateSpectrumOfFrame(float[] samples, float centerFrameSample) {
        float[] samplesWindowDrivative = new float[samples.length];
        float[] samplesWindowTimeWeighted = new float[samples.length];
        float[] samplesWindowTimeWeightedDerivative = new float[samples.length];

        System.arraycopy(samples, 0, samplesWindowDrivative, 0, samples.length);
        System.arraycopy(samples, 0, samplesWindowTimeWeighted, 0, samples.length);
        System.arraycopy(samples, 0, samplesWindowTimeWeightedDerivative, 0, samples.length);

        WindowFunction.apply(samples, 0, windowLength, WindowType.values()[windowType], WindowOption.WINDOW);
        WindowFunction.apply(samplesWindowDrivative, 0, windowLength, WindowType.values()[windowType], WindowOption.WINDOW_FREQUENCY_WEIGHTED, sampleRate);
        WindowFunction.apply(samplesWindowTimeWeighted, 0, windowLength, WindowType.values()[windowType], WindowOption.WINDOW_TIME_WEIGHTED, sampleRate);
        WindowFunction.apply(samplesWindowTimeWeightedDerivative, 0, windowLength, WindowType.values()[windowType], WindowOption.WINDOW_TIME_FREQUENCY_WEIGHTED, sampleRate);


        double[] samples_d = getSamplesWithProperLengthAndType(samples, 0);
        double[] samples_d_derivative = getSamplesWithProperLengthAndType(samplesWindowDrivative, 0);
        double[] samples_d_timeWeighted = getSamplesWithProperLengthAndType(samplesWindowTimeWeighted, 0);
        double[] samples_d_timeWeightedDerivative = getSamplesWithProperLengthAndType(samplesWindowTimeWeightedDerivative, 0);


        float[][] out = new float[2][numberOfFreqBinsInTheOutputSpectrogram];
        float[] firstTerm = new float[windowLength / 2];
        float[] secondTerm = new float[windowLength / 2];
        float[] thirdTerm = new float[windowLength / 2];
        float[] phaseDoubleDerivative = new float[windowLength / 2];

        float[] magSpectrum;
        float[] magSpecTime;
        float[] magSpecDer;
        float[] magSpecTimeDer;

        try {


//            fft.calcReal(samples_d, -1);
            samples_d = applyFFT(samples_d);
            magSpectrum = extractMagSpec(samples_d);
            Complex[] complexSpectrum = extractComplexSpec(samples_d);
//            inverseFFT(samples_d);

//            fft.calcReal(samples_d_derivative, -1);
            samples_d_derivative = applyFFT(samples_d_derivative);
            magSpecDer = extractMagSpec(samples_d_derivative);
            Complex[] complexSpectrumDerivative = extractComplexSpec(samples_d_derivative);

//            fft.calcReal(samples_d_timeWeighted, -1);
            samples_d_timeWeighted = applyFFT(samples_d_timeWeighted);
            magSpecTime = extractMagSpec(samples_d_timeWeighted);
            Complex[] complexSpectrumTimeWeighted = extractComplexSpec(samples_d_timeWeighted);

            fft.calcReal(samples_d_timeWeighted, -1);
            applyFFT(samples_d_timeWeightedDerivative);
            magSpecTimeDer = extractMagSpec(samples_d_timeWeightedDerivative);
            Complex[] complexSpectrumTimeWeightedDerivative = extractComplexSpec(samples_d_timeWeightedDerivative);

            float reassignedFreq;
            int newBinIndex;
            float timeDelta;
            for (int i = 0; i < magSpectrum.length; i++) {

                //Frequency reassignment

//                Complex devisionResult = complexSpectrumDerivative[i].multiply(complexSpectrum[i].conjugate()).divide(complexSpectrum[i].multiply(complexSpectrum[i].conjugate()));
                Complex devisionResult = complexSpectrumDerivative[i].divide(complexSpectrum[i]);
                reassignedFreq = index2freq(i, 0.5f * sampleRate / magSpectrum.length) - devisionResult.getImag();
                newBinIndex = freq2index(reassignedFreq, (0.5f * sampleRate) / numberOfFreqBinsInTheOutputSpectrogram);

                //Time reassignment
                Complex devisionResultTime = complexSpectrumTimeWeighted[i].divide(complexSpectrum[i]);
                timeDelta = devisionResultTime.getReal();
                float newStartIndexInSamples = centerFrameSample + Math.round(timeDelta * sampleRate);
                int frameIndex = getIndexForSample(newStartIndexInSamples);


                //Now calculate phaseDoubleDerivative and check the necessary condition
                firstTerm[i] = (complexSpectrumTimeWeightedDerivative[i].divide(complexSpectrum[i])).getReal();


                Complex secondTerm1 = (complexSpectrumDerivative[i].divide(complexSpectrum[i]));
                Complex secondTerm2 = (complexSpectrumTimeWeighted[i].divide(complexSpectrum[i]));
                secondTerm[i] = -1 * secondTerm1.getReal() * secondTerm2.getReal();
                thirdTerm[i] = secondTerm1.getImag() * secondTerm2.getImag();

                phaseDoubleDerivative[i] = (firstTerm[i] + secondTerm[i] + thirdTerm[i]) * 2 * (float) Math.PI;


                if (checkCondition(phaseDoubleDerivative[i])) {
                    if (newBinIndex >= 0 && newBinIndex < out[0].length && frameIndex >= 0 && frameIndex < this.magSpec.length) {
                        if (magSpec[frameIndex] != null && magSpec[frameIndex].length > 0) { //Is necessary when building only a part of a spectrum
                            this.magSpec[frameIndex][newBinIndex] += magSpectrum[i];
                        }
                    }
                }
            }
            System.out.println("");
        }
        catch (ArrayIndexOutOfBoundsException e) {
            logger.error("Error while calculating spectrum ");
            logger.error(Helper.getStackTrace(e));
        }
        return out;
    }


}
