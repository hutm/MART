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

package org.mart.tools.hps;

import org.mart.crs.config.ExecParams;
import org.mart.crs.core.AudioReader;
import org.mart.crs.utils.helper.HelperFile;
import de.dfki.maths.Complex;
import de.dfki.maths.FFT;
import org.mart.crs.utils.helper.HelperArrays;

//TODO migrate to rasmus fft

/**
 * User: hut
 * Date: Oct 19, 2008
 * Time: 1:27:35 AM
 */
public class HarmonicPercussionSeparator {

    public static final String WORKING_DIR = "/home/hut/temp/hps";

    private static final int maxFreqForHarmonicComponent = 2000;

    private static final int fftSize = ExecParams._initialExecParameters.windowLength;
    private static final int overlappingFactor = 2;
    private static final float rate = 1f;

    private static final int maxIterations = 200;
    private static final float alpha = 0.4f;

    private static final float beatBound = 0.3f;
    private static final boolean isFileStoreNeeded = true;

    private float[][] functions;
    private float[] segmentFunction;

    public HarmonicPercussionSeparator(String fileName) {
        AudioReader audioReader = new AudioReader(fileName);
        makeTransform(audioReader);
    }

    public static void main(String[] args) {
        //The argument is filename
        System.out.println("processing file " + args[0]);
        new HarmonicPercussionSeparator(args[0]);
    }

    public void makeTransform(AudioReader audioReader) {


        float[] samples = audioReader.getSamples();


        int index = 0;
        Complex[] buffer = new Complex[fftSize];
        float[][] magnitude = new float[samples.length / fftSize * overlappingFactor][fftSize];
        Complex[][] spectrumHar = new Complex[samples.length / fftSize * overlappingFactor][fftSize];
        while (index + fftSize <= samples.length) {
            //first fill the buffer with initial values
            for (int j = 0; j < fftSize; j++) {
                buffer[j] = new Complex(samples[index + j], 0);
            }

            FFT.fft(buffer, fftSize);

            magnitude[index / (fftSize / overlappingFactor)] = calculateMagnitude(buffer, rate);
            System.arraycopy(buffer, 0, spectrumHar[index / (fftSize / overlappingFactor)], 0, fftSize);
            //spectrumHar[index/fftSize] = buffer;

            index += fftSize / overlappingFactor;
        }

        audioReader = new AudioReader(getSamplesFromSpectrum(spectrumHar, overlappingFactor), audioReader.getSampleRate());
        audioReader.exportWavFile(String.format("%s/tot.wav", WORKING_DIR), 0, 1000);

        //sozdadim vtoruyu kopiyu dlya perkusionnoi chasti (spectrumHar - dlya garmonicheskoi)
        Complex[][] spectrumPer = new Complex[spectrumHar.length][spectrumHar[0].length];
        for (int i = 0; i < spectrumHar.length - 1; i++) {
            for (int j = 0; j < spectrumHar[0].length; j++) {
                spectrumPer[i][j] = new Complex(spectrumHar[i][j].getReal(), spectrumHar[i][j].getImag());
            }
        }

        //teper' razdelim garmonicheskuyu i perkusionnuyu chast' amplitudnogo spektra
        boolean[][] separationMatrix = separateHarmonicAndPercussive(magnitude);
        //smoothBeats(separationMatrix);

        if (isFileStoreNeeded) {
            //obnulim sootvetstvuyuschie yacheiki v spektrah pered OBPF
            for (int i = 0; i < separationMatrix.length; i++) {
                for (int j = 0; j < separationMatrix[0].length; j++) {
                    if (separationMatrix[i][j]) {
                        spectrumPer[i][j] = new Complex();
                        if (j > 0) {
                            spectrumPer[i][fftSize - j] = new Complex();
                        }
                    } else {
                        spectrumHar[i][j] = new Complex();
                        if (j > 0) {
                            spectrumHar[i][fftSize - j] = new Complex();
                        }
                    }
                    //spectrumHar[i][fftSize/2] = new Complex();
                    //spectrumPer[i][fftSize/2] = new Complex();

                }

            }

            HelperFile.createDir(WORKING_DIR);

            audioReader = new AudioReader(getSamplesFromSpectrum(spectrumHar, overlappingFactor), audioReader.getSampleRate());
            audioReader.exportWavFile(String.format("%s/har.wav", WORKING_DIR), 0, 1000);
            audioReader = new AudioReader(getSamplesFromSpectrum(spectrumPer, overlappingFactor), audioReader.getSampleRate());
            audioReader.exportWavFile(String.format("%s/per.wav", WORKING_DIR), 0, 1000);
        }

    }


