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

import org.mart.crs.management.features.extractor.chroma.TrebleBassReas;
import org.mart.crs.utils.DCT;

import java.util.List;

/**
 * @version 1.0 6/30/11 3:29 PM
 * @author: Hut
 */
public class DCTTrebleBassReas extends TrebleBassReas {

    public static final int NUMBER_OF_DCT_COEFF = 16;


    @Override
    public float[][] postProcess(List<float[][]> vectorList) {
        float[][] concatenated = super.postProcess(vectorList);
        return DCT.applyInverseDCT(concatenated, NUMBER_OF_DCT_COEFF, 1);
    }


    @Override
    public int getVectorSize() {
        return NUMBER_OF_DCT_COEFF;
    }
}
