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

package org.mart.tools.svf;

import org.mart.crs.management.beat.BeatStructure;
import org.mart.crs.management.label.chord.ChordSegment;

import java.util.List;

/**
 * This class is used to make synchronzation between time series
 *
 * @version 1.0 Jun 6, 2009 10:04:48 PM
 * @author: Maksim Khadkevich
 */
public class Synchronizer {
    /**
     * Ground truth segment borders
     */
    protected double[] GT;

    /**
     * output of <ref>Segmenter</ref>
     */
    protected double[] seg;

    /**
     * Array that contains corespondance of ground truth indexes to segmented indexes
     */
    protected Integer[] match;
    protected Integer[] matchSeconOrder;

    protected double[] distances;
    protected double[] distancesSecondOrder;

    /**
     * Constructor
     *
     * @param GT  Ground truth
     * @param seg output of <ref>Segmenter</ref>
     */
    public Synchronizer(double[] GT, double[] seg) {
        this.GT = GT;
        this.seg = seg;
        match = new Integer[GT.length];
        matchSeconOrder = new Integer[GT.length];
        distances = new double[GT.length];
        distancesSecondOrder = new double[GT.length];
    }

    protected void findBestCandidates() {
        int indexClosestRight, indexClosestLeft;
        double distanceClosestRight, distanceClosestLeft;

        for (int index = 0; index < GT.length; index++) {
            indexClosestLeft = getClosestLeft(index);
            indexClosestRight = getClosestRight(index);
            if (indexClosestLeft < 0) {
                indexClosestLeft = 0;
            }
            if (indexClosestRight < 0) {
                indexClosestRight = 0;
            }
            if (indexClosestRight >= seg.length) {
                indexClosestRight = seg.length - 1;
            }
            if (indexClosestLeft >= seg.length) {
                indexClosestLeft = seg.length - 1;
            }

            distanceClosestLeft = Math.abs(GT[index] - seg[indexClosestLeft]);
            distanceClosestRight = Math.abs(GT[index] - seg[indexClosestRight]);

            if (distanceClosestLeft < distanceClosestRight) {
                match[index] = indexClosestLeft;
                distances[index] = distanceClosestLeft;

                matchSeconOrder[index] = indexClosestRight;
                distancesSecondOrder[index] = distanceClosestRight;
            } else {
                match[index] = indexClosestRight;
                distances[index] = distanceClosestRight;

                matchSeconOrder[index] = indexClosestLeft;
                distancesSecondOrder[index] = distanceClosestLeft;
            }
        }

    }

    protected void removeDuplicates() {
        for (int i = 0; i < match.length; i++) {
            for (int j = 0; j < match.length; j++) {
                if (match[i] != null && match[j] != null && match[i].intValue() == match[j].intValue() && i != j) {
                    if (distances[i] > distances[j]) {
                        match[i] = null;
                    } else {
                        match[j] = null;
                    }
                }
            }
        }
    }


    /**
     * Returns the closest left point from segmented points to the GT point with given index
     *
     * @param index index
     * @return Segmented point index
     */
    protected int getClosestLeft(int index) {
        for (int i = 0; i < seg.length; i++) {
            if (GT[index] < seg[i]) {
                return i - 1;
            }
        }
        return seg.length - 1;
    }

    /**
     * Returns the closest right point from segmented points to the GT point with given index
     *
     * @param index index
     * @return Segmented point index
     */
    protected int getClosestRight(int index) {
        for (int i = seg.length - 1; i >= 0; i--) {
            if (GT[index] > seg[i]) {
                return i + 1;
            }
        }
        return 0;
    }

    public static void synchronizeChordLabelsWithBeats(List<ChordSegment> segmentList, BeatStructure beatStructure) {
        if (beatStructure == null) {
            return;
        }
        double[] beatsFloat = beatStructure.getBeats();
        synchronizeChordLabelsWithBeats(segmentList, beatsFloat);
    }


    public static void synchronizeChordLabelsWithBeats(List<ChordSegment> segmentList, double[] beats) {

        //Start time of each segment and end time of the last segment, since there are no pauses in between chords
        double[] timeInstances = new double[segmentList.size() + 2];
        timeInstances[0] = 0.0; //Beginning of the song
        for (int i = 0; i < segmentList.size(); i++) {
            timeInstances[i+1] = segmentList.get(i).getOnset();
        }
        timeInstances[timeInstances.length - 1] = segmentList.get(segmentList.size() - 1).getOffset();

        Synchronizer synchronizer = new Synchronizer(beats, timeInstances);
        synchronizer.findBestCandidates();
        synchronizer.removeDuplicates();

        for (int i = 0; i < synchronizer.match.length; i++) {
            if (synchronizer.match[i] != null) {
                //make substitution
                for (ChordSegment segment : segmentList) {
                    if (segment.getOnset() == timeInstances[synchronizer.match[i]]) {
                        segment.setOnset(beats[i]);
                    }
                    if (segment.getOffset() == timeInstances[synchronizer.match[i]]) {
                        segment.setOffset(beats[i]);
                    }
                }
            }
        }

        int startBeat = -1;
        int endBeat = -1;
        for (int i = 0; i < synchronizer.match.length; i++) {
            if (synchronizer.match[i] != null) {
                if (startBeat < 0) {
                    startBeat = i;
                } else {
                    endBeat = i;
                    segmentList.get(synchronizer.match[startBeat] - 1).setDurationInBeats(endBeat - startBeat);
                    startBeat = endBeat;
                }
            }
        }

        System.out.println("");


//        //Set chord durations in beats
//        for (ChordSegment segment : segmentList) {
//            int startBeatIndex = 0;
//            for (int i = 0; i < beats.length; i++) {
//                if (segment.getOnset() == beats[i]) {
//                    startBeatIndex = i;
//                    break;
//                }
//            }
//
//            int endBeatIndex = 0;
//            for (int i = 0; i < beats.length; i++) {
//                if (segment.getOffset() == beats[i]) {
//                    endBeatIndex = i;
//                    break;
//                }
//            }
//
//            if (startBeatIndex > 0 && endBeatIndex > 0) {
//                segment.setDurationInBeats(endBeatIndex - startBeatIndex);
//            }
//        }

    }


}
