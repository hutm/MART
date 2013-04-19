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

package org.mart.crs.core.pcp;

import org.mart.crs.analysis.filterbank.Channel;
import org.mart.crs.analysis.filterbank.FilterBankManager;
import org.mart.crs.core.AudioReader;
import org.mart.crs.core.spectrum.SpectrumCombTime;
import org.mart.crs.utils.helper.Helper;

import java.util.List;

/**
 * @version 1.0 26-May-2010 13:31:37
 * @author: Hut
 */
public class PCPCombTemporal extends PCPComb {


    protected PCPCombTemporal() {
    }

    protected PCPCombTemporal(double refFreq, int averagingFactor, int numberOfBinsPerSemitone, boolean toNormalize, int startNoteForPCPWrapped, int endNoteForPCPWrapped, float chromaSpectrumRate) {
        super(refFreq, averagingFactor, numberOfBinsPerSemitone, toNormalize, startNoteForPCPWrapped, endNoteForPCPWrapped, chromaSpectrumRate);
    }

    public void init(String audioFilePath, String filterConfigurationFilePath) {
        FilterBankManager manager = new FilterBankManager(new AudioReader(audioFilePath), filterConfigurationFilePath, (float)frameSizePCP);
        List<Channel> channelList = manager.getChannelList();


        for (Channel aChannel : channelList) {
            int midiNumber = aChannel.getChannelnumber();
            float samplingFreq = aChannel.getSamplingFreq();

            AudioReader audioReader = new AudioReader(audioFilePath, samplingFreq);
            float[] out = aChannel.filterSamples(audioReader.getSamples());

            audioReader = new AudioReader(out, audioReader.getSampleRate());

            int frameLengthInSamples = (int)Math.round(frameSizePCP * samplingFreq);

            SpectrumCombTime spectrum = new SpectrumCombTime(audioReader.getSamples(), audioReader.getSampleRate(), frameLengthInSamples, 0, Helper.getFreqForMIDINote(midiNumber));
            spectrum.calculateHarmonicProfileVectors();

            sampleRatePCP = spectrum.getSampleRateSpectrum();

            noteProbabilities.put(midiNumber, spectrum.getHarmonicPVScore());
            scoresVectorMap.put(midiNumber, spectrum.getHarmonicProfileVectors());
        }

        constructPCP();
    }


//    protected void init(String audioFilePath, String filterConfigurationFilePath) {
//        String audioFileName = audioFilePath;
//        List<String[]> filtersMap = HelperFile.readTokensFromFileStrings(filterConfigurationFilePath, 3);
//
//
//        for (String[] filterData : filtersMap) {
//            int midiNumber = parseInt(filterData[0]);
//            String samplingFreqString = filterData[1];
//            float samplingFreqOfAudio = Float.parseFloat(samplingFreqString);
//            int delayInSamples = parseInt(filterData[2]);
//
//            String filterFilePath = String.originalFormat("data/filters/%s_%s_%s.%s", String.valueOf(midiNumber), samplingFreqString, String.valueOf(delayInSamples), "flt");
//
//            logger.info("passing signal through filter " + filterFilePath);
//
//            Filter filter;
//            filter = FilterManager.getFilter(filterFilePath);
//
//            AudioReaderImpl audioReader = new AudioReaderImpl(audioFileName, samplingFreqOfAudio, 1);
//
//            float[] out = filter.process(audioReader.getSamples(), delayInSamples);
//            audioReader.setSamples(out);
//
//            int frameLengthInSamples = Math.round(frameSizePCP * samplingFreqOfAudio);
//
//            SpectrumCombTime spectrum = new SpectrumCombTime(audioReader.getSamples(), audioReader.getSampleRate(), frameLengthInSamples, 0, Helper.getFreqForMIDINote(midiNumber));
//            spectrum.calculateHarmonicProfileVectors();
//
//            sampleRatePCP = spectrum.getSampleRateSpectrum();
//
//            noteProbabilities.put(midiNumber, spectrum.getHarmonicPVScore());
//            scoresVectorMap.put(midiNumber, spectrum.getHarmonicProfileVectors());
//        }
//
//        constructPCP();
//    }


}
