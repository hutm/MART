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

package org.mart.tools.multipitch;

import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mart.crs.utils.helper.HelperArrays.searchPeakIndexes;
import static org.mart.crs.utils.helper.HelperArrays.sum;

/**
 * @version 1.0 23-Jun-2010 16:33:20
 * @author: Hut
 */
public class PitchExtractorReassignedFrequency extends PitchExtractorStandard{


     public Map<Integer, DetectedHarmonic> preparePeaks(SpectrumImpl spectrum, int frameIndex) {



        Map<Integer, DetectedHarmonic> outMap = new HashMap<Integer, DetectedHarmonic>();

        float[] magSpec = spectrum.getMagSpec()[frameIndex];

        float spectralResolution = spectrum.getFrequencyResolution();

        float spectralEnergy = HelperArrays.sum(magSpec); //In the current version is just sum of amplitudes

        int startFreqIndex;
        int endFreqIndex;

        //For each octave we will extract some possible peaks
        float[] tempSpec;
        for (int octave = firstOctave; octave <= lastOctave; octave++) {                           //TODO use ENUM for notes
            startFreqIndex = SpectrumImpl.freq2index(Helper.getFreqForMIDINote(Helper.getMidiNumberForNote("C" + octave)), spectralResolution);
            endFreqIndex = SpectrumImpl.freq2index(Helper.getFreqForMIDINote(Helper.getMidiNumberForNote("C" + (octave + 1))), spectralResolution);
            tempSpec = new float[magSpec.length];
            System.arraycopy(magSpec, startFreqIndex, tempSpec, startFreqIndex, endFreqIndex - startFreqIndex);
            processFreqRange(tempSpec, startFreqIndex, endFreqIndex, outMap, frameIndex, spectralEnergy);
        }

        //Make index refinement
        Set<Integer> indexes = outMap.keySet();
        for (Integer index_ : indexes) {
            for (Integer index2 : indexes) {
                if (index_.intValue() != index2.intValue() && Math.abs(Helper.getSemitoneDistance(outMap.get(index_).getFreq(), outMap.get(index2).getFreq())) < MIN_DISTANCE_BETWEEN_PEAKS) {
                    if (magSpec[index_] > magSpec[index2]) {
                        outMap.get(index2).setFreq(0f);
                    } else {
                        outMap.get(index_).setFreq(0f);
                        break;
                    }
                }
            }
        }


        return outMap;
    }

    public Map<Integer, DetectedHarmonic> processFreqRange(float[] magSpec, int startFreqIndex, int endFreqIndex, Map<Integer, DetectedHarmonic> outMap, int frameIndex, float spectralEnergy) {
        //leave only part of the magnitude spectrum
        for (int j = 0; j < magSpec.length; j++) {
            if (j < startFreqIndex || j > endFreqIndex) {
                magSpec[j] = 0;
            }
        }

        int[] indexes = HelperArrays.searchPeakIndexes(magSpec, startFreqIndex, numberOfCandidatesPerOctave, MIN_DISTANCE_BETWEEN_PEAKS);


        for (int k = 0; k < indexes.length; k++) {
            outMap.put(indexes[k], new DetectedHarmonic(spectrum.index2freq(indexes[k]), magSpec[indexes[k]], frameIndex, spectralEnergy));
        }

        return outMap;
    }




}
