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

import org.mart.crs.utils.helper.HelperArrays;

import java.util.ArrayList;
import java.util.List;

/**
 * Segmenter contains functionality that can perform segmantation based on the function values
 *
 * @version 1.0 07.03.2009 1:01:03
 * @author: Maksim Khadkevich
 */
public class Segmenter {


    /**
     * Function values
     */
    private float[] function;

    /**
     * Threshold, which defines segment border detection in comparison with the average function value
     */
    private float threshold = 0.7f;

    /**
     * Constructor
     *
     * @param function  Function data
     * @param threshold Threshold
     */
    public Segmenter(float[] function, float threshold) {
        this.function = function;
        this.threshold = threshold;
    }

    /**
     * Constructor
     *
     * @param function Function data
     */
    public Segmenter(float[] function) {
        this.function = function;
    }

    /**
     * Performs segmantation
     *
     * @return array of indexes where there is a segment border
     */
    public int[] segment() {
        List<Integer> segmentBorders = new ArrayList<Integer>();

        float meanValue = HelperArrays.calculateMean(function);
        float absoluteThreshold = threshold;
        boolean isSegmentStarted = false;
        float maxValue = 0;
        int maxIndex = 0;

        for (int i = 0; i < function.length; i++) {
            if (function[i] > absoluteThreshold) {
                if (!isSegmentStarted) {
                    isSegmentStarted = true;
                    maxValue = function[i];
                    maxIndex = i;
                } else {
                    if (function[i] > maxValue) {
                        maxValue = function[i];
                        maxIndex = i;
                    }
                }

            } else {
                if (isSegmentStarted) {
                    segmentBorders.add(maxIndex);
                }
                isSegmentStarted = false;
            }
        }

        //Now form output array
        int[] output = new int[segmentBorders.size()];
        for (int i = 0; i < segmentBorders.size(); i++) {
            output[i] = segmentBorders.get(i);
        }
        return output;
    }


    /**
     * Getter
     *
     * @return Function
     */
    public float[] getFunction() {
        return function;
    }

    /**
     * Setetr
     *
     * @param function Function data array
     */
    public void setFunction(float[] function) {
        this.function = function;
    }

    /**
     * Getter
     *
     * @return Threshold
     */
    public float getThreshold() {
        return threshold;
    }

    /**
     * Setter
     *
     * @param threshold Threshold value
     */
    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }
}
