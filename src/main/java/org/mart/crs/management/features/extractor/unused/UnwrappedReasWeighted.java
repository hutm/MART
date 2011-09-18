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
import org.mart.crs.utils.windowing.Hamming;
import org.mart.crs.utils.windowing.WindowOption;

/**
 * @version 1.0 01-Jul-2010 11:25:06
 * @author: Hut
 */
public class UnwrappedReasWeighted extends UnwrapReas {

    public static boolean isToWrapFinally = true;

    protected int startMidiNote;
    protected int endMidiNote;


    public UnwrappedReasWeighted(int startMidiNote, int endMidiNote) {
        this.startMidiNote = startMidiNote;
        this.endMidiNote = endMidiNote;
    }

    @Override
    public int getVectorSize() {
        //TODO add here what is needed
        return super.getVectorSize();
    }

    protected float[][] cutPCPUnwrapped(PCP pcp) {
        float[][] outPCP;

        if (!isToWrapFinally) {
            outPCP= new float[pcp.getPCP().length][endMidiNote - startMidiNote];
        } else{
            outPCP= new float[pcp.getPCP().length][pcp.getNumerOfBinsInWrappedChromagram()];
        }

        for (int frame = 0; frame < pcp.getPCPUnwrapped().length; frame++) {
            for (int octaveStartNote = startMidiNote - Settings.START_NOTE_FOR_PCP_UNWRAPPED; octaveStartNote < endMidiNote - Settings.START_NOTE_FOR_PCP_UNWRAPPED; octaveStartNote += Settings.NUMBER_OF_SEMITONES_IN_OCTAVE) {
                float[] wider3OctavesVector = new float[36];
                if (octaveStartNote - 12 >= 0) {
                    System.arraycopy(pcp.getPCPUnwrapped()[frame], octaveStartNote - 12, wider3OctavesVector, 0, 12);
                }
                System.arraycopy(pcp.getPCPUnwrapped()[frame], octaveStartNote, wider3OctavesVector, 12, 12);
                if (octaveStartNote + 12 < pcp.getPCPUnwrapped()[0].length) {
                    System.arraycopy(pcp.getPCPUnwrapped()[frame], octaveStartNote + 12, wider3OctavesVector, 24, 12);
                }

                (new Hamming()).apply(wider3OctavesVector,0 ,36, WindowOption.WINDOW);
                float[] summedOctaves = new float[12];
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 12; j++) {
                        summedOctaves[j] += wider3OctavesVector[12 * i + j];
                    }
                }

                if (!isToWrapFinally) {
                    for (int i = 0; i < 12; i++) {
                        outPCP[frame][octaveStartNote + i - (startMidiNote - Settings.START_NOTE_FOR_PCP_UNWRAPPED)] = summedOctaves[i];
                    }
                } else{
                    for (int i = 0; i < 12; i++) {
                        outPCP[frame][i] = summedOctaves[i];
                    }
                }
            }

        }


        return outPCP;
    }

}