    private boolean[][] separateHarmonicAndPercussive(float[][] magnitude) {
        int searchStepTime = 1;
        int searchStepFreq = 1;

        int maxT = magnitude.length;
        int maxF = magnitude[0].length;
        boolean[][] out = new boolean[maxT][maxF];

        //initialization
        float[][] p = new float[maxT][maxF];
        float[][] h = new float[maxT][maxF];
        for (int i = 0; i < maxT; i++) {
            for (int j = 0; j < maxF; j++) {
                p[i][j] = h[i][j] = magnitude[i][j] / 2;
            }
        }

        //start iterations
        float[][] delta = new float[maxT][maxF];
        for (int k = 0; k < maxIterations; k++) {
            //System.out.println("Iteration " + k);

            //calculate deltas
            for (int i = searchStepTime; i < maxT - searchStepTime; i++) {
                for (int j = searchStepFreq; j < maxF - searchStepFreq; j++) {
                    delta[i][j] = 0.25f * (alpha * (h[i - searchStepTime][j] - 2 * h[i][j] + h[i + searchStepTime][j]) - (1 - alpha) * (p[i][j - searchStepFreq] - 2 * p[i][j] + p[i][j + searchStepFreq]));
                }
            }

            //update h and p
            for (int i = 1; i < maxT - 1; i++) {
                for (int j = 1; j < maxF - 1; j++) {
                    h[i][j] = Math.min(Math.max(h[i][j] + delta[i][j], 0), magnitude[i][j]);
                    p[i][j] = magnitude[i][j] - h[i][j];
                }
            }
        }

        //make binarization
        for (int i = 1; i < maxT - 1; i++) {
            for (int j = 1; j < maxF - 1; j++) {
                out[i][j] = h[i][j] > p[i][j];
            }
        }

        //Now save function of time for determining beat peaks
        functions = new float[3][maxT];
        segmentFunction = functions[2];
        float sumHar, sumPer;
        for (int i = 2; i < maxT - 2; i++) {
            sumHar = 0;
            sumPer = 0;

            //Function for percussion componrnent (takes whole frequency range)
            for (int j = 1; j < maxF - 1; j++) {
                sumPer += p[i][j];
            }

            //Function for harmonic componrnent (takes lower part of frequency range)
            int maxindex = Math.round(maxFreqForHarmonicComponent / ((ExecParams._initialExecParameters.samplingRate / 2) / fftSize));
            for (int j = 1; j < maxindex - 1; j++) {
                if (out[i - 2][j] && out[i - 1][j] && out[i][j] && out[i + 1][j] && out[i + 2][j]) {
//                if (out[i - 1][j] && out[i][j] && out[i + 1][j] ) {
//                if (out[i][j]) {
                    sumHar++;
                }
            }

            functions[0][i] = sumHar;
            functions[1][i] = sumPer;


        }

        functions[0] = HelperArrays.normalizeVector(functions[0]);
        functions[0] = HelperArrays.normalizeVector(functions[1]);
        float bound = 0.02f;
        for (int k = 0; k < functions[0].length; k++) {
            if (functions[0][k] > bound) {
                segmentFunction[k] = bound;
            } else {
                segmentFunction[k] = 0;
            }
        }

        return out;
    }


    /**
     * calculates magnitude spectrum
     *
     * @param buffer Complex buffer
     * @param rate   rate
     * @return magnitude
     */
    public static float[] calculateMagnitude(Complex[] buffer, float rate) {
        int fftSize = buffer.length;

        // calculate Magnitude
        float[] mag = new float[fftSize / 2];
        mag[0] = (float) Math.exp(2 * rate * Math.log(buffer[0].getMagnitude() / fftSize));
        for (int i = 1; i < fftSize / 2; i++)
            mag[i] = (float) Math.exp(2 * rate * Math.log(2 * buffer[i].getMagnitude() / fftSize));
        return mag;
    }


    public static float[] getSamplesFromSpectrum(Complex[][] spectrum, int overlappingFactor) {
        float[] samplesAfterFFT = new float[spectrum[0].length * spectrum.length / overlappingFactor];

        for (int i = 0; i < spectrum.length - 1; i++) {

            try {
                FFT.inverseFFT(spectrum[i], fftSize);
            } catch (Exception e) {
                System.out.println("i=" + i + "; all=" + spectrum.length);
            }

            //Now fill output array
            for (int j = 0 + Math.round(0.5f * fftSize * (1 - 1f / overlappingFactor)); j < fftSize - Math.round(0.5f * fftSize * (1 - 1f / overlappingFactor)); j++) {
                samplesAfterFFT[i * fftSize / overlappingFactor + j - Math.round(0.5f * fftSize * (1 - 1f / overlappingFactor))] = spectrum[i][j].getReal();
            }
        }
        return samplesAfterFFT;
    }


    public static void smoothBeats(boolean[][] separationMatrix) {


        for (int i = 0; i < separationMatrix.length; i++) {
            //count number of percussive part
            int count = 0;
            for (int j = 0; j < separationMatrix[0].length; j++) {
                if (!separationMatrix[i][j]) {
                    count++;
                }
            }
            if (1.0f * count / fftSize < beatBound) {
                for (int j = 0; j < separationMatrix[0].length; j++) {
                    separationMatrix[i][j] = true;
                }
            }
        }


    }


}


