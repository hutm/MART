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
import org.mart.crs.config.Settings;
import org.mart.crs.core.AudioReader;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;
import org.mart.crs.utils.windowing.WindowFunction;
import org.mart.crs.utils.windowing.WindowOption;
import org.mart.crs.utils.windowing.WindowType;
import de.dfki.maths.Complex;
import jnt.FFT.ComplexDoubleFFT;
import jnt.FFT.ComplexDoubleFFT_Mixed;
import org.apache.log4j.Logger;
import org.mart.crs.utils.helper.HelperData;
import rasmus.interpreter.sampled.util.FFT;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * User: Hut
 * Date: 17.06.2008
 * Time: 22:53:48
 * Calculates spectogram
 */
public class SpectrumImpl {

    protected static Logger logger = CRSLogger.getLogger(SpectrumImpl.class);

    protected ExecParams execParams;

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
    protected float[][] phaseSpec;
    protected float[][] complexSpectrumRealPart;
    protected float[][] complexSpectrumImagPart;


    protected int sampleNumber; //Number of samples in audio it was created from
    protected int numberOfFrames;

    protected FFT fft;


    /**
     * Lazy initialization is used here. This flag indicates whether the initialization was performed
     */
    protected boolean initialized;



    public SpectrumImpl(AudioReader audioReader, ExecParams execParams) {
        this(audioReader.getSamples(), 0, audioReader.getSamples().length - 1, audioReader.getSampleRate(), execParams.windowLength, execParams.windowType, execParams.overlapping, execParams);
    }


    public SpectrumImpl(AudioReader audioReader, int startSampleIndex, int endSampleIndex, int windowLength, int windowType, float overlapping, ExecParams execParams) {
        this(audioReader.getSamples(), startSampleIndex, endSampleIndex, audioReader.getSampleRate(), windowLength, windowType, overlapping, execParams);
    }

    public SpectrumImpl(AudioReader audioReader, int windowLength, int windowType, float overlapping, ExecParams execParams) {
        this(audioReader.getSamples(), 0, audioReader.getSamples().length - 1, audioReader.getSampleRate(), windowLength, windowType, overlapping, execParams);
    }


    public SpectrumImpl(float[] samples, int startSampleIndex, int endSampleIndex, float sampleRate, ExecParams execParams) {
        this(samples, startSampleIndex, endSampleIndex, sampleRate, execParams.windowLength, execParams.windowType, execParams.overlapping, execParams);
    }

    public SpectrumImpl(float[] samples, float sampleRate, ExecParams execParams) {
        this(samples, 0, samples.length - 1, sampleRate, execParams.windowLength, execParams.windowType, execParams.overlapping, execParams);
    }


    public SpectrumImpl(float[] samples, int startSampleIndex, int endSampleIndex, float sampleRate, int windowLength, float overlapping, ExecParams execParams) {
        this(samples, startSampleIndex, endSampleIndex, sampleRate, windowLength, execParams.windowType, overlapping, execParams);
    }

    public SpectrumImpl(float[] samples, float sampleRate, int windowLength, float overlapping, ExecParams execParams) {
        this(samples, 0, samples.length - 1, sampleRate, windowLength, execParams.windowType, overlapping, execParams);
    }


    public SpectrumImpl(float[] samples, int startSampleIndex, int endSampleIndex, float sampleRate, int windowLength, int windowType, float overlapping, ExecParams execParams) {
        this.execParams = execParams;
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
    }

    /**
     * Explicitly difine spectrum data
     *
     * @param spectrumData
     * @param spectrumSampleRate
     */
    public SpectrumImpl(float[][] spectrumData, float sampleRate, float spectrumSampleRate, ExecParams execParams) {
        this(spectrumData, sampleRate, spectrumSampleRate, execParams.windowLength, execParams.windowType, execParams.overlapping, execParams);
    }

