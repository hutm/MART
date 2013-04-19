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

package org.mart.crs.core.pcp;

import org.mart.crs.core.AudioReader;
import org.mart.crs.core.spectrum.SpectrumCombSpectral;
import org.mart.crs.core.spectrum.SpectrumCombTime;
import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.management.config.Configuration;
import org.mart.crs.utils.helper.Helper;

/**
 * @version 1.0 26-May-2010 13:31:51
 * @author: Hut
 */
public class PCPCombSpectral extends PCPComb {

    protected PCPCombSpectral() {
    }

    protected PCPCombSpectral(double refFreq, int averagingFactor, int numberOfBinsPerSemitone, boolean toNormalize, int startNoteForPCPWrapped, int endNoteForPCPWrapped, float chromaSpectrumRate) {
        super(refFreq, averagingFactor, numberOfBinsPerSemitone, toNormalize, startNoteForPCPWrapped, endNoteForPCPWrapped, chromaSpectrumRate);
    }

    public void init(String audioFilePath, float samplingFreq, SpectrumImpl spectrum) {
        super.init();
        if(spectrum != null){
            init(spectrum);
            return;
        }
        if(audioFilePath != null){
            init(audioFilePath, samplingFreq, Configuration.windowTypeDefault);
        }
    }


    protected void init(String audioFilePath, float samplingFreq, int windowType) {
        String audioFileName = audioFilePath;
        int frameLengthInSamples = (int)Math.round(frameSizePCP * samplingFreq);
        AudioReader audioReader = new AudioReader(audioFileName, samplingFreq);
        SpectrumImpl spectrumObj = new SpectrumImpl(audioReader, frameLengthInSamples, windowType, 0);
        init(spectrumObj);
    }


    protected void init(SpectrumImpl spectrumObj) {
        for (int midiNumber = this.startNoteForPCPUnwrapped; midiNumber <= this.endNoteForPCPUnwrapped; midiNumber++) {
            SpectrumCombTime spectrum = new SpectrumCombSpectral(spectrumObj, Helper.getFreqForMIDINote(midiNumber));
            spectrum.calculateHarmonicProfileVectors();

            sampleRatePCP = spectrum.getSampleRateSpectrum();

            noteProbabilities.put(midiNumber, spectrum.getHarmonicPVScore());
            scoresVectorMap.put(midiNumber, spectrum.getHarmonicProfileVectors());
        }
        constructPCP();
    }

}
