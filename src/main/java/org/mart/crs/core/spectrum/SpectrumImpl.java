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

import jnt.FFT.ComplexDoubleFFT;
import jnt.FFT.ComplexDoubleFFT_Mixed;
import org.apache.log4j.Logger;
import org.mart.crs.core.AudioReader;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;
import org.mart.crs.utils.windowing.WindowFunction;
import org.mart.crs.utils.windowing.WindowOption;
import org.mart.crs.utils.windowing.WindowType;
import rasmus.interpreter.sampled.util.FFT;

import java.util.Arrays;


/**
 * User: Hut
 * Date: 17.06.2008
 * Time: 22:53:48
 * Calculates spectogram
 */
public class SpectrumImpl {

    protected static Logger logger = CRSLogger.getLogger(SpectrumImpl.class);


    public static final int SEGMENT_SIZE_FOR_MEMORY_OPTIMIZED_EXTRACTION = 10; //in Seconds

    protected int windowLength;
    protected int windowType;
    protected float overlapping;


    //This audioReader consists of samples with win_len/2 of zeros in the beginning and end
    protected AudioReader audioReader;
    protected float[] samples;
    protected int startSampleIndex;
    protected int endSampleIndex;

    protected float sampleRate;
    protected float sampleRateSpectrum;
    protected float[][] magSpec;

    protected int sampleNumber; //Number of samples in audio it was created from
    protected int numberOfFrames;  //Number of frames in spectrum

    protected FFT fft;


    /**
     * Lazy initialization is used here. This flag indicates whether the initialization was performed
     */
    protected boolean initialized;


    protected SpectrumImpl() {
    }


    public SpectrumImpl(AudioReader audioReader, int startSampleIndex, int endSampleIndex, int windowLength, int windowType, float overlapping) {
        this(audioReader.getSamples(), startSampleIndex, endSampleIndex, audioReader.getSampleRate(), windowLength, windowType, overlapping);
    }

    public SpectrumImpl(AudioReader audioReader, int windowLength, int windowType, float overlapping) {
        this(audioReader.getSamples(), 0, audioReader.getSamples().length - 1, audioReader.getSampleRate(), windowLength, windowType, overlapping);
    }

    public SpectrumImpl(float[] samples, float sampleRate, int windowLength, int windowType, float overlapping) {
        this(samples, 0, samples.length, sampleRate, windowLength, windowType, overlapping);
    }

    public SpectrumImpl(float[] samples, int startSampleIndex, int endSampleIndex, float sampleRate, int windowLength, int windowType, float overlapping) {
        this.audioReader = new AudioReader(samples, sampleRate);
        this.sampleRate = sampleRate;
        this.windowLength = windowLength;
        this.windowType = windowType;
        this.overlapping = overlapping;
        this.samples = samples;
        this.startSampleIndex = startSampleIndex;
        this.endSampleIndex = endSampleIndex;
        this.sampleRateSpectrum = sampleRate / getFrameStep();
        this.sampleNumber = endSampleIndex - startSampleIndex;
        numberOfFrames = (int) Math.floor((this.sampleNumber) / getFrameStep()) + 1;
        fft = new FFT(getProperSpectrumLength(windowLength));
    }

    protected void initialize() {
        if (!initialized) {
            initialize(0, numberOfFrames);
            initialized = true;
        }
    }

    protected void initialize(int startIndex, int endIndex) {
        initializeSpectrumDataArrays(startIndex, endIndex);

        for (int i = startIndex; i < endIndex; i++) {
            float centerFrameSampleIndex = i * getFrameStep();
            processFrame(centerFrameSampleIndex);
        }
    }


    protected void initializeSpectrumDataArrays(int startIndex, int endIndex) {
        magSpec = new float[numberOfFrames][];
    }


    protected void processFrame(float centerFrameSampleIndex) {
        int centerFrameSampleIndexInt = Math.round(centerFrameSampleIndex);
        float[] inputFrame = new float[windowLength];

        int destStartIndex = -1 * Math.min(0, centerFrameSampleIndexInt - windowLength / 2);
        int sourceStartIndex = Math.max(0, centerFrameSampleIndexInt - windowLength / 2);
        int actualLength = Math.min((inputFrame.length - destStartIndex), samples.length - sourceStartIndex);

        System.arraycopy(samples, sourceStartIndex, inputFrame, destStartIndex, actualLength);
        spectralTransform(inputFrame, centerFrameSampleIndex);
    }


