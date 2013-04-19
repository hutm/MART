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

/**
 * @version 1.0 6/8/11 4:28 PM
 * @author: Hut
 */
public class Histogram {

    protected float[][] data;
    protected float[][] values;
    protected float minValue;

    protected int[] histValues;
    protected float[] histPoints;
    protected static final int NUMBER_OF_BINS_IN_HISTOGRAM = 201;




    public Histogram(float[][] data, float[][] values, float minValue) {
        this.data = data;
        this.values = values;
        this.minValue = minValue;
    }


    public void initialize() {
        float rangeInSDT = 3;
        float[] meanAndSTD = calculateMeanAndStandardDeviation();
        float  mean = meanAndSTD[0];
        float std = meanAndSTD[1];
        histPoints = new float[NUMBER_OF_BINS_IN_HISTOGRAM];
        float step = rangeInSDT * std / (NUMBER_OF_BINS_IN_HISTOGRAM - 1);
        for(int i = 0; i < histPoints.length; i++){
            histPoints[i] = -(rangeInSDT/2) * std + step * i;
        }

        histValues = new int[NUMBER_OF_BINS_IN_HISTOGRAM];

        //NOW assign values to histogram
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                if (values[i][j] > minValue) {
                    for(int k = 0; k < NUMBER_OF_BINS_IN_HISTOGRAM; k++){
                        if(histPoints[k] + step / 2 > data[i][j] && histPoints[k] - step/2 < data[i][j]){
                            histValues[k]+=values[i][j];
                            break;
                        }
                    }
                }
            }
        }
    }


    public float[] calculateMeanAndStandardDeviation() {
        float sumMean = 0;
        float sumDev = 0;
        float mean, dev;
        int count = 0;

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                if (values[i][j] > minValue) {
                    sumMean += data[i][j];
                    count++;
                }
            }
        }
        mean = sumMean / count;

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                if (values[i][j] > minValue) {
                    sumDev += (mean - data[i][j]) * (mean - data[i][j]);
                }
            }
        }
        dev = (float) Math.sqrt(sumDev / count);

        float[] out = new float[2];
        out[0] = mean;
        out[1] = dev;

        return out;
    }


}
