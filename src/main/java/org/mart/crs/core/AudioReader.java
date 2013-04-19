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

package org.mart.crs.core;

import org.apache.log4j.Logger;
import org.mart.crs.audio.musique.AudioSampleInputStream;
import org.mart.crs.audio.musique.AudioSampleInputStreamArray;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.AudioHelper;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;
import org.mart.crs.utils.helper.HelperFile;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * @version 1.0 Nov 26, 2009 5:13:52 PM
 * @author: Maksim Khadkevich
 */
public class AudioReader {

    protected static Logger logger = CRSLogger.getLogger(AudioReader.class);

    protected String audiofilePath;

    protected AudioSampleInputStream audioSampleInputStream;

    protected short[] samples;

    protected boolean storeSamplesInMemory;

    public AudioReader(short[] samples, float samplingRate) {
        this.samples = samples;
        this.audioSampleInputStream = new AudioSampleInputStreamArray(samples, samplingRate);
        storeSamplesInMemory = true;
    }

    public AudioReader(float[] samples, float samplingRate) {
        this(HelperArrays.getFloatAsShort(samples), samplingRate);
    }

    /**
     * Default constructor.
     *
     * @param audiofilePath Audio file filepath
     * @param samplingRate  Desired sampling rate of output samples
     */
    public AudioReader(String audiofilePath, float samplingRate) {
        this.audiofilePath = audiofilePath;
        this.audioSampleInputStream = new AudioSampleInputStream(audiofilePath, samplingRate);
        samples = null;
    }


    public AudioReader(String audiofilePath) {
        this(audiofilePath, 0);
    }


    public short[] getSamplesShort(int startSample, int endSample) {
        short[] out;

        if (endSample > audioSampleInputStream.getSampleCount() || endSample <= 0) {
            endSample = (int) audioSampleInputStream.getSampleCount() - 1;
        }
        if (startSample < 0) {
            startSample = 0;
        }

        out = new short[endSample - startSample];

        if (storeSamplesInMemory) {
            if (samples == null) {
                audioSampleInputStream.seek(0);
                samples = new short[(int) audioSampleInputStream.getSampleCount()];
                try {
                    audioSampleInputStream.readSamplesMono(samples);
                } catch (IOException e) {
                    logger.error(Helper.getStackTrace(e));
                }
            }
            System.arraycopy(samples, startSample, out, 0, out.length);
        } else {
            audioSampleInputStream.seek(startSample);

            try {
                audioSampleInputStream.readSamplesMono(out);
            } catch (IOException e) {
                logger.error(Helper.getStackTrace(e));
            }
        }
        return out;
    }


    public short[] getSamplesShort(float startTime, float endTime) {
        int startSample = Math.round(startTime * audioSampleInputStream.getSampleRate());
        int endSample = Math.round(endTime * audioSampleInputStream.getSampleRate());
        return getSamplesShort(startSample, endSample);
    }


    public short[] getSamplesShort() {
        return getSamplesShort(0, (int) audioSampleInputStream.getSampleCount());
    }

    public float[] getSamples(int startSample, int endSample) {
        return HelperArrays.getShortAsFloat(getSamplesShort(startSample, endSample));
    }


    public float[] getSamples(float startTime, float endTime) {
        return HelperArrays.getShortAsFloat(getSamplesShort(startTime, endTime));
    }


    public float[] getSamples() {
        return HelperArrays.getShortAsFloat(getSamplesShort());
    }


    public String getFilePath() {
        return audiofilePath;
    }

    public AudioSampleInputStream getAudioSampleInputStream() {
        return audioSampleInputStream;
    }

    public float getDuration() {
        float sampleRate = audioSampleInputStream.getSampleRate();
        return audioSampleInputStream.getSampleCount() / sampleRate;
    }


    public AudioFormat getAudioFormat() {
        return audioSampleInputStream.getFormat();
    }

    public float getSampleRate() {
        return audioSampleInputStream.getSampleRate();
    }

    public void changeSamplingRate(float newSamplingRate) {
        this.audioSampleInputStream = new AudioSampleInputStream(audiofilePath, newSamplingRate);
        samples = null;
    }

    public void setStoreSamplesInMemory(boolean storeSamplesInMemory) {
        this.storeSamplesInMemory = storeSamplesInMemory;
    }


    public void exportWavFile(String outFileName) {
        exportWavFile(outFileName, 0, getDuration());
    }


    public int getSampleIndexForTimeInstant(float time){
        return Math.round(time * audioSampleInputStream.getFormat().getSampleRate());
    }

    public void exportWavFile(String outFileName, float startTime, float endTime) {
        short[] samples;
        if (endTime == 0) {
            samples = getSamplesShort();
        } else {
            samples = getSamplesShort(startTime, endTime);
        }
        byte[] streamData = AudioHelper.getDataForStream(samples);
        ByteArrayInputStream bais = new ByteArrayInputStream(streamData);
        AudioInputStream ais = new AudioInputStream(bais, getAudioFormat(), streamData.length / getAudioFormat().getFrameSize());
        try {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, HelperFile.getFile(outFileName + ".wav"));
        } catch (IOException e) {
            logger.error("Unexpected error occured", e);
            logger.error(Helper.getStackTrace(e));
        }
    }


    //TODO some problems with correct sample length in the output wav file
    public static void storeAPieceOfMusicAsWav(float[] samples, AudioFormat audioFormat, String outFileName) {
        try {
            byte[] byte_array = AudioHelper.getDataForStream(samples);
            long length = (long) (byte_array.length / ( audioFormat.getFrameSize()));
            ByteArrayInputStream bais = new ByteArrayInputStream(byte_array);
            AudioInputStream audioInputStreamTemp = new AudioInputStream(bais, audioFormat, length);
            File fileOut = new File(outFileName);
            AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

            if (AudioSystem.isFileTypeSupported(fileType, audioInputStreamTemp)) {
                AudioSystem.write(audioInputStreamTemp, fileType, fileOut);
            }
        } catch (Exception e) {
            logger.error("Unexpected error occured", e);
            logger.error(Helper.getStackTrace(e));
        }
    }


}