    /**
     * Performs spectral transform
     *
     * @param samples                Array of nput samples
     * @param centerFrameSampleIndex Index of the center sample
     */
    protected void spectralTransform(float[] samples, float centerFrameSampleIndex) {
        try {
            WindowFunction.apply(samples, 0, windowLength, WindowType.values()[windowType], WindowOption.WINDOW);
            double[] samples_d = getSamplesWithProperLengthAndType(samples, 0);

            fft.calcReal(samples_d, -1);
            int frameIndex = Math.round(centerFrameSampleIndex / getFrameStep());

            innerSpectralDataExtraction(frameIndex, samples_d);
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("Error while calculating spectrum ");
            logger.error(Helper.getStackTrace(e));
        }
    }


    protected void innerSpectralDataExtraction(int frameIndex, double[] fftTransformedSamples) {
        magSpec[frameIndex] = extractMagSpec(fftTransformedSamples);
    }


    /**
     * Sets the frame length to the rate of 2
     *
     * @param samples
     * @return
     */
    protected double[] getSamplesWithProperLengthAndType(float[] samples, int startIndex) {
        int spectrumLength = getProperSpectrumLength(this.windowLength);
        double out[] = new double[spectrumLength];

        for (int i = startIndex; i < spectrumLength + startIndex; i++) {
            if (i < samples.length) {
                out[i] = samples[i];
            } else {
                out[i] = 0;
            }
        }
        return out;
    }

    /**
     * Reurns spectrum length as a rate of 2.
     *
     * @param actualSpectrumLength actualSpectrumLength
     * @return proper spectrum length
     */
    protected static int getProperSpectrumLength(int actualSpectrumLength) {
        return 1 << (int) Math.ceil(Math.log(actualSpectrumLength) / Math.log(2));
    }

    protected int getNumberOfSpectralBins() {
        return getProperSpectrumLength(windowLength) / 2;
    }

    public float getFrameStep() {
        return windowLength * (1 - overlapping);
    }

    /**
     * Extracts part of the spectrum between the specified time moments
     *
     * @param startTime onset
     * @param endTime   offset
     * @return spectrum
     */
    public SpectrumImpl extractSpectrumPart(float startTime, float endTime) {
        int startIndex = getIndexForTimeMoment(startTime);
        int endIndex = getIndexForTimeMoment(endTime);
        return extractSpectrumPart(startIndex, endIndex);
    }

    /**
     * Extracts part of the spectrum between the specified indixes
     *
     * @param startIndex startIndex
     * @param endIndex   endIndex
     * @return Spectrum
     */
    public SpectrumImpl extractSpectrumPart(int startIndex, int endIndex) {
        initialize(startIndex, endIndex);
        float[][] data = new float[endIndex - startIndex][];
        for (int i = startIndex; i < endIndex; i++) {
            data[i - startIndex] = magSpec[i];
        }
        SpectrumImpl out = null;
        try {
            out = new SpectrumImplMatrixData(data, this.sampleRate, this.sampleRateSpectrum, this.windowLength, this.windowType, this.overlapping);
        } catch (Exception e) {
            logger.error(String.format("Error in the extraction of spectrum part between indices %d %d ", startIndex, endIndex));
        }
        initialized = false;
        return out;
    }


    public double[] applyFFT(double[] array) {
        int length = array.length;
        double[] arrayDoubleSize = new double[length * 2];
        for (int i = 0; i < length; i++) {
            arrayDoubleSize[2 * i] = array[i];
        }

        ComplexDoubleFFT fft = new ComplexDoubleFFT_Mixed(length);
        fft.transform(arrayDoubleSize);
        return arrayDoubleSize;
    }

    public double[] inverseFFT(double[] array) {
        ComplexDoubleFFT fft = new ComplexDoubleFFT_Mixed(array.length / 2);
        fft.inverse(array);
        double[] out = new double[array.length / 2];
        for (int i = 0; i < out.length; i++) {
            out[i] = array[2 * i];
        }
        return out;
    }

