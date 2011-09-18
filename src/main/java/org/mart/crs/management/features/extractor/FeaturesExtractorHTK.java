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

package org.mart.crs.management.features.extractor;

import org.mart.crs.config.ExecParams;
import org.mart.crs.core.AudioReader;
import org.mart.crs.core.pcp.spectral.PCP;
import org.mart.crs.management.features.FeaturesManager;
import org.mart.crs.management.label.chord.Root;
import org.mart.crs.utils.helper.HelperArrays;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 15-Mar-2010 20:30:04
 * @author: Maksim Khadkevich
 */
public abstract class FeaturesExtractorHTK {

    protected String songFilePath;
    protected AudioReader audioReader;

    protected ExecParams execParams;

    /**
     * these variables are used to store vectors for the whole song
     */
    protected List<float[][]> globalVectors;

    protected abstract void extractGlobalFeatures(double refFrequency);

    public abstract int getVectorSize();


    public void setExecParams(ExecParams execParams) {
        this.execParams = execParams;
    }

    public float[][] extractFeatures(double startTime, double endTime, double refFrequency, Root chordFrom) {
        List<float[][]> globalVector = getGlobalVector(refFrequency);
        List<float[][]> segment = extractTimeSegmentFromGlobalVector(globalVector, startTime, endTime);
        List<float[][]> rotated = rotateFeatures(segment, chordFrom);
        return postProcess(rotated);
    }

    public float[][] extractFeatures(double refFrequency, Root chordFrom) {
        List<float[][]> rotated = rotateFeatures(getGlobalVector(refFrequency), chordFrom);
        return postProcess(rotated);
    }


    public float[][] extractFeatures(double startTime, double endTime, double refFrequency) {
        List<float[][]> globalVector = getGlobalVector(refFrequency);
        List<float[][]> segment = extractTimeSegmentFromGlobalVector(globalVector, startTime, endTime);
        return postProcess(segment);
    }

    public float[][] extractFeatures(double refFrequency) {
        List<float[][]> vectorList = getGlobalVector(refFrequency);
        return postProcess(vectorList);
    }

    public float[][] postProcess(List<float[][]> vectorList) {
        float[][] concatenated = HelperArrays.concatColumnWise(vectorList);
        return concatenated;
    }


    protected List<float[][]> getGlobalVector(double refFrequency) {
        if (globalVectors == null) {
            globalVectors = new ArrayList<float[][]>();
            extractGlobalFeatures(refFrequency);
            System.gc();
            List<float[][]> newGlobalVectors = new ArrayList<float[][]>();
            for (float[][] globalVector : globalVectors) {
                if (execParams.isToNormalizeFeatureVectors) {
                    newGlobalVectors.add(HelperArrays.normalizeVectors(globalVector));
                } else {
                    newGlobalVectors.add(globalVector);
                }
                if (execParams.extractDeltaCoefficients) {
                    newGlobalVectors.add(HelperArrays.getDeltaCoefficients(globalVector, execParams.regressionWindowForDeltaCoefficients));
                }
                globalVectors = newGlobalVectors;
            }
        }
        return globalVectors;
    }

    protected List<float[][]> extractTimeSegmentFromGlobalVector(List<float[][]> globalVector, double startTime, double endTime) {
        List<float[][]> outList = new ArrayList<float[][]>();

        int startIndex = FeaturesManager.getIndexForTimeInstant(startTime, execParams);
        int endIndex = FeaturesManager.getIndexForTimeInstant(endTime, execParams);
        if (endIndex >= globalVector.get(0).length) {
            endIndex = globalVector.get(0).length - 1;
        }

        for (float[][] aGlobalVector : globalVectors) {
            if (startIndex >= endIndex) {
                continue;
            }
            float[][] out = new float[endIndex - startIndex + 1][];
            for (int i = startIndex; i <= endIndex; i++) {
                out[i - startIndex] = aGlobalVector[i];
            }
            outList.add(out);
        }
        return outList;
    }


    public float[][] rotateFeatures(float[][] feature, Root chordNameFrom) {
        return rotateFeaturesStatic(feature, chordNameFrom);
    }


    public static float[][] rotateFeaturesStatic(float[][] feature, Root chordNameFrom) {
        if (chordNameFrom == null || chordNameFrom.equals(Root.C)) {
            return feature;
        }
        feature = PCP.rotatePCP(feature, 1, chordNameFrom, Root.C);
        return feature;
    }

    public List<float[][]> rotateFeatures(List<float[][]> feature, Root chordNameFrom) {
        return rotateFeaturesStatic(feature, chordNameFrom);
    }


    public static List<float[][]> rotateFeaturesStatic(List<float[][]> feature, Root chordNameFrom) {
        List<float[][]> out = new ArrayList<float[][]>();
        for (float[][] aFeature : feature) {
            out.add(rotateFeaturesStatic(aFeature, chordNameFrom));
        }
        return out;
    }


    public void initialize(String songFilePath) {
        this.globalVectors = null;
        this.songFilePath = songFilePath;
    }

    public void initialize(AudioReader audioReader) {
        this.globalVectors = null;
        this.songFilePath = audioReader.getFilePath();
        this.audioReader = audioReader;
        audioReader.setStoreSamplesInMemory(true);
    }


    public float getDuration() {
        return -1;
    }

}
