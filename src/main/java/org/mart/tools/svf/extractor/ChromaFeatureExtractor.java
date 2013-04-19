/*
 * Copyright (c) 2008-2013 Maksim Khadkevich and Fondazione Bruno Kessler.
 *
 * This file is part of MART.
 * MART is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2, as published
 * by the Free Software Foundation.
 *
 * MART is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with MART; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.mart.tools.svf.extractor;

/**
 * User: hut
 * Date: Nov 14, 2008
 * Time: 1:25:38 PM
 */
public class ChromaFeatureExtractor implements SVFFeatureExtractorInterface {

    public static final boolean IS_UNWRAPPED_CHROMA = false;

    public static float[] frequencyBands = {0, 300, 2000}; // Only for ChromaPlusEnergyFeatureExtractor


    /**
     * Sampling rate of the signal
     */
    protected float samplingRate;


    public float[][] extract(float[][] data, float sampleRate) {
        return data;
    }

//    public float[][] extract(SpectrumImpl spectrum) {
//        samplingRate = spectrum.getSampleRate();
//        PCP pcp = new PCPBuilder(PCPBuilder.BASIC_ALG).setSpectrum(spectrum).setStartNoteForPCPUnwrapped(24).setEndNoteForPCPWrapped(54).build();
//
//        if (IS_UNWRAPPED_CHROMA) {
//            return pcp.getPCPUnwrapped();
//        } else {
//            return pcp.getPCP();
//        }
//    }
}
