package simpleClasses;

import junit.framework.TestCase;
import org.mart.crs.config.ExecParams;
import org.mart.crs.core.AudioReader;
import org.mart.crs.core.spectrum.reassigned.ReassignedFromMatlabFitz;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrum;

/**
 * @version 1.0 18-Oct-2010 21:48:23
 * @author: Hut
 */
public class TestReasFitzAndOurs extends TestCase {

    public void testReasFitz() {

        float[] samples = new float[512];
        for(int i = 0; i < samples.length; i++){
            samples[i] = 30000*(float)Math.sin(i/3.0f);
        }
        AudioReader reader = new AudioReader(samples, 11025);
        ReassignedSpectrum spectrum = new ReassignedSpectrumLikeFitz(reader, 0, samples.length, 512, ExecParams._initialExecParameters.windowType, 0.5f, ExecParams._initialExecParameters);
        spectrum.getMagSpec();
    }


    public void testReasOurs() {
       float[] samples = new float[512];
        for(int i = 0; i < samples.length; i++){
            samples[i] = 30000*(float)Math.sin(i/3);
        }
        AudioReader reader = new AudioReader(samples, 11025);
        ReassignedSpectrum spectrum = new ReassignedFromMatlabFitz(reader, 0, samples.length, 512, ExecParams._initialExecParameters.windowType, 0, ExecParams._initialExecParameters.reassignedSpectrogramThreshold, ExecParams._initialExecParameters);
        spectrum.getMagSpec();
    }


}
