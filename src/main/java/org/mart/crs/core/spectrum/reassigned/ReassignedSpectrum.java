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

package org.mart.crs.core.spectrum.reassigned;

import de.dfki.maths.Complex;
import org.mart.crs.core.AudioReader;
import org.mart.crs.core.spectrum.SpectrumImplPhaseComponents;
import org.mart.crs.utils.windowing.WindowFunction;
import org.mart.crs.utils.windowing.WindowOption;
import org.mart.crs.utils.windowing.WindowType;

/**
 * @version 1.0 18-May-2010 13:51:00
 * @author: Hut
 */
public class ReassignedSpectrum extends SpectrumImplPhaseComponents {

    public static final int NUMBER_OF_BINS_FOR_REASSIGNED_SPECTROGRAM = 4096;

    {
        this.saveComplexComponents = false;
        this.savePhaseSpectrum = false;
    }


    protected float[][] timeReasValues;
    protected float[][] frequencyReasValues;
    protected float[][] energyReasValues;

    /**
     * This value is needed for avoiding "scattered" spectrum. Used when performing harmonic filtering
     */
    protected float threshold;


    public ReassignedSpectrum(AudioReader audioReader, int startSampleIndex, int endSampleIndex, int windowLength, int windowType, float overlapping) {
        super(audioReader, startSampleIndex, endSampleIndex, windowLength, windowType, overlapping);
    }

    public ReassignedSpectrum(AudioReader audioReader, int windowLength, int windowType, float overlapping) {
        super(audioReader, 0, audioReader.getSamples().length - 1, windowLength, windowType, overlapping);
    }

    public ReassignedSpectrum(float[] samples, int startSampleIndex, int endSampleIndex, float sampleRate, int windowLength, int windowType, float overlapping) {
        super(samples, startSampleIndex, endSampleIndex, sampleRate, windowLength, windowType, overlapping);
    }

    public ReassignedSpectrum(float[] samples, float sampleRate, int windowLength, int windowType, float overlapping) {
        super(samples, sampleRate, windowLength, windowType, overlapping);
    }


    @Override
    public float getFrequencyResolution() {
        int spectrumLength = getMagSpectrumStandardArrayForm(NUMBER_OF_BINS_FOR_REASSIGNED_SPECTROGRAM)[0].length;
        float spectralResolution = (float) (0.5 * sampleRate) / spectrumLength;
        return spectralResolution;
    }

    protected void initializeSpectrumDataArrays(int startFrame, int endFrame) {
        timeReasValues = new float[numberOfFrames][windowLength / 2];
        frequencyReasValues = new float[numberOfFrames][windowLength / 2];
        energyReasValues = new float[numberOfFrames][windowLength / 2];
    }


    /**
     * Calculates magnitude and phase spectrum from samples
     *
     * @param samples samples
     * @return
     */
    protected void spectralTransform(float[] samples, float centerFrameSample) {
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

        int currentFrameIndex = getIndexForSample(centerFrameSample);

        fft.calcReal(samples_d, -1);
        Complex[] complexSpectrum = extractComplexSpec(samples_d);

        fft.calcReal(samples_d_derivative, -1);
        Complex[] complexSpectrumDerivative = extractComplexSpec(samples_d_derivative);

        fft.calcReal(samples_d_timeWeighted, -1);
        Complex[] complexSpectrumTimeWeighted = extractComplexSpec(samples_d_timeWeighted);

        fft.calcReal(samples_d_timeWeightedDerivative, -1);
        Complex[] complexSpectrumTimeWeightedDerivative = extractComplexSpec(samples_d_timeWeightedDerivative);

        innerSpectralDataExtraction(currentFrameIndex, samples_d);
        innerSpectralDataExtractionReassigned(currentFrameIndex, centerFrameSample, complexSpectrum, complexSpectrumDerivative, complexSpectrumTimeWeighted, complexSpectrumTimeWeightedDerivative);
    }

    /**
     * Data from ffttransformedSamples is extracted here.
     * @param currentFrameIndex  currentFrameIndex
     * @param ffttransformedSamples   ffttransformedSamples
     */
    protected void innerSpectralDataExtraction(int currentFrameIndex, double[] ffttransformedSamples){
        this.energyReasValues[currentFrameIndex] = extractMagSpec(ffttransformedSamples);
    }



