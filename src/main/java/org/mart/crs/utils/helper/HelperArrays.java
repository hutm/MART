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

package org.mart.crs.utils.helper;

import de.dfki.maths.Complex;
import org.apache.log4j.Logger;
import org.mart.crs.logging.CRSException;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.AOToolkit;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


/**
 * @version 1.0 Jan 18, 2010 9:05:08 AM
 * @author: Maksim Khadkevich
 */
public class HelperArrays {
    protected static Logger logger = CRSLogger.getLogger(HelperArrays.class);


    /**
     * Concatenates two arrays into one
     *
     * @param A first array
     * @param B second array
     * @return concatenated array
     */
    public static float[] concat(float[] A, float[] B) {
        float[] C = new float[A.length + B.length];
        System.arraycopy(A, 0, C, 0, A.length);
        System.arraycopy(B, 0, C, A.length, B.length);
        return C;
    }


    public static short[] concat(short[] A, short[] B) {
        short[] C = new short[A.length + B.length];
        System.arraycopy(A, 0, C, 0, A.length);
        System.arraycopy(B, 0, C, A.length, B.length);
        return C;
    }

    public static int[] concat(int[] A, int[] B) {
        int[] C = new int[A.length + B.length];
        System.arraycopy(A, 0, C, 0, A.length);
        System.arraycopy(B, 0, C, A.length, B.length);
        return C;
    }

    /**
     * Concatenates two arrays into one
     *
     * @param A first array
     * @param B second array
     * @return concatenated array
     */
    public static byte[] concat(byte[] A, byte[] B) {
        byte[] C = new byte[A.length + B.length];
        System.arraycopy(A, 0, C, 0, A.length);
        System.arraycopy(B, 0, C, A.length, B.length);
        return C;
    }

    /**
     * Concatenated 2d arrays column by column
     *
     * @param in1
     * @param in2
     * @return
     */
    public static float[][] concatColumnWise(float[][] in1, float[][] in2) {
        float[][] output = new float[in1.length][];
        for (int i = 0; i < in1.length; i++) {
            output[i] = HelperArrays.concat(in1[i], in2[i]);
        }
        return output;
    }

    /**
     * Concatenated 2d arrays column by column
     *
     * @return
     */
    public static float[][] concatColumnWise(List<float[][]> inputs) {
        if (inputs.size() <= 0) {
            return new float[0][0];
        }
        float[][] output = new float[inputs.get(0).length][0];
        for (float[][] anInput : inputs) {
            output = concatColumnWise(output, anInput);
        }
        return output;
    }

    /**
     * Concatenates 2d arrays
     *
     * @return
     */
    public static float[][] concat(List<float[][]> inputs) {
        int numberOfFrames = 0;
        for (float[][] anInput : inputs) {
            numberOfFrames += anInput.length;
        }
        float[][] output = new float[numberOfFrames][];
        int pointer = 0;
        for (float[][] anInput : inputs) {
            for (float[] anAnInput : anInput) {
                output[pointer++] = anAnInput;
            }
        }
        return output;
    }

    /**
     * Concatenates 2d arrays
     *
     * @return
     */
    public static float[] concat(List<float[]> inputs) {
        int numberOfFrames = 0;
        for (float[] anInput : inputs) {
            numberOfFrames += anInput.length;
        }
        float[] output = new float[numberOfFrames];
        int pointer = 0;
        for (float[] anInput : inputs) {
            for (float anAnInput : anInput) {
                output[pointer++] = anAnInput;
            }
        }
        return output;
    }


    /**
     * Concatenates 2d arrays
     *
     * @return
     */
    public static short[] concat(List<short[]> inputs) {
        int numberOfFrames = 0;
        for (short[] anInput : inputs) {
            numberOfFrames += anInput.length;
        }
        short[] output = new short[numberOfFrames];
        int pointer = 0;
        for (short[] anInput : inputs) {
            for (short anAnInput : anInput) {
                output[pointer++] = anAnInput;
            }
        }
        return output;
    }


    public static float[][] cut(float[][] inData, int startIndex, int endIndex) {
        float[][] out = new float[endIndex - startIndex][];
        System.arraycopy(inData, startIndex, out, 0, endIndex - startIndex);
        return out;
    }


