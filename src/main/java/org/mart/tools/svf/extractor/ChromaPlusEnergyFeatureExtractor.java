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

package org.mart.tools.svf.extractor;

import org.mart.crs.core.pcp.PCP;
import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.core.spectrum.SpectrumImplMatrixData;
import org.mart.crs.utils.helper.HelperArrays;


/**
 * User: hut
 * Date: Nov 14, 2008
 * Time: 1:25:38 PM
 */
public class ChromaPlusEnergyFeatureExtractor extends ChromaFeatureExtractor{

//    /**
//     * Sampling rate of the signal
//     */
    protected float samplingRate;

    public float[][] extract(float[][] data, float sampleRate){
        return this.extract(new SpectrumImplMatrixData(data, sampleRate, sampleRate));
    }

    public float[][] extract(SpectrumImpl spectrum) {

        samplingRate = spectrum.getSampleRate();
        PCP pcp = PCP.getPCP(PCP.BASIC_ALG);
        pcp.initSpectrum(spectrum);
        float[][] chromagram;

        if (IS_UNWRAPPED_CHROMA) {
            chromagram = pcp.getPCPUnwrapped();
        } else {
            chromagram = pcp.getPCP();
        }

        //Now calculate energy in spectrum bands and add this information to the Chroma vector
        float[][] energyBins = spectrum.getEnergyBins(frequencyBands);
        //Make normalization
        for (int i = 0; i < energyBins.length; i++) {
            energyBins[i] = HelperArrays.normalizeVector(energyBins[i]);
            energyBins[i] = HelperArrays.emphasizeVector(energyBins[i], 4);
        }

        //Produce one data array
        int vectorLength = chromagram[0].length + energyBins[0].length;
        float[][] output = new float[chromagram.length][vectorLength];
        for (int i = 0; i < chromagram.length; i++) {
            System.arraycopy(chromagram[i], 0, output[i], 0, chromagram[0].length);
            System.arraycopy(energyBins[i], 0, output[i], chromagram[0].length, energyBins[0].length);
        }

        return output;

    }
}
