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

import org.mart.crs.utils.helper.Helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.mart.crs.utils.helper.HelperArrays.*;


/**
 * @version 1.0 Sep 9, 2009 2:59:14 PM
 * @author: Maksim Khadkevich
 */
public class HarmonicLine implements Comparable {

    private List<int[]> nonFundamentalIntervals = new ArrayList<int[]>();

    private List<DetectedHarmonic> detectedHarmonicList;

    private int startFrameIndex;
    private int endFrameIndex;

    private float startFrequency;
    private float averageFrequency;
    private float averageAmplitude;

    private float[] ampData;
    private float[] freqData;
    private float[] weightData;

    private int[] onsets; //relative to the start frame
    private int[] onsetsAbsolute; //Absolute values

    private int[] offsets; //relative to the start frame
    private int[] offsetsAbsolute; //Absolute values

    //Grouping data
    private List<HarmonicLine> probableHigherHarmonics;
    private List<HarmonicLine> probableFundamentals;


    public HarmonicLine() {
        this.detectedHarmonicList = new LinkedList<DetectedHarmonic>();
        this.probableHigherHarmonics = new ArrayList<HarmonicLine>();
        this.probableFundamentals = new ArrayList<HarmonicLine>();
    }


    public int compareTo(Object o) {
        HarmonicLine anotherHarmonicLine = (HarmonicLine) o;
        if (anotherHarmonicLine.getFrameDuration() > this.getFrameDuration()) {
//        if (anotherHarmonicLine.getAverageFrequency() > this.getAverageFrequency()) {
            return 1;
        } else {
            return -1;
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (DetectedHarmonic detectedHarmonic : detectedHarmonicList) {
            builder.append(detectedHarmonic.getAmp()).append("\n");
        }
        return builder.toString();
    }


    public int getStartFrameIndex() {
        return startFrameIndex;
    }

    public void setStartFrameIndex(int startFrameIndex) {
        this.startFrameIndex = startFrameIndex;
    }

    public int getEndFrameIndex() {
        return endFrameIndex;
    }

    public void setEndFrameIndex(int endFrameIndex) {
        this.endFrameIndex = endFrameIndex;
    }

    public float getStartFrequency() {
        return startFrequency;
    }

    public void setStartFrequency(float startFrequency) {
        this.startFrequency = startFrequency;
    }

    public DetectedHarmonic getFirstDetectedHarmonic() {
        return detectedHarmonicList.get(0);
    }

    //TODO organize detected harmonics

    public void addDetectedHarmonic(DetectedHarmonic detectedHarmonic) {
        if (detectedHarmonicList.size() == 0) {
            this.startFrameIndex = detectedHarmonic.getFrameIndex();
            this.startFrequency = detectedHarmonic.getFreq();
        }

        this.endFrameIndex = detectedHarmonic.getFrameIndex();
        detectedHarmonicList.add(detectedHarmonic);
    }


    public int getFrameDuration() {
        return this.endFrameIndex - this.startFrameIndex + 1;
    }

    /**
     * Calculates average frequency of the Harmonic line
     */
    public void calculateAverageFreqAndAmp() {
        float sumFreq = 0;
        float sumAmp = 0;
        ampData = new float[detectedHarmonicList.size()];
        freqData = new float[detectedHarmonicList.size()];
        int i = 0;
        for (DetectedHarmonic detectedHarmonic : detectedHarmonicList) {
            sumFreq += detectedHarmonic.getFreq();
            sumAmp += detectedHarmonic.getAmp();
            ampData[i] = detectedHarmonic.getAmp();
            freqData[i++] = detectedHarmonic.getFreq();
        }
        this.averageFrequency = sumFreq / detectedHarmonicList.size();
        this.averageAmplitude = sumAmp / detectedHarmonicList.size();
    }

    /**
     * This method tries to find frame positions with probable onset
     * and offsets (it happens, when energy falls down below 0.3 of average amplitude starting from onset)
     */
    public void innerSegmentation() {
        int numberOfFramesToCalculateMean = 10; //while calculating function
        float onsetThreshold = 1.4f;
        float offsetThreshold = 0.2f;
        float meanEnergyThreshold = 2f;

        if (ampData.length < 2 * numberOfFramesToCalculateMean) {
            this.onsets = new int[]{0};
            this.onsetsAbsolute = new int[]{startFrameIndex};
            this.offsets = new int[]{endFrameIndex - startFrameIndex};
            this.offsetsAbsolute = new int[]{endFrameIndex};
            return;
        }

        //calculate objective function
        float[] function = new float[ampData.length]; //Average n-samples to the left, average n-samples to the right and their RATIO
        float[] function2 = new float[ampData.length]; //Average m-samples to the left

        float sumLeft = 0;
        float sumRight = 0;

        int currentFrame = numberOfFramesToCalculateMean - 1;
        for (int i = 0; i <= currentFrame; i++) {
            sumLeft += ampData[i];
            sumRight += ampData[i + currentFrame];
        }
        function[currentFrame] = sumRight / sumLeft;
        function2[currentFrame] = sumLeft;
        currentFrame++;


        while (currentFrame < ampData.length - numberOfFramesToCalculateMean + 1) {
            sumLeft += ampData[currentFrame] - ampData[currentFrame - numberOfFramesToCalculateMean];
            sumRight += -ampData[currentFrame - 1] + ampData[currentFrame + numberOfFramesToCalculateMean - 1];
            function[currentFrame] = sumRight / sumLeft;
            function2[currentFrame] = sumLeft;
            currentFrame++;
        }
        //Finished calculation of objective function


        List<Integer> onsetList = new ArrayList<Integer>();
        int startIndex;
        for (int i = 0; i < function.length; i++) {
            if (function[i] > onsetThreshold) {
                startIndex = i;
                while (function[i++] > onsetThreshold) {
                }
                onsetList.add(findIndexWithMaxValue(function, startIndex, i));
            }
        }


        //Delete false onsets
        int currentUpperOnset = ampData.length - 1;
        int counter = 0;
        for (int i = onsetList.size() - 1; i >= 0; i--) {
            if (calculateMean(ampData, onsetList.get(i), currentUpperOnset) * meanEnergyThreshold < averageAmplitude) {
                onsetList.set(i, 0);
            } else {
                counter++;
            }
        }

        //Write to the ouput originalFormat
        onsets = new int[counter + 1];
        onsetsAbsolute = new int[counter + 1];

        // first onset is the beginning
        onsets[0] = 0;
        onsetsAbsolute[0] = startFrameIndex;

        for (int i = 0; i < counter; i++) {
            if (onsetList.get(i) != 0) {
                onsets[i + 1] = onsetList.get(i);
                onsetsAbsolute[i + 1] = onsets[i + 1] + startFrameIndex;
            }
        }


        //Now detect offsets
        List<Integer> offsetList = new ArrayList<Integer>();
        int startSearch, endSearch;
        boolean lastWasOffset = false;
        for (int i = 0; i < onsets.length; i++) {
            startSearch = onsets[i];
            if (i + 1 < onsets.length) {
                endSearch = onsets[i + 1];
            } else {
                endSearch = ampData.length - 1;
            }

            float sum = function2[startSearch];
            lastWasOffset = false;
            for (int j = startSearch + 1; j < endSearch - numberOfFramesToCalculateMean; j++) {
                if (sum / (j - startSearch) * offsetThreshold > function2[j]) {
                    offsetList.add(j);
                    lastWasOffset = true;
                    break;
                }
                sum += function2[j];
            }
        }
        if (!lastWasOffset) {
            offsetList.add(ampData.length - 1);
        }


        //Write to the ouput originalFormat
        offsets = new int[offsetList.size()];
        offsetsAbsolute = new int[offsetList.size()];

        for (int i = 0; i < offsets.length; i++) {
            if (offsetList.get(i) != 0) {
                offsets[i] = offsetList.get(i);
                offsetsAbsolute[i] = offsets[i] + startFrameIndex;
            }
        }

    }


    /**
     * Performs weight calculation on a frame-by-frame basis
     */
    public void calculateWeight() {
        weightData = new float[ampData.length];
        System.arraycopy(ampData, 0, weightData, 0, ampData.length);
        for (HarmonicLine higherHarmonic : probableHigherHarmonics) {
            int[] indexes = getIntersection(higherHarmonic);
            int startIndex = indexes[0];
            int endIndex = indexes[1];
            for (int i = startIndex; i <= endIndex; i++) {
                weightData[startIndex - startFrameIndex] += getIncrementFunction(higherHarmonic, i);
            }
        }


    }

    private float getIncrementFunction(HarmonicLine higerHarmonicLine, int absIndex) {
        float ampValue = higerHarmonicLine.getAmpData()[absIndex - higerHarmonicLine.getStartFrameIndex()];
        int n = Math.round(higerHarmonicLine.getAverageFrequency() / this.getAverageFrequency());
//        float outFunction = (averageFrequency + 20) / (averageFrequency * n + 120) * ampValue;
        float outFunction = ampValue;

        return outFunction;
    }


    public float getAverageWeight(int startIndex, int endIndex) {
        if (startIndex < startFrameIndex) {
            startIndex = startFrameIndex;
        }
        if (endIndex > endFrameIndex) {
            endIndex = endFrameIndex;
        }

        return calculateMean(weightData, startIndex - startFrameIndex, endIndex - startFrameIndex);
    }


    public float getAverageAmplitude(int startIndex, int endIndex) {
        if (startIndex < startFrameIndex) {
            startIndex = startFrameIndex;
        }
        if (endIndex > endFrameIndex) {
            endIndex = endFrameIndex;
        }

        return calculateMean(ampData, startIndex - startFrameIndex, endIndex - startFrameIndex);
    }

    public float getAverageFrequency(int startIndex, int endIndex) {
        if (startIndex < startFrameIndex) {
            startIndex = startFrameIndex;
        }
        if (endIndex > endFrameIndex) {
            endIndex = endFrameIndex;
        }

        return calculateMean(freqData, startIndex - startFrameIndex, endIndex - startFrameIndex);
    }

    public float getAverageAmp2FrameAmpRatio(int startIndex, int endIndex) {

        if (startIndex < startFrameIndex) {
            startIndex = startFrameIndex;
        }

        if (endIndex > endFrameIndex) {
            endIndex = endFrameIndex;
        }

        float out = 0;
        for (DetectedHarmonic detectedHarmonic : detectedHarmonicList) {
            out += detectedHarmonic.getAmp2FrameAmpRatio();
        }

        return out / detectedHarmonicList.size();

    }

//    public int[] getOnsetOffSetIntervals() {
//        if (onsetsAbsolute == null || offsetsAbsolute == null) {
//            return null;
//        }
//        int[] outData = new int[onsetsAbsolute.length + offsetsAbsolute.length];
//        System.arraycopy(onsetsAbsolute, 0, outData, 0, onsetsAbsolute.length);
//        System.arraycopy(offsetsAbsolute, 0, outData, onsetsAbsolute.length, offsetsAbsolute.length);
//        Arrays.sort(outData);
//        return outData;
//    }

    public List<int[]> getOnsetOffSetIntervals() {
        List<int[]> outList = new ArrayList<int[]>();

        int[] outData = new int[onsetsAbsolute.length + offsetsAbsolute.length];
        System.arraycopy(onsetsAbsolute, 0, outData, 0, onsetsAbsolute.length);
        System.arraycopy(offsetsAbsolute, 0, outData, onsetsAbsolute.length, offsetsAbsolute.length);
        Arrays.sort(outData);

        for (int i = 0; i < outData.length; i++) {
            for (int j = 0; j < onsetsAbsolute.length; j++) {
                if (outData[i] == onsetsAbsolute[j] && i < outData.length - 1) {
                    outList.add(new int[]{outData[i], outData[i + 1]});
                }
            }
        }


        return outList;
    }


    /**
     * Get harmonic for this line at a specific frame
     *
     * @param frameIndex
     * @return
     */
    public DetectedHarmonic getDetectedHarmonic(int frameIndex) {
        if (frameIndex >= startFrameIndex && frameIndex <= endFrameIndex) {
            return detectedHarmonicList.get(frameIndex - startFrameIndex);
        } else {
            return null;
        }
    }

    /**
     * Checks a HarmonicLine for Intersection
     *
     * @param harmonicLine a harmonicLine
     * @return intersection
     */
    public boolean intersects(HarmonicLine harmonicLine) {
        return this.getStartFrameIndex() < harmonicLine.getEndFrameIndex() && this.getEndFrameIndex() > harmonicLine.getStartFrameIndex();
    }

    public int[] getIntersection(HarmonicLine harmonicLine) {
        int[] out = new int[2];
        out[0] = Math.max(this.getStartFrameIndex(), harmonicLine.getStartFrameIndex());
        out[1] = Math.min(this.getEndFrameIndex(), harmonicLine.getEndFrameIndex());

        return out;
    }

    public boolean amplitudeCondition(int startIndex, int endIndex) {
//        return isStrongEnoughWithRespectToFrameEnergy(startIndex, endIndex) && isStrongEnoughWithRespectToHarmonics(startIndex, endIndex) && hasHigherHarmonicsEnough(startIndex, endIndex);
        return isStrongEnoughWithRespectToFrameEnergy(startIndex, endIndex) && hasHigherHarmonicsEnough(startIndex, endIndex);
    }

    public boolean isStrongEnoughWithRespectToFrameEnergy(int startIndex, int endIndex) {
        float averageAmp2FrameAmpRatio = getAverageAmp2FrameAmpRatio(startIndex, endIndex);
        return averageAmp2FrameAmpRatio >= PitchExtractor.MIN_FUNDAMENTAL_FRAME_ENERGY_AMP_RELATION;
    }

    public boolean isStrongEnoughWithRespectToHarmonics(int startIndex, int endIndex) {
        float averageAmp = getAverageAmplitude(startIndex, endIndex);
        float averageWeight = getAverageWeight(startIndex, endIndex);
        return averageAmp >= PitchExtractor.MIN_FUNDAMENTAL_HARMONICS_AMP_RELATION * averageWeight;
    }

    public boolean hasHigherHarmonicsEnough(int startIndex, int endIndex) {
        float averageAmp = getAverageAmplitude(startIndex, endIndex);
        float averageWeight = getAverageWeight(startIndex, endIndex);
        return averageAmp <= PitchExtractor.MAX_FUNDAMENTAL_AMP_RELATION * averageWeight;
    }


    //TODO take into account onsets

    public float correlationCoefficient(HarmonicLine harmonicLine) {
        if (this.intersects(harmonicLine)) {
            int[] indexes = this.getIntersection(harmonicLine);
            return correlation(this.getAmpData(), harmonicLine.getAmpData(), indexes[0] - this.getStartFrameIndex(), indexes[0] - harmonicLine.getStartFrameIndex(), indexes[1] - indexes[0] + 1);
        } else {
            return -2;
        }
    }

    public boolean isProbableHigerHarmonicFor(HarmonicLine harmonicLine) {
        for (int i = 2; i < PitchExtractor.numberOfHarmonicsToConsider; i++) {
            if (Math.abs(Helper.getSemitoneDistance(this.getAverageFrequency(), i * harmonicLine.getAverageFrequency())) < PitchExtractor.maxAbsPeakDistance) {
                return true;
            }
        }
        //Otherwise
        return false;
    }

    public boolean isProbableFundamentalFor(HarmonicLine harmonicLine) {
        for (int i = 2; i < PitchExtractor.numberOfHarmonicsToConsider; i++) {
            if (Math.abs(Helper.getSemitoneDistance(i * this.getAverageFrequency(), harmonicLine.getAverageFrequency())) < PitchExtractor.maxAbsPeakDistance) {
                return true;
            }
        }
        //Otherwise
        return false;
    }

    public void addProbableHigerHarmonic(HarmonicLine harmonicLine) {
        if (!probableHigherHarmonics.contains(harmonicLine)) {
            probableHigherHarmonics.add(harmonicLine);
        }
    }

    public void addProbableFundamental(HarmonicLine harmonicLine) {
        if (!probableFundamentals.contains(harmonicLine)) {
            probableFundamentals.add(harmonicLine);
        }
    }


    public List<int[]> getFundamentalIntervals() {
        List<int[]> outList = new ArrayList<int[]>();
        for (int[] interval : getOnsetOffSetIntervals()) {
            outList.add(interval);
        }
        Helper.removeIntersection(outList, nonFundamentalIntervals);
        return outList;
    }


    public void addNonFundamentalInterval(int startIndex, int endIndex) {
        this.nonFundamentalIntervals.add(new int[]{startIndex, endIndex});
    }


    public float getAverageFrequency() {
        return averageFrequency;
    }

    public float getAverageAmplitude() {
        return averageAmplitude;
    }


    public int[] getOnsets() {
        return onsets;
    }

    public int[] getOnsetsAbsolute() {
        return onsetsAbsolute;
    }


    public float[] getAmpData() {
        return ampData;
    }

    public List<HarmonicLine> getProbableHigherHarmonics() {
        return probableHigherHarmonics;
    }

    public List<HarmonicLine> getProbableFundamentals() {
        return probableFundamentals;
    }
}
