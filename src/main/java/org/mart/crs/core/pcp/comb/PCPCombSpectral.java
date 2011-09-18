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

package org.mart.crs.core.pcp.comb;

import org.mart.crs.core.AudioReader;
import org.mart.crs.core.pcp.PCPBuilder;
import org.mart.crs.core.spectrum.SpectrumCombSpectral;
import org.mart.crs.core.spectrum.SpectrumCombTime;
import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.logging.CRSException;
import org.mart.crs.utils.helper.Helper;

/**
 * @version 1.0 26-May-2010 13:31:51
 * @author: Hut
 */
public class PCPCombSpectral extends PCPComb {

    public PCPCombSpectral(PCPBuilder builder) {
        super(builder);
    }

    protected void init(PCPBuilder builder) throws CRSException {
        super.init();
        String audioFilePath = builder.getAudioFilePath();
        float samplingFreq = builder.getSamplingFreqOfAudio();
        SpectrumImpl spectrum = builder.getSpectrum();
        if(spectrum != null){
            init(spectrum);
            return;
        }
        if(audioFilePath != null){
            init(audioFilePath, samplingFreq);
        }
        throw new CRSException("Could not instantiate PCP from builder: not enough data");
    }


    protected void init(String audioFilePath, float samplingFreq) {
        String audioFileName = audioFilePath;
        int frameLengthInSamples = (int)Math.round(frameSizePCP * samplingFreq);
        AudioReader audioReader = new AudioReader(audioFileName, samplingFreq);
        SpectrumImpl spectrumObj = new SpectrumImpl(audioReader, frameLengthInSamples, execParams.windowType, 0, execParams);
        init(spectrumObj);
    }


    protected void init(SpectrumImpl spectrumObj) {
        for (int midiNumber = this.startNoteForPCPUnwrapped; midiNumber <= this.endNoteForPCPUnwrapped; midiNumber++) {
            SpectrumCombTime spectrum = new SpectrumCombSpectral(spectrumObj, Helper.getFreqForMIDINote(midiNumber), execParams);
            spectrum.calculateHarmonicProfileVectors();

            sampleRatePCP = spectrum.getSampleRateSpectrum();

            noteProbabilities.put(midiNumber, spectrum.getHarmonicPVScore());
            scoresVectorMap.put(midiNumber, spectrum.getHarmonicProfileVectors());
        }
        constructPCP();
    }

}
