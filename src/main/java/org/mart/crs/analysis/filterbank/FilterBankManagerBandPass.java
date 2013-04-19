/*
 * Copyright (c) 2008-2013 Maksim Khadkevich and Fondazione Bruno Kessler.
 *
 * This file is part of MART.
 * MART is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2, as published
 * by the Free Software Foundation.
 *
 * MART is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with MART; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.mart.crs.analysis.filterbank;

import org.mart.crs.core.AudioReader;
import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.core.spectrum.SpectrumImplMatrixData;
import org.mart.crs.logging.CRSException;
import org.mart.crs.management.xml.XMLManager;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;
import org.mart.crs.utils.helper.HelperData;
import org.mart.crs.utils.helper.HelperFile;
import org.w3c.dom.Element;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @version 1.0 18-Jun-2010 00:04:11
 * @author: Hut
 */
public class FilterBankManagerBandPass extends FilterBankManager {

    private List<ChannelBandPass> channelList;

    public FilterBankManagerBandPass(AudioReader audioReader, String configFilePath, float frameSizeOfFeaturesInSecs) {
        super(audioReader, configFilePath, frameSizeOfFeaturesInSecs);
    }

    public FilterBankManagerBandPass() {
        super();
    }

    protected void addChannelToList(Element element) throws CRSException {
        if (channelList == null) {
            channelList = new ArrayList<ChannelBandPass>();
        }
        ChannelBandPass channel = new ChannelBandPass(element);
        channelList.add(channel);
    }


    public void detectPeriodicities() {
        AudioReader originalAudioReader = this.audioReader;

        AudioReader downsampledAudio = null;
        float samplingRate = 0;
        for (ChannelBandPass channel : channelList) {
            if (downsampledAudio != null) {
                samplingRate = downsampledAudio.getSampleRate();
            }
            if (samplingRate != channel.getSamplingFreq()) {
                downsampledAudio = new AudioReader(originalAudioReader.getSamples(), originalAudioReader.getSampleRate());
                downsampledAudio.changeSamplingRate(channel.getSamplingFreq());
            }
            XMLManager.logger.info(String.format("%s: channel: %s", HelperFile.getShortFileName(audioReader.getFilePath()), channel));
            channel.extractPeriodicities(downsampledAudio.getSamples(), frameSizeOfFeaturesInSecs);
        }
    }

