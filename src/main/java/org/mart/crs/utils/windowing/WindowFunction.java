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

package org.mart.crs.utils.windowing;

import jnt.FFT.ComplexFloatFFT;
import jnt.FFT.ComplexFloatFFT_Mixed;
import org.mart.crs.utils.helper.HelperArrays;

/**
 * @version 1.0 25-Aug-2010 10:56:24
 * @author: Hut
 */
public abstract class WindowFunction {

    protected float[] windowFunctionArray;
    protected float[] derivativeFunctionArray;

    public abstract float getFunction(int i, int offset, int width);

    /**
     * @param i      sample number
     * @param offset offset
     * @param width  window width
     * @return
     * @deprecated use getFunctionDerivativeViaFFT instead
     */
    public abstract float getFunctionDerivative(int i, int offset, int width);

    public float getFunctionDerivativeViaFFT(int i, int offset, int width) {
        if (windowFunctionArray == null) {
            windowFunctionArray = new float[width];
            for (int j = 0; j < width; j++) { //TODO include possible offset
                windowFunctionArray[j] = getFunction(j, 0, width);
            }
            derivativeFunctionArray = calculateFunctionDerivativeViaFFT(windowFunctionArray);
        }
        return derivativeFunctionArray[i];
    }


    public static float[] calculateFunctionDerivativeViaFFT(float[] window) {
        int length = window.length;
        float[] weights = new float[length];
        for (int i = 0; i < length / 2; i++) {
            weights[i] = i / (float) length;
        }
        for (int i = length / 2; i < length; i++) {
            weights[i] = (i - length) / (float) length;
        }

        float[] windowDoubleSize = new float[length * 2];
        for (int i = 0; i < length; i++) {
            windowDoubleSize[2 * i] = window[i];
        }

        ComplexFloatFFT fft = new ComplexFloatFFT_Mixed(length);
        fft.transform(windowDoubleSize);


        for (int i = 0; i < length; i++) {
            windowDoubleSize[2 * i] *= weights[i];
            windowDoubleSize[2 * i + 1] *= weights[i];
        }

        fft.inverse(windowDoubleSize);


        float[] out = new float[length];
        for (int i = 0; i < length; i++) {
            out[i] = -1 * windowDoubleSize[2 * i + 1] * 2 * (float) Math.PI;
        }
        return out;
    }

    public float getFunctionFrequencyWeighted(int i, int offset, int width, float samplingFreq) {
//        return getFunctionDerivative(i, offset, width) * (samplingFreq / (float)(2 * Math.PI));
        return getFunctionDerivativeViaFFT(i, offset, width) * (samplingFreq / (float) (2 * Math.PI));

    }

    public float getFunctionTimeWeighted(int i, int offset, int width, float samplingFreq) {
        return getFunction(i, offset, width) * (i - offset - width / 2) / samplingFreq;
    }

    public float getFunctionTimeFrequencyWeighted(int i, int offset, int width, float samplingFreq) {
        return getFunctionFrequencyWeighted(i, offset, width, samplingFreq) * (i - offset - width / 2) / samplingFreq;
    }


    public void apply(float[] data, int offset, int width, WindowOption windowingOption) {
        this.apply(data, offset, width, windowingOption, 1f);
    }

    public void apply(float[] data, int offset, int width, WindowOption windowingOption, float samplingFrequency) {
        float[] windowFunction = getWindowFunction(data.length, offset, width, windowingOption, samplingFrequency);
        HelperArrays.multiply(data, windowFunction);
    }

    public void applyReverse(float[] data, int offset, int width, WindowOption windowingOption, float samplingFrequency) {
        float[] windowFunction = getWindowFunction(data.length, offset, width, windowingOption, samplingFrequency);
        HelperArrays.divideVectors(data, windowFunction);
    }


    public float[] getWindowFunction(int dataLength, int offset, int width, WindowOption windowingOption, float samplingFrequency) {
        float[] out = new float[dataLength];
        for (int i = 0; i < dataLength; i++) {
            if (i < offset) {
                out[i] = 0;
                continue;
            }
            if (i < width + offset) {
                switch (windowingOption) {
                    case WINDOW:
                        out[i] = getFunction(i, offset, width);
                        break;
                    case WINDOW_FREQUENCY_WEIGHTED:
                        out[i] = getFunctionFrequencyWeighted(i, offset, width, samplingFrequency);
                        break;
                    case WINDOW_TIME_WEIGHTED:
                        out[i] = getFunctionTimeWeighted(i, offset, width, samplingFrequency);
                        break;
                    case WINDOW_TIME_FREQUENCY_WEIGHTED:
                        out[i] = getFunctionTimeFrequencyWeighted(i, offset, width, samplingFrequency);
                        break;
                }
            } else {
                out[i] = 0;
            }
        }
        return out;
    }

    public static void apply(float[] data, int offset, int width, WindowType windowType, WindowOption windowOption, float samplingRate) {
        WindowFunction windowFunction = getWindowFunction(windowType);
        windowFunction.apply(data, offset, width, windowOption, samplingRate);
    }

    public static void applyReverse(float[] data, int offset, int width, WindowType windowType, WindowOption windowOption, float samplingRate) {
        WindowFunction windowFunction = getWindowFunction(windowType);
        windowFunction.applyReverse(data, offset, width, windowOption, samplingRate);
    }

    protected static WindowFunction getWindowFunction(WindowType windowType) {
        WindowFunction windowFunction;
        switch (windowType) {
            case RECTANGULAR_WINDOW:
                windowFunction = new Rectangular();
                break;
            case TRIANGULAR_WINDOW:
                windowFunction = new Triangular();
                break;
            case HAMMING_WINDOW:
                windowFunction = new Hamming();
                break;
            case HANNING_WINDOW:
                windowFunction = new Hanning();
                break;
            case BLACKMAN_WINDOW:
                windowFunction = new Blackman();
                break;
            case KAISER_WINDOW:
                windowFunction = new Kaiser();
                break;
            default:
                windowFunction = new Rectangular();
        }
        return windowFunction;
    }

    public static void apply(float[] data, int offset, int width, WindowType windowType, WindowOption windowOption) {
        apply(data, offset, width, windowType, windowOption, 1);
    }

    public static void applyReverse(float[] data, int offset, int width, WindowType windowType, WindowOption windowOption) {
        applyReverse(data, offset, width, windowType, windowOption, 1);
    }



    public static void apply(double[] data, int width, int offset, WindowType windowType, WindowOption windowOption) {
        apply(HelperArrays.getDoubleAsFloat(data), offset, width, windowType, windowOption);
    }


    public static void apply(float[] data, WindowType windowType, WindowOption windowOption) {
        apply(data, 0, data.length, windowType, windowOption);
    }

    public static void apply(double[] data, WindowType windowType, WindowOption windowOption) {
        apply(HelperArrays.getDoubleAsFloat(data), 0, data.length, windowType, windowOption);
    }


    public float[] getWindowFunctionArrayNormalized(int width){
        float sum = 0;
        float[] out = new float[width];
        for (int i = 0; i < width; ++i) {
            out[i] = getFunction(i, 0, width);
            sum += out[i];
        }
        for (int i = 0; i < width; ++i) {
            out[i] = out[i] / sum;
        }
        return out;
    }

}
