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

package org.mart.crs.audio.musique;

import org.mart.crs.audio.musique.decoder.DecoderInputStreamArray;

import javax.sound.sampled.AudioFormat;

/**
 * @version 1.0 28-Jul-2010 16:53:35
 * @author: Hut
 */
public class AudioSampleInputStreamArray extends AudioSampleInputStream {

    protected long samplesLength;


    public AudioSampleInputStreamArray(short[] samples, float desiredSampleRate) {
        this.samplesLength = samples.length;
        this.decoderInputStream = new DecoderInputStreamArray(samples);
        originalFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                desiredSampleRate,
                16,
                1,
                2,
                desiredSampleRate,
                false);
        desiredFormat = originalFormat;
        initialize();
    }


    protected long getTotalSamples() {
        return samplesLength;
    }

}