    protected void innerSpectralDataExtractionReassigned(int currentFrameIndex, float centerFrameSample,  Complex[] complexSpectrum, Complex[] complexSpectrumDerivative, Complex[] complexSpectrumTimeWeighted, Complex[] complexSpectrumTimeWeightedDerivative) {


        float[] spectralFram = energyReasValues[currentFrameIndex];


        for (int i = 0; i < spectralFram.length; i++) {


            ReassignedFrame frame = new ReassignedFrame();
            frame.setCurrentFrameIndex(currentFrameIndex);
            frame.setCurrentFreqIndex(i);

            //Initialize with negative values first
            frequencyReasValues[currentFrameIndex][i] = -1;
            timeReasValues[currentFrameIndex][i] = -1;

            //Frequency reassignment
            Complex devisionResult = complexSpectrumDerivative[i].divide(complexSpectrum[i]);
            float centerFrequency = index2freq(i, 0.5f * sampleRate / spectralFram.length);
            frame.setFreqDelta(devisionResult.getImag());


            //Time reassignment
            Complex divisionResultTime = complexSpectrumTimeWeighted[i].divide(complexSpectrum[i]);
            frame.setTimeDelta(divisionResultTime.getReal());


            //Now calculate phaseDoubleDerivative and check the necessary condition
            Complex firstTerm = complexSpectrumTimeWeightedDerivative[i].divide(complexSpectrum[i]);
            Complex secondTerm = (complexSpectrumTimeWeighted[i].divide(complexSpectrum[i]));
            secondTerm = secondTerm.multiply(complexSpectrumDerivative[i].divide(complexSpectrum[i]));

            frame.setPhaseDoubleDerivative((firstTerm.getReal() - secondTerm.getReal()) * 2 * (float) Math.PI);


            if (checkCondition(frame.getPhaseDoubleDerivative())) {
                frequencyReasValues[currentFrameIndex][i] = centerFrequency - frame.getFreqDelta();
                timeReasValues[currentFrameIndex][i] = centerFrameSample + frame.getTimeDelta() * sampleRate;
            }

            additionalCalculations(frame);
        }
    }


    protected void additionalCalculations(ReassignedFrame frame){
        //Overloadded in child classes
    }


    public float[][] getMagSpectrumStandardArrayForm(int numberOfFreqBinsInTheOutputSpectrogram){
        initialize();
        float[][] outSpectrum = new float[energyReasValues.length][numberOfFreqBinsInTheOutputSpectrogram];
        for(int i = 0; i < energyReasValues.length; i++){
            for(int j = 0; j < energyReasValues[i].length; j++){
                int newFrameIndex = getIndexForSample(timeReasValues[i][j]);
                int newBinIndex = freq2index(frequencyReasValues[i][j], (0.5f * sampleRate) / numberOfFreqBinsInTheOutputSpectrogram);

                //check that there is no out of bound error
                if (newBinIndex >= 0 && newBinIndex < outSpectrum[0].length && newFrameIndex >= 0 && newFrameIndex < outSpectrum.length) {
                    outSpectrum[newFrameIndex][newBinIndex] += energyReasValues[i][j];
                }
            }
        }
        return outSpectrum;
    }


    protected boolean checkCondition(float phaseDoubleDerivative) {
        return true; //no filtering. All energies are added.
    }


    public float[][] getTimeReasValues() {
        initialize();
        return timeReasValues;
    }

    public float[][] getFrequencyReasValues() {
        initialize();
        return frequencyReasValues;
    }

    public float[][] getEnergyReasValues() {
        initialize();
        return energyReasValues;
    }





    class ReassignedFrame{

        protected float timeDelta;
        protected float freqDelta;

        protected int currentFrameIndex;
        protected int currentFreqIndex;

        protected float phaseDoubleDerivative;

        public float getTimeDelta() {
            return timeDelta;
        }

        public void setTimeDelta(float timeDelta) {
            this.timeDelta = timeDelta;
        }

        public float getFreqDelta() {
            return freqDelta;
        }

        public void setFreqDelta(float freqDelta) {
            this.freqDelta = freqDelta;
        }

        public int getCurrentFrameIndex() {
            return currentFrameIndex;
        }

        public void setCurrentFrameIndex(int currentFrameIndex) {
            this.currentFrameIndex = currentFrameIndex;
        }

        public int getCurrentFreqIndex() {
            return currentFreqIndex;
        }

        public void setCurrentFreqIndex(int currentFreqIndex) {
            this.currentFreqIndex = currentFreqIndex;
        }

        public float getPhaseDoubleDerivative() {
            return phaseDoubleDerivative;
        }

        public void setPhaseDoubleDerivative(float phaseDoubleDerivative) {
            this.phaseDoubleDerivative = phaseDoubleDerivative;
        }
    }


}
