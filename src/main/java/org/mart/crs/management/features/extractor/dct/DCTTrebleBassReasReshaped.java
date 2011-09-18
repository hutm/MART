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

import org.mart.crs.utils.DCT;
import org.mart.crs.utils.helper.HelperArrays;

import java.util.List;

/**
 * @version 1.0 7/5/11 7:07 PM
 * @author: Hut
 */
public class DCTTrebleBassReasReshaped extends DCTTrebleBassReas {

    @Override
    public float[][] postProcess(List<float[][]> vectorList) {
        float[][] concatenated = HelperArrays.concatColumnWise(vectorList);

        float[][] reshaped = new float[concatenated.length][concatenated[0].length];
        for(int i = 0; i < concatenated.length; i++){
            for(int j = 0; j < concatenated[0].length/2; j++){
                reshaped[i][2*j] = concatenated[i][j];
                reshaped[i][2*j+1] = concatenated[i][concatenated[0].length/2 + j];
            }
        }

        return DCT.applyInverseDCT(concatenated, NUMBER_OF_DCT_COEFF, 1);
    }
}
