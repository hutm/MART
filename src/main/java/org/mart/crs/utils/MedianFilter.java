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

package org.mart.crs.utils;

import java.util.Arrays;

/**
 * Author: Maksim Khadkevich
 * Date: 13.02.2009
 * Time: 18:12:02
 */
public class MedianFilter {

    public static float[][] filter(float[][] data, int order) {
        //If order is even, make it odd addin 1
        if (order / 2 == order / 2.0) {
            order++;
        }
        float[][] output = new float[data.length][data[0].length];
        float[] tempArray = new float[order];
        for (int k = 0; k < data[0].length; k++) {
            for (int i = 0; i < data.length; i++) {
                //filling in the temp array
                for (int j = -(order / 2); j <= (order / 2); j++) {
                    if ((i + j) < 0) {
                        tempArray[j + (order / 2)] = data[0][k];
                    } else if ((i + j) >= data.length) {
                        tempArray[j + (order / 2)] = data[data.length - 1][k];
                    } else {
                        tempArray[j + (order / 2)] = data[i + j][k];
                    }
                }

                //sorting temp array
                Arrays.sort(tempArray);
                //writing output value taking value of the middle cell
                output[i][k] = tempArray[tempArray.length / 2];
            }
        }

        return output;
    }

    public static float[] filter(float[] data, int order) {
        //If order is even, make it odd addin 1
        if (order / 2 == order / 2.0) {
            order++;
        }
        float[] outputArray = new float[data.length];
        float[] tempArray = new float[order];
        for (int i = 0; i < data.length; i++) {
            //filling in the temp array
            for (int j = -(order / 2); j <= (order / 2); j++) {
                if ((i + j) < 0) {
                    tempArray[j + (order / 2)] = data[0];
                } else if ((i + j) >= data.length) {
                    tempArray[j + (order / 2)] = data[data.length - 1];
                } else {
                    tempArray[j + (order / 2)] = data[i + j];
                }
            }

            //sorting temp array
            Arrays.sort(tempArray);
            //writing output value taking value of the middle cell
            outputArray[i] = tempArray[tempArray.length / 2];
        }
        return outputArray;
    }


    public static void main(String[] args) {
        float[][] array = {{2, 2}, {80, 80}, {6, 6}, {3, 3}};
//        float[][] array = {{2, 80, 6, 3}, {2, 80, 6, 3}};
        float[][] out = filter(array, 3);
    }

}
