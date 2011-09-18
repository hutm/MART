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

import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.core.AudioReader;
import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.utils.Histogram;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.windowing.WindowOption;
import org.mart.crs.utils.windowing.WindowFunction;
import de.dfki.maths.Complex;
import org.mart.crs.utils.windowing.WindowType;

/**
 * @version 1.0 18-May-2010 13:51:00
 * @author: Hut
 */
public class ReassignedSpectrum extends SpectrumImpl {

    protected int numberOfFreqBinsInTheOutputSpectrogram;

    protected boolean[][] harmonicComponentsMatrix;
    protected boolean[][] percussiveComponentsMatrix;
    protected boolean[][] noiseComponentsMatrix;

    protected float[][] timeReasStatistics;
    protected float[][] frequencyReasStatistics;
    protected float[][] energyReasStatistics;

    protected float[][] timeReasValues;
    protected float[][] frequencyReasValues;
    protected float[][] energyReasValues;

    /**
     * This value is needed for avoiding "scattered" spectrum. Used when performing harmonic filtering
     */
    protected float threshold;


    public ReassignedSpectrum(AudioReader audioReader, int startSampleIndex, int endSampleIndex, int windowLength, int windowType, float overlapping, ExecParams execParams) {
        super(audioReader, startSampleIndex, endSampleIndex, windowLength, windowType, overlapping, execParams);
        this.numberOfFreqBinsInTheOutputSpectrogram = execParams.numberOfFreqBinsInTheOutputSpectrogram;
    }

    public ReassignedSpectrum(AudioReader audioReader, int windowLength, int windowType, float overlapping, ExecParams execParams) {
        super(audioReader, 0, audioReader.getSamples().length - 1, windowLength, windowType, overlapping, execParams);
        this.numberOfFreqBinsInTheOutputSpectrogram = execParams.numberOfFreqBinsInTheOutputSpectrogram;
    }


    public ReassignedSpectrum(float[] samples, int startSampleIndex, int endSampleIndex, float sampleRate, ExecParams execParams) {
        super(samples, startSampleIndex, endSampleIndex, sampleRate, execParams);
        this.threshold = execParams.reassignedSpectrogramThreshold;
        this.numberOfFreqBinsInTheOutputSpectrogram = execParams.numberOfFreqBinsInTheOutputSpectrogram;
    }

    public ReassignedSpectrum(float[] samples, float sampleRate, ExecParams execParams) {
        super(samples, 0, samples.length - 1, sampleRate, execParams);
        this.numberOfFreqBinsInTheOutputSpectrogram = execParams.numberOfFreqBinsInTheOutputSpectrogram;
    }


    public ReassignedSpectrum(AudioReader audioReader, int startSampleIndex, int endSampleIndex, ExecParams execParams) {
        this(audioReader.getSamples(), startSampleIndex, endSampleIndex, audioReader.getSampleRate(), execParams);
        this.numberOfFreqBinsInTheOutputSpectrogram = execParams.numberOfFreqBinsInTheOutputSpectrogram;
    }

    public ReassignedSpectrum(AudioReader audioReader, ExecParams execParams) {
        this(audioReader.getSamples(), 0, audioReader.getSamples().length - 1, audioReader.getSampleRate(), execParams);
        this.numberOfFreqBinsInTheOutputSpectrogram = execParams.numberOfFreqBinsInTheOutputSpectrogram;
    }


    protected void initializeSpectrumDataArrays(int numberOfFrames) {
        if (Settings.saveMagSpectrum) {
            magSpec = new float[numberOfFrames][getNumberOfSpectralBins()];
        }
        if (Settings.savePhaseSpectrum) {
            phaseSpec = new float[numberOfFrames][];
        }
        if (Settings.saveHPSRelatedInformation) {
            complexSpectrumRealPart = new float[numberOfFrames][];
            complexSpectrumImagPart = new float[numberOfFrames][];
            harmonicComponentsMatrix = new boolean[numberOfFrames][];
            percussiveComponentsMatrix = new boolean[numberOfFrames][];
            noiseComponentsMatrix = new boolean[numberOfFrames][];
        }

        if (Settings.saveReassignmentStatistics) {
            timeReasStatistics = new float[numberOfFrames][windowLength / 2];
            frequencyReasStatistics = new float[numberOfFrames][windowLength / 2];
            energyReasStatistics = new float[numberOfFrames][windowLength / 2];
        }
        if (Settings.saveNoResolutionRepresentation) {
            timeReasValues = new float[numberOfFrames][windowLength / 2];
            frequencyReasValues = new float[numberOfFrames][windowLength / 2];
            energyReasValues = new float[numberOfFrames][windowLength / 2];
        }

    }

