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

package org.mart.crs.core.onset;

import org.mart.crs.core.pcp.PCP;
import org.mart.crs.utils.helper.HelperArrays;

/**
 * @version 1.0 21/11/10 21:34
 * @author: Hut
 */
public class OnsetDetectionDeltaChroma extends OnsetDetectionFunction {


    private PCP pcp;


    private int regressionWindowChroma;


    public OnsetDetectionDeltaChroma(PCP pcp, int regressionWindowChroma) {
        this.pcp = pcp;
        this.regressionWindowChroma = regressionWindowChroma;
    }


    @Override
    public void computeOnsetDetection() {
        DeltaChromaFeatures deltaChromaFeatures = new DeltaChromaFeatures(pcp, regressionWindowChroma);

        float[][] deltaChroma = deltaChromaFeatures.getDeltaChroma();
        this.detectionFunction = new float[deltaChroma.length];
        for (int frame = 0; frame < deltaChroma.length; frame++) {
            for (int bin = 0; bin < deltaChroma[frame].length; bin++) {
                detectionFunction[frame] += deltaChroma[frame][bin];
            }
        }
        detectionFunction = HelperArrays.normalizeVector(detectionFunction);
    }
}
