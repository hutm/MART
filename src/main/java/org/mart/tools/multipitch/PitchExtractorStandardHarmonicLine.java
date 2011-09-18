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

package org.mart.tools.multipitch;

import org.mart.crs.core.spectrum.SpectrumImpl;

import java.util.Collections;
import java.util.List;

/**
 * @version 1.0 Oct 6, 2009 4:50:25 PM
 * @author: Maksim Khadkevich
 */
public class PitchExtractorStandardHarmonicLine extends PitchExtractorStandard {


    protected void wrapHarmonics() {

        HarmonicLineFactory.reset();
        //Grouping harmonics in time
        for (int i = 0; i < magSpec.length; i++) {
            groupHarmonics(extractedFrequencies, i);
        }

        //Sort
        Collections.sort(HarmonicLineFactory.getHarmonicLines());

        HarmonicLineFactory.afterGroupingInHarmonicLines();


        //Now Extract Fundamentals
        HarmonicLineFactory.extractFundamentals();


        //Put fundamentals into output data structure
        List<HarmonicLine> fundamentalLines = HarmonicLineFactory.getHarmonicLines();
        List<int[]> fundamentalIntervals;
        for (HarmonicLine line : fundamentalLines) {
            fundamentalIntervals = line.getFundamentalIntervals();

            float freq;
            int freqIndex;
            for (int[] interval : fundamentalIntervals) {
//                freq = line.getAverageFrequency(interval[0], interval[1]);
//                freqIndex = SpectrumImpl.freq2index(freq, spectrum.getFrequencyResolution());
                for (int i = interval[0]; i <= interval[1]; i++) {
                    freq = line.getDetectedHarmonic(i).getFreq();
                    freqIndex = SpectrumImpl.freq2index(freq, spectrum.getFrequencyResolution());
                    //                    out[i][freqIndex] = line.getAverageAmplitude();
                    out[i][freqIndex] = 20;
                }
            }
        }

    }


    protected static void groupHarmonics(DetectedHarmonic[][] extractedFrequencies, int frameNumber) {
        DetectedHarmonic[] currentFrameHarmonics = extractedFrequencies[frameNumber];
        for (DetectedHarmonic aHarmonic : currentFrameHarmonics) {
            HarmonicLineFactory.assingHarmonicLine(aHarmonic);
        }
    }


}
