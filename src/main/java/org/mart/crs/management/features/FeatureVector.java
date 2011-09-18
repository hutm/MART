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

package org.mart.crs.management.features;

import org.mart.crs.utils.helper.HelperArrays;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.mart.crs.config.Settings.CHROMA_EXT;

/**
 * This class represents feature vectors data structure
 */
public class FeatureVector implements Serializable {

    private List<float[][]> vectors;

    private float duration;

    private int samplePeriod;

    private String additionalInfo;

    public FeatureVector(float[][] vectors, int samplePeriod) {
        this.vectors = new ArrayList<float[][]>();
        this.vectors.add(vectors);
        this.samplePeriod = samplePeriod;
    }

    public FeatureVector(List<float[][]> vectors, int samplePeriod) {
        this.vectors = vectors;
        this.samplePeriod = samplePeriod;
    }

    public List<float[][]> getVectors() {
        return vectors;
    }

    public List<float[][]> getNormalizedVectors() {
        List<float[][]> outList = new ArrayList<float[][]>();
        float[][] out = new float[0][];
        for (float[][] aVector:vectors) {
            out = new float[aVector.length][];
            for (int i = 0; i < aVector.length; i++) {
                out[i] = HelperArrays.normalizeVector(aVector[i]);
            }
            outList.add(out);
        }
        return outList;
    }

    public void setVectors(List<float[][]> vectors) {
        this.vectors = vectors;
    }

    public int getSamplePeriod() {
        return samplePeriod;
    }

    public void setSamplePeriod(int samplePeriod) {
        this.samplePeriod = samplePeriod;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public String getFileNameToStoreTestVersion(){
        String filenameToSave = new StringBuilder().append("0.00_").append(getDuration()).append("_").append(CHROMA_EXT).toString();
        return filenameToSave;
    }


    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