    /**
     * Sums all elements of array
     *
     * @param array
     * @return
     */
    public static float sum(float[] array) {
        float sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }

        return sum;
    }

    /**
     * Sums all elements of array
     *
     * @param array
     * @return
     */
    public static double sum(double[] array) {
        double sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }

        return sum;
    }


    /**
     * Sums all elements of array
     *
     * @param array
     * @return
     */
    public static float sum(float[] array, int startIndex, int endIndex) {
        float sum = 0;
        for (int i = startIndex; i < endIndex; i++) {
            sum += array[i];
        }

        return sum;
    }


    /**
     * Sums all elements of array
     *
     * @param array
     * @return
     */
    public static double sum(double[] array, int startIndex, int endIndex) {
        double sum = 0;
        for (int i = startIndex; i < endIndex; i++) {
            sum += array[i];
        }

        return sum;
    }


    /**
     * Sums all elements of array
     *
     * @param array
     * @return
     */
    public static int sum(int[] array) {
        int sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }

        return sum;
    }

    public static float[] pow(float[] inData, float rate) {
        float[] out = new float[inData.length];
        for (int i = 0; i < inData.length; i++) {
            out[i] = (float) Math.pow(inData[i], rate);
        }
        return out;
    }


    /**
     * Normalizes a vector so that average value of all bins is equal to 1;
     *
     * @param data data
     */
    private void normalize(float[] data) {
        float sum = sum(data);
        for (int i = 0; i < data.length; i++) {
            data[i] /= sum;
        }
    }

    /**
     * Finds minimal element in array
     *
     * @param array array
     * @return Minimum Value
     */
    public static float findMin(float[] array) {
        float min = Float.MAX_VALUE;
        for (int i = 0; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    /**
     * Finds maximal element in array
     *
     * @param array array
     * @return Minimum Value
     */
    public static float findMax(float[] array) {
        float max = Float.MIN_VALUE;
        for (int i = 0; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    /**
     * Finds maximal abs element in array
     *
     * @param array array
     * @return Minimum Value
     */
    public static float findMaxAbs(float[] array) {
        float max = 0;
        for (int i = 0; i < array.length; i++) {
            if (Math.abs(array[i]) > max) {
                max = array[i];
            }
        }
        return max;
    }


    public static float[] product(float[] v1, float[] v2) {
        if (v1.length != v2.length) {
            throw new IllegalArgumentException("Arrays lengths are different");
        }
        float[] out = new float[v1.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = v1[i] * v2[i];
        }
        return out;
    }

    public static void multiply(float[] v1, float[] v2) {
        if (v1.length != v2.length) {
            throw new IllegalArgumentException("Arrays lengths are different");
        }
        for (int i = 0; i < v1.length; i++) {
            v1[i] = v1[i] * v2[i];
        }
    }

    public static void divideVectors(float[] v1, float[] v2) {
        if (v1.length != v2.length) {
            throw new IllegalArgumentException("Arrays lengths are different");
        }
        for (int i = 0; i < v1.length; i++) {
            if (v2[i] != 0) {
                v1[i] /= v2[i];
            }
        }
    }


    public static float[] difference(float[] v1, float[] v2) {
        if (v1.length != v2.length) {
            throw new IllegalArgumentException("Arrays lengths are different");
        }
        float[] out = new float[v1.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = v1[i] - v2[i];
        }
        return out;
    }

    public static float module(float[] vector) {
        float out = 0;
        for (int i = 0; i < vector.length; i++) {
            out += vector[i] * vector[i];
        }
        return (float) Math.sqrt(out);
    }


    /**
     * Finds index in array with min value
     *
     * @param array
     * @return
     */
    public static int findIndexWithMinValue(float[] array) {
        return findIndexWithMinValue(array, 0, array.length - 1);
    }


    /**
     * Finds index in array with min value
     *
     * @param array
     * @param startIndex
     * @param endIndex
     * @return
     */
    public static int findIndexWithMinValue(float[] array, int startIndex, int endIndex) {
        int outIndex = 0;
        float minValue = array[0];
        for (int i = startIndex; i <= endIndex; i++) {
            if (array[i] < minValue) {
                minValue = array[i];
                outIndex = i;
            }
        }
        return outIndex;
    }

    public static int findIndexWithMaxValue(int[] array) {
        return findIndexWithMaxValue(HelperArrays.getIntegerAsFloat(array));
    }

    public static int findIndexWithMaxValue(float[] array) {
        return findIndexWithMaxValue(array, 0, array.length);
    }

    public static int findIndexWithMaxValue(float[] array, int startIndex, int endIndex) {
        float max = Float.MIN_VALUE;
        int index = 0;
        for (int i = startIndex; i < endIndex; i++) {
            if (array[i] > max) {
                max = array[i];
                index = i;
            }
        }
        return index;
    }


    public static float findInterpolatedMax(float[] data, int maxIntIndex) {
        float step = 0.01f;
        float[] interpolatedData = new float[200];
        for (int i = -100; i < 100; i++) {
            interpolatedData[i + 100] = AOToolkit.interpolate3(data, maxIntIndex + i * step);
        }
        float maxValue = findIndexWithMaxValue(interpolatedData);

        return maxIntIndex + (maxValue - 100) / 100;
    }

    public static int findIndexValue(float[] array, float value) {
        int out = -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return out;
    }

    /**
     * Calculates average vector
     *
     * @param data
     * @param startIndex
     * @param endIndex
     * @return
     */
    public static float[] average(float[][] data, int startIndex, int endIndex) {
        if(data.length == 0){
            return  new float[]{};
        }
        float[] out = new float[data[startIndex].length];
        for (int i = startIndex; i < endIndex; i++) {
            for (int j = 0; j < out.length; j++) {
                out[j] += data[i][j] / (endIndex - startIndex + 1);
            }
        }
        return out;
    }


    /**
     * Calculates mean value of float array
     *
     * @param data float values
     * @return Mean Value
     */
    public static float calculateMean(float[] data) {
        float sum = 0;
        for (int i = 0; i < data.length; i++) {
            sum += data[i];
        }
        return sum / data.length;
    }

    /**
     * Calculates mean value of float array
     *
     * @param data float values
     * @return Mean Value
     */
    public static float calculateMean(float[] data, int startIndex, int endIndex) {
        float sum = 0;
        for (int i = startIndex; i <= endIndex; i++) {
            sum += data[i];
        }
        return sum / (endIndex - startIndex + 1);
    }


    public static float[] calculateMeanAndStandardDeviation(float[] data) {
        float sumMean = 0;
        float sumDev = 0;
        float mean, dev;
        int count = 0;

        for (int i = 0; i < data.length; i++) {
            sumMean += data[i];
            count++;
        }
        mean = sumMean / count;

        for (int i = 0; i < data.length; i++) {
            sumDev += (mean - data[i]) * (mean - data[i]);
        }
        dev = (float) Math.sqrt(sumDev / count);

        float[] out = new float[2];
        out[0] = mean;
        out[1] = dev;

        return out;
    }


    public static float[] calculateMeanAndStandardDeviation(float[][] data) {
        float sumMean = 0;
        float sumDev = 0;
        float mean, dev;
        int count = 0;

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                sumMean += data[i][j];
                count++;
            }
        }
        mean = sumMean / count;

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                sumDev += (mean - data[i][j]) * (mean - data[i][j]);
            }
        }
        dev = (float) Math.sqrt(sumDev / count);

        float[] out = new float[2];
        out[0] = mean;
        out[1] = dev;

        return out;
    }


    public static float[][] calculateMeanAndStandardDeviationVectors(float[][] vectors) {
        float[][] out = new float[2][vectors[0].length];
        for (int i = 0; i < vectors[0].length; i++) {
            float[] inputVector = new float[vectors.length];
            for (int j = 0; j < inputVector.length; j++) {
                inputVector[j] = vectors[j][i];
            }
            float[] data = calculateMeanAndStandardDeviation(inputVector);
            out[0][i] = data[0];
            out[1][i] = data[1];

        }
        return out;
    }


    /**
     * All negative values in array are ignored
     *
     * @param data
     * @return
     */
    public static float[] calculateMeanAndStandardDeviationIgnoreNegatives(float[] data) {
        float sumMean = 0;
        float sumDev = 0;
        float mean, dev;
        int count = 0;

        for (int i = 0; i < data.length; i++) {
            if (data[i] > 0) {
                sumMean += data[i];
                count++;
            }
        }

        if (count == 0) {
            return new float[2];
        }

        mean = sumMean / count;

        for (int i = 0; i < data.length; i++) {
            if (data[i] > 0) {
                sumDev += (mean - data[i]) * (mean - data[i]);
            }
        }
        dev = (float) Math.sqrt(sumDev / count);

        float[] out = new float[2];
        out[0] = mean;
        out[1] = dev;

        return out;
    }


    /**
     * Calculates mean value of the given vectors
     *
     * @param vectors input vectors
     * @return mean value
     */
    public static float[] getMeanVector(float[][] vectors) {
        float[] out = new float[vectors[0].length];
        for (int i = 0; i < vectors.length; i++) {
            for (int j = 0; j < vectors[0].length; j++) {
                out[j] += vectors[i][j];
            }
        }
        //Divide by the number of vectors
        for (int j = 0; j < out.length; j++) {
            out[j] /= vectors.length;
        }
        return out;
    }

    /**
     * transforms array of floats to array of doubles
     *
     * @param in
     * @return
     */
    public static double[] getFloatAsDouble(float[] in) {
        double[] out = new double[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i];
        }
        return out;
    }


    /**
     * transforms array of floats to array of doubles
     *
     * @param in
     * @return
     */
    public static float[] getShortAsFloat(short[] in) {
        float[] out = new float[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i];
        }
        return out;
    }

    /**
     * transforms array of doubles to array of floats
     *
     * @param in
     * @return
     */
    public static float[] getDoubleAsFloat(double[] in) {
        float[] out = new float[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = (float) in[i];
        }
        return out;
    }


    public static float[][] getDoubleAsFloat(double[][] in) {
        float[][] out = new float[in.length][];
        for (int i = 0; i < in.length; i++) {
            out[i] = getDoubleAsFloat(in[i]);
        }
        return out;
    }

    public static int[] getFloatAsInteger(float[] in) {
        int[] out = new int[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = Math.round(in[i]);
        }
        return out;
    }

    public static float[] getIntegerAsFloat(int[] in) {
        float[] out = new float[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = (float) in[i];
        }
        return out;
    }

    public static short[] getFloatAsShort(float[] in) {
        short[] out = new short[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = (short) Math.round(in[i]);
        }
        return out;
    }


    public static double[][] getFloatAsDouble(float[][] in) {
        double[][] out = new double[in.length][];
        for (int i = 0; i < in.length; i++) {
            out[i] = getFloatAsDouble(in[i]);
        }
        return out;
    }

    public static Complex[] getFloatAsComplex(float[] in) {
        Complex[] out = new Complex[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = new Complex(in[i], 0);
        }
        return out;
    }

    /**
     * Calculates Pearson's correlation
     *
     * @param x           x
     * @param y           y
     * @param startIndexX index from x-array to start with
     * @param startIndexY index from y-array to start with
     * @param length      length
     * @return correlation value
     */
    public static float correlation(float[] x, float[] y, int startIndexX, int startIndexY, int length) {
        float[] x1 = new float[length];
        float[] y1 = new float[length];
        System.arraycopy(x, startIndexX, x1, 0, length);
        System.arraycopy(y, startIndexY, y1, 0, length);
        return correlation(x1, y1);
    }

    /**
     * Calculates Pearson's correlation
     *
     * @param x x
     * @param y y
     * @return
     */
    public static float correlation(float[] x, float[] y) {
        float result;
        float sum_sq_x = 0;
        float sum_sq_y = 0;
        float sum_coproduct = 0;
        float mean_x = x[0];
        float mean_y = y[0];
        for (int i = 2; i < x.length + 1; i += 1) {
            float sweep = ((float) i - 1) / i;
            float delta_x = x[i - 1] - mean_x;
            float delta_y = y[i - 1] - mean_y;
            sum_sq_x += delta_x * delta_x * sweep;
            sum_sq_y += delta_y * delta_y * sweep;
            sum_coproduct += delta_x * delta_y * sweep;
            mean_x += delta_x / i;
            mean_y += delta_y / i;
        }
        float pop_sd_x = (float) Math.sqrt(sum_sq_x / x.length);
        float pop_sd_y = (float) Math.sqrt(sum_sq_y / x.length);
        float cov_x_y = sum_coproduct / x.length;
        if ((pop_sd_x * pop_sd_y) != 0) {
            result = cov_x_y / (pop_sd_x * pop_sd_y);
            return result;
        } else {
            return 0;
        }
    }


    /**
     * Transformation is performed bt subtraction or addition of %range% to the value
     *
     * @param value value
     * @param range range
     * @return The return value is in the range [0; range - 1]
     */
    public static int transformIntValueToBaseRange(int value, int range) {
        while (value < 0) {
            value += range;
        }
        while (value >= range) {
            value -= range;
        }
        return value;
    }


    /**
     * Normalized Autocorrelation function
     *
     * @param data
     * @param windowLength
     * @return
     */
    public static float[] autoCorrelation(float[] data, int windowLength) {
        float[] autoCorrFunc = new float[data.length];
        float sum, sumAbs;

        for (int i = 0; i < data.length; i++) {
            sum = 0;
            sumAbs = 0;
            for (int j = 0; j < Math.min(j + windowLength, data.length - i); j++) {
                sum += data[j] * data[j + i];
                sumAbs += data[j] * data[j];
            }
            autoCorrFunc[i] = sum / sumAbs;
        }
        return autoCorrFunc;
    }

    /**
     * Normalized Soft Autocorrelation function
     *
     * @param data
     * @param windowLength
     * @return
     */
    public static float[] autoCorrelationSoft(float[] data, int windowLength) {
        float[] autoCorrFunc = new float[data.length];
        float sum, sumAbs;

        for (int i = 0; i < data.length; i++) {
            sum = 0;
            sumAbs = 0;
            for (int j = 0; j < Math.min(j + windowLength, data.length - i); j++) {
                sum += Math.abs(data[j] - data[j + i]);
                sumAbs += Math.abs(data[j]);
            }
            autoCorrFunc[i] = sum / sumAbs;
        }
        return autoCorrFunc;
    }


    public static float[] autocorrelationFull(float[] samples, int windowLength) {
        float[] nom, denom, result;
        nom = autoCorrelation(samples, windowLength);
        denom = autoCorrelationSoft(samples, windowLength);
        result = divide(nom, denom);
        return result;
    }


    //Difference Function intoduced by YIN

    public static float[] differenceFunction(float[] data, int windowLength) {
        float[] diffFunction = new float[data.length];
        float sum;
        for (int t = 0; t < data.length; t++) {
            sum = 0;
            for (int j = 0; j < Math.min(j + windowLength, data.length - t); j++) {
                sum += Math.pow(data[j] - data[j + t], 2);
            }
            diffFunction[t] = sum;
        }
        return diffFunction;
    }

    //Cumulative Mean Normalized Difference Function introduced by YIN

    public static float[] cumulativeMeanNormalizedDifferenceFunction(float[] diffFunc) {
        float[] CMDdiffFunction = new float[diffFunc.length];
        float sum;
        CMDdiffFunction[0] = 1;
        for (int t = 1; t < diffFunc.length; t++) {
            sum = 0;
            for (int j = 0; j < t; j++) {
                sum += diffFunc[j];
            }

            CMDdiffFunction[t] = diffFunc[t] / (sum / t);
        }
        return CMDdiffFunction;
    }


    public static float[] divide(float[] nom, float[] denom) {
        float[] difference = new float[nom.length];
        for (int i = 0; i < nom.length; i++) {
            if (denom[i] != 0) {
                difference[i] = nom[i] / denom[i];
            } else {
                difference[i] = nom[i];
            }
        }
        return difference;
    }

    public static float[] subtract(float[] dividend, float[] divisor) {
        float[] difference = new float[dividend.length];
        for (int i = 0; i < dividend.length; i++) {
            difference[i] = dividend[i] - divisor[i];
        }
        return difference;
    }

    public static float[] subtract(float[] dividend, float divisor) {
        float[] difference = new float[dividend.length];
        for (int i = 0; i < dividend.length; i++) {
            difference[i] = dividend[i] - divisor;
        }
        return difference;
    }

    public static float[] subtractKeepPositiveValues(float[] dividend, float divisor) {
        float[] difference = new float[dividend.length];
        for (int i = 0; i < dividend.length; i++) {
            difference[i] = Math.max(dividend[i] - divisor, 0);
        }
        return difference;
    }

    public static float[] add(float[] in1, float[] in2) {
        if (in1.length != in2.length) {
            try {
                throw new CRSException("Arrays dimensions are different");   //TODO throw exception out of the method
            } catch (CRSException e) {
                logger.error("Arrays dimensions are different");
                logger.error(Helper.getStackTrace(e));
            }
        }
        float[] result = new float[in1.length];
        for (int i = 0; i < in1.length; i++) {
            result[i] = in1[i] + in2[i];
        }
        return result;
    }

    public static float[] subtractPhasesAbs(float[] dividend, float[] divisor) {
        float[] difference = new float[dividend.length];
        for (int i = 0; i < dividend.length; i++) {
            difference[i] = Math.min(Math.min(Math.abs(dividend[i] - divisor[i]), Math.abs(dividend[i] + 2 * (float) Math.PI - divisor[i])), Math.abs(dividend[i] - 2 * (float) Math.PI - divisor[i]));
        }
        return difference;
    }


    /**
     * Calculates distance between 2 vectors
     *
     * @param vec1
     * @param vec2
     * @return
     */
    public static float calculateDistance(float[] vec1, float[] vec2) {
        float distance = 0;
        float abs1 = 0;
        float abs2 = 0;
        for (int i = 0; i < vec1.length; i++) {
            distance += vec1[i] * vec2[i];
            abs1 += vec1[i] * vec1[i];
            abs2 += vec2[i] * vec2[i];
        }

        //secure division by zero
        if (abs1 != 0 && abs2 != 0) {
            distance /= (Math.sqrt(abs1) * Math.sqrt(abs2));
        }
        return distance;
    }

    /**
     * Returns the Kullback-Leibler divergence between the
     * two specified distributions, which must have the same
     * number of elements.  This is defined as
     * the sum over all <code>i</code> of
     * <code>dist[i] * Math.log(dist[i] / reference[i])</code>.
     * Note that this value is not symmetric; see
     * <code>symmetricKL</code> for a symmetric variant.
     */
    public static float KullbackLeibler(float[] dist, float[] reference) {
        float distance = 0;

        checkLengths(dist, reference);

        for (int i = 0; i < dist.length; i++) {
            if (dist[i] > 0 && reference[i] > 0)
                distance += dist[i] * Math.log(dist[i] / reference[i]);
        }
        return distance;
    }

    /**
     * Throws an <code>IllegalArgumentException</code> if the two arrays are not of the same length.
     */
    protected static void checkLengths(float[] dist, float[] reference) {
        if (dist.length != reference.length)
            throw new IllegalArgumentException("Arrays must be of the same length");
    }


    /**
     * Calculates distance between 2 vectors without normalizing
     *
     * @param vec1
     * @param vec2
     * @return
     */
    public static float calculateDistanceNoNormalization(float[] vec1, float[] vec2) {
        float distance = 0;
        for (int i = 0; i < vec1.length; i++) {
            distance += vec1[i] * vec2[i];
        }

        return distance;
    }


    /**
     * Normalizes vector so that all values are in the range [0, 1]
     *
     * @param samples
     * @return
     */
    public static float[] normalizeVector(float[] samples) {
        float[] out = new float[samples.length];
        float maxValue = findMaxAbs(samples);
        if (maxValue != 0) {
            for (int i = 0; i < samples.length; i++) {
                out[i] = samples[i] / Math.abs(maxValue);
            }
        }
        return out;
    }


    public static float[][] normalizeVectors(float[][] vectors) {
        float[][] out = new float[vectors.length][];
        for (int i = 0; i < vectors.length; i++) {
            out[i] = normalizeVector(vectors[i]);
        }
        return out;
    }

    public static float[] emphasizeVector(float[] samples, float factor) {
        float[] out = new float[samples.length];
        for (int i = 0; i < samples.length; i++) {
            out[i] = samples[i] * factor;
        }
        return out;
    }

    /**
     * Given input array of float data, produces output array of numberOfPeaks indexes with max values. Distance between each of them
     * should be at least  minDistance
     *
     * @param data               data
     * @param endIndex           endIndex
     * @param numberOfPeaks      numberOfPeaks
     * @param minDistance        minDistance
     * @param isSemitoneDistance isSemitoneDistance
     * @return indexes
     */
    public static int[] searchPeakIndexes(float[] data, int startIndex, int endIndex, int numberOfPeaks, float minDistance, boolean isSemitoneDistance) {
        int[] outIndexes = new int[numberOfPeaks];
        float[] maxValues = new float[numberOfPeaks];


        boolean isSetValue;
        for (int i = startIndex; i < endIndex; i++) {
            isSetValue = false;
            int indexWithMinValue = findIndexWithMinValue(maxValues);
            if (data[i] > maxValues[indexWithMinValue]) {
                for (int k = 0; k < outIndexes.length; k++) {
                    if (outIndexes[k] != 0 && getDistanceAbs(i, outIndexes[k], isSemitoneDistance) < minDistance) { //Here we can not to take into consideration frequency resolution - the result is the same
                        if (data[i] > maxValues[k]) {
                            maxValues[k] = data[i];
                            outIndexes[k] = i;
                            isSetValue = true;
                            break;
                        } else {
                            isSetValue = false;
                        }
                    }
                }
                if (!isSetValue) {
                    boolean isOK = true;
                    for (int m = 0; m < numberOfPeaks; m++) {
                        if (getDistanceAbs(i, outIndexes[m], isSemitoneDistance) < minDistance) {
                            isOK = false;
                        }
                    }
                    if (isOK) {
                        maxValues[indexWithMinValue] = data[i];
                        outIndexes[indexWithMinValue] = i;
                    }
                }
            }
        }


        //Make index refinement
        for (int index_ = 0; index_ < outIndexes.length; index_++) {
            for (int index2 = 0; index2 < outIndexes.length; index2++) {
                if (index_ != index2 && getDistanceAbs(outIndexes[index_], outIndexes[index2], isSemitoneDistance) < minDistance) {
                    if (maxValues[index_] > maxValues[index2]) {
                        outIndexes[index2] = 0;
                        maxValues[index2] = 0;
                    } else {
                        outIndexes[index_] = 0;
                        maxValues[index_] = 0;
                    }
                }
            }
        }


        //Detele all bins with zero energy
        int numberOfNONZeros = 0;
        for (int i = 0; i < maxValues.length; i++) {
            if (maxValues[i] > 0 && outIndexes[i] > 0) {
                numberOfNONZeros++;
            }
        }

        int[] outIndexesFinal = new int[numberOfNONZeros];
        int counter = 0;
        for (int i = 0; i < maxValues.length; i++) {
            if (maxValues[i] > 0 && outIndexes[i] > 0) {
                outIndexesFinal[counter++] = outIndexes[i];
            }
        }

        return outIndexesFinal;
    }


    private static double getDistanceAbs(int index1, int index2, boolean isSemitone) {
        if (isSemitone) {
            return Helper.getSemitoneDistanceAbs(index1, index2);
        } else {
            return Math.abs(index1 - index2);
        }
    }


    public static int[] searchPeakIndexes(float[] data, int startIndex, int numberOfPeaks, float minDistanceInSemitoneScale) {
        return searchPeakIndexes(data, startIndex, data.length, numberOfPeaks, minDistanceInSemitoneScale, true);
    }


    /**
     * Calculates delta coffeicient by linear regression
     *
     * @param inVectors
     * @param regressionWindow total widow length is 2 * regressionWindow + 1
     * @return
     */
    public static float[][] getDeltaCoefficients(float[][] inVectors, int regressionWindow) {
        float[][] out = new float[inVectors.length][inVectors[0].length];
        for (int frameIndex = regressionWindow; frameIndex < out.length - regressionWindow; frameIndex++) {
            for (int featureIndex = 0; featureIndex < inVectors[0].length; featureIndex++) {
                float[] window = new float[2 * regressionWindow + 1];
                //Fill in the window
                for (int k = -regressionWindow; k <= regressionWindow; k++) {
                    window[k + regressionWindow] = inVectors[frameIndex + k][featureIndex];
                }
                //Now calculate delta coefficient
                float nom = 0;
                float denom = 0;
                for (int i = 1; i <= regressionWindow; i++) {
                    nom += i * (window[regressionWindow + i] - window[regressionWindow - i]);
                    denom += i * i;
                }
                out[frameIndex][featureIndex] = nom / (2 * denom);
            }
        }
        return out;
    }


    public static float[][] addDeltaCoefficients(float[][] inVectors, int regressionWindow) {
        float[][] deltas = getDeltaCoefficients(inVectors, regressionWindow);
        return concatColumnWise(inVectors, deltas);
    }


    public static float[][] halfWaveRectification(float[][] inData) {
        float[][] out = new float[inData.length][];
        for (int i = 0; i < inData.length; i++) {
            out[i] = halfWaveRectification(inData[i]);
        }
        return out;

    }

    public static float[] halfWaveRectification(float[] inData) {
        float[] out = new float[inData.length];
        for (int i = 0; i < inData.length; i++) {
            if (inData[i] <= 0) {
                out[i] = 0;
            } else {
                out[i] = inData[i];
            }
        }
        return out;
    }


    public static int getIndexInArray(Object[] array, Object instance) {
        if (instance != null) {
            for (int i = 0; i < array.length; i++) {
                Object object = array[i];
                if (instance.equals(object)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static String arrayToString(float[][] array) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
                out.append(array[i][j]).append(" ");
            }
            out.append("\n");
        }
        return out.toString();
    }


    public static void save2DArray(float[][] data, String filePath) {
        try {
            FileWriter writer = new FileWriter(filePath);
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[0].length; j++) {
                    writer.write(String.format("%5.4f ", data[i][j]));
                }
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static float[][] transposeMatrix(float[][] inMatrix) {
        float[][] out = new float[inMatrix[0].length][inMatrix.length];
        for (int i = 0; i < inMatrix.length; i++) {
            for (int j = 0; j < inMatrix[0].length; j++) {
                out[j][i] = inMatrix[i][j];
            }
        }
        return out;
    }


    /**
     * returns segmentation indexes that are greater than threshold
     *
     * @param function
     * @param threshold
     * @return
     */
    public static int[] getSegmentationIndexes(float[] function, float threshold) {
        float[] subtracted = subtractKeepPositiveValues(function, threshold);
        int[] indexes = searchPeakIndexes(subtracted, 0, subtracted.length, subtracted.length, 10, false);
        return indexes;
    }


    /**
     * Performs vertical segmentation of a vector sequence. the segmentation is performed according to indices
     *
     * @param inVectors
     * @param indices
     * @return
     */
    public static float[][] segmentVectorsVertically(float[][] inVectors, int[] indices) {
        indices = HelperArrays.concat(indices, new int[]{inVectors[0].length});
        float[][] outVectors = new float[inVectors.length][indices.length];
        int startIndex = 0;
        for (int endIndexInIndicesArray = 0; endIndexInIndicesArray < indices.length; endIndexInIndicesArray++) {
            int endIndex = indices[endIndexInIndicesArray];
            for (int j = startIndex; j < endIndex; j++) {
                for (int k = 0; k < inVectors.length; k++) {
                    outVectors[k][endIndexInIndicesArray] += inVectors[k][j] / (endIndex - startIndex);
                }
            }
            startIndex = endIndex;
        }
        return outVectors;
    }


    public static boolean[][] inverseBooleans(boolean[][] inbooleans) {
        boolean[][] outBooleans = new boolean[inbooleans.length][inbooleans[0].length];
        for (int i = 0; i < outBooleans.length; i++) {
            for (int j = 0; j < outBooleans[0].length; j++) {
                outBooleans[i][j] = !inbooleans[i][j];
            }
        }
        return outBooleans;
    }




}
