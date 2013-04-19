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

package org.mart.crs.utils;

import org.apache.log4j.Logger;
import org.mart.crs.core.AudioReader;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;

import javax.sound.sampled.*;
import java.io.IOException;

/**
 * AudioHelper class presents some methods for working with audio stream
 */


public class AudioHelper {


    protected static Logger logger = CRSLogger.getLogger(AudioHelper.class);


  

    public static float getRMSEnergy(float[] samples, int startSample, int endSample) {
        double sum = 0.0;
        for (int samp = startSample; samp < endSample; samp++) {
            sum += Math.pow(samples[samp], 2);
        }
        float rms = (float) Math.sqrt(sum / (endSample - startSample));

        return rms;
    }


    /**
     * Mono samples calculator
     *
     * @param buf       buf
     * @param bytesRead bytesRead
     * @return array of samples
     */
    public static float[] getSamplesMono(byte[] buf, int bytesRead, boolean isBigEndian) {
        float out[] = new float[bytesRead / 2];
        for (int i = 0; i < bytesRead / 2; i++) {
            if (!isBigEndian) {
                out[i] = (short) (((buf[i * 2 + 1] & 0xff) << 8) |
                        ((buf[i * 2] & 0xff)));
            } else {
                out[i] = (short) (((buf[i * 2] & 0xff) << 8) |
                        ((buf[i * 2 + 1] & 0xff)));
            }
        }
        return out;
    }


    public static short[] getSamplesMonoAsShorts(byte[] buf, int bytesRead, boolean isBigEndian) {
        short out[] = new short[bytesRead / 2];
        for (int i = 0; i < bytesRead / 2; i++) {
            if (!isBigEndian) {
                out[i] = (short) (((buf[i * 2 + 1] & 0xff) << 8) |
                        ((buf[i * 2] & 0xff)));
            } else {
                out[i] = (short) (((buf[i * 2] & 0xff) << 8) |
                        ((buf[i * 2 + 1] & 0xff)));
            }
        }
        return out;
    }


    public static short[] decodeBytes(byte[] buf, int bytesRead, boolean isBigEndian) {
        short out[] = new short[bytesRead / 2];
        for (int i = 0; i < bytesRead / 2; i++) {
            if (!isBigEndian) {
                out[i] = (short) (((buf[i * 2 + 1] & 0xff) << 8) |
                        ((buf[i * 2] & 0xff)));
            } else {
                out[i] = (short) (((buf[i * 2] & 0xff) << 8) |
                        ((buf[i * 2 + 1] & 0xff)));
            }
        }
        return out;
    }


    /**
     * Given input samples, generate byte array to be sent to stream
     *
     * @param samples
     * @return
     */
    public static byte[] getDataForStream(short[] samples) {
        int bufferSize = samples.length * 2;
        byte[] buffer = new byte[bufferSize];
        short shortValue;
        for (int i = 0; i < samples.length; i++) {
            buffer[2 * i] = (byte) ((samples[i] >>> 0) & 0xFF);
            buffer[2 * i + 1] = (byte) ((samples[i] >>> 8) & 0xFF);
        }
        return buffer;
    }

    public static byte[] getDataForStream(short[] samples, int samplesRead) {
        short[] samplesThatHasBeenRead = new short[samplesRead];
        System.arraycopy(samples, 0, samplesThatHasBeenRead, 0, samplesRead);
        return getDataForStream(samplesThatHasBeenRead);
    }


    public static byte[] getDataForStream(float[] samples) {
        return getDataForStream(HelperArrays.getFloatAsShort(samples));
    }

    public static byte[] getDataForStream(float[] samples, int samplesRead) {
        return getDataForStream(HelperArrays.getFloatAsShort(samples), samplesRead);
    }

    /**
     * Plays audio from the given audio input stream.
     */
    public static void playAudioStream(AudioInputStream audioInputStream, int startFrame, int framesToRead) {
        // Audio originalFormat provides information like sample rate, size, channels.
        AudioFormat audioFormat = audioInputStream.getFormat();
        System.out.println("Play input audio Format=" + audioFormat);

        // Open a data line to play our type of sampled audio.
        // Use SourceDataLine for play and TargetDataLine for record.
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Play.playAudioStream does not handle this type of audio on this system.");
            return;
        }

        try {
            // Create a SourceDataLine for play back (throws LineUnavailableException).
            SourceDataLine dataLine = (SourceDataLine) AudioSystem.getLine(info);
            // System.out.println( "SourceDataLine class=" + dataLine.getClass() );


            // The line acquires system resources (throws LineAvailableException).
            dataLine.open(audioFormat);

            // Adjust the volume on the output line.
            if (dataLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl volume = (FloatControl) dataLine.getControl(FloatControl.Type.MASTER_GAIN);
                volume.setValue(2.0F);
            }

            // Allows the line to move data in and out to a port.
            dataLine.start();

            // Create a buffer for moving data from the audio stream to the line.
            int bufferSize = (int) audioFormat.getSampleRate() * audioFormat.getFrameSize();
            byte[] buffer = new byte[bufferSize];


            // Move the data until done or there is an error.
            try {
                int bytesRead = 0;
                int framesWritten = 0;

                //First skip some frames if needed
                if (startFrame > 0) {
                    byte[] skipBuffer = new byte[startFrame * audioFormat.getFrameSize()];
                    audioInputStream.read(skipBuffer);
                    skipBuffer = null;
                    System.gc();
                }


                while (bytesRead >= 0) {
                    if (framesToRead > 0 && framesWritten >= framesToRead) {
                        break;
                    }
                    bytesRead = audioInputStream.read(buffer, 0, buffer.length);
                    if (bytesRead >= 0) {
                        if (framesToRead > 0) {
                            framesWritten += dataLine.write(buffer, 0, Math.min(bytesRead, (framesToRead - framesWritten) * audioFormat.getFrameSize())) / audioFormat.getFrameSize();
                        } else {
                            framesWritten += dataLine.write(buffer, 0, bytesRead);
                        }
                    }
                } // while
            } catch (IOException e) {
                logger.error("Smth is wrong");
                logger.error(Helper.getStackTrace(e));
            }

            System.out.println("Play.playAudioStream draining line.");
            // Continues data line I/O until its buffer is drained.
            dataLine.drain();

            System.out.println("Play.playAudioStream closing line.");
            // Closes the data line, freeing any resources such as the audio device.
            dataLine.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    } // playAudioStream



    public static  void play(AudioReader reader) {
        AudioHelper.playAudioStream(reader.getAudioSampleInputStream().getAudioInputStream(false), 0, 0);
    }


}
