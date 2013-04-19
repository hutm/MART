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

import de.dfki.maths.Complex;
import org.mart.crs.core.AudioReader;
import org.mart.crs.utils.helper.HelperArrays;
import org.mart.crs.utils.windowing.WindowFunction;
import org.mart.crs.utils.windowing.WindowOption;
import org.mart.crs.utils.windowing.WindowType;
import rasmus.interpreter.sampled.util.FFT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @version 1.0 3/28/12 4:00 PM
 * @author: Hut
 */
public class SpectrumImplPhaseComponents extends SpectrumImpl {


    protected boolean savePhaseSpectrum;
    protected boolean saveComplexComponents;


    protected float[][] phaseSpec;
    protected float[][] complexSpectrumRealPart;
    protected float[][] complexSpectrumImagPart;

    {
        this.saveComplexComponents = true;
        this.savePhaseSpectrum = true;
    }




    public SpectrumImplPhaseComponents(AudioReader audioReader, int startSampleIndex, int endSampleIndex, int windowLength, int windowType, float overlapping) {
        super(audioReader, startSampleIndex, endSampleIndex, windowLength, windowType, overlapping);
    }

    public SpectrumImplPhaseComponents(AudioReader audioReader, int windowLength, int windowType, float overlapping) {
        super(audioReader, windowLength, windowType, overlapping);
    }

    public SpectrumImplPhaseComponents(float[] samples, float sampleRate, int windowLength, int windowType, float overlapping) {
        super(samples, sampleRate, windowLength, windowType, overlapping);
    }

    public SpectrumImplPhaseComponents(float[] samples, int startSampleIndex, int endSampleIndex, float sampleRate, int windowLength, int windowType, float overlapping) {
        super(samples, startSampleIndex, endSampleIndex, sampleRate, windowLength, windowType, overlapping);
    }


    @Override
    protected void initializeSpectrumDataArrays(int startIndex, int endIndex) {
        super.initializeSpectrumDataArrays(startIndex, endIndex);
        phaseSpec = new float[numberOfFrames][];
        complexSpectrumRealPart = new float[numberOfFrames][];
        complexSpectrumImagPart = new float[numberOfFrames][];


//        for (int i = startIndex; i < endIndex; i++) {       //TODO: remove this part after testing
//            if (saveMagSpectrum) {              //TODO: revise this. the variable should be removed
//                magSpec[i] = new float[getNumberOfSpectralBins()];
//            }
//            if (savePhaseSpectrum) {
//                phaseSpec[i] = new float[getNumberOfSpectralBins()];
//            }
//            if (saveComplexComponents) {
//                complexSpectrumRealPart[i] = new float[getNumberOfSpectralBins()];
//                complexSpectrumImagPart[i] = new float[getNumberOfSpectralBins()];
//            }
//        }
    }

    @Override
    protected void innerSpectralDataExtraction(int frameIndex, double[] fftTransformedSamples) {
        super.innerSpectralDataExtraction(frameIndex, fftTransformedSamples);
        if (savePhaseSpectrum) {
            phaseSpec[frameIndex] = extractPhaseSpec(fftTransformedSamples);
        }
        if (saveComplexComponents) {
            complexSpectrumRealPart[frameIndex] = extractComplexSpecRealPart(fftTransformedSamples);
            complexSpectrumImagPart[frameIndex] = extractComplexSpecImagPart(fftTransformedSamples);
        }
    }

    protected static float[] extractPhaseSpec(double[] buff) {
        float[] out = new float[buff.length / 2];
        for (int i = 0; i < buff.length; i += 2) {
            out[i / 2] = (float) (Math.atan2(buff[i + 1], buff[i]));
        }
        return out;
    }

    protected static Complex[] extractComplexSpec(double[] buff) {
        Complex[] out = new Complex[buff.length / 2];
        for (int i = 0; i < buff.length; i += 2) {
            out[i / 2] = new Complex((float) buff[i], (float) buff[i + 1]);
        }
        return out;
    }

    protected static float[] extractComplexSpecRealPart(double[] buff) {
        float[] out = new float[buff.length / 2];
        for (int i = 0; i < buff.length; i += 2) {
            out[i / 2] = (float) buff[i];
        }
        return out;
    }


    protected static float[] extractComplexSpecImagPart(double[] buff) {
        float[] out = new float[buff.length / 2];
        for (int i = 0; i < buff.length; i += 2) {
            out[i / 2] = (float) buff[i + 1];
        }
        return out;
    }




