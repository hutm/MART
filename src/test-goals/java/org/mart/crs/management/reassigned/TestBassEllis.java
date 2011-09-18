package org.mart.crs.management.reassigned;

import org.mart.crs.config.ExecParams;
import org.mart.crs.core.AudioReader;
import org.mart.crs.core.spectrum.SpectrumImpl;
import com.jamal.JamalException;
import com.jamal.client.MatlabClient;
import junit.framework.TestCase;

/**
 * @version 1.0 20-Sep-2010 01:15:29
 * @author: Hut
 */
public class TestBassEllis extends TestCase {


    public void testExtractBassChroma() {
        AudioReader reader = new AudioReader("d:/My Documents/!audio/all_short.wav", 11025);
        float[] samples = reader.getSamples();
        try {
            MatlabClient matlabClient = new MatlabClient();

            //First we pass an array of integers and calculate sum in Matlab
            Object[] inArgs = new Object[3];
            inArgs[0] = samples;
            inArgs[1] = new Double(reader.getSampleRate());
            inArgs[2] = new Integer(ExecParams._initialExecParameters.windowLength);

            Object[] outputArgs = matlabClient.executeMatlabFunction("getChroma", inArgs, 3);
            double[] bass = (double[]) outputArgs[0];
            double[] treble = (double[]) outputArgs[1];

            float[][] out = new float[bass.length / 12][12];
            for (int i = 0; i < out.length; i++) {
                for (int j = 0; j < 12; j++) {
                    out[i][j] = (float) bass[i * 12 + j];
                }
            }

            System.out.println("");
//            matlabClient.executeMatlabFunction("quit", null, 0);

        } catch (JamalException e) {
            e.printStackTrace();
        }

    }


    public void testFitzSpectrogram() {
        AudioReader reader = new AudioReader("d:/My Documents/!audio/all_short.wav", 11025);
        float[] samples = reader.getSamples();

        try {
            MatlabClient matlabClient = new MatlabClient();

            //First we pass an array of integers and calculate sum in Matlab
            Object[] inArgs = new Object[5];
            inArgs[0] = samples;
            inArgs[1] = new Integer(ExecParams._initialExecParameters.windowLength);
            inArgs[2] = new Double(reader.getSampleRate());
            inArgs[3] = new Integer(ExecParams._initialExecParameters.windowLength);
            inArgs[4] = new Integer(ExecParams._initialExecParameters.windowLength / 10);

            Object[] outputArgs = matlabClient.executeMatlabFunction("racomponents", inArgs, 3);
            double[] stftData = (double[]) outputArgs[0];
            double[] freq = (double[]) outputArgs[1];
            double[] time = (double[]) outputArgs[2];

            int spectrumLength = (ExecParams._initialExecParameters.windowLength / 2);
            int frameNumber = stftData.length / spectrumLength;

            float[][] magSpec = new float[stftData.length][8192];
            for (int i = 0; i < frameNumber; i++) {
                for(int j = 0; j < (ExecParams._initialExecParameters.windowLength / 2); j++){
                    float energy = (float)stftData[i * spectrumLength + j];
                    float freqValue = (float)freq[i * spectrumLength + j];
                    float timeValue = (float)time[i * spectrumLength + j];

                    int frameIndexFinal = Math.round(timeValue / reader.getSampleRate() / ExecParams._initialExecParameters.windowLength * (1 - ExecParams._initialExecParameters.overlapping));
                    int freqIndexFinal = SpectrumImpl.freq2index(freqValue, (float) (0.5 * reader.getSampleRate()) / 8192);
                    if(frameIndexFinal >= 0 && frameIndexFinal < magSpec.length && freqIndexFinal >= 0 && frameIndexFinal < magSpec[0].length){
                        magSpec[frameIndexFinal][freqIndexFinal] += energy;
                    }
                }
            }

            System.out.println("");
//            matlabClient.executeMatlabFunction("quit", null, 0);

        } catch (JamalException e) {
            e.printStackTrace();
        }

    }


    public void testshutDown() {
        try {
            MatlabClient matlabClient = new MatlabClient();

            matlabClient.executeMatlabFunction("quit", null, 0);

        } catch (JamalException e) {
            e.printStackTrace();
        }


    }


}
