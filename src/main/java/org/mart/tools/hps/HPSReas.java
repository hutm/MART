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

package org.mart.tools.hps;

import org.mart.crs.config.ExecParams;
import org.mart.crs.core.AudioReader;
import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrum;

/**
 * @version 1.0 3/24/11 10:31 PM
 * @author: Hut
 */
public class HPSReas extends HPS2 {

    protected ReassignedSpectrum reassignedSpectrum;

    public HPSReas(String audioFilePath) {
        super(audioFilePath);
    }

    @Override
    protected SpectrumImpl createSpecrumFromAurioReader(AudioReader reader) {
        ReassignedSpectrum spectrum = new ReassignedSpectrum(reader, ExecParams._initialExecParameters);
        return spectrum;
    }


    @Override
    protected boolean[][] getSeparationMatrixHarmonicPart(SpectrumImpl spectrum) {
        reassignedSpectrum = (ReassignedSpectrum) spectrum;
        reassignedSpectrum.getMagSpec();
        return reassignedSpectrum.getHarmonicComponentsMatrix();
    }

    @Override
    protected boolean[][] getSeparationMatrixPercussivePart() {
        return super.getSeparationMatrixPercussivePart();
//        reassignedSpectrum = (ReassignedSpectrum) spectrum;
//        reassignedSpectrum.getMagSpec();
//        return reassignedSpectrum.getPercussiveComponentsMatrix();
    }

    public static void main(String[] args) {
        String audioPath = "/home/hut/Documents/!audio/hps/225_BillieJean.wav";
//        String audioPath = "./data/audio/test.wav";
        HPSReas hpsReas = new HPSReas(audioPath);
        hpsReas.separateParts();
    }

}

