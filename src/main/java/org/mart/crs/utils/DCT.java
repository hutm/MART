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

package org.mart.crs.utils;

import java.util.Arrays;

/**
 * @version 1.0 6/30/11 11:24 AM
 * @author: Hut
 */
public class DCT {


    public static void applyDCT(float[] x, float[] result) {
        int N = x.length;
        for (int k = 0; k < result.length; k++) {
            result[k] = 0;
            for (int n = 0; n < x.length; n++) {
                result[k] += x[n] * Math.cos((Math.PI * (2 * (n + 1) - 1) * (k)) / (2.0f * N));
            }
            if (k == 0) {
                result[k] *= Math.sqrt(1.0 / N);
            } else {
                result[k] *= Math.sqrt(2.0 / N);
            }
        }
    }


    public static float[][] applyDCT(float[][] x, int numberOfDCTCoeff) {
        float[][] out = new float[x[0].length][numberOfDCTCoeff];
        for (int i = 0; i < x.length; i++) {
            float[] dctOutput = new float[numberOfDCTCoeff];
            applyDCT(x[i], dctOutput);
            out[i] = Arrays.copyOf(dctOutput, dctOutput.length);
        }
        return out;
    }


    public static void applyInverseDCT(float[] x, float[] result) {
        int N = x.length;
        for(int i = 0; i < result.length; i++){
            result[i] = 0;
        }
        for (int n = 0; n < result.length; n++) {
            for (int k = 0; k < N; k++) {
                double coeff;
                if (k == 0) {
                    coeff = Math.sqrt(1.0 / N);
                } else {
                    coeff = Math.sqrt(2.0 / N);
                }
                result[n] += (coeff * x[k] * Math.cos((Math.PI * (2 * (n + 1) - 1) * k) / (2.0f * N)));
            }
        }
    }


    public static float[][] applyInverseDCT(float[][] x, int numberOfDCTCoeff, int firstCoeff) {
        float[][] out = new float[x.length][numberOfDCTCoeff];
        for (int i = 0; i < x.length; i++) {
            float[] dctOutput = new float[x[0].length];
            applyInverseDCT(x[i], dctOutput);
            for(int coeff = firstCoeff; coeff < numberOfDCTCoeff + firstCoeff; coeff++){
                out[i][coeff - firstCoeff] = dctOutput[coeff];
            }
        }
        return out;
    }


}
