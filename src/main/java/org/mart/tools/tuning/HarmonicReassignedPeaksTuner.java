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

package org.mart.tools.tuning;

import org.mart.crs.config.ExecParams;
import org.mart.crs.core.AudioReader;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrum;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrumHarmonicPart;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;

import static org.mart.crs.utils.helper.HelperArrays.sum;

/**
 * @version 1.0 9/4/11 1:50 PM
 * @author: Hut
 */
public class HarmonicReassignedPeaksTuner extends F0PeaksTuner{

    public static final int START_MIDI_NOTE_FOR_TUNING = 24;
    public static final int END_MIDI_NOTE_FOR_TUNING = 70;


    protected ReassignedSpectrum spectrum;
    protected float[][] freqValues;
    protected float[][] ampValues;
    protected float[][] semitoneDeviationValues;


    public HarmonicReassignedPeaksTuner(String audioFilePath) {
        super(audioFilePath);
    }



    protected void initialize() {
        AudioReader audioReader = new AudioReader(audioFilePath, ExecParams._initialExecParameters.samplingRate);
        this.spectrum = new ReassignedSpectrumHarmonicPart(audioReader, ExecParams._initialExecParameters.reassignedSpectrogramThreshold, ExecParams._initialExecParameters);
        spectrum.getMagSpec();
        this.freqValues = spectrum.getFrequencyReasValues();
        this.ampValues = spectrum.getEnergyReasValues();
    }


    public float getReferenceFrequency() {

        if (semitoneScale == null) {
            semitoneScale = createSemitoneScale(Helper.getFreqForMIDINote(START_MIDI_NOTE_FOR_TUNING),
                    Helper.getFreqForMIDINote(END_MIDI_NOTE_FOR_TUNING));
        }

        semitoneDeviationValues = new float[freqValues.length][freqValues[0].length];

        for(int i = 0; i < freqValues.length; i++){
            semitoneDeviationValues[i] = getSemitoneDistance(freqValues[i], semitoneScale);
        }


        int[] hist = new int[Math.round(1 / BIN_STEP)];
        int[] histAmp = new int[Math.round(1 / BIN_STEP)];

        for (int i = 0; i < semitoneDeviationValues.length; i++) {
            for (int j = 0; j < semitoneDeviationValues[0].length; j++) {
                int value = Math.round(semitoneDeviationValues[i][j] / BIN_STEP);
                if(value < -1.0 / BIN_STEP || value > 1.0 / BIN_STEP ){
                    continue;
                }
                if (value >= 0) {
                    hist[value] += 1;
                    histAmp[value] += ampValues[i][j];
                } else {
                    hist[hist.length + value] += 1;
                    histAmp[hist.length + value] += ampValues[i][j];
                }
            }
        }
        int maxindex = HelperArrays.findIndexWithMaxValue(hist);
        float mistuning = maxindex * BIN_STEP;
        if (mistuning > 0.5) {
            mistuning--;
        }

        referenceFreq = Helper.getFreqForMIDINote(69 + mistuning);
        logger.debug(String.format("Found reference freq %5.2f", referenceFreq));
        return referenceFreq;
    }




    public static void main(String[] args) {
        HarmonicReassignedPeaksTuner tuner = new HarmonicReassignedPeaksTuner("/home/hut/Beatles/data/wav/16_-_The_End.wav");
        float frequency = tuner.getReferenceFrequency();
        System.out.println(frequency);
    }




}