    protected void initializeSpectrumDataArrays(int startIndex, int endIndex) {
        super.initializeSpectrumDataArrays(startIndex, endIndex);
        harmonicComponentsMatrix = new boolean[numberOfFrames][];
        percussiveComponentsMatrix = new boolean[numberOfFrames][];
        noiseComponentsMatrix = new boolean[numberOfFrames][];
        for (int i = startIndex; i < endIndex; i++) {
            if (Settings.saveHPSRelatedInformation) {
                harmonicComponentsMatrix[i] = new boolean[windowLength / 2];
                percussiveComponentsMatrix[i] = new boolean[windowLength / 2];
                noiseComponentsMatrix[i] = new boolean[windowLength / 2];
            }
            if (Settings.saveReassignmentStatistics) {
                timeReasStatistics[i] = new float[windowLength / 2];
                frequencyReasStatistics[i] = new float[windowLength / 2];
                energyReasStatistics[i] = new float[windowLength / 2];
            }
            if (Settings.saveNoResolutionRepresentation) {
                timeReasValues[i] = new float[windowLength / 2];
                frequencyReasValues[i] = new float[windowLength / 2];
                energyReasValues[i] = new float[windowLength / 2];
            }
        }
    }


    protected int getNumberOfSpectralBins() {
        return numberOfFreqBinsInTheOutputSpectrogram;
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
        try {
            int currentFrameIndex = getIndexForSample(centerFrameSample);

            fft.calcReal(samples_d, -1);

            if (Settings.saveHPSRelatedInformation) {
                complexSpectrumRealPart[currentFrameIndex] = extractComplexSpecRealPart(samples_d);
                complexSpectrumImagPart[currentFrameIndex] = extractComplexSpecImagPart(samples_d);
                harmonicComponentsMatrix[currentFrameIndex] = new boolean[complexSpectrumRealPart[currentFrameIndex].length];
                percussiveComponentsMatrix[currentFrameIndex] = new boolean[complexSpectrumRealPart[currentFrameIndex].length];
                noiseComponentsMatrix[currentFrameIndex] = new boolean[complexSpectrumRealPart[currentFrameIndex].length];
            }

            float[] magSpectrum = extractMagSpec(samples_d);
            Complex[] complexSpectrum = extractComplexSpec(samples_d);

            fft.calcReal(samples_d_derivative, -1);
            Complex[] complexSpectrumDerivative = extractComplexSpec(samples_d_derivative);

            fft.calcReal(samples_d_timeWeighted, -1);
            Complex[] complexSpectrumTimeWeighted = extractComplexSpec(samples_d_timeWeighted);

            fft.calcReal(samples_d_timeWeightedDerivative, -1);
            Complex[] complexSpectrumTimeWeightedDerivative = extractComplexSpec(samples_d_timeWeightedDerivative);

            float reassignedFreq;
            int newBinIndex;
            float timeDelta;
            for (int i = 0; i < magSpectrum.length; i++) {

                //Frequency reassignment
                Complex devisionResult = complexSpectrumDerivative[i].divide(complexSpectrum[i]);
                float centerFrequency = index2freq(i, 0.5f * sampleRate / magSpectrum.length);
                float deltaFreq = devisionResult.getImag();
                reassignedFreq = centerFrequency - deltaFreq;
                newBinIndex = freq2index(reassignedFreq, (0.5f * sampleRate) / numberOfFreqBinsInTheOutputSpectrogram);
                boolean deltaFreqOK = isDeltaFrequencyOK(deltaFreq);


                //Time reassignment
                Complex devisionResultTime = complexSpectrumTimeWeighted[i].divide(complexSpectrum[i]);
                timeDelta = devisionResultTime.getReal();
                float newStartIndexInSamples = centerFrameSample + Math.round(timeDelta * sampleRate);
                int frameIndex = getIndexForSample(newStartIndexInSamples);
                boolean deltaTimeOK = isDeltaTimeOK(timeDelta);


                if (!(deltaFreqOK && deltaTimeOK)) {
                    if (!isToRemoveEnergyIfConditionsAreNotMet()) {
                        newBinIndex = i;
                        frameIndex = currentFrameIndex;
                        reassignedFreq = centerFrequency;
                        centerFrameSample -= timeDelta * sampleRate;
                        deltaFreqOK = true;
                        deltaTimeOK = true;
                    } else {
                        continue;
                    }
                }

                //Now calculate phaseDoubleDerivative and check the necessary condition
                Complex firstTerm = complexSpectrumTimeWeightedDerivative[i].divide(complexSpectrum[i]);

                Complex secondTerm = (complexSpectrumTimeWeighted[i].divide(complexSpectrum[i]));
                secondTerm = secondTerm.multiply(complexSpectrumDerivative[i].divide(complexSpectrum[i]));


                float phaseDoubleDerivative = (firstTerm.getReal() - secondTerm.getReal()) * 2 * (float) Math.PI;


                if (checkCondition(phaseDoubleDerivative)) {
                    if (deltaFreqOK && deltaTimeOK) {
                        if (Settings.saveMagSpectrum) {
                            if (newBinIndex >= 0 && newBinIndex < out[0].length && frameIndex >= 0 && frameIndex < this.magSpec.length) {
                                if (magSpec[frameIndex] != null && magSpec[frameIndex].length > 0) { //Is necessary when building only a part of a spectrum
                                    this.magSpec[frameIndex][newBinIndex] += magSpectrum[i];
                                }
                            }
                        }
                        if (Settings.saveReassignmentStatistics) {
                            frequencyReasStatistics[currentFrameIndex][i] = -1 * devisionResult.getImag();
                            timeReasStatistics[currentFrameIndex][i] = timeDelta * sampleRate;
                            energyReasStatistics[currentFrameIndex][i] = magSpectrum[i];
                        }
                        if (Settings.saveNoResolutionRepresentation) {
                            frequencyReasValues[currentFrameIndex][i] = reassignedFreq;
                            timeReasValues[currentFrameIndex][i] = centerFrameSample + timeDelta * sampleRate;
                            energyReasValues[currentFrameIndex][i] = magSpectrum[i];
                        }
                    }
                }


                if (Settings.saveHPSRelatedInformation) {
                    if (Math.abs(1 + phaseDoubleDerivative) < threshold) {
                        harmonicComponentsMatrix[currentFrameIndex][i] = true;
                    } else if (Math.abs(phaseDoubleDerivative) < threshold) {
                        percussiveComponentsMatrix[currentFrameIndex][i] = true;
                    } else {
                        noiseComponentsMatrix[currentFrameIndex][i] = true;
                    }
                }


            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("Error while calculating spectrum ");
            logger.error(Helper.getStackTrace(e));
        }
        return out;
    }


    protected boolean isDeltaFrequencyOK(float deltaFreq) {
        return true;
    }

    protected boolean isDeltaTimeOK(float deltaFreq) {
        return true;
    }


    protected boolean isToRemoveEnergyIfConditionsAreNotMet() {
        return false;
    }

    protected boolean checkCondition(float phaseDoubleDerivative) {
        return true; //no filtering. All energies are added.
    }


    public boolean[][] getHarmonicComponentsMatrix() {
        return harmonicComponentsMatrix;
    }

    public boolean[][] getNoiseComponentsMatrix() {
        return noiseComponentsMatrix;
    }

    public boolean[][] getPercussiveComponentsMatrix() {
        return percussiveComponentsMatrix;
    }

    public float[][] getTimeReasStatistics() {
        return timeReasStatistics;
    }

    public float[][] getFrequencyReasStatistics() {
        return frequencyReasStatistics;
    }

    public float[][] getEnergyReasStatistics() {
        return energyReasStatistics;
    }

    public float[][] getTimeReasValues() {
        return timeReasValues;
    }

    public float[][] getFrequencyReasValues() {
        return frequencyReasValues;
    }

    public float[][] getEnergyReasValues() {
        return energyReasValues;
    }

    public static void main(String[] args) {
        //Test array version of Reassignment calculation
        String fileName = "/home/hut/Beatles/data/wav/01_-_Drive_My_Car.wav";
//        String fileName = "/home/hut/data/!audio/0001 - U2 - The Joshua Tree - With or without you.wav";
        AudioReader reader = new AudioReader(fileName, 11025);


        Settings.saveReassignmentStatistics = true;
        Settings.saveMagSpectrum = false;


        ReassignedSpectrum spectrum = new ReassignedSpectrum(reader.getSamples(), 0.4f, ExecParams._initialExecParameters);

        spectrum.getMagSpec();

        Histogram histogram = new Histogram(spectrum.frequencyReasStatistics, spectrum.energyReasStatistics, 0);
        histogram.initialize();

        logger.info("done");
    }


}
