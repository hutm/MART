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

package org.mart.crs.management.features.extractor.chroma;

import org.mart.crs.config.Settings;
import org.mart.crs.core.pcp.PCPBuilder;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrum;

/**
 * Use one stream of 2 chroma vectors: bass and treble
 *
 * @version 1.0 15-Oct-2010 00:38:48
 * @author: Hut
 */
public class TrebleBassReas extends TrebleReas {

    @Override
    public int getVectorSize() {
        return Settings.NUMBER_OF_SEMITONES_IN_OCTAVE * 2;
    }


    public void extractGlobalFeatures(double refFrequency) {
        float[] samples = audioReader.getSamples();
        addCombinedVectors(samples, refFrequency);
    }


    protected void addCombinedVectors(float[] samples, double refFrequency){
        ReassignedSpectrum spectrum = getReassignedSpectrum(samples, audioReader.getSampleRate());
        float[][] pcpTreble = new PCPBuilder(PCPBuilder.BASIC_ALG).setExecParams(execParams).setSpectrum(spectrum).setRefFreq(refFrequency).setInitializationByParts(Settings.initializationByParts).build().getPCP();
        globalVectors.add(pcpTreble);


        float[][] pcpBass = new PCPBuilder(PCPBuilder.BASIC_ALG).setExecParams(execParams).setSpectrum(spectrum).setRefFreq(refFrequency).setInitializationByParts(Settings.initializationByParts).setStartNoteForPCPWrapped(execParams.startMidiNoteBass).setEndNoteForPCPWrapped(execParams.endMidiNoteBass).build().getPCP();
        globalVectors.add(pcpBass);
    }


}
