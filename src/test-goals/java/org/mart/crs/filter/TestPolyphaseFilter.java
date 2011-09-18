package org.mart.crs.filter;

import org.mart.crs.audio.resample.Polyphase;
import org.mart.crs.config.ExecParams;
import org.mart.crs.core.AudioReader;
import junit.framework.TestCase;

/**
 * @version 1.0 Nov 25, 2009 10:52:08 PM
 * @author: Maksim Khadkevich
 */
public class TestPolyphaseFilter extends TestCase {

    public void testResampler() {
        float outSamplingRate = 11025f;

        ExecParams._initialExecParameters.samplingRate = 44100f;
        AudioReader audioReader = new AudioReader("d:/My Documents/!audio/4.wav");
        Polyphase filter = new Polyphase(44100f, outSamplingRate);


        float[] samples = audioReader.getSamples();
        int[] intSamples = new int[samples.length];

        for (int i = 0; i < samples.length; i++) {
            intSamples[i] = (int) samples[i];
        }
        int[] out = filter.resample(intSamples);
//        int[] outTail = filter.drain();
        float[] outFloats = new float[out.length];
        for (int i = 0; i < outFloats.length; i++) {
            outFloats[i] = out[i];
        }

//        int[] intSamples = new int[samples.length];
//        for (int i = 0; i < samples.length; i++) {
//            intSamples[i] = (int) samples[i];
//        }
//        int[] out = filter.resample(intSamples);
////        int[] outTail = filter.drain();
//        float[] outFloats = new float[out.length];
//        for (int i = 0; i < outFloats.length; i++) {
//            outFloats[i] = out[i];
//        }
////        for(int i = 0; i < outTail.length; i++){
////            outFloats[i + out.length] = outTail[i];
////        }

        AudioReader audioReader_ = new AudioReader(outFloats, outSamplingRate);
        audioReader_.exportWavFile("data/cd_5_1000Polyphase", 0, 0);
    }

    public void testOldResampler(){


        ExecParams._initialExecParameters.samplingRate = 30000f;
        AudioReader audioReader = new AudioReader("D:\\installs\\audio\\work\\sweep.wav");
        audioReader.exportWavFile("D:\\installs\\audio\\work\\oldResampler.wav", 0, 0);
    }



}
