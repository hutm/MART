package org.mart.crs.utils.windowing;


import org.testng.annotations.Test;

/**
 * @version 1.0 19-Sep-2010 19:43:52
 * @author: Hut
 */
public class KaiserWindowTest {

    @Test
    public void testCreateKaiserWindow() {

        WindowFunction kaiser = new Kaiser();

        int winLength = 512;
        float[] kaiserWindow = new float[winLength];
        for (int i = 0; i < winLength; i++) {
            kaiserWindow[i] = kaiser.getFunction(i, 0, winLength);
        }

        float[] derivativeWindow = WindowFunction.calculateFunctionDerivativeViaFFT(kaiserWindow);

    }


}