    /**
     * Calculates samples from magnitude and phase spectrum
     *
     * @return
     */
    public float[] inverseFFT(boolean[][] mask, boolean isToUseMask) {
        List<float[]> out = new ArrayList<float[]>();
        getMagSpec();
        fft = new FFT(complexSpectrumRealPart[0].length * 2);
        for (int i = 0; i < complexSpectrumRealPart.length; i++) {

            double[] buffer = new double[complexSpectrumRealPart[0].length * 2];
            float[] samplesBuffer = new float[complexSpectrumRealPart[0].length * 2];

            for (int j = 0; j < complexSpectrumRealPart[0].length; j++) {
                if (!isToUseMask || (mask != null && mask[i][j])) {
                    buffer[2 * j] = complexSpectrumRealPart[i][j];
                    buffer[2 * j + 1] = complexSpectrumImagPart[i][j];
                }
            }

            fft.calcReal(buffer, 1);

            for (int j = 0; j < buffer.length; j++) {
                samplesBuffer[j] = (float) buffer[j] / (complexSpectrumRealPart[0].length);
            }
            WindowFunction.applyReverse(samplesBuffer, 0, windowLength, WindowType.values()[windowType], WindowOption.WINDOW);


            int frameStep = Math.round(getFrameStep());
            float[] sampleBufferPart = new float[frameStep];
            System.arraycopy(samplesBuffer, (samplesBuffer.length - frameStep) / 2 + 3, sampleBufferPart, 0, frameStep);   //TODO: hardcoded numberm 3 samples

            out.add(sampleBufferPart);
        }

        return HelperArrays.concat(out);
    }


    public float[] inverseFFT() {
        return inverseFFT(null, false);
    }



    public float[][] getUnwrappedPhaseSpecValue(float timeMoment, int numberOfAdjacentFrames) {
        int ind, index = getIndexForTimeMoment(timeMoment);
        float[][] result = new float[2 * numberOfAdjacentFrames + 1][];
        for (int i = -numberOfAdjacentFrames; i <= numberOfAdjacentFrames; i++) {
            ind = index + i;
            if (ind < 0) {
                result[i + numberOfAdjacentFrames] = getPhaseSpecValueUnwrapped(getPhaseSpec()[0]);
            } else if (ind >= getPhaseSpec()[0].length) {
                result[i + numberOfAdjacentFrames] = getPhaseSpecValueUnwrapped(getPhaseSpec()[getPhaseSpec()[0].length - 1]);
            } else {
                result[i + numberOfAdjacentFrames] = getPhaseSpecValueUnwrapped(getPhaseSpec()[ind]);
            }
        }
        return result;
    }


    private float[] getPhaseSpecValueUnwrapped(float[] wrapedPhaseSpec) {
        double cutOff = Math.PI;

        //Create object for unwrapped spec. Algorithm is taken from Matlab unwrap() function
        float[] unwrappedSpec = Arrays.copyOf(wrapedPhaseSpec, wrapedPhaseSpec.length);

        float[] diffFunction = new float[wrapedPhaseSpec.length - 1];
        float[] diffFunctionInRange = new float[wrapedPhaseSpec.length - 1];
        float[] incrementalPhaseCorrection = new float[wrapedPhaseSpec.length - 1];
        for (int j = 0; j < unwrappedSpec.length - 1; j++) {
            diffFunction[j] = unwrappedSpec[j + 1] - unwrappedSpec[j];

            diffFunctionInRange[j] = diffFunction[j];
            //Should be in the interval [-Pi; Pi)
            while (diffFunctionInRange[j] > Math.PI) {
                diffFunctionInRange[j] -= 2 * Math.PI;
            }
            while (diffFunctionInRange[j] <= -Math.PI) {
                diffFunctionInRange[j] += 2 * Math.PI;
            }

            incrementalPhaseCorrection[j] = diffFunctionInRange[j] - diffFunction[j];
            if (Math.abs(incrementalPhaseCorrection[j]) < cutOff) {
                incrementalPhaseCorrection[j] = 0;
            }

        }

        float sum = 0;
        for (int j = 0; j < unwrappedSpec.length - 1; j++) {
            sum += incrementalPhaseCorrection[j];
            unwrappedSpec[j + 1] += sum;
        }

        return unwrappedSpec;
    }






    /**
     * getter for phase spectrum
     *
     * @return
     */
    public float[][] getPhaseSpec() {
        initialize();
        return phaseSpec;
    }

    public float[] getPhaseSpecValue(float timeMoment) {
        int index = getIndexForTimeMoment(timeMoment);
        float[] result = Arrays.copyOf(getPhaseSpec()[index], getPhaseSpec()[index].length);
        return result;
    }

    public float[] getUnwrappedPhaseSpecValue(float timeMoment) {
        int index = getIndexForTimeMoment(timeMoment);
        float[] result = getPhaseSpecValueUnwrapped(getPhaseSpec()[index]);
        return result;
    }

    public float[][] getPhaseSpecUnwrapped() {
        float[][] unwrappedSpec = new float[getPhaseSpec().length][];

        for (int i = 0; i < getPhaseSpec().length; i++) {
            unwrappedSpec[i] = getPhaseSpecValueUnwrapped(getPhaseSpec()[i]);
        }
        return unwrappedSpec;
    }

}
