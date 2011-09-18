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

package org.mart.crs.management.features.extractor.tonalcentroid;

import org.mart.crs.management.features.extractor.chroma.TrebleReas;
import org.mart.crs.utils.TonalCentroid;

import java.util.List;

/**
 * @version 1.0 7/5/11 4:44 PM
 * @author: Hut
 */
public class TonalCentroidTrebleReas extends TrebleReas {


    @Override
    public float[][] postProcess(List<float[][]> vectorList) {
        float[][] concatenated = super.postProcess(vectorList);
        return TonalCentroid.getTonalCentroid(concatenated);
    }


    @Override
    public int getVectorSize() {
        return TonalCentroid.DIMENSION;
    }
}
