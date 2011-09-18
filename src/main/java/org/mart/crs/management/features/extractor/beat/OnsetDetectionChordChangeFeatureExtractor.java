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

package org.mart.crs.management.features.extractor.beat;

import org.mart.crs.core.onset.OnsetDetectionFunction;
import org.mart.crs.core.pcp.PCPBuilder;
import org.mart.crs.core.pcp.spectral.PCP;
import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrumHarmonicPart;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrumPercussivePart;
import org.mart.tools.svf.SVF;
import org.mart.tools.svf.extractor.SVFExtractor;

/**
 * @version 1.0 Nov 29, 2010 7:32:53 PM
 * @author: Hut
 */
public class OnsetDetectionChordChangeFeatureExtractor extends OnsetDetectionFeatureExtractor {


    @Override
    protected void extractGlobalFeatures(double refFrequency) {
        SpectrumImpl spectrumPercussive = new ReassignedSpectrumPercussivePart(audioReader, execParams.windowLengthOnsetDetection, execParams.windowType, execParams.overlappingOnsetDetection, execParams.beatReasPercussivePartThreshold, execParams);
        OnsetDetectionFunction onsetDetectionFunction = new OnsetDetectionFunction(spectrumPercussive, execParams.startFreqOnsetDetection, execParams.endFreqOnsetDetection);
//        OnsetDetectionFunction onsetDetectionFunctionBass = new OnsetDetectionFunction(spectrumPercussive, execParams.startFreqOnsetDetection, Helper.getFreqForMIDINote(execParams.endMidiNoteBass));


        SpectrumImpl spectrumHarmonic = new ReassignedSpectrumHarmonicPart(audioReader, execParams.windowLengthSVFChroma, execParams.windowType, execParams.overlappingSVFChroma, execParams.beatReasHarmonicPartThreshold, execParams);

        PCP pcp = new PCPBuilder(PCPBuilder.BASIC_ALG).setExecParams(execParams).setSpectrum(spectrumHarmonic).setRefFreq(refFrequency).build();
        SVF svf = new SVF(pcp.getPCP(), pcp.getSampleRatePCP(), execParams.contextLengthSVFChroma, SVFExtractor.ChromaFeature);

//        SVF svfLarge = new SVF(pcp.getPCP(), pcp.getSampleRatePCP(), execParams.contextLengthSVFChromaLarge, SVFExtractor.ChromaFeature);

//        PCP pcpBass = new PCPBuilder(PCPBuilder.BASIC_ALG).setExecParams(execParams).setSpectrum(spectrumHarmonic).setRefFreq(refFrequency).setStartNoteForPCPWrapped(execParams.startMidiNoteBass).setEndNoteForPCPWrapped(execParams.startMidiNoteBass).build();
//        SVF svfBass = new SVF(pcpBass.getPCP(), pcpBass.getSampleRatePCP(), execParams.contextLengthSVFChroma, SVFExtractor.ChromaFeature);

//        SimilarityMatrix matrix = new SimilarityMatrix(pcp.getPCP());
//        matrix.setContextLength(execParams.contextLengthSVFChroma);
//        float[] onsetDetectionFromSelfSimilarityMatrix = matrix.getDetectionFunction();

        int length = onsetDetectionFunction.getDetectionFunction().length;
        float[][] out = new float[length][getVectorSize()];
        for (int i = 0; i < length; i++) {
            out[i][0] = onsetDetectionFunction.getDetectionFunction()[i];
            out[i][1] = svf.getSVFFunctionNormalized()[i];
        }
        globalVectors.add(out);
    }

    @Override
    public int getVectorSize() {
        return 2;
    }
}
