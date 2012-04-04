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

package org.mart.crs.core.pcp;

import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;

import java.util.ArrayList;
import java.util.List;

import static org.mart.crs.utils.helper.HelperArrays.searchPeakIndexes;
import static org.mart.crs.utils.helper.HelperArrays.sum;


/**
 * @version 1.0 24-May-2010 18:03:46
 * @author: Hut
 */
public class PCPHarmonic extends PCP {

    public static int numberOfCandidatesPerOctave = 7;   //Per octave
    public static int firstOctave = 2;
    public static int lastOctave = 9;

    public static float maxAbsPeakDistance = 0.3f; //in semitone scale. (When searching for higher harmonics). It is the Distance  <f0*n, f1>)
    public static int numberOfHarmonicsToConsider = 20; //Number of possible higher harmonics


    public static int minNumberOfHarmonics = 2;  //Default is 4
    public static int minNumberOfOddHarmonics = 1;

    public static float MIN_DISTANCE_BETWEEN_PEAKS = 0.95f; //In semitone scale. (When searching for peaks)


    protected PCPHarmonic() {
    }

    protected PCPHarmonic(double refFreq, int averagingFactor, int numberOfBinsPerSemitone, boolean toNormalize, int startNoteForPCPWrapped, int endNoteForPCPWrapped, float chromaSpectrumRate) {
        super(refFreq, averagingFactor, numberOfBinsPerSemitone, toNormalize, startNoteForPCPWrapped, endNoteForPCPWrapped, chromaSpectrumRate);
    }

    /**
     * calculates scores for Harmonic PCP
     *
     * @param magSpec            spectrFrame
     * @param spectralResolution spectralResolution
     * @return scoredSpectrum
     */
    protected float[] preProcessEnergySpectrumFrame(float[] magSpec, float spectralResolution) {
        float[] outSpectrum = new float[magSpec.length];

        float spectralEnergy = sum(magSpec); //In the current version is just sum of amplitudes

        int startFreqIndex;
        int endFreqIndex;

        int[] candidates = new int[0];

        //For each octave we will extract some possible peaks
        float[] tempSpec;
        for (int octave = firstOctave; octave <= lastOctave; octave++) {
            startFreqIndex = SpectrumImpl.freq2index(Helper.getFreqForMIDINote(Helper.getMidiNumberForNote("C" + octave)), spectralResolution);
            endFreqIndex = SpectrumImpl.freq2index(Helper.getFreqForMIDINote(Helper.getMidiNumberForNote("C" + (octave + 1))), spectralResolution);
            tempSpec = new float[magSpec.length];
            System.arraycopy(magSpec, startFreqIndex, tempSpec, startFreqIndex, endFreqIndex - startFreqIndex);
            int[] indexes = searchPeakIndexes(magSpec, startFreqIndex, numberOfCandidatesPerOctave, MIN_DISTANCE_BETWEEN_PEAKS);
            candidates = HelperArrays.concat(candidates, indexes);
        }

        candidates = makeIndexRefinement(candidates, magSpec);
        float[] fundamentals = getF0s(candidates);


        return outSpectrum;
    }


    public static int[] makeIndexRefinement(int[] outIndexes, float[] spectrum){
        //Make index refinement
        for (int index_ = 0; index_ < outIndexes.length; index_++) {
            for (int index2 = 0; index2 < outIndexes.length; index2++) {
                if (index_ != index2 && Helper.getSemitoneDistanceAbs(index_, index2) < MIN_DISTANCE_BETWEEN_PEAKS) {
                    if (spectrum[index_] > spectrum[index2]) {
                        outIndexes[index2] = 0;
                        spectrum[index2] = 0;
                    } else {
                        outIndexes[index_] = 0;
                        spectrum[index_] = 0;
                    }
                }
            }
        }

        int counter = 0;
        for(int i=0; i < outIndexes.length; i++){
            if(i != 0){
                counter++;
            }
        }

        int[] out = new int[counter];
        for(int i=0; i < outIndexes.length; i++){
            if(i != 0){
                counter++;
            }
        }

        return out;
    }


    /**
     * First method for detecting F0s from a number of harmonics
     *
     * @param candidates array of candidates
     * @return array of F0s
     */
    public static float[] getF0s(int[] candidates) {
        List<Float> fundamentals = new ArrayList<Float>();

        for (int i = 0; i < candidates.length; i++) {
            if (candidates[i] == 0) {
                continue;
            }

            int[] indexesEvenHarmonics = new int[candidates.length];
            int[] indexesOddHarmonics = new int[candidates.length];
            int numberOfEvenHarmonics = 0;
            int numberOfOddHarmonics = 0; //(3, 5, 7 harmonincs)
            //Check i-th candidate
            for (int j = 2; j <= 7; j++) {
                for (int k = i + 1; k < candidates.length; k++) {
                    if (Math.abs(Helper.getSemitoneDistance(candidates[i] * j, candidates[k])) < maxAbsPeakDistance) {
                        if (j == 3 || j == 5 || j == 7) {
                            indexesOddHarmonics[numberOfOddHarmonics++] = k;
                        } else {
                            indexesEvenHarmonics[numberOfEvenHarmonics++] = k;
                        }
                        break;
                    }
                }
            }

            if (numberOfEvenHarmonics + numberOfOddHarmonics >= minNumberOfHarmonics && numberOfOddHarmonics > minNumberOfOddHarmonics) {
//                fundamentals.add(candidates[i]);
                //Now, remove only the candidate at double frequency
                for (int l = 0; l < candidates.length; l++) {
                    if (Math.abs(Helper.getSemitoneDistance(2 * candidates[i], candidates[l])) < maxAbsPeakDistance) {
                        candidates[l] = 0;
                    }
                    if (Math.abs(Helper.getSemitoneDistance(3 * candidates[i], candidates[l])) < maxAbsPeakDistance) {
                        candidates[l] = 0;
                    }
                }
            }
        }

        float[] out = new float[fundamentals.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = fundamentals.get(i);
        }

        return out;
    }


}
