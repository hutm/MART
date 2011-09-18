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

/**
 * @version 1.0 7/5/11 4:45 PM
 * @author: Hut
 */
public class TonalCentroid {

    public static final int DIMENSION = 6;
    public static final int CHROMA_BINS = 12;

    public static final float R1 = 1;
    public static final float R2 = 1;
    public static final float R3 = 0.5f;



    private static double[][] transformationMatrix;

    protected static double[][] getTransformationMatrix(){
        if(transformationMatrix == null){
            transformationMatrix = new double[DIMENSION][CHROMA_BINS];
            for(int i = 0; i < CHROMA_BINS; i++){
                transformationMatrix[0][i] = R1 * Math.sin(Math.PI * i * 7.0f / 6.0f);
                transformationMatrix[1][i] = R1 * Math.cos(Math.PI * i * 7.0f / 6.0f);
                transformationMatrix[2][i] = R2 * Math.sin(Math.PI * i * 3.0f / 2.0f);
                transformationMatrix[3][i] = R2 * Math.cos(Math.PI * i * 3.0f / 2.0f);
                transformationMatrix[4][i] = R3 * Math.sin(Math.PI * i * 2.0f / 3.0f);
                transformationMatrix[5][i] = R3 * Math.cos(Math.PI * i * 2.0f / 3.0f);
            }
        }
        return transformationMatrix;
    }


    public static float[] getTonalCentroid(float[] chroma){
        if(chroma.length != CHROMA_BINS){
            throw new IllegalArgumentException(String.format("Cannot calculate tonal centroid from a vector with dimension of %d", chroma.length));
        }
        double[][] matrix = getTransformationMatrix();
        float[] out = new float[DIMENSION];

        for(int i = 0; i < DIMENSION; i++){
            for(int j = 0; j < CHROMA_BINS; j++){
                out[i] += matrix[i][j] * chroma[j];
            }
        }
        return out;
    }


    public static float[][] getTonalCentroid(float[][] chroma){
        float[][] out = new float[chroma.length][];
        for(int i = 0; i < chroma.length; i++){
            out[i] = getTonalCentroid(chroma[i]);
        }
        return out;
    }


}
