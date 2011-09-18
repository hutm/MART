package org.mart.crs.core.spectrum;

import rasmus.interpreter.sampled.util.FFT;

/**
 * SpectrumImpl Tester.
 *
 * @author Hut
 * @since <pre>03/24/2011</pre>
 * @version 1.0
 */
public class SpectrumImplTest {


    @org.junit.Test
    public void testFFTAndFFTInverse(){
        double[] data = new double[64];
        for(int i = 0; i < data.length; i++){
            data[i] = Math.sin(i/16.0f);
        }
        FFT fft = new FFT(data.length);
        fft.calcReal(data, -2);
        fft.calcReal(data, 2);
    }



}
