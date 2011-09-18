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

package org.mart.crs.core.similarity;

import org.mart.crs.config.Settings;
import org.mart.crs.utils.helper.HelperArrays;
import org.mart.tools.svf.SVF;
import org.mart.tools.svf.extractor.SVFExtractor;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 1/18/11 3:56 PM
 * @author: Hut
 */
public class SimilarityMatrix {


    public static final int contextLengthForVerticalSegmentation = 20;

    public static int numberOfFramesWhenInitializeByParts = 2000;

    /**
     * Features used for similarity calculation
     */
    protected float[][] features;

    protected float[][] similarityMatrix;

    protected float[] detectionFunction;

    protected int contextLength;


    public SimilarityMatrix(float[][] features) {
        this.features = features;
    }

    protected float[][] computeSimilarityMatrix(int startIndex, int endIndex) {
        int length = endIndex - startIndex;
        float[][] similarityMatrix = new float[length][features.length];
        for (int i = startIndex; i < endIndex; i++) {
            for (int j = 0; j < features.length; j++) {
                similarityMatrix[i-startIndex][j] = getDistance(i, j);
            }
        }
        return similarityMatrix;
    }


    protected float[][] computeSimilarityMatrix() {
        return computeSimilarityMatrix(0, features.length);
    }




    protected void computeDetectionFunction() {
        if (!Settings.initializationByParts) {
            getSimilarityMatrix();
            int length = features.length;
            detectionFunction = new float[length];

            SVF svf = new SVF(similarityMatrix, 0.009f, contextLength, SVFExtractor.NoTransformFeatureExtractor);
            detectionFunction = svf.getSVFFunctionNormalized();
        } else{
            int counter = 0;
            List<float[]> detectionFunctionList = new ArrayList<float[]>();
            detectionFunctionList.add(new float[contextLength]);
            while (counter < features.length) {
                int startIndex = Math.max(0, counter - contextLength);
                int endIndex = Math.min(counter + numberOfFramesWhenInitializeByParts + contextLength, features.length);
                float[][] matrixPart  = computeSimilarityMatrix(startIndex, endIndex);
                matrixPart = computeDetectionFunctionWithHorizontalSegmentation(matrixPart);

                SVF svf = new SVF(matrixPart, 0.009f, contextLength, SVFExtractor.NoTransformFeatureExtractor);
                float[] detectionfunctionPartWithContext = svf.getSvfFunction();

                int arrayLength = Math.max(0, detectionfunctionPartWithContext.length - 2 * contextLength);
                if(arrayLength == 0){
                    break;
                }
                float[] detectionFunctionPart = new float[arrayLength];
                System.arraycopy(detectionfunctionPartWithContext, contextLength, detectionFunctionPart, 0, detectionfunctionPartWithContext.length - 2 * contextLength);
                detectionFunctionList.add(detectionFunctionPart);
                counter += numberOfFramesWhenInitializeByParts;
            }
            detectionFunctionList.add(new float[contextLength]);
            this.detectionFunction = SVF.normalizeSVFFunction(HelperArrays.concat(detectionFunctionList));
        }


        //The attempts below did not bring any nice curve
        /*//--------delta function----------
        similarityMatrix = HelperArrays.getDeltaCoefficients(similarityMatrix, contextLength);
        for (int i = 0; i < similarityMatrix.length; i++) {

        }

        //--------Just sum vertically----------
        for (int frame = 0; frame < length; frame++) {
            for (int i = frame; i < length; i++) {
                int j = i - frame;
                detectionFunction[frame] += similarityMatrix[i][j];
            }
        }
        //normalize
        for (int i = 0; i < length; i++) {
            detectionFunction[i] /= (length - i);
        }

        //Also take into account negative values
//        for (int i = 0; i < detectionFunction.length; i++) {
//            detectionFunction[i] = Math.abs(detectionFunction[i]);
//        }

        detectionFunction = HelperArrays.normalizeVector(detectionFunction);*/
    }


    protected float[][] computeDetectionFunctionWithHorizontalSegmentation(float[][] matrixPart){
        float[][] transposedMatrix = HelperArrays.transposeMatrix(matrixPart);
        SVF svf = new SVF(transposedMatrix, 0.009f, contextLengthForVerticalSegmentation, SVFExtractor.NoTransformFeatureExtractor);
        float[] svfFunction = svf.getSVFFunctionNormalized();
        int[] segmentationPoints = HelperArrays.getSegmentationIndexes(svfFunction, 0.7f);
        float[][] segmentedHorisontallyVectors = HelperArrays.segmentVectorsVertically(matrixPart, segmentationPoints);
        return segmentedHorisontallyVectors;
    }


    /**
     * Calculates distance measure between two frames
     *
     * @param index1 first index
     * @param index2 second index
     * @return distance measure
     */
    protected float getDistance(int index1, int index2) {
        return HelperArrays.calculateDistance(features[index1], features[index2]);
    }


    public float[][] getSimilarityMatrix() {
        if (similarityMatrix == null) {
            this.similarityMatrix = computeSimilarityMatrix();
        }
        return similarityMatrix;
    }

    public float[] getDetectionFunction() {
        if (detectionFunction == null) {
            computeDetectionFunction();
        }
        return detectionFunction;
    }


    public void setContextLength(int contextLength) {
        this.contextLength = contextLength;
    }
}
