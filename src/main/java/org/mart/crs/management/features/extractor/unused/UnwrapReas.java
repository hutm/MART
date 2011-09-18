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

import org.mart.crs.config.Settings;
import org.mart.crs.core.pcp.spectral.PCP;
import org.mart.crs.management.features.extractor.chroma.TrebleReas;
import org.mart.crs.management.label.chord.Root;
import org.mart.crs.management.label.chord.ChordType;

import java.util.Arrays;

/**
 * @version 1.0 01-Jul-2010 11:31:21
 * @author: Hut
 */
public class UnwrapReas extends TrebleReas {

    public float[][] extractFeatures(double startTime, double endTime, double refFrequency, Root chordFrom ) {
        float[] samples = audioReader.getSamples((float)startTime, (float)endTime);
        return rotateFeatures(cutPCPUnwrapped(producePCP(samples, refFrequency)), chordFrom);
    }


    public float[][] extractFeatures(double refFrequency, Root chordFrom ) {
        float[] samples = audioReader.getSamples();
        return rotateFeatures(cutPCPUnwrapped(producePCP(samples, refFrequency)), chordFrom);
    }


    @Override
    public int getVectorSize() {
        return execParams.endMidiNote - execParams.startMidiNote;
    }

    protected float[][] cutPCPUnwrapped(PCP pcp) {
        float[][] outPCP = new float[pcp.getPCP().length][execParams.endMidiNote - execParams.startMidiNote];

        for (int frame = 0; frame < pcp.getPCPUnwrapped().length; frame++) {
            for (int i = execParams.startMidiNote - Settings.START_NOTE_FOR_PCP_UNWRAPPED; i < execParams.endMidiNote - Settings.START_NOTE_FOR_PCP_UNWRAPPED; i++) {
                outPCP[frame][i - (execParams.startMidiNote - Settings.START_NOTE_FOR_PCP_UNWRAPPED)] = pcp.getPCPUnwrapped()[frame][i];
            }
        }
        return outPCP;
    }


    @Override
    public float[][] rotateFeatures(float[][] feature, Root chordNameFrom) {
        if(chordNameFrom.equals(ChordType.NOT_A_CHORD.getName()) || chordNameFrom.equals("")){
            return feature;
        }
        float[][] output = new float[feature.length][feature[0].length];
        for (int startNote = 0; startNote < feature[0].length; startNote += Settings.NUMBER_OF_SEMITONES_IN_OCTAVE) {
            float[][] octavePCP = new float[feature.length][Settings.NUMBER_OF_SEMITONES_IN_OCTAVE];
            for (int i = 0; i < feature.length; i++) {
                octavePCP[i]=Arrays.copyOfRange(feature[i], startNote, startNote + Settings.NUMBER_OF_SEMITONES_IN_OCTAVE);
            }
            octavePCP  = PCP.rotatePCP(octavePCP, 1, chordNameFrom, Root.C);
            for (int i = 0; i < feature.length; i++) {
                System.arraycopy(octavePCP[i], 0, output[i], startNote,  Settings.NUMBER_OF_SEMITONES_IN_OCTAVE);
            }
        }
        return output;
    }
}
