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

package org.mart.crs.management.features.extractor.dct;

import org.mart.crs.core.AudioReader;
import org.mart.crs.utils.helper.HelperArrays;

import java.util.List;

/**
 * @version 1.0 7/9/11 12:29 AM
 * @author: Hut
 */
public class DCTTrebleBassSubtructMean extends DCTTrebleBassReas {

    protected float[] meanVector;

    @Override
    public float[][] postProcess(List<float[][]> vectorList) {
        float[][] concatenated = super.postProcess(vectorList);
        float[] meanVector = getMeanVector();

        for (int i = 0; i < concatenated.length; i++) {
            concatenated[i] = HelperArrays.subtract(concatenated[i], meanVector);
        }

        return concatenated;
    }

    protected float[] getMeanVector() {
        if (meanVector == null) {
            float[][] concatenatedGlobal = super.postProcess(globalVectors);
            meanVector = HelperArrays.calculateMeanAndStandardDeviationVectors(concatenatedGlobal)[0];
        }
        return meanVector;
    }


    public void initialize(String songFilePath) {
        super.initialize(songFilePath);
        meanVector = null;
    }

    public void initialize(AudioReader audioReader) {
        super.initialize(audioReader);
        meanVector = null;
    }

}
