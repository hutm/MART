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

package org.mart.crs.management.features.extractor.unused;

import org.mart.crs.core.pcp.spectral.PCP;
import org.mart.crs.management.label.chord.ChordType;
import org.mart.crs.management.label.chord.Root;

/**
 * @version 1.0 22-Sep-2010 16:25:01
 * @author: Hut
 */
public class UnwrappedReasOctaveShift extends UnwrapReas {


    @Override
    public int getVectorSize() {
        //TODO add here what is needed
        return super.getVectorSize();
    }


    public float[][] extractFeaturesRotated(double startTime, double endTime, double refFrequency, Root chordFrom) {
        float[] samples = audioReader.getSamples((float)startTime, (float)endTime);
        PCP pcp = producePCP(samples, refFrequency);
        float[][] pcpRotated = PCP.rotatePCP(pcp.getPCPUnwrapped(), 1, chordFrom, Root.C, false);
        pcp.setPcpUnwrapped(pcpRotated);
        return cutPCPUnwrapped(pcp);
    }


    @Override
    public float[][] rotateFeatures(float[][] feature, Root chordNameFrom) {
        if (chordNameFrom.equals(ChordType.NOT_A_CHORD.getName()) || chordNameFrom.equals("")) {
            return feature;
        }
        float[][] output = PCP.rotatePCP(feature, 1, chordNameFrom, Root.C, false);
        return output;
    }


}
