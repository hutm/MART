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

import org.mart.crs.core.AudioReader;
import org.mart.crs.core.SilenceDetector;
import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.utils.helper.Helper;

import java.util.*;

import static org.mart.crs.utils.helper.HelperArrays.searchPeakIndexes;
import static org.mart.crs.utils.helper.HelperArrays.sum;

/**
 * @version 1.0 Jun 9, 2009 2:26:12 AM
 * @author: Maksim Khadkevich
 */
public class PitchExtractorStandard extends PitchExtractor {

    protected SpectrumImpl spectrum;

    protected float[][] magSpec;
    protected float[][] out;
    protected DetectedHarmonic[][] extractedFrequencies;


    public float[][] getSpectrumOfFundamentals(SpectrumImpl spectrum) {

        this.spectrum = spectrum;

        AudioReader audioReader = spectrum.getAudioReader();

        magSpec = spectrum.getMagSpec();

        out = new float[magSpec.length][magSpec[0].length];

        extractedFrequencies = new DetectedHarmonic[magSpec.length][];

        //Calculate silence bin intervals
        List<int[]> silenceIntervals = new ArrayList<int[]>();
        for (float[] intervals : (new SilenceDetector(audioReader)).getSilenceIntervals()) {
            int[] data = new int[]{(int) Math.floor(intervals[0] * audioReader.getSampleRate() / ((1 - spectrum.getOverlapping()) * spectrum.getWindowLength())),
                    (int) Math.floor(intervals[1] * audioReader.getSampleRate() / ((1 - spectrum.getOverlapping()) * spectrum.getWindowLength()))};
            silenceIntervals.add(data);
        }

        //First find harmonics taking into account silence regions
        for (int i = 0; i < magSpec.length; i++) {
            boolean isSilenceFrame = false;
            for (int[] silenceInterval : silenceIntervals) {
                if (i >= silenceInterval[0] && i <= silenceInterval[1]) {
                    isSilenceFrame = true;
                    break;
                }
            }
            if (!isSilenceFrame) {
                extractedFrequencies[i] = extractHarmonics(spectrum, i);
            } else {
                extractedFrequencies[i] = new DetectedHarmonic[0];
            }
        }

        //Now wrap harmonics into fundamentals
        wrapHarmonics();

        return out;
    }


    public DetectedHarmonic[] extractHarmonics(SpectrumImpl spectrum, int frameIndex) {

        Map<Integer, DetectedHarmonic> peaks = preparePeaks(spectrum, frameIndex);

        List<DetectedHarmonic> detectedHarmonics = new ArrayList<DetectedHarmonic>();
        DetectedHarmonic aHarmonic;
        for (Iterator it = peaks.values().iterator(); it.hasNext();) {
            aHarmonic = (DetectedHarmonic) it.next();
            if (aHarmonic.getFreq() != 0f) {
                detectedHarmonics.add(aHarmonic);
            }
        }

        return detectedHarmonics.toArray(new DetectedHarmonic[]{});

    }


