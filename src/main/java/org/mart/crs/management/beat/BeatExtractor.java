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

package org.mart.crs.management.beat;

import org.mart.crs.core.AudioReader;
import org.mart.crs.core.onset.OnsetDetectionFunction;
import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrumPercussivePart;
import org.mart.crs.management.config.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 08/11/10 13:43
 * @author: Hut
 */
public class BeatExtractor {

    protected List<OnsetDetectionFunction> onsetDetecionFunctions;

    protected SpectrumImpl spectrum;

    protected int numberOfIntervals = 8;


    public BeatExtractor(SpectrumImpl spectrum) {
        this.spectrum = spectrum;
        buildOnsetDetectors();
    }

    public BeatExtractor(String audioFilePath, float sapmlingRate, float reassignedSpectrogramThreshold){
        AudioReader reader = new AudioReader(audioFilePath, sapmlingRate);
        this.spectrum = new ReassignedSpectrumPercussivePart(reader, Configuration.windowLengthDefault, Configuration.windowTypeDefault, Configuration.overlappingDefault, reassignedSpectrogramThreshold);
        buildOnsetDetectors();
    }


    protected void buildOnsetDetectors() {
        onsetDetecionFunctions = new ArrayList<OnsetDetectionFunction>();
        for (int i = 0; i < numberOfIntervals ; i++) {
            float startFreq = (spectrum.getSampleRate() / 2) / numberOfIntervals * i;
            float endFreq = (spectrum.getSampleRate() / 2) / numberOfIntervals * (i + 1);
            OnsetDetectionFunction aFunction = new OnsetDetectionFunction(spectrum, startFreq, endFreq);
            onsetDetecionFunctions.add(aFunction);
        }
    }


    public float[][] getDetectionFunctionsData() {
        float[][] out = new float[numberOfIntervals][];
        for (int i = 0; i < numberOfIntervals; i++) {
            out[i] = onsetDetecionFunctions.get(i).getDetectionFunction();
        }
        return out;
    }

    public float[] getDetectionFunction(int index) {
        return onsetDetecionFunctions.get(index).getDetectionFunction();
    }

    public float[] getDetectionFunctionAveraged() {
        float[][] functions = getDetectionFunctionsData();
        float[] out = new float[functions[0].length];
        for(int i = 0; i < out.length; i++){
            for(int j = 0; j < numberOfIntervals; j++){
                out[i] += functions[j][i];
            }
        }
//        return out;
        return functions[0];  //TODO seems to be better just first band

    }

    public int getNumberOfIntervals() {
        return numberOfIntervals;
    }

    public float getOnsetDetectionSamplingFreq() {
        return spectrum.getSampleRateSpectrum();
    }


    public static void main(String[] args) {
        BeatExtractor extractor = new BeatExtractor("d:/My Documents/!audio/0001 - U2 - The Joshua Tree - With or without you.wav", 11025f, Configuration.reassignedthresholdDefault);
        System.out.println(extractor.getOnsetDetectionSamplingFreq());
    }
}
