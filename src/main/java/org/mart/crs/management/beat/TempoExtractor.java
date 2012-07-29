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

package org.mart.crs.management.beat;

import org.apache.log4j.Logger;
import org.mart.crs.config.Extensions;
import org.mart.crs.core.spectrum.SpectrumCrossCorrelationBasedImpl;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.beat.segment.BeatSegment;
import org.mart.crs.utils.helper.HelperArrays;
import org.mart.crs.utils.helper.HelperFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs periodicity analysis for extracting tempo from detection function
 *
 * @version 1.0 19/11/10 13:57
 * @author: Hut
 */
public class TempoExtractor {

    protected static Logger logger = CRSLogger.getLogger(TempoExtractor.class);

    public static final float CONTEXT_LENGTH = 3; //In seconds
    public static final float CONTEXT_STEP = 3; //In seconds

    public static final float MIN_TEMPO_PERIOD = 0.3f; //in seconds
    public static final float MAX_TEMPO_PERIOD = 0.7f;  //in seconds


    protected float[] onsetDetectionFunction;

    protected float sampleRate;

    protected SpectrumCrossCorrelationBasedImpl spectrumCrossCorrelationBased;


    public TempoExtractor(float[] onsetDetectionFunction, float sampleRate) {
        this.onsetDetectionFunction = onsetDetectionFunction;
        this.sampleRate = sampleRate;
    }


    public void extractBeats(String xmlPath) {
        float[] out = new float[onsetDetectionFunction.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = onsetDetectionFunction[i] * 1000;
        }
        spectrumCrossCorrelationBased = new SpectrumCrossCorrelationBasedImpl(out, sampleRate, 200);
        float startTime = 0;
        List<float[]> allBeats = new ArrayList<float[]>();
        while (getIndexForTimeInstant(startTime + CONTEXT_LENGTH) < onsetDetectionFunction.length) {
            float endTime = startTime + CONTEXT_STEP;
            float[] localOnsetDetection = getOnsetDetectionFunction(startTime, endTime);
            float[][] crossCorrelationSpectrumPart = HelperArrays.cut(spectrumCrossCorrelationBased.getMagSpec(), getIndexForTimeInstant(startTime), getIndexForTimeInstant(endTime));
            int tempoPeriodInSamples = getTempo(crossCorrelationSpectrumPart);
            float[] beats = getBeats(localOnsetDetection, tempoPeriodInSamples, startTime);
            allBeats.add(beats);
            startTime += CONTEXT_LENGTH;
        }
        saveBeats(allBeats, xmlPath);
    }


    protected int getTempo(float[][] spectrum) {
        float[] horizontalSum = new float[spectrum[0].length];
        for (int i = Math.round(MIN_TEMPO_PERIOD * sampleRate); i < Math.round(MAX_TEMPO_PERIOD * sampleRate); i++) {
            for (int j = 0; j < spectrum.length; j++) {
                horizontalSum[i] += spectrum[j][i];
            }
        }

        int maxIndex = HelperArrays.findIndexWithMaxValue(horizontalSum); //Period in samples of exracted tempo
        return maxIndex;
    }


    protected float[] getBeats(float[] localOnsetDetection, int tempoPeriodInSamples, float startTime){
        float[] phaseCandidates = new float[tempoPeriodInSamples];
        for(int i = 0; i < localOnsetDetection.length; i++){
            int index = i % tempoPeriodInSamples;
            phaseCandidates[index] += localOnsetDetection[i];
        }
        int winningPhaseIndex = HelperArrays.findIndexWithMaxValue(phaseCandidates);

        float[] output = new float[localOnsetDetection.length/tempoPeriodInSamples + 1];
        for(int i = winningPhaseIndex; i< localOnsetDetection.length; i+= tempoPeriodInSamples){
            output[(i-winningPhaseIndex)/tempoPeriodInSamples] = startTime + i / sampleRate;
        }
        return output;
    }


    protected float[] getOnsetDetectionFunction(float startTime, float endTime) {
        int startIndex = getIndexForTimeInstant(startTime);
        int endIndex = getIndexForTimeInstant(endTime);
        float[] out = new float[endIndex - startIndex];
        System.arraycopy(onsetDetectionFunction, startIndex, out, 0, out.length);
        return out;
    }

    protected int getIndexForTimeInstant(float timeInstant) {
        return Math.min(Math.round(timeInstant * sampleRate), onsetDetectionFunction.length);
    }


    protected void saveBeats(List<float[]> beats, String xmlPath){
        List<BeatSegment> beatSegments = new ArrayList<BeatSegment>();
        for(float[] beatsPart:beats){
            for(int i = 0; i < beatsPart.length;i++){
                if (beatsPart[i] != 0) {
                    beatSegments.add(new BeatSegment(beatsPart[i], 0));
                }
            }
        }

        BeatStructure beatStructure = new BeatStructure(beatSegments);
        beatStructure.setSongName(HelperFile.getNameWithoutExtension(xmlPath + Extensions.WAV_EXT));
        beatStructure.serializeIntoXML(xmlPath);
    }



    //TODO: move to tests
    public static void main(String[] args) {
/*//        String fileName = "0001 - U2 - The Joshua Tree - With or without you";
//        String fileName = "09_-_Girl_short";
        String fileName = "all";

        String filePath = String.format("d:\\My Documents\\!audio\\%s.wav", fileName);
        String outXMLPath = String.format("d:\\My Documents\\!audio\\testXML\\%s.xml", fileName);
        AudioReader reader = new AudioReader(filePath, ExecParams._initialExecParameters.samplingRate);
        ReassignedSpectrum reassignedSpectrum =  new ReassignedSpectrum(reader, ExecParams._initialExecParameters);
        OnsetDetectionFunction onsetDetecionFunction = new OnsetDetectionFunction(reassignedSpectrum);
        TempoExtractor tempoExtractor = new TempoExtractor(onsetDetecionFunction.getDetectionFunction(), reassignedSpectrum.getSampleRateSpectrum());
        tempoExtractor.extractBeats(outXMLPath);*/
    }


}
