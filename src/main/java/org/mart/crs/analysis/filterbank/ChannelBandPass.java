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

package org.mart.crs.analysis.filterbank;

import org.mart.crs.analysis.filter.EllipticFBFilter;
import org.mart.crs.core.spectrum.SpectrumCrossCorrelationBasedImpl;
import org.mart.crs.logging.CRSException;
import org.mart.crs.management.xml.XMLManager;
import org.w3c.dom.Element;

import static org.mart.crs.management.xml.Tags.END_FREQ_TAG;
import static org.mart.crs.management.xml.Tags.START_FREQ_TAG;

public class ChannelBandPass extends Channel {

    public static boolean isToConsiderHigerPeaks = false;

    protected float startFreq;
    protected float endFreq;

    public static float freqRangeEnlargementFactor = .0f;

    /**     
     * Is used for numberOfPointsCC calculation (numberOfPointsCC=getEndFreq * HIGHER_ORDER_PEAKS)
     */
    public static final int HIGHER_ORDER_PEAKS  = 5;


    public ChannelBandPass(float samplingFreq, int channelnumber) {
        super(samplingFreq, channelnumber);
    }

    public ChannelBandPass(Element rootElement) throws CRSException {
        super(rootElement);
        this.startFreq = Float.valueOf(XMLManager.getStringData(rootElement, START_FREQ_TAG));
        this.endFreq = Float.valueOf(XMLManager.getStringData(rootElement, END_FREQ_TAG));
    }


     public void extractPeriodicities(float[] samples, float frameSizeInSec) {
        float[] filteredSamples = new float[0];
        float[] periodicityData = new float[0];
        filteredSamples = filterSamples(samples);

        if (!(filter instanceof EllipticFBFilter)) {
            SpectrumCrossCorrelationBasedImpl spectrumCCB = new SpectrumCrossCorrelationBasedImpl(filteredSamples, samplingFreq, 100); //TODO use instead of magic number 100 numberOfPointsCC. Needs testing
            float startFreq = getStartFreqFactored(this.startFreq, this.endFreq, freqRangeEnlargementFactor);
            float endFreq = getEndFreqFactored(this.startFreq, this.endFreq, freqRangeEnlargementFactor);
            periodicityData = spectrumCCB.analyzeSCCBFrame(startFreq, endFreq, true, isToConsiderHigerPeaks);
        } else{
            periodicityData = new float[filteredSamples.length];
            float freq = startFreq + (endFreq - startFreq) / 2; //TODO add log scaling
            for(int i = 0; i < periodicityData.length; i++){
                periodicityData[i] = freq;
            }
        }

        float frameSizeInSamples = frameSizeInSec * samplingFreq;
        formFeatureVectors(filteredSamples, periodicityData, frameSizeInSamples);
    }




}
