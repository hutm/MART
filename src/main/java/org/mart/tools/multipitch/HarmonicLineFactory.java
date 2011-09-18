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
import java.util.List;

/**
 * @version 1.0 Sep 9, 2009 3:00:44 PM
 * @author: Maksim Khadkevich
 */
public class HarmonicLineFactory {


    private static List<HarmonicLine> harmonicLines = new ArrayList<HarmonicLine>();


    public static void reset() {
        harmonicLines = new ArrayList<HarmonicLine>();
    }

    public static void assingHarmonicLine(DetectedHarmonic detectedHarmonic) {
        float lineCenterFreq;
        float currentFreq;
        boolean isAssigned = false;
        for (HarmonicLine harmonicLine : harmonicLines) {
            if (harmonicLine.getEndFrameIndex() + 1 == detectedHarmonic.getFrameIndex()) {
                lineCenterFreq = harmonicLine.getFirstDetectedHarmonic().getFreq();
                currentFreq = detectedHarmonic.getFreq();
                if (Math.abs(Helper.getSemitoneDistance(lineCenterFreq, currentFreq)) < PitchExtractor.MAX_DISTANCE_IN_HARMONIC_LINES) {
                    harmonicLine.addDetectedHarmonic(detectedHarmonic);
                    isAssigned = true;
                    break;
                }
            }
        }
        if (!isAssigned) {
            HarmonicLine newHarmonicLine = new HarmonicLine();
            newHarmonicLine.addDetectedHarmonic(detectedHarmonic);
            harmonicLines.add(newHarmonicLine);
        }
    }


    /**
     * Once having collected information on all the harmonics, now it is time to make grouping
     * A HarmonicLine can belong to different groups at the same time
     * //TODO
     *
     * @param harmonicLine a HarmonicLine
     */
    public static void groupingOfHarmonicLines(HarmonicLine harmonicLine) {

        //At first all harmonic lines that intersects this harmonic and have frequency relations are detected.
        for (HarmonicLine harmonicLine1 : harmonicLines) {
            if (harmonicLine1 != harmonicLine) {
                if (harmonicLine1.intersects(harmonicLine)) {
                    if (harmonicLine1.isProbableHigerHarmonicFor(harmonicLine)) {
                        harmonicLine.addProbableHigerHarmonic(harmonicLine1);
                    }
                    if (harmonicLine1.isProbableFundamentalFor(harmonicLine)) {
                        harmonicLine.addProbableFundamental(harmonicLine1);
                    }
                }
            }
        }

    }

    public static void afterGroupingInHarmonicLines() {
        // It is necessary to
        // 1. Remove too short lines
        // 2. calculate average frequency and amplitude of the group
        List<HarmonicLine> tempHarmonicLineList = new ArrayList<HarmonicLine>();

        for (HarmonicLine harmonicLine : harmonicLines) {
            if (harmonicLine.getFrameDuration() > PitchExtractor.MIN_DURATION_IN_FRAMES) {
                harmonicLine.calculateAverageFreqAndAmp();
                harmonicLine.innerSegmentation();
                tempHarmonicLineList.add(harmonicLine);
            }

        }
        harmonicLines = tempHarmonicLineList;

        //Grouping and calculating weight function
        for (HarmonicLine harmonicLine : harmonicLines) {
            groupingOfHarmonicLines(harmonicLine);
        }
        for (HarmonicLine harmonicLine : harmonicLines) {
            harmonicLine.calculateWeight();
        }

    }


    public static void extractFundamentals() {

        //For each harmonicLine find parent or higher harmonics
        for (HarmonicLine harmonicLine : harmonicLines) {
            List<int[]> intervals = harmonicLine.getOnsetOffSetIntervals();
            for (int[] interval : intervals) {
                int startIndex = interval[0];
                int endIndex = interval[1];

                //Check amplitude condition
                if (!harmonicLine.isStrongEnoughWithRespectToFrameEnergy(startIndex, endIndex)) {
                    harmonicLine.addNonFundamentalInterval(startIndex, endIndex);
                    break;
                }

                //First check possible fundamental candidates
                for (HarmonicLine probableFundamental : harmonicLine.getProbableFundamentals()) {
                    if (probableFundamental.getAverageWeight(startIndex, endIndex) > harmonicLine.getAverageWeight(startIndex, endIndex) &&
                            probableFundamental.amplitudeCondition(startIndex, endIndex)) {
                        harmonicLine.addNonFundamentalInterval(startIndex, endIndex);
                        break;
                    }
                }
            }
        }

        //Now supress all higher harmonics
        for (HarmonicLine harmonicLine : harmonicLines) {
            List<int[]> f0Intervals = harmonicLine.getFundamentalIntervals();
            for (int[] interval : f0Intervals) {
                for (HarmonicLine higherHarmonic : harmonicLine.getProbableHigherHarmonics()) {
                    higherHarmonic.addNonFundamentalInterval(interval[0], interval[1]);
                }
            }
        }

    }


    /**
     * Does not wrap to fundamentals - only removes too silent regions
     */
    public static void extractFundamentals_harmonicsOnly() {

        //For each harmonicLine find parent or higher harmonics
        for (HarmonicLine harmonicLine : harmonicLines) {
            List<int[]> intervals = harmonicLine.getOnsetOffSetIntervals();
            for (int[] interval : intervals) {
                int startIndex = interval[0];
                int endIndex = interval[1];

                //Check amplitude condition
                if (!harmonicLine.isStrongEnoughWithRespectToFrameEnergy(startIndex, endIndex)) {
                    harmonicLine.addNonFundamentalInterval(startIndex, endIndex);
                    break;
                }
            }
        }
    }


//    /**
//     * Extracts fundamentals that are present in the given time frame
//     * //TODO needs finalizing
//     * @param frameIndex frameIndex
//     */
//    public static void extractFundamentalsForFrame(int frameIndex) {
//        //First collect all lines that intersect this frame
//        List<HarmonicLine> harmonicLinesForFrame = new ArrayList<HarmonicLine>();
//        for (HarmonicLine harmonicLine : harmonicLines) {
//            if (harmonicLine.getStartFrameIndex() <= frameIndex && harmonicLine.getEndFrameIndex() > frameIndex) {
//                harmonicLinesForFrame.add(harmonicLine);
//            }
//        }
//        Collections.sort(harmonicLinesForFrame);
//
//        //Extract frequency values (already sorted)
//        DetectedHarmonic[] detectedHarmonics = new DetectedHarmonic[harmonicLinesForFrame.size()];
//        for (int i = 0; i < detectedHarmonics .length; i++) {
//            detectedHarmonics[i] = harmonicLinesForFrame.get(i).getDetectedHarmonic(frameIndex);
//        }
//
//        //Now detectection of fundamentals is performed on the frame basis
//        DetectedHarmonic[] probableFundamentals = F0Exctractor.getF0s(detectedHarmonics);
//
//
//    }


    public static List<HarmonicLine> getHarmonicLines() {
        return harmonicLines;
    }
}
