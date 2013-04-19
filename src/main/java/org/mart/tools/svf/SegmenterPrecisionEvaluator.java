/*
 * Copyright (c) 2008-2013 Maksim Khadkevich and Fondazione Bruno Kessler.
 *
 * This file is part of MART.
 * MART is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2, as published
 * by the Free Software Foundation.
 *
 * MART is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with MART; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.mart.tools.svf;

/**
 * This class performs evaluations of the segmentation data produced by Segmenter
 *
 * @version 1.0 09.03.2009 14:52:32
 * @author: Maksim Khadkevich
 */
public class SegmenterPrecisionEvaluator extends Synchronizer {


    protected int delitions;
    protected int insertions;
    protected double error;

    public SegmenterPrecisionEvaluator(double[] GT, double[] seg) {
        super(GT, seg);
    }


    /**
     * Performs matching ground truth points to segmented points
     */
    public void matchPoints() {
        findBestCandidates();
        removeDuplicates();
        assignRest();
    }

    /**
     * Assigns the rest of the unassigned points
     */
    private void assignRest() {
        //TODO in the current version this functionality is not used
//        for (int i = 0; i < match.length; i++) {
//        }
    }

    /**
     * Calculates delitions, insertions, errors
     */
    public void calculateMatchingRate() {
        //First calculate delitions and errors
        delitions = 0;
        error = 0;
        for (int i = 0; i < match.length; i++) {
            if (match[i] == null) {
                delitions++;
            } else {
                error += Math.abs(GT[i] - seg[match[i]]);
            }
        }

        //Now calculate insertions
        insertions = seg.length - (GT.length - delitions);
    }


    /**
     * Synchronizes the input segments with ground-truth
     *
     * @return
     */
    public float[] getCorrectedSegments() {
        float[] output = new float[seg.length];


        return output;
    }

    /**
     * toString method
     *
     * @return Representation in String originalFormat
     */
    public String toString() {
        return new String("\nERROR :" + error + "\nINSERTIONS :" + insertions + "\nDELITIONS :" + delitions);
    }

    /**
     * Getter
     *
     * @return delitions
     */
    public int getDelitions() {
        return delitions;
    }

    /**
     * Getter
     *
     * @return insertions
     */
    public int getInsertions() {
        return insertions;
    }

    /**
     * Getter
     *
     * @return error
     */
    public double getError() {
        return error;
    }
}
