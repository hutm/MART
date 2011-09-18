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

package org.mart.crs.core.spectrum;

import org.mart.crs.config.ExecParams;
import org.mart.crs.core.AudioReader;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrumPercussivePart;

/**
 * @version 1.0 1/28/11 2:01 PM
 * @author: Hut
 */
public class ReassignedPercussivePartFromMatlab {

    protected float[][] magnitudeData;

    public ReassignedPercussivePartFromMatlab(String filePath, float samplingRate, int windowLength, int windowType, float overlap, float tolerance) {
        AudioReader reader = new AudioReader(filePath, samplingRate);
        ReassignedSpectrumPercussivePart reassignedSpectrumPercussivePart = new ReassignedSpectrumPercussivePart(reader, windowLength, windowType, overlap, tolerance, ExecParams._initialExecParameters);
        this.magnitudeData = reassignedSpectrumPercussivePart.getMagSpec();
    }

    public float[][] getMagnitudeData() {
        return magnitudeData;
    }
}