    /**
     * Explicitly difine spectrum data
     *
     * @param spectrumData
     * @param spectrumSampleRate
     */
    public SpectrumImpl(float[][] spectrumData, float sampleRate, float spectrumSampleRate, int windowLength, int windowType, float overlapping, ExecParams execParams) {
        this.execParams = execParams;
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


    protected void initializespectrum() {

        initializeSpectrumDataArrays(numberOfFrames);

        fft = new FFT(getProperSpectrumLength(windowLength));

        for (int i = 0; i < numberOfFrames; i++) {
            float centerFrameSampleIndex = i * getFrameStep();
            processFrame(centerFrameSampleIndex);
        }
    }

    protected void initializespectrum(int startIndex, int endIndex) {

        fft = new FFT(getProperSpectrumLength(windowLength));

        //In order to take into account time reassignment, extend the calculation by contextLength that is set to 1 sec time interval
        int contextLength = 30;//Math.round(3 / (1 - overlapping));//(int) Math.round(1.0 * this.getSampleRateSpectrum());
        int startIndexWithContext = Math.max(startIndex - contextLength, 0);
        int endIndexWithContext = Math.min(endIndex + contextLength, numberOfFrames);

        initializeSpectrumDataArrays(startIndexWithContext, endIndexWithContext);

        for (int i = startIndexWithContext; i < endIndexWithContext; i++) {
            float centerFrameSampleIndex = i * getFrameStep();
            processFrame(centerFrameSampleIndex);
        }
    }

    protected void initializeSpectrumDataArrays(int numberOfFrames) {
        magSpec = new float[numberOfFrames][];
        phaseSpec = new float[numberOfFrames][];
        complexSpectrumRealPart = new float[numberOfFrames][];
        complexSpectrumImagPart = new float[numberOfFrames][];
    }

    protected void initializeSpectrumDataArrays(int startIndex, int endIndex) {
        magSpec = new float[numberOfFrames][];
        phaseSpec = new float[numberOfFrames][];
        complexSpectrumRealPart = new float[numberOfFrames][];
        complexSpectrumImagPart = new float[numberOfFrames][];

        for (int i = startIndex; i < endIndex; i++) {
            if (Settings.saveMagSpectrum) {
                magSpec[i] = new float[getNumberOfSpectralBins()];
            }
            if (Settings.savePhaseSpectrum) {
                phaseSpec[i] = new float[getNumberOfSpectralBins()];
            }
            if (Settings.saveHPSRelatedInformation) {
                complexSpectrumRealPart[i] = new float[getNumberOfSpectralBins()];
                complexSpectrumImagPart[i] = new float[getNumberOfSpectralBins()];
            }
        }
    }


    protected int getNumberOfSpectralBins() {
        return getProperSpectrumLength(windowLength) / 2;
    }

    protected float[][] processFrame(float centerFrameSampleIndex) {
        int centerFrameSampleIndexInt = Math.round(centerFrameSampleIndex);
        float[] inputFrame = new float[windowLength];

        int destStartIndex = -1 * Math.min(0, centerFrameSampleIndexInt - windowLength / 2);
        int sourceStartIndex = Math.max(0, centerFrameSampleIndexInt - windowLength / 2);
        int actualLength = Math.min((inputFrame.length - destStartIndex), samples.length - sourceStartIndex);

        System.arraycopy(samples, sourceStartIndex, inputFrame, destStartIndex, actualLength);
        float[][] spectrum = calculateSpectrumOfFrame(inputFrame, centerFrameSampleIndex);


        return spectrum;
    }


    /**
     * Calculates magnitude and phase spectrum from samples
     *
     * @param samples samples
     * @return
     */
    protected float[][] calculateSpectrumOfFrame(float[] samples, float centerFrameSampleIndex) {
        float[][] out = new float[4][];
        try {
            WindowFunction.apply(samples, 0, windowLength, WindowType.values()[windowType], WindowOption.WINDOW);
            double[] samples_d = getSamplesWithProperLengthAndType(samples, 0);

            fft.calcReal(samples_d, -1);
            out[0] = extractMagSpec(samples_d);
            out[1] = extractPhaseSpec(samples_d);
            out[2] = extractComplexSpecRealPart(samples_d);
            out[3] = extractComplexSpecImagPart(samples_d);


            int frameIndex = Math.round(centerFrameSampleIndex / getFrameStep());
            if (Settings.saveMagSpectrum) {
                magSpec[frameIndex] = out[0];
            }
            if (Settings.savePhaseSpectrum) {
                phaseSpec[frameIndex] = out[1];
            }
            if (Settings.saveHPSRelatedInformation) {
                complexSpectrumRealPart[frameIndex] = out[2];
                complexSpectrumImagPart[frameIndex] = out[3];
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("Error while calculating spectrum ");
            logger.error(Helper.getStackTrace(e));
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

            for (int j = 0; j < complexSpectrumRealPart[0].length; j ++) {
                if (!isToUseMask || (mask != null && mask[i][j])) {
                    buffer[2 *j] = complexSpectrumRealPart[i][j];
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
            System.arraycopy(samplesBuffer, (samplesBuffer.length - frameStep)/2 + 3, sampleBufferPart, 0, frameStep);   //TODO hardcoded numberm 3 samples

            out.add(sampleBufferPart);
        }

        return HelperArrays.concat(out);
    }


    public float[] inverseFFT(){
        return inverseFFT(null, false);
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
    public static int getProperSpectrumLength(int actualSpectrumLength) {
        return 1 << (int) Math.ceil(Math.log(actualSpectrumLength) / Math.log(2));
    }

    public float getFrameStep() {
        return windowLength * (1 - overlapping);
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


    public float[][] getPhaseSpecUnwrapped() {
        float[][] unwrappedSpec = new float[getPhaseSpec().length][];

        for (int i = 0; i < getPhaseSpec().length; i++) {
            unwrappedSpec[i] = getPhaseSpecValueUnwrapped(getPhaseSpec()[i]);
        }
        return unwrappedSpec;
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

    public float[] getMagSpecValue(float timeMoment) {
        int index = getIndexForTimeMoment(timeMoment);
        float[] result = Arrays.copyOf(getMagSpec()[index], getMagSpec()[index].length);
        return result;
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

    public float[] getPhaseChange(float timeMoment, int shiftInSamples) {
        int index = getIndexForTimeMoment(timeMoment);

        int startIndexInSamples = (int) (index * getFrameStep()) + shiftInSamples;
        float[][] spec = processFrame(startIndexInSamples);
        float[] framePhase = spec[1];

        startIndexInSamples += shiftInSamples;
        spec = processFrame(startIndexInSamples);
        float[] framePhaseShifted = spec[1];

        float[] phaseChange = HelperArrays.subtractPhasesAbs(framePhaseShifted, framePhase);
        phaseChange[0] = 0;
        for (int i = 1; i < phaseChange.length; i++) {
            phaseChange[i] /= 2 * Math.PI * shiftInSamples / sampleRate;
        }

        return phaseChange;
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
        return (int) Math.round(freq / spectralResolution);
    }

    public static float index2freq(int index, float spectralResolution) {
        return (float) (index * spectralResolution);
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
        initializespectrum(startIndex, endIndex);
        float[][] data = new float[endIndex - startIndex][];
        for (int i = startIndex; i < endIndex; i++) {
            data[i - startIndex] = magSpec[i];
        }
        SpectrumImpl out = null;
        try {
            out = new SpectrumImpl(data, sampleRate, sampleRateSpectrum, execParams);
        } catch (Exception e) {
            logger.error(String.format("%d %d ", startIndex, endIndex));
        }
        initialized = false;
        return out;
    }


    public void exportSpectum(String outFilePath) {
        logger.info("Saving spectrum data in file " + outFilePath);
        try {
            BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(outFilePath));

            int maxIndexToStore = getMagSpec()[0].length;


            HelperData.writeFloat(sampleRate, outStream);
            HelperData.writeFloat(sampleRateSpectrum, outStream);
            HelperData.writeInt(numberOfFrames, outStream);
            HelperData.writeInt(getMagSpec()[0].length, outStream);
            HelperData.writeInt(maxIndexToStore, outStream);

            for (int i = 0; i < numberOfFrames; i++) {
                for (int j = 0; j < maxIndexToStore; j++) {
                    HelperData.writeFloat(getMagSpec()[i][j], outStream);
                }
            }
            outStream.close();
        } catch (IOException e) {
            logger.error("Could not export spectrum to file " + outFilePath);
            logger.error(Helper.getStackTrace(e));
        }
    }

    public static SpectrumImpl importSpectrum(String inFilePath) {
        try {
            BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(inFilePath));
            float sampleRate = HelperData.readFloat(inStream);
            float sampleRateSpectrum = HelperData.readFloat(inStream);
            int dim1 = HelperData.readInt(inStream);
            int dim2 = HelperData.readInt(inStream);
            int maxIndexToStore = HelperData.readInt(inStream);

            float[][] magSpec = new float[dim1][dim2];
            for (int i = 0; i < magSpec.length; i++) {
                for (int j = 0; j < maxIndexToStore; j++) {
                    magSpec[i][j] = HelperData.readFloat(inStream);
                }
            }
            inStream.close();
            return new SpectrumImpl(magSpec, sampleRate, sampleRateSpectrum, ExecParams._initialExecParameters);

        } catch (Exception e) {
            logger.error("Could not import spectrum from file " + inFilePath);
            logger.error(Helper.getStackTrace(e));
            return null;
        }

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
        if (!initialized) {
            initialized = true;
            initializespectrum();
        }
        return magSpec;
    }


    /**
     * getter for magnitude spectrum
     *
     * @return spectrumPart
     */
    public SpectrumImpl getSpectrumPart(int startIndex, int endIndex) {
        initializespectrum(startIndex, endIndex);
        return extractSpectrumPart(startIndex, endIndex);
    }

    /**
     * getter for phase spectrum
     *
     * @return
     */
    public float[][] getPhaseSpec() {
        if (!initialized) {
            initialized = true;
            initializespectrum();
        }
        return phaseSpec;
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
