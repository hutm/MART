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

package org.mart.crs.core.pcp;

import org.mart.crs.core.AudioReader;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrumHarmonicPart;
import org.mart.crs.management.config.Configuration;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @version 1.0 6/26/12 7:50 PM
 * @author: Hut
 */
public class PCPTest {

    @Test
    public void testReassignedSpectrumNewMethod(){
        String audioFilePath = this.getClass().getResource("/audio/chord.wav").getPath();
        AudioReader reader = new AudioReader(audioFilePath, 4000);
        ReassignedSpectrumHarmonicPart reassignedSpectrum = new ReassignedSpectrumHarmonicPart(reader, 1024, 2, 0.5f, 0.4f);
        PCP pcp =  PCP.getPCP(PCP.BASIC_ALG, Configuration.REFERENCE_FREQUENCY, 1, 3, false, Configuration.START_NOTE_FOR_PCP_UNWRAPPED, Configuration.END_NOTE_FOR_PCP_UNWRAPPED, 1);
        pcp.initReassignedSpectrum(reassignedSpectrum);
        float[][] unwrapped = pcp.getPCPUnwrapped();
        Assert.assertTrue(unwrapped.length > 2);
    }



}