    public Map<Integer, DetectedHarmonic> preparePeaks(SpectrumImpl spectrum, int frameIndex) {

        float timeInstant = frameIndex * spectrum.getWindowLength() * (1 - spectrum.getOverlapping()) / spectrum.getSampleRate();

        Map<Integer, DetectedHarmonic> outMap = new HashMap<Integer, DetectedHarmonic>();

        float[] magSpec = spectrum.getMagSpecValue(timeInstant);
        float[] phaseChange = spectrum.getPhaseChange(timeInstant, TIME_SHIFT_IN_SAMPLES);

        float spectralResolution = spectrum.getFrequencyResolution();

        float spectralEnergy = sum(magSpec); //In the current version is just sum of amplitudes

        int startFreqIndex;
        int endFreqIndex;

        //For each octave we will extract some possible peaks
        float[] tempSpec;
        for (int octave = firstOctave; octave <= lastOctave; octave++) {                           //TODO use ENUM for notes
            startFreqIndex = SpectrumImpl.freq2index(Helper.getFreqForMIDINote(Helper.getMidiNumberForNote("C" + octave)), spectralResolution);
            endFreqIndex = SpectrumImpl.freq2index(Helper.getFreqForMIDINote(Helper.getMidiNumberForNote("C" + (octave + 1))), spectralResolution);
            tempSpec = new float[magSpec.length];
            System.arraycopy(magSpec, startFreqIndex, tempSpec, startFreqIndex, endFreqIndex - startFreqIndex);
            processFreqRange(tempSpec, phaseChange, startFreqIndex, endFreqIndex, outMap, frameIndex, spectralEnergy);
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

    public Map<Integer, DetectedHarmonic> processFreqRange(float[] magSpec, float[] phaseChange, int startFreqIndex, int endFreqIndex, Map<Integer, DetectedHarmonic> outMap, int frameIndex, float spectralEnergy) {
        //leave only part of the magnitude spectrum
        for (int j = 0; j < magSpec.length; j++) {
            if (j < startFreqIndex || j > endFreqIndex) {
                magSpec[j] = 0;
            }
        }

        int[] indexes = searchPeakIndexes(magSpec, startFreqIndex, numberOfCandidatesPerOctave, MIN_DISTANCE_BETWEEN_PEAKS);


        for (int k = 0; k < indexes.length; k++) {
            outMap.put(indexes[k], new DetectedHarmonic(phaseChange[indexes[k]], magSpec[indexes[k]], frameIndex, spectralEnergy));
        }

        return outMap;
    }


    protected void wrapHarmonics() {
        //Wrap harmonics to fundamentals
        for (int i = 0; i < magSpec.length; i++) {
            extractedFrequencies[i] = getF0s(extractedFrequencies[i]);
        }

        //Put detected fundamentals into Spectrum Object
        for (int i = 0; i < magSpec.length; i++) {
            for (int freqIndex = 0; freqIndex < extractedFrequencies[i].length; freqIndex++) {
                float freq = extractedFrequencies[i][freqIndex].getFreq();
                int index = SpectrumImpl.freq2index(freq, spectrum.getFrequencyResolution());
                out[i][index] = 20.0f;
//                    out[i][index] = extractedFrequencies[i][freqIndex].getWeight();
            }
        }
    }


    /**
     * @param candidates harmonics
     * @return array of detected fundamentals
     */
    public static DetectedHarmonic[] getF0s(DetectedHarmonic[] candidates) {
        List<DetectedHarmonic> candidatesList;
        List<DetectedHarmonic> fundamentals = new ArrayList<DetectedHarmonic>();

        calculateWeights(candidates);

        makeRefinement(candidates);

        //Recalculate weights
        calculateWeights(candidates);


        candidatesList = Arrays.asList(candidates);
        Collections.sort(candidatesList);

        float sum = 0;
        int index = 0;
        while (index < candidatesList.size()) {
            if (index > 0 && sum / Math.pow(index, rate) > (sum + candidatesList.get(index).getWeight()) / Math.pow(index + 1, rate)) {
                break;
            }
            sum += candidatesList.get(index).getWeight();
            fundamentals.add(candidatesList.get(index++));
        }

        return fundamentals.toArray(new DetectedHarmonic[]{});
    }


    private static void calculateWeights(DetectedHarmonic[] candidates) {
        //Calculating weight functions for each candidate
        float freq0, freq1;
        for (int i = 0; i < candidates.length; i++) {
            if (candidates[i].isAmIHarmonic()) {
                candidates[i].setWeight(0);
                continue;
            }

            //The objective function
            float weigthValue = candidates[i].getAmp();

            freq0 = candidates[i].getFreq();
            for (int j = 0; j < candidates.length; j++) {
                if (i == j) {
                    continue;
                }

                freq1 = candidates[j].getFreq();
                if (!candidates[i].isInIgnoreList(candidates[j])) {
                    for (int n = 2; n < numberOfHarmonicsToConsider; n++) {
                        if (Math.abs(Helper.getSemitoneDistance(freq0 * n, freq1)) < maxAbsPeakDistance) {
//                            weigthValue += Math.pow(harmonicCoeff, n) * candidates[j].getAmp();
                            weigthValue += (freq0 + e1) / (freq0 * n + e2) * candidates[j].getAmp();
                            candidates[i].addHarmonic(candidates[j], n);
                        }
                        if (Math.abs(Helper.getSemitoneDistance(freq0 / n, freq1)) < maxAbsPeakDistance) {
                            candidates[i].addFundamentalForThisHamonic(candidates[j], n);
                        }
                    }
                }
            }
            candidates[i].setWeight(weigthValue);
        }
    }


    /**
     * This method was used while estimating F0s on a frame-by-frame basis. Now there is better solution makeRefinement_upd
     *
     * @param candidates detectedHarmonics candidates
     */
    private static void makeRefinement(DetectedHarmonic[] candidates) {
        //Now for each candidate detect a set of harmonics that are not harmonics, but individual fundamentals
        DetectedHarmonic aHarmonic;
        for (int i = 0; i < candidates.length; i++) {
            Set<DetectedHarmonic> F0set = candidates[i].getProbableFundamentalsForThisHarmonic().keySet();

            for (DetectedHarmonic probableFundamental : F0set) {
                int currentHarmonicNumber = probableFundamental.getNumberForHarmonic(candidates[i]);
                //Energy of harmonics to the left, right, and exactly in the center
                float leftEnergy = 0;
                float rightEnergy = 0;
                float centerEnergy = 0;
                int counterCenter, counterLeft, counterRight;

                //First calculate center energy
                counterCenter = 0;
                for (int j = currentHarmonicNumber * 2; j < numberOfHarmonicsToConsider; j += currentHarmonicNumber) {
                    aHarmonic = probableFundamental.getHarmonicWithNumber(j);
                    if (aHarmonic != null) {
                        centerEnergy += aHarmonic.getAmp();
                        counterCenter++;
                    }
                }

                //Then calculate left energy
                counterLeft = 0;
                for (int j = (currentHarmonicNumber - 1) * 2; j < 7; j += currentHarmonicNumber) {
                    aHarmonic = probableFundamental.getHarmonicWithNumber(j);
                    if (aHarmonic != null) {
                        leftEnergy += aHarmonic.getAmp();
                        counterLeft++;
                    }
                }

                //Then calculate right energy
                counterRight = 0;
                for (int j = (currentHarmonicNumber + 1) * 2; j < 9; j += currentHarmonicNumber) {
                    aHarmonic = probableFundamental.getHarmonicWithNumber(j);
                    if (aHarmonic != null) {
                        rightEnergy += aHarmonic.getAmp();
                        counterRight++;
                    }
                }

                float maxEnergyRate = 2;

                float sumEnergy = 0;
                int localcounter = 0;
                if (counterRight > 0) {
                    sumEnergy += rightEnergy / counterRight;
                    localcounter++;
                }
                if (counterLeft > 0) {
                    sumEnergy += leftEnergy / counterLeft;
                }
                if (localcounter > 0) {
                    sumEnergy /= localcounter;
                }

                if (centerEnergy / counterCenter > maxEnergyRate * sumEnergy) {
                    probableFundamental.addHarmonicToIgnoreSet(candidates[i]);
                    for (DetectedHarmonic higherHarmonic : candidates[i].getProbableHigherHarmonicsForThisFundamental().keySet()) {
                        probableFundamental.addHarmonicToIgnoreSet(higherHarmonic);
                    }
                }
            }
        }
    }


    /**
     * First method for detecting F0s from a number of harmonics
     *
     * @param candidates array of candidates
     * @return array of F0s
     */
    public static float[] getF0s(float[] candidates) {
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
                fundamentals.add(candidates[i]);
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


    /**
     * Frame-based fundamental detection .
     * This method is similar to Klapuri's method extracting F0s from spectrum of harmonics
     *
     * @param candidates harmonics
     * @return array of detected fundamentals
     */
    public static DetectedHarmonic[] getF0s_Klapuri(DetectedHarmonic[] candidates) {
        List<DetectedHarmonic> candidatesList;
        List<DetectedHarmonic> fundamentals = new ArrayList<DetectedHarmonic>();

        //Calculating weight functions for each candidate
        float freq0, freq1;
        for (int i = 0; i < candidates.length; i++) {
            //The objective function
            float weigthValue = candidates[i].getAmp();

            freq0 = candidates[i].getFreq();
            for (int j = 0; j < candidates.length; j++) {
                if (i == j) {
                    continue;
                }

                freq1 = candidates[j].getFreq();
                for (int n = 2; n < numberOfHarmonicsToConsider; n++) {
                    if (Math.abs(Helper.getSemitoneDistance(freq0 * n, freq1)) < maxAbsPeakDistance) {
                        weigthValue += (freq0 + e1) / (freq0 * n + e2) * candidates[j].getAmp();
                    }
                    if (Helper.getSemitoneDistance(freq0 / n, freq1) < maxAbsPeakDistance) {
                        weigthValue -= (freq0 + e1) / (freq0 / n + e2) * candidates[j].getAmp();
//                        weigthValue -= candidates[j].getAmp()/n;
                    }
                }
            }
            candidates[i].setWeight(weigthValue);
        }

        candidatesList = Arrays.asList(candidates);
        Collections.sort(candidatesList);

        float sum = 0;
        int index = 0;
        while (index < candidatesList.size()) {
            if (index > 0 && sum / Math.pow(index, rate) > (sum + candidatesList.get(index).getWeight()) / Math.pow(index + 1, rate)) {
                break;
            }
            sum += candidatesList.get(index).getWeight();
            fundamentals.add(candidatesList.get(index++));
        }

        return fundamentals.toArray(new DetectedHarmonic[]{});
    }


}

