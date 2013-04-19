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


    @Test
    public void testHanningWindow() {

        WindowFunction hanning = new Hanning();

        int winLength = 1024;
        float[] window = new float[winLength];
        float[] timeWeighted = new float[winLength];
        float[] freqWeighted = new float[winLength];
        float[] timeFreqWeighted = new float[winLength];
        for (int i = 0; i < winLength; i++) {
            window[i] = hanning.getFunction(i, 0, winLength);
            timeWeighted[i] = hanning.getFunctionTimeWeighted(i, 0, winLength, 4000);
            freqWeighted[i] = hanning.getFunctionFrequencyWeighted(i, 0, winLength, 4000);
            timeFreqWeighted[i] = hanning.getFunctionTimeFrequencyWeighted(i, 0, winLength, 4000);
        }

        System.out.println("done");

    }


}
