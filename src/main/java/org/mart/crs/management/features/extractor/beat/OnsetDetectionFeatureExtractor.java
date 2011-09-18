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

import org.mart.crs.core.AudioReader;
import org.mart.crs.core.onset.OnsetDetectionFunction;
import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrumPercussivePart;
import org.mart.crs.management.features.extractor.FeaturesExtractorHTK;

/**
 * @version 1.0 3/10/11 6:53 PM
 * @author: Hut
 */
public class OnsetDetectionFeatureExtractor extends FeaturesExtractorHTK {

    @Override
    public void initialize(String songFilePath) {
        super.initialize(songFilePath);
        this.audioReader = new AudioReader(songFilePath, execParams.samplingRate);
    }

    @Override
    protected void extractGlobalFeatures(double refFrequency) {
        SpectrumImpl spectrumPercussive = new ReassignedSpectrumPercussivePart(audioReader, execParams.windowLengthOnsetDetection, execParams.windowType, execParams.overlappingOnsetDetection, execParams.beatReasPercussivePartThreshold, execParams);
        OnsetDetectionFunction onsetDetectionFunction = new OnsetDetectionFunction(spectrumPercussive, execParams.startFreqOnsetDetection, execParams.endFreqOnsetDetection);


        int length = onsetDetectionFunction.getDetectionFunction().length;
        float[][] out = new float[length][getVectorSize()];
        for (int i = 0; i < length; i++) {
            out[i][0] = onsetDetectionFunction.getDetectionFunction()[i];
        }
        globalVectors.add(out);
    }

    @Override
    public int getVectorSize() {
        return 1;
    }


}
