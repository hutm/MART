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

package org.mart.crs.analysis.filter.PQMF;

import org.apache.log4j.Logger;
import org.mart.crs.analysis.filterbank.FilterBankManagerBandPass;
import org.mart.crs.core.AudioReader;
import org.mart.crs.logging.CRSLogger;

/**
 * @version 1.0 Jul 14, 2009 11:31:24 PM
 * @author: Maksim Khadkevich
 */
public class PQMFHarmonicExtraction {

    protected static Logger logger = CRSLogger.getLogger(PQMFHarmonicExtraction.class);

    //If set to false, the output contains all detected harmonics
    private static boolean wrapHarmonicsToFundamentals = true;

    private FilterBankManagerBandPass pqmfManager;

    private static final float maxDeviation = 0.2f; //In semitone scale

    public PQMFHarmonicExtraction(String audioFilePath, String configFilePath, float frameSizeOfFeaturesInSecs) {
        this.pqmfManager = new FilterBankManagerBandPass(new AudioReader(audioFilePath), configFilePath, frameSizeOfFeaturesInSecs);
    }

    public float[][] extractHarmonics() {
        pqmfManager.detectPeriodicities();

        //TODO fix it when working with pitch extraction
//        //Output data in Spectrogram originalFormat
//        float[][] outData = null;
//
//        //This data structure contains information on presice frequency estimates
//        List<DetectedHarmonic>[] detectedHarmonics = null;
//
//        float outDataSamplingFreq = samplingRate;
//
//        AudioReaderImpl audioReader = null, audioReaderFilteredSamples;
//        float samplingFreqOfAudio;
//        int channelNumber;
//        int numberOfPointsCC;
//        boolean isSamplingFreqChanged;
//
//        float[] filteredSamples;
//        SpectrumCrossCorrelationBasedImpl spectrumCCB;
//        for (ChannelBandPass channel : channelList) {
//            samplingFreqOfAudio = channel.getSamplingFreq();
//            channelNumber = channel.getChannelnumber();
//            numberOfPointsCC = channel.getNumberOfPointsCC();
//            isSamplingFreqChanged = samplingRate != samplingFreqOfAudio;
//            samplingRate = samplingFreqOfAudio;
//            crossCorrSpectrNumberOfBins = numberOfPointsCC;
//
//            if (isSamplingFreqChanged || audioReader == null) {
//                audioReader = new AudioReaderImpl(audioFilePath);
//                if (outData == null) {
//                    int numberOfFrames = (int) Math.floor(audioReader.getDuration() / blockSize);
//                    outData = new float[numberOfFrames][(int) audioReader.getSampleRate() / 2];
//                    detectedHarmonics = (List<DetectedHarmonic>[]) new ArrayList[numberOfFrames];
//                    //Now initialize all Lists
//                    for (int i = 0; i < numberOfFrames; i++) {
//                        detectedHarmonics[i] = new ArrayList<DetectedHarmonic>();
//                    }
//
//                }
//            }
//
//            PQMF pqmf = new PQMF(channelNumber, audioReader.getSampleRate());
//            filteredSamples = pqmf.filterSamples(audioReader.getSamples());
//            audioReaderFilteredSamples = new AudioReaderImpl(filteredSamples, audioReader.getSampleRate());
//            spectrumCCB = new SpectrumCrossCorrelationBasedImpl(filteredSamples, audioReader.getSampleRate(), crossCorrSpectrNumberOfBins);
//
//
//            float[] detectedFreqs;
//            int numberOfFreq;
//            int delayInSamples;
//
//            for (int i = 0; i < outData.length; i++) {
//                numberOfFreq = (int) Math.floor(samplingFreqOfAudio * blockSize);
//                detectedFreqs = new float[numberOfFreq];
//
//                //Since filters introduce a delay, take it into account.
//                if (audioReaderFilteredSamples.getSampleRate() == samplingRate) {
//                    delayInSamples = (PQMF.ORDER / 2);
//                } else {
//                    delayInSamples = 0;
//                }
//
//                for (int j = i * numberOfFreq + delayInSamples; j < (i + 1) * numberOfFreq + delayInSamples; j++) {
//                    try {
//                        detectedFreqs[j - i * numberOfFreq - delayInSamples] = analyzeSCCBFrame_interpolate(spectrumCCB, j);
//                    } catch (ArrayIndexOutOfBoundsException e) {
//                        logger.error("ArrayIndexOutOfBoundsException :", e);
//                    }
//                }
//
//                //Now calculate RMSEnergy
//                float RMSEnergy = AudioHelper.getRMSEnergy(audioReader.getSamples(), i * numberOfFreq + delayInSamples, (i + 1) * numberOfFreq + delayInSamples);
//
//                float[] meanValueAndDeviation = calculateMeanAndStandardDeviationIgnoreNegatives(detectedFreqs);
//                if (Math.abs(Helper.getSemitoneDistance(meanValueAndDeviation[0], meanValueAndDeviation[0] - meanValueAndDeviation[1])) < maxDeviation && meanValueAndDeviation[1] != 0) {
//                    int outFreqIndex = SpectrumImpl.freq2index(meanValueAndDeviation[0], outDataSamplingFreq / windowLength);
//                    outData[i][outFreqIndex] = RMSEnergy;
////                    outData[i][outFreqIndex] = 20;
//                    detectedHarmonics[i].add(new DetectedHarmonic(meanValueAndDeviation[0], RMSEnergy, i));
//                }
//
//            }
//
//
//        }
//        samplingRate = outDataSamplingFreq;
//
//
//        if (!wrapHarmonicsToFundamentals) {
//            return outData;
//        } else {
//            //Detect harmonics
//            for (int i = 0; i < detectedHarmonics.length; i++) {
//                Collections.sort(detectedHarmonics[i]);
////                float[] floatData = new float[detectedHarmonics[i].size()];;
////                for (int j = 0; j < floatData.length; j++) {
////                    floatData[j] = detectedHarmonics[i].get(j).getFreq();
////                }
//
//                DetectedHarmonic[] fundamentals = PitchExtractorStandard.getF0s(detectedHarmonics[i].toArray(new DetectedHarmonic[]{}));
//                outData[i] = new float[outData[i].length];      //Set all zeros
//                for (int j = 0; j < fundamentals.length; j++) {
//                    int outFreqIndex = SpectrumImpl.freq2index(fundamentals[j].getFreq(), outDataSamplingFreq / windowLength);
////                    outData[i][outFreqIndex] = 20;
//                    outData[i][outFreqIndex] = fundamentals[j].getAmp();
//                }
//            }
//
//            return outData;
//        }
//
        return null;

    }

}


