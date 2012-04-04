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

import com.jamal.JamalException;
import com.jamal.client.MatlabClient;
import org.mart.crs.core.AudioReader;
import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.utils.helper.HelperArrays;

/**
 * @version 1.0 20-Sep-2010 18:18:44
 * @author: Hut
 */
public class ReassignedFromMatlabEllis extends ReassignedSpectrum {


    public ReassignedFromMatlabEllis(AudioReader audioReader, int startSampleIndex, int endSampleIndex, int windowLength, int windowType, float overlapping) {
        super(audioReader, startSampleIndex, endSampleIndex, windowLength, windowType, overlapping);
    }

    public ReassignedFromMatlabEllis(AudioReader audioReader, int windowLength, int windowType, float overlapping) {
        super(audioReader, windowLength, windowType, overlapping);
    }

    public ReassignedFromMatlabEllis(float[] samples, int startSampleIndex, int endSampleIndex, float sampleRate, int windowLength, int windowType, float overlapping) {
        super(samples, startSampleIndex, endSampleIndex, sampleRate, windowLength, windowType, overlapping);
    }

    public ReassignedFromMatlabEllis(float[] samples, float sampleRate, int windowLength, int windowType, float overlapping) {
        super(samples, sampleRate, windowLength, windowType, overlapping);
    }


    //TODO: needs testing
    @Override
    public void initialsizeMagSpectrum(int numberOfFreqBinsInTheOutputSpectrogram) {

        this.sampleNumber = endSampleIndex - startSampleIndex + 1;

        try {
            MatlabClient matlabClient = new MatlabClient();

            //First we pass an array of integers and calculate sum in Matlab
            Object[] inArgs = new Object[3];
            inArgs[0] = HelperArrays.getFloatAsDouble(samples);
            inArgs[1] = new Double(audioReader.getSampleRate());
            inArgs[2] = new Integer(windowLength);

            Object[] outputArgs = matlabClient.executeMatlabFunction("getReasSpectrogram", inArgs, 3);
            double[] freq = (double[]) outputArgs[0];
            double[] magnitude = (double[]) outputArgs[1];

            int spectrumLength = (windowLength / 2);
            int frameNumber = freq.length / spectrumLength;

            magSpec = new float[frameNumber][numberOfFreqBinsInTheOutputSpectrogram];
            for (int i = 0; i < frameNumber; i++) {
                for (int j = 0; j < (windowLength / 2); j++) {
                    float energy = (float) magnitude[i * spectrumLength + j];
                    if (energy <= 0) {
                        continue;
                    }
                    float freqValue = (float) freq[i * spectrumLength + j];

                    int frameIndexFinal = i;//Math.round(timeValue / audioReader.getSampleRate() / Settings.windowLength * (1 - Settings.overlapping));
                    int freqIndexFinal = SpectrumImpl.freq2index(freqValue, (float) (0.5 * audioReader.getSampleRate()) / numberOfFreqBinsInTheOutputSpectrogram);
                    if (frameIndexFinal >= 0 && frameIndexFinal < magSpec.length && freqIndexFinal >= 0 && freqIndexFinal < numberOfFreqBinsInTheOutputSpectrogram) {
                        magSpec[frameIndexFinal][freqIndexFinal] += energy;
                    }
                }
            }

            System.out.println("");
//            matlabClient.executeMatlabFunction("quit", null, 0);

        } catch (JamalException e) {
            e.printStackTrace();
        }

        this.sampleRateSpectrum = sampleRate / getFrameStep();

        this.windowLength = numberOfFreqBinsInTheOutputSpectrogram * 2;


    }

}
