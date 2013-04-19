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

package org.mart.crs.audio.resample;

/**
 * @version 1.0 06-May-2010 17:33:47
 * @author: Hut
 */
public abstract class CRSResampler {

    public abstract int[] resample(int[] inSamples);

    public float[] resample(float[] samples) {
        float[] output;
        int[] data = new int[samples.length];
        for (int j = 0; j < samples.length; j++) {
            data[j] = Math.round(samples[j]);
        }

        int[] outputResampler = resample(data);

        output = new float[outputResampler.length];
        for (int j = 0; j < output.length; j++) {
            output[j] = outputResampler[j];
        }
        return output;
    }


    public static CRSResampler getResampler(float oldSamplingRate, float newSamplingRate) {
        CRSResampler resampler;
        resampler = new Polyphase(oldSamplingRate, newSamplingRate);
        return resampler;
    }


}
