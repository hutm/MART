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

import org.mart.crs.analysis.filter.Filter;
import org.mart.crs.analysis.filter.FilterManager;
import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.logging.CRSException;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.xml.XMLManager;
import org.mart.crs.utils.AudioHelper;
import org.mart.crs.utils.helper.Helper;
import org.apache.log4j.Logger;
import org.mart.crs.management.xml.Tags;
import org.mart.crs.utils.helper.HelperArrays;
import org.w3c.dom.Element;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.mart.crs.utils.helper.HelperData.*;

/**
 * @version 1.0 26-May-2010 14:35:05
 * @author: Hut
 */
public class Channel {

    protected static Logger logger = CRSLogger.getLogger(ChannelBandPass.class);


    protected float samplingFreq;
    protected int channelnumber;


    protected int numberOfPointsCC;

    protected float[] dominantFrequencies;
    protected float[] dominantFrequencyEnergies;

    protected Filter filter;

    protected static final float MAX_DEVIATION = 0.2f; //In semitone scale

    public Channel(float samplingFreq, int channelnumber) {
        this.samplingFreq = samplingFreq;
        this.channelnumber = channelnumber;
    }

    public Channel(Element rootElement) throws CRSException {
        this.samplingFreq = Float.valueOf(XMLManager.getStringData(rootElement, Tags.SAMPLING_FREQ_TAG));
        this.channelnumber = Integer.valueOf(XMLManager.getStringData(rootElement, Tags.CHANNEL_NUMBER_TAG));

        Element childElement = (Element)rootElement.getElementsByTagName(Tags.FILTER_TAG).item(0);
        Filter filter =  FilterManager.getFilter(childElement);
        this.filter =  filter;
    }





    public float[] filterSamples(float[] samples){
        return filter.process(samples);
    }




    protected void formFeatureVectors(float[] filteredSamples, float[] periodicityData, float frameSizeInSamples) {
        int featureVectorLength = (int) Math.floor(filteredSamples.length / frameSizeInSamples);
        dominantFrequencies = new float[featureVectorLength];
        dominantFrequencyEnergies = new float[featureVectorLength];

        for (int i = 0; i < featureVectorLength; i++) {
            int startIndex = Math.round(i * frameSizeInSamples);
            int endIndex = Math.round((i + 1) * frameSizeInSamples);

            float RMSEnergy = AudioHelper.getRMSEnergy(filteredSamples, startIndex, endIndex);
            dominantFrequencyEnergies[i] = RMSEnergy;

            float[] periodicityInterval = Arrays.copyOfRange(periodicityData, startIndex, endIndex);
            dominantFrequencies[i] = averageDominantFrequency(periodicityInterval);
        }

    }

    protected float averageDominantFrequency(float[] periodicityInterval) {
        float[] meanValueAndDeviation = HelperArrays.calculateMeanAndStandardDeviationIgnoreNegatives(periodicityInterval);
        if (Helper.getSemitoneDistanceAbs(meanValueAndDeviation[0], meanValueAndDeviation[0] - meanValueAndDeviation[1]) < MAX_DEVIATION && meanValueAndDeviation[1] != 0) {
//            logger.debug("found: " + meanValueAndDeviation[0]);
            return meanValueAndDeviation[0];
        } else {
            return 0;
        }
    }


    /**
     * Sets output of the extracted features int SpecrumObject
     *
     * @param spectrum
     */
    public void contribute(SpectrumImpl spectrum) {
        float[][] magSpec = spectrum.getMagSpec();
        for (int i = 0; i < this.dominantFrequencies.length; i++) {
            if (dominantFrequencies[i] > 0) {
                int spectralIndex = spectrum.freq2index(dominantFrequencies[i]);
                if (magSpec[i].length > spectralIndex && magSpec[i][spectralIndex] < dominantFrequencyEnergies[i]) {
//                    logger.info(String.originalFormat("spectral index done! %5.2f < %5.2f ", magSpec[i][spectralIndex], dominantFrequencyEnergies[i]));
                    magSpec[i][spectralIndex] = dominantFrequencyEnergies[i];
                }
            }
        }
        spectrum.setMagSpec(magSpec);
    }


    public void export(BufferedOutputStream outputStream) throws IOException {
        writeFloat(samplingFreq, outputStream);
        writeInt(channelnumber, outputStream);
        writeInt(numberOfPointsCC, outputStream);
        writeInt(dominantFrequencies.length, outputStream);
        for (int i = 0; i < dominantFrequencies.length; i++) {
            writeFloat(dominantFrequencies[i], outputStream);
            writeFloat(dominantFrequencyEnergies[i], outputStream);
        }
    }

    public static ChannelBandPass import_(BufferedInputStream inputStream) throws IOException {
        float samplingFreq = readFloat(inputStream);
        int channelnumber = readInt(inputStream);
        int numberOfPointsCC = readInt(inputStream);  //TODO just for compatibility with extracted features
        int dim = readInt(inputStream);
        float[] dominantFrequencies = new float[dim];
        float[] dominantFrequencyEnergies = new float[dim];
        for (int i = 0; i < dominantFrequencies.length; i++) {
            dominantFrequencies[i] = readFloat(inputStream);
            dominantFrequencyEnergies[i] = readFloat(inputStream);
        }

        ChannelBandPass channel = new ChannelBandPass(samplingFreq, channelnumber);
        channel.setDominantFrequencies(dominantFrequencies);
        channel.setDominantFrequencyEnergies(dominantFrequencyEnergies);
        channel.setNumberOfPointsCC(numberOfPointsCC);
        return channel;
    }


    /**
     * Enlarges frequency range by a factor
     * @param startFreq startFreq
     * @param endFreq endFreq
     * @param factor factor
     * @return
     */
    protected float getStartFreqFactored(float startFreq, float endFreq, float factor){
        return startFreq - (endFreq - startFreq) * factor;
    }

    protected float getEndFreqFactored(float startFreq, float endFreq, float factor){
        return endFreq + (endFreq - startFreq) * factor;
    }




    public float getSamplingFreq() {
        return samplingFreq;
    }

    public int getChannelnumber() {
        return channelnumber;
    }

    public int getNumberOfPointsCC() {
        return numberOfPointsCC;
    }


    public float[] getDominantFrequencies() {
        return dominantFrequencies;
    }

    public float[] getDominantFrequencyEnergies() {
        return dominantFrequencyEnergies;
    }

    public void setDominantFrequencies(float[] dominantFrequencies) {
        this.dominantFrequencies = dominantFrequencies;
    }

    public void setDominantFrequencyEnergies(float[] dominantFrequencyEnergies) {
        this.dominantFrequencyEnergies = dominantFrequencyEnergies;
    }

    public void setNumberOfPointsCC(int numberOfPointsCC) {
        this.numberOfPointsCC = numberOfPointsCC;
    }

    @Override
    public String toString() {
        return String.format("{%5.1f : %d}", samplingFreq, channelnumber);
    }




}
