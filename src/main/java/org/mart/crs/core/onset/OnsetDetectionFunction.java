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

package org.mart.crs.core.onset;

import org.mart.crs.config.Settings;
import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.utils.helper.HelperArrays;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 04/11/10 12:19
 * @author: Hut
 */
public class OnsetDetectionFunction extends AbstractOnsetDetectionFunction {


    protected SpectrumImpl spectrum;
    protected float  startFreq;
    protected float endFreq;

    protected float[] detectionFunction;


    public OnsetDetectionFunction(SpectrumImpl spectrum, float startFreq, float endFreq) {
        this.spectrum = spectrum;
        this.startFreq = startFreq;
        this.endFreq = endFreq;
    }


    public OnsetDetectionFunction(SpectrumImpl spectrum) {
        this.spectrum = spectrum;
        this.startFreq = 0;
        this.endFreq = spectrum.getSampleRate() / 2 - 1;
    }


    protected OnsetDetectionFunction(){
    }


    protected void computeOnsetDetection(){
        if (Settings.initializationByParts) {
            int extractionWindowLength = Math.round(SpectrumImpl.SEGMENT_SIZE_FOR_MEMORY_OPTIMIZED_EXTRACTION * spectrum.getSampleRateSpectrum());

            int counter = 0;
            List<float[][]> onsetDataList = new ArrayList<float[][]>();
            SpectrumImpl originalSpectrum = spectrum;
            float[][] onsetDetectionFunctionPart = null;
            while (counter < originalSpectrum.getNumberOfFrames()) {
                int endIndex = Math.min(counter + extractionWindowLength, originalSpectrum.getNumberOfFrames());
                SpectrumImpl partSpectrum = originalSpectrum.extractSpectrumPart(counter, endIndex);
                onsetDetectionFunctionPart = calculateDetectionFunctionFromSpectrum(partSpectrum);
                onsetDataList.add(onsetDetectionFunctionPart);
                counter += extractionWindowLength;
            }
            this.detectionFunction = HelperArrays.concatColumnWise(onsetDataList)[0];
        }
        else{
            this.detectionFunction = calculateDetectionFunctionFromSpectrum(this.spectrum)[0];
        }

        detectionFunction = HelperArrays.normalizeVector(detectionFunction);
    }

    protected float[][] calculateDetectionFunctionFromSpectrum(SpectrumImpl spectrum){
        float[][] magSpectrum = spectrum.getMagSpec();
        float[][] detectionFunction = new float[1][magSpectrum.length];
        for(int i = 0; i < magSpectrum.length; i++){
            detectionFunction[0][i] = HelperArrays.sum(magSpectrum[i], spectrum.freq2index(this.startFreq), spectrum.freq2index(this.endFreq));
        }
        return detectionFunction;
    }



    public float[] getDetectionFunction() {
        if (detectionFunction == null) {
            computeOnsetDetection();
        }
        return detectionFunction;
    }
}
