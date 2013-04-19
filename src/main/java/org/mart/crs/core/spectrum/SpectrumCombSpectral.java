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

package org.mart.crs.core.spectrum;

import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;


/**
 * @version 1.0 Jan 26, 2010 3:43:28 PM
 * @author: Maksim Khadkevich
 */
public class SpectrumCombSpectral extends SpectrumCombTime {

    public static final int NUMBER_OF_HARMONICS = 50;

    public static final float DEFAULT_FILTER_WIDTH_SEMITONE_SCALE = 0.5f;

    protected float filterWidthSemitoneScale;
    protected boolean inverse;

    public SpectrumCombSpectral(SpectrumImpl spectrum, float fundamentalFreqCenter) {
        super(spectrum, fundamentalFreqCenter);
        this.filterWidthSemitoneScale = DEFAULT_FILTER_WIDTH_SEMITONE_SCALE;
        filterSpectrum(fundamentalFreqCenter);
    }

    public SpectrumCombSpectral(SpectrumImpl spectrum, float fundamentalFreqCenter, float filterWidthSemitoneScale, boolean inverse) {
        super(spectrum, fundamentalFreqCenter);
        this.filterWidthSemitoneScale = filterWidthSemitoneScale;
        this.inverse = inverse;
        filterSpectrum(fundamentalFreqCenter);
    }

    public void filterSpectrum(float fundamentalFreqCenter) {
        float[] filter = createSpectralFilter(fundamentalFreqCenter);
        if(inverse){
            for(int i = 0; i < filter.length; i++){
                if(filter[i] == 1){
                    filter[i] =0;
                } else{
                    filter[i] = 1;
                }
            }
        }
        for (int i = 0; i < getMagSpec().length; i++) {
            magSpec[i] = HelperArrays.product(magSpec[i], filter);
        }
    }


    private float[] createSpectralFilter(float fundamentalFreqCenter) {
        float[] filter = new float[getMagSpec()[0].length];
        for (int i = 0; i < NUMBER_OF_HARMONICS; i++) {
            int startIndex = freq2index(Helper.getFreqForMIDINote(Helper.getMidiNoteForFreq(fundamentalFreqCenter * (i + 1)) - filterWidthSemitoneScale / 2));
            int endIndex = freq2index(Helper.getFreqForMIDINote(Helper.getMidiNoteForFreq(fundamentalFreqCenter * (i + 1)) + filterWidthSemitoneScale / 2));
            for (int j = startIndex; j <= endIndex; j++) {
                if (j < filter.length) {
                    filter[j] = 1.0f;
                } else {
                    //The index of (sampling freq)/2 is lower than that index
                }
            }
        }
        return filter;
    }


}
