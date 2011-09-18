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

package org.mart.tools.pcp;

import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.core.AudioReader;
import org.mart.crs.core.spectrum.ReassignedSpectrumPhaseChange;
import org.mart.crs.core.spectrum.SpectrumCombSpectral;
import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrum;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrumHarmonicPart;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;
import org.mart.crs.utils.windowing.WindowType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @version 1.0 14-Sep-2010 11:18:56
 * @author: Hut
 */
public class SpectrumQualityEstimator {

    protected SpectrumImpl spectrum;
    protected float f0;
    protected float filterWidthSemitoneScale;

    public SpectrumQualityEstimator(SpectrumImpl spectrum, float f0, float filterWidthSemitoneScale) {
        this.f0 = f0;
        this.spectrum = spectrum;
        this.filterWidthSemitoneScale = filterWidthSemitoneScale;
    }


    public float getSNR() {
        SpectrumImpl filteresSpectrum = new SpectrumCombSpectral(spectrum, f0, filterWidthSemitoneScale, false, ExecParams._initialExecParameters);
        float[] energyFiltered = filteresSpectrum.getFullEnergy();
        float[] energyAll = spectrum.getFullEnergy();

        float[] energyNoise = HelperArrays.subtract(energyAll, energyFiltered);

        float[] SNR = HelperArrays.divide(energyFiltered, energyNoise);

        return HelperArrays.calculateMean(SNR, 1, SNR.length - 1);
    }


    public static void main(String[] args) throws IOException {
        for(int winLength = 512; winLength <= 4096; winLength *= 2){
            for(float filterWidthSemitoneScale = 0.5f ;filterWidthSemitoneScale <= 1.5; filterWidthSemitoneScale+= 0.5){
                System.out.println(String.format("------------------------Running fold %d - %5.2f", winLength, filterWidthSemitoneScale));
                runFold(winLength, filterWidthSemitoneScale);
            }
        }

    }


    public static void runFold(int winLength, float filterWidthSemitoneScale) throws IOException {

        String dirName = "/temp/notesData/data";

        FileWriter writer = new FileWriter(dirName + String.format("/../results_%5.2f_%d.txt", filterWidthSemitoneScale, winLength));
        float snr1Sum = 0;
        float snr2Sum = 0;
        float snr3Sum = 0;
        float snr4Sum = 0;

        File[] listFiles = new File(dirName).listFiles(new ExtensionFileFilter(Settings.WAV_EXT, false));
        for (File file : listFiles) {
            System.out.println(String.format("Processing file %s", file.getName()));
            AudioReader reader = new AudioReader(file.getPath(), 11025);
            String note = file.getName().substring(0, file.getName().indexOf("_"));
            float freq = Helper.getFreqForMIDINote(Helper.getMidiNumberForNote(note));
            SpectrumImpl spectrum = new SpectrumImpl(reader, winLength, WindowType.HANNING_WINDOW.ordinal(), 0.5f, ExecParams._initialExecParameters);
            ReassignedSpectrum reassignedSpectrum = new ReassignedSpectrum(reader, winLength, WindowType.HANNING_WINDOW.ordinal(), 0.5f, ExecParams._initialExecParameters);
            ReassignedSpectrumPhaseChange reassignedSpectrumPhase = new ReassignedSpectrumPhaseChange(reader, winLength, WindowType.HANNING_WINDOW.ordinal(), 0.5f, ExecParams._initialExecParameters);
            ReassignedSpectrumHarmonicPart reassignedSpectrumHarmonic = new ReassignedSpectrumHarmonicPart(reader, winLength, WindowType.HANNING_WINDOW.ordinal(), 0.5f, 0.5f, ExecParams._initialExecParameters);

            float snr1 = (new SpectrumQualityEstimator(spectrum, freq, filterWidthSemitoneScale)).getSNR();
            float snr2 = (new SpectrumQualityEstimator(reassignedSpectrum, freq, filterWidthSemitoneScale)).getSNR();
            float snr3 = (new SpectrumQualityEstimator(reassignedSpectrumPhase, freq, filterWidthSemitoneScale)).getSNR();
            float snr4 = (new SpectrumQualityEstimator(reassignedSpectrumHarmonic, freq, filterWidthSemitoneScale)).getSNR();

            snr1Sum += snr1;
            snr2Sum += snr2;
            snr3Sum += snr3;
            snr4Sum += snr4;

            writer.write(String.format("%s\t\t%5.2f\t\t%5.2f\t\t%5.2f\t\t%5.2f\n", file.getName(), snr1, snr3, snr2, snr4));
        }
        writer.write("----------------------------------------\n");
        writer.write(String.format("%5.2f\t\t%5.2f\t\t%5.2f\t\t%5.2f\n", snr1Sum / listFiles.length, snr3Sum / listFiles.length, snr2Sum / listFiles.length, snr4Sum / listFiles.length));
        writer.close();

    }


}