    protected static float[] extractMagSpec(double[] buff) {
        float[] out = new float[buff.length / 2];
        for (int i = 0; i < buff.length; i += 2) {
            out[i / 2] = (float) (Math.sqrt(buff[i] * buff[i] + buff[i + 1] * buff[i + 1])) / 512.0f;
        }
        return out;
    }


    public float[] getMagSpecValue(float timeMoment) {
        int index = getIndexForTimeMoment(timeMoment);
        float[] result = Arrays.copyOf(getMagSpec()[index], getMagSpec()[index].length);
        return result;
    }

    public int getIndexForTimeMoment(float timeMoment) {
        int index = (int) Math.floor(timeMoment * sampleRateSpectrum);
        if (index >= numberOfFrames) {
            index = numberOfFrames - 1;
        }
        return index;
    }

    public int getIndexForSample(float sampleNumber) {
        int index = Math.round(sampleNumber / getFrameStep());
        if (index >= numberOfFrames) {
            index = numberOfFrames - 1;
        }
        return index;
    }

    public double getTimeMomentForIndex(int index) {
        float time = index / sampleRateSpectrum;
        return time;
    }


    public float[][] getEnergyBins(float[] frequencyRanges) {
        float[][] output = new float[numberOfFrames][frequencyRanges.length];
        for (int i = 0; i < numberOfFrames; i++) {
            float energySum = 0;
            for (int j = 0; j < frequencyRanges.length - 1; j++) {
                float sum = 0;
                for (int k = Math.min(freq2index(frequencyRanges[j], sampleRate / windowLength), getMagSpec()[0].length - 1); k < Math.min(freq2index(frequencyRanges[j + 1], sampleRate / windowLength), getMagSpec()[0].length - 1); k++) {
                    sum += getMagSpec()[i][k];
                }
                energySum += sum;
                output[i][j] = sum;
            }
            output[i][frequencyRanges.length - 1] = energySum;
        }
        return output;
    }


    public static int freq2index(float freq, float spectralResolution) {
        return Math.round(freq / spectralResolution);
    }

    public static float index2freq(int index, float spectralResolution) {
        return (index * spectralResolution);
    }

    public int freq2index(float freq) {
        return Math.round(freq / getFrequencyResolution());
    }

    public float index2freq(int index) {
        return (index * getFrequencyResolution());
    }


    public float getFrequencyResolution() {
        int spectrumLength = getMagSpec()[0].length;
        float spectralResolution = (float) (0.5 * sampleRate) / spectrumLength;
        return spectralResolution;
    }


    /**
     * getter for samplerate
     *
     * @return
     */
    public float getSampleRate() {
        return sampleRate;
    }


    public int getSampleNumber() {
        if (sampleNumber > 0) {
            return sampleNumber;
        } else {
            return (int) (numberOfFrames / sampleRateSpectrum * sampleRate);
        }
    }


    /**
     * getter for magnitude spectrum
     *
     * @return
     */
    public float[][] getMagSpec() {
        initialize();
        return magSpec;
    }


    /**
     * getter for magnitude spectrum
     *
     * @return spectrumPart
     */
    public SpectrumImpl getSpectrumPart(int startIndex, int endIndex) {
        initialize(startIndex, endIndex);
        return extractSpectrumPart(startIndex, endIndex);
    }


    public float[] getFullEnergy() {
        float[] out = new float[magSpec.length];
        for (int i = 0; i < magSpec.length; i++) {
            out[i] = HelperArrays.sum(magSpec[i]);
        }
        return out;
    }

    public float getSampleRateSpectrum() {
        return sampleRateSpectrum;
    }

    public AudioReader getAudioReader() {
        return audioReader;
    }

    public float getFrameLengthInSamples() {
        return (sampleRate / sampleRateSpectrum);
    }

    public int getWindowLength() {
        return windowLength;
    }

    public int getWindowType() {
        return windowType;
    }

    public float getOverlapping() {
        return overlapping;
    }

    public void setMagSpec(float[][] magSpec) {
        this.magSpec = magSpec;
    }

    public void setSampleNumber(int sampleNumber) {
        this.sampleNumber = sampleNumber;
    }

    public int getNumberOfFrames() {
        return numberOfFrames;
    }
}
