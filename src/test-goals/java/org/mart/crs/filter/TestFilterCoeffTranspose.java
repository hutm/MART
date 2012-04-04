package org.mart.crs.filter;

import junit.framework.TestCase;
import org.mart.crs.audio.resample.Polyphase;
import org.mart.crs.config.ExecParams;
import org.mart.crs.core.AudioReader;
import org.mart.crs.utils.helper.HelperArrays;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * @version 1.0 Jun 8, 2009 9:23:12 PM
 * @author: Maksim Khadkevich
 */
public class TestFilterCoeffTranspose extends TestCase{

    public static final String  filename = "";
    public static final String  filename_out = "";

    public  void testFilterCoeffTranspose() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename_out));

        List<String> rowData = new ArrayList<String>();
        List<List<String>> rows = new ArrayList<List<String>>();

        //Read values
        String line, token ;
        while((line= reader.readLine()) != null && line.length() > 1){
            StringTokenizer tokenizer = new StringTokenizer(line);
            while(tokenizer.hasMoreTokens()){
                token = tokenizer.nextToken();

            }

        }


    }


    public void testResampler() {
        float outSamplingRate = 11025f;

        ExecParams._initialExecParameters.samplingRate = 44100f;
        AudioReader audioReader = new AudioReader("d:/My Documents/!audio/4.wav");
        Polyphase filter = new Polyphase(44100f, outSamplingRate);


        float[] samples = audioReader.getSamples();
        float[] samplesOutGlobal = new float[0];

        float[] outFloats = new float[0];
        int currentSample = 0;
        while (currentSample < samples.length - 1024) {

            int[] intSamples = new int[1024];

            for (int i = 0; i < intSamples.length; i++) {
                intSamples[i] = (int) samples[currentSample + i ];
            }
            int[] out = filter.resample(intSamples);
            outFloats = new float[out.length];
            for (int i = 0; i < outFloats.length; i++) {
                outFloats[i] = out[i];
            }
            samplesOutGlobal = HelperArrays.concat(samplesOutGlobal, outFloats);
            currentSample += 1024;
        }

//        int[] outTail = filter.drain();
//        float[] outFloats = new float[out.length];
//        for (int i = 0; i < outFloats.length; i++) {
//            outFloats[i] = out[i];
//        }

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

        AudioReader audioReader_ = new AudioReader(samplesOutGlobal, outSamplingRate);
        audioReader_.exportWavFile("cd_5_1000Polyphase", 0, 0);
    }

}
