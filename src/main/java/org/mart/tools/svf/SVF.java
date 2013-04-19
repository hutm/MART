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

import org.apache.log4j.Logger;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.MedianFilter;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;
import org.mart.tools.svf.extractor.SVFExtractor;
import org.mart.tools.svf.extractor.SVFFeatureExtractorInterface;


public class SVF {

    protected static Logger logger = CRSLogger.getLogger(SVF.class);


    //SVF Settings
    public static boolean USE_SUBTRACTION = true;   //Whether use subtraction or not
    public static int MEDIAN_FILTER_ORDER = 1;      //IF value is > 1, median filter is applied


    protected static final int defaultL = 9;
    protected int L;                        //Order of SVF

    protected float sampleRate; //SampleRate of the spectrum

    protected SVFFeatureExtractorInterface featureExtractor;

    float[] svfFunction;
    float[] svfFunctionNormalized;

    public SVF(float[][] data, float sampleRate) {
        this(data, sampleRate, defaultL, SVFExtractor.MelBandsEnergy);
    }


    public SVF(float[][] data, float sampleRate, int L) {
        this(data, sampleRate, L, SVFExtractor.MelBandsEnergy);
    }

    public SVF(float[][] data, float sampleRate, int L, SVFExtractor extractor) {
        this.sampleRate = sampleRate;
        this.L = L;
        try {
            this.featureExtractor = (SVFFeatureExtractorInterface) Class.forName(extractor.getClassName()).newInstance();
        } catch (InstantiationException e) {
            logger.error(Helper.getStackTrace(e));
        } catch (IllegalAccessException e) {
            logger.error(Helper.getStackTrace(e));
        } catch (ClassNotFoundException e) {
            logger.error(Helper.getStackTrace(e));
        }
        this.svfFunction = calculateSvfFunction(data);
    }


    public float[] calculateSvfFunction(float[][] data) {
        float[][] featureVecs = featureExtractor.extract(data, sampleRate);
        float[] svfFunction = calculateSVF(featureVecs);
        if (MEDIAN_FILTER_ORDER > 1) {
            svfFunction = MedianFilter.filter(svfFunction, MEDIAN_FILTER_ORDER);
        }
        return svfFunction;
    }

    private float[] calculateSVF(float[][] featureVecs) {

        float[] svf = new float[featureVecs.length];

        float[][] context = new float[2 * L + 1][featureVecs[0].length];
        for (int i = L; i <= featureVecs.length - L - 1; i++) {

            //Copy context for given center i
            for (int j = i - L; j <= i + L; j++) {
                System.arraycopy(featureVecs[j], 0, context[j - i + L], 0, featureVecs[j].length);
            }
            svf[i] = getSVFValue(context);

        }
        return svf;
    }


    public float getSVFValue(float[][] context) {
        double minValue = 1;
        double value;

        //Mean value for the whole context
        float[] meanAll = calculateAverage(context, 0, 2 * L);

        //subtract mean value from the whole context
        if (USE_SUBTRACTION) {
            for (int i = 0; i < context.length; i++) {
                context[i] = subtract(context[i], meanAll);
            }
        }

        //Normalize vectors of the context
        for (int i = 0; i < context.length; i++) {
            normalizeSVFVector(context[i]);
        }


        float[] meanLeft = calculateAverage(context, 0, L - 1);
        float[] meanRight = calculateAverage(context, L + 1, 2 * L);
        float[] slice;

        //Left context is constant
        for (int i = L + 1; i <= 2 * L; i++) {
            slice = calculateAverage(context, L + 1, i);
            value = calculateDistance(meanLeft, slice);
            if (value < minValue) {
                minValue = value;
            }
        }

        //Right context is constant
        for (int i = 0; i < L; i++) {
            slice = calculateAverage(context, L - 1 - i, L - 1);
            value = calculateDistance(meanRight, slice);
            if (value < minValue) {
                minValue = value;
            }
        }

        return (float) (1 - minValue) / 2;

    }


    public static float calculateDistance(float[] vec1, float[] vec2) {
//        return HelperArrays.KullbackLeibler(vec1, vec2);
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
            if (!USE_SUBTRACTION) {
                distance /= (Math.sqrt(abs1) * Math.sqrt(abs2));
            } else {
                distance /= vec1.length;
            }
        } else {
            return 2;
        }
        return distance;
    }


    private static float[] calculateAverage(float[][] data, int startIndex, int endIndex) {
        float[] averageValue = new float[data[0].length];

        for (int i = startIndex; i <= endIndex; i++) {
            for (int j = 0; j < averageValue.length; j++) {
                averageValue[j] += data[i][j];
            }
        }
        for (int j = 0; j < averageValue.length; j++) {
            averageValue[j] /= (endIndex - startIndex + 1);
        }

        return averageValue;
    }

    private static float[] subtract(float[] nom, float[] denom) {
        float[] difference = new float[nom.length];
        for (int i = 0; i < nom.length; i++) {
            difference[i] = nom[i] - denom[i];
        }
        return difference;
    }


    private static void normalizeSVFVector(float[] vector) {
        float sum = 0;
        for (int i = 0; i < vector.length; i++) {
            sum += vector[i] * vector[i];
        }
        sum = (float) Math.sqrt(sum / vector.length);
        for (int i = 0; i < vector.length; i++) {
            vector[i] /= sum;
        }
    }


    public float[] getSvfFunction() {
        return this.svfFunction;
    }


    public float[] getSVFFunctionNormalized() {
        if (!USE_SUBTRACTION) {
            return svfFunction;
        }
        if (svfFunctionNormalized == null) {
            svfFunctionNormalized = normalizeSVFFunction(this.svfFunction);
        }
        return svfFunctionNormalized;
    }

    public static float[] normalizeSVFFunction(float[] svfFunction) {
        float[] svfFunctionNormalized = new float[svfFunction.length];
        float max = HelperArrays.findMax(svfFunction);
        for (int i = 0; i < svfFunction.length; i++) {
            svfFunctionNormalized[i] = Math.max(0, (svfFunction[i] - 0.5f) / (max - 0.5f));
        }
        return svfFunctionNormalized;
    }


    public float getSampleRate() {
        return sampleRate;
    }
}
