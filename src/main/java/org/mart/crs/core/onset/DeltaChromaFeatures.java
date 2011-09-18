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

import org.mart.crs.core.pcp.spectral.PCP;
import org.mart.crs.utils.helper.HelperArrays;

/**
 * //TODO why it is not finished
 * @version 1.0 10/11/10 11:52
 * @author: Hut
 */
public class DeltaChromaFeatures {


    protected float[][] deltaChroma;

    protected float samplingRate;


    public DeltaChromaFeatures(PCP pcp, int regressionWindowForDeltas) {
        float[][] deltas = HelperArrays.getDeltaCoefficients(pcp.getPCPUnwrapped(), regressionWindowForDeltas);
        deltas = HelperArrays.halfWaveRectification(deltas);
        float[][] tempUnwrappedPCPData = pcp.getPCPUnwrapped();
        pcp.setPcpUnwrapped(deltas);
        this.deltaChroma = pcp.getPCP();
        samplingRate = pcp.getSampleRatePCP();
        pcp.setPcpUnwrapped(tempUnwrappedPCPData);
    }

    public float[][] getDeltaChroma() {
        return deltaChroma;
    }

    public float getSamplingRate() {
        return samplingRate;
    }
}