    public void exportDetectedPeriodicities(String filePath) {
        try {
            BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(filePath));
            //Writer Header
            HelperData.writeInt(4508365, outStream);

            int numberOfChannels = channelList.size();

            HelperData.writeFloat(frameSizeOfFeaturesInSecs, outStream);
            HelperData.writeInt(numberOfChannels, outStream);

            for (Channel channel : channelList) {
                channel.export(outStream);
            }
            outStream.close();
        } catch (IOException e) {
            XMLManager.logger.error("Could not export spectrum to file " + filePath);
            XMLManager.logger.error(Helper.getStackTrace(e));
        }
    }


    public static FilterBankManagerBandPass importDetectedPeriodicities(String inFilePath) {
        FilterBankManagerBandPass manager = new FilterBankManagerBandPass();

        try {
            BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(inFilePath));
            int header = HelperData.readInt(inStream);
            if (header != 4508365) {
                XMLManager.logger.error(String.format("Error importing data from file %s. Wrong header.", inFilePath));
                return null;
            }

            float frameSize = HelperData.readFloat(inStream);
            int numberOfChannels = HelperData.readInt(inStream);

            manager.channelList = new ArrayList<ChannelBandPass>();
            manager.frameSizeOfFeaturesInSecs = frameSize;
            for (int i = 0; i < numberOfChannels; i++) {
                manager.channelList.add(Channel.import_(inStream));
            }

            inStream.close();

        } catch (Exception e) {
            XMLManager.logger.error("Could not import spectrum from file " + inFilePath);
            XMLManager.logger.error(Helper.getStackTrace(e));
        }
        return manager;
    }


    /**
     * This method returns an array for further evaluation
     * the first index in the frame
     * The second index is the freq-weight pointer
     * The third index in the values behind
     *
     * @return
     */
    public float[][][] getDataForEvaluation() {
        int contextLength = 5; //Context length in frames. Must be odd.
        float energyThreshold = 0.03f;
        float maxDistancenSemitone = 0.3f;
        float maxDistanceInSemitoneForTheSameNote = 0.2f;

        int numberOfFrames = channelList.get(0).getDominantFrequencies().length;
        float[][][] out = new float[numberOfFrames][][];
        float[][][] outFinal = new float[numberOfFrames][2][0];

        for (int i = 0; i < numberOfFrames; i++) {
            out[i] = getDataForFrame(i);
        }

        //Now remove harmonics with very low energies

        for (int i = contextLength / 2; i < numberOfFrames - contextLength / 2 - 1; i++) {   //i is the center frame for which all calculations are performed
//            logger.info(String.originalFormat("Processing frame %d", i));
            float averageSumFrameEnergy = 0;
            for (int j = i - contextLength / 2; j < i + contextLength / 2 + 1; j++) {
                float[] energiesArray = out[j][1];
                for (int k = 0; k < energiesArray.length; k++) {
                    averageSumFrameEnergy += energiesArray[k];
                }
            }
            averageSumFrameEnergy /= contextLength;

            //Now delete vfreqs with very low energies for frame i
            float[] energiesArray = out[i][1];
            for (int k = 0; k < energiesArray.length; k++) {
                if (energiesArray[k] < averageSumFrameEnergy * energyThreshold) {
                    energiesArray[k] = 0;
                    out[i][0][k] = -1f; //Mark frequency as non-existing
                }
            }

            //Now apply median filtering to calculate values for frame i
            List<float[]> newValuesForFrameList = new ArrayList<float[]>();
            for (int freqIndex = 0; freqIndex < out[i][1].length; freqIndex++) {
                float currentFreq = out[i][0][freqIndex];
                if (currentFreq < 0) {
                    continue;
                }
                float[] arrayForMedianFiltering = new float[contextLength];
                float[] arrayOfEnergiesForstore = new float[contextLength];

                for (int j = i - contextLength / 2; j < i + contextLength / 2 + 1; j++) {
                    float[] freqsArray = out[j][0];
                    float[] energyArray = out[j][1];
                    double minDistanceFound = maxDistancenSemitone;
                    for (int k = 0; k < freqsArray.length; k++) {
                        double currentDistance = Helper.getSemitoneDistanceAbs(freqsArray[k], currentFreq);
                        if (currentDistance < maxDistancenSemitone && currentDistance < minDistanceFound) {
                            arrayForMedianFiltering[j - i + contextLength / 2] = freqsArray[k];
                            arrayOfEnergiesForstore[j - i + contextLength / 2] = energyArray[k];
                            minDistanceFound = currentDistance;
                        }
                    }
                }
                float[] copy = Arrays.copyOf(arrayForMedianFiltering, arrayForMedianFiltering.length);
                Arrays.sort(arrayForMedianFiltering);
                float newFreqValue = arrayForMedianFiltering[contextLength / 2];
                int oldIndex = HelperArrays.findIndexValue(copy, newFreqValue);
                float newEnergyValue = arrayOfEnergiesForstore[oldIndex];
                if (newFreqValue > 0) {
                    newValuesForFrameList.add(new float[]{newFreqValue, newEnergyValue});
                }
            }

            //Now remove duplicates from different filters in the filterBank
            boolean modified = true;
            while (modified) {
                modified = false;
                for (int s = 0; s < newValuesForFrameList.size(); s++) {
                    for (int g = 0; g < newValuesForFrameList.size(); g++) {
                        if (g != s && Helper.getSemitoneDistanceAbs(newValuesForFrameList.get(g)[0], newValuesForFrameList.get(s)[0]) < maxDistanceInSemitoneForTheSameNote) {
                            if (newValuesForFrameList.get(g)[1] > newValuesForFrameList.get(s)[1]) {
                                newValuesForFrameList.remove(s);
                                modified = true;
                                continue;
                            } else {
                                newValuesForFrameList.remove(g);
                                modified = true;
                                continue;
                            }
                        }
                    }
                }
            }


            float[][] newValuesForFrame = new float[2][newValuesForFrameList.size()];
            int counter = 0;
            for (float[] record : newValuesForFrameList) {
                newValuesForFrame[0][counter] = record[0];
                newValuesForFrame[1][counter++] = record[1];
            }


            outFinal[i] = newValuesForFrame;
        }
        return outFinal;
    }

    public float[][] getDataForFrame(int frameIndex) {
        List<Float> freqs = new ArrayList<Float>();
        List<Float> weights = new ArrayList<Float>();

        for (Channel chan : channelList) {
            if (chan.getDominantFrequencies().length > frameIndex) {
                float freq = chan.getDominantFrequencies()[frameIndex];
                if (freq > 0) {
                    freqs.add(freq);
                    weights.add(chan.getDominantFrequencyEnergies()[frameIndex]);
                }
            }
        }

        float[][] out = new float[2][freqs.size()];
        for (int i = 0; i < freqs.size(); i++) {
            out[0][i] = freqs.get(i);
            out[1][i] = weights.get(i);
        }
        return out;
    }


    /**
     * New version that includes weak harmonics filtering out and median filtering
     *
     * @return
     */
    public SpectrumImpl getSpectrum() {
        float sampleRate = 8000f;
        int numberOfFreqBins = 2048;
        float sampRateSpectrum = 1 / frameSizeOfFeaturesInSecs;

        float[][] spectrumData = new float[channelList.get(0).getDominantFrequencyEnergies().length][numberOfFreqBins];

        SpectrumImpl spectrum = new SpectrumImplMatrixData(spectrumData, sampleRate, sampRateSpectrum);
        int sampleNumber = Math.round(spectrumData.length * frameSizeOfFeaturesInSecs * sampleRate);
        spectrum.setSampleNumber(sampleNumber);

        float[][][] data = getDataForEvaluation();
        float[][] magSpec = spectrum.getMagSpec();
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i][0].length; j++) {
                float freq = data[i][0][j];
                float energy = data[i][1][j];
                int spectralIndex = spectrum.freq2index(freq);
                if (magSpec[i].length > spectralIndex && magSpec[i][spectralIndex] < energy) {
                    magSpec[i][spectralIndex] = energy;
                }
            }
        }
        spectrum.setMagSpec(magSpec);

        return spectrum;
    }

    /**
     * Old version that forms Spectrogram directly from channels without any post-processing
     *
     * @return
     */
    public SpectrumImpl getSpectrum_() {
        float sampleRate = 8000f;
        int numberOfFreqBins = 2048;
        float sampRateSpectrum = 1 / frameSizeOfFeaturesInSecs;

        float[][] spectrumData = new float[channelList.get(0).getDominantFrequencyEnergies().length][numberOfFreqBins];

        SpectrumImpl spectrum = new SpectrumImplMatrixData(spectrumData, sampleRate, sampRateSpectrum);
        int sampleNumber = Math.round(spectrumData.length * frameSizeOfFeaturesInSecs * sampleRate);
        XMLManager.logger.info(String.format("%d %f %d", spectrumData.length, frameSizeOfFeaturesInSecs, sampleNumber));
        spectrum.setSampleNumber(sampleNumber);

        for (Channel channel : channelList) {
            channel.contribute(spectrum);
        }

        return spectrum;
    }

    public float getFrameSizeOfFeaturesInSecs() {
        return frameSizeOfFeaturesInSecs;
    }

    public List<ChannelBandPass> getChannelListPass() {
        return this.channelList;
    }

    public static void main(String[] args) {
//        FilterBankManager manager = new FilterBankManager(new AudioReaderImpl("data/1.wav"), CONFIG_PATH, 0.02f);
//        manager.detectPeriodicities();
//        SpectrumImpl spectrum = manager.getSpectrum();

//        List<String> files = Helper.readLinesFromTextFile(args[0]);
//        String outFolder = args[1];
//
//        String outFilePath;
//        for(String file:files){
//            outFilePath = outFolder + File.separator + HelperFile.getShortFileName(file);
//            logger.info(String.originalFormat("Processing file %s -> %s", file, outFilePath));
//            FilterBankManager.importDetectedPeriodicities(file).exportDetectedPeriodicities(outFilePath);
//        }

        FilterBankManager man1 = FilterBankManagerBandPass.importDetectedPeriodicities("D:\\temp\\compare\\01_-_I_Saw_short_old.chan");
        FilterBankManager man2 = FilterBankManagerBandPass.importDetectedPeriodicities("D:\\temp\\compare\\01_-_I_Saw_short_new100.chan");
        System.out.println("");

    }

}
