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

package org.mart.crs.core.spectrum;

import org.testng.annotations.Test;
import rasmus.interpreter.sampled.util.FFT;

/**
 * SpectrumImpl Tester.
 *
 * @author Hut
 * @since <pre>03/24/2011</pre>
 * @version 1.0
 */
public class SpectrumImplTest {


    @Test
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
