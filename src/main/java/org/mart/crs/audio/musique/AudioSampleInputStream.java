/*
 * This file is part of MART.
 * MART is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2, as published
 * by the Free Software Foundation.
 *
 * MART is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MART; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.mart.crs.audio.musique;

import com.jssrc.resample.JSSRCResampler;
import com.tulskiy.musique.audio.AudioFileReader;
import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.system.Codecs;
import com.tulskiy.musique.system.TrackIO;
import org.apache.log4j.Logger;
import org.mart.crs.audio.musique.decoder.DecoderInputStream;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.AudioHelper;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class AudioSampleInputStream {

    protected static Logger logger = CRSLogger.getLogger(AudioSampleInputStream.class);


    protected AudioFileReader reader;
    protected Track track;

    protected Decoder decoder;
    protected DecoderInputStream decoderInputStream;

    protected AudioFormat originalFormat;
    protected AudioFormat desiredFormat;

    protected boolean sampleRateConversionNeeded;

    protected JSSRCResampler resampler;


    protected AudioSampleInputStream() {
    }

    public AudioSampleInputStream(String filePath) {
        this(filePath, 0);
    }


    public AudioSampleInputStream(String filePath, float desiredSampleRate) {

        reader = TrackIO.getAudioFileReader(filePath);
        track = reader.read(new File(filePath));
        try {
            decoder = Codecs.getDecoder(track).getClass().newInstance();
        } catch (InstantiationException e) {
            logger.error(String.format("Could not instantiate decoder for audio reader"));
            logger.error(e.getStackTrace());
        } catch (IllegalAccessException e) {
            logger.error(String.format("Could not instantiate decoder for audio reader"));
            logger.error(e.getStackTrace());
        }
        decoder.open(track);
        decoderInputStream = new DecoderInputStream(decoder);
        originalFormat = decoder.getAudioFormat();

        if (desiredSampleRate == 0) {
            desiredSampleRate = originalFormat.getSampleRate();
        }

        desiredFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                desiredSampleRate,
                decoder.getAudioFormat().getSampleSizeInBits(),
                1,
                decoder.getAudioFormat().getFrameSize() / decoder.getAudioFormat().getChannels(),
                desiredSampleRate,
                decoder.getAudioFormat().isBigEndian());

        sampleRateConversionNeeded = originalFormat.getSampleRate() != desiredFormat.getSampleRate();
        initialize();
    }


    protected void initialize() {
        try {
            if (resampler != null) {
                resampler.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (sampleRateConversionNeeded) {
            resampler = new JSSRCResampler(originalFormat, desiredFormat, decoderInputStream);
        }
    }


    public long getSampleCount() {
        return (int) Math.floor(getTotalSamples() / getSamplingRateConversionFactor());
    }


    /**
     * Seeks a given position in the stream in the decoder's original format
     *
     * @param sample
     */
    public void seek(long sample) {
        decoder.seekSample(Math.round(sample / getSamplingRateConversionFactor()));
        decoderInputStream.initializeBuffer();
        initialize();
    }


    public void seekTimeInstant(float timeInstant) {
        decoder.seekSample(Math.round(timeInstant * originalFormat.getSampleRate()));
        decoderInputStream.initializeBuffer();
        initialize();
    }


    public void close() {
        decoder.close();
        //TODO close here everything
    }


    public int readSamplesMono(short[] samples) throws IOException {
        int bytesToRead = samples.length * originalFormat.getFrameSize();
        byte[] byteArray = new byte[bytesToRead];
        int decodedBytesNumber;
        if (sampleRateConversionNeeded) {
            decodedBytesNumber = resampler.read(byteArray);
        } else{
            decodedBytesNumber = decoderInputStream.read(byteArray);
        }

        short[] samplesAllChannels = AudioHelper.decodeBytes(byteArray, decodedBytesNumber, originalFormat.isBigEndian());
        if (originalFormat.getChannels() > 1) {
            mixChannels(samplesAllChannels, samples);
        } else{
            System.arraycopy(samplesAllChannels, 0, samples, 0, samplesAllChannels.length);
        }


        return samplesAllChannels.length / originalFormat.getChannels();
    }



    protected void mixChannels(short[] samplesAllChannels, short[] samples) {
        float scale = originalFormat.getChannels();

        Random rand = new Random();
        for (int i = 0; i < samplesAllChannels.length; i+=2) {
            samples[i / originalFormat.getChannels()] += (short) ((samplesAllChannels[i] + samplesAllChannels[i+1])/ scale);//+ rand.nextFloat());   //Dithering is commented (in current implementation only for 2 channels)
        }
    }


    public AudioInputStream getAudioInputStream(boolean isOriginalFormat) {
        return new AudioInputStream(decoderInputStream, originalFormat, AudioSystem.NOT_SPECIFIED);
    }

    /**
     * When seeking position or reading number Of Samples, this value is used in order
     * to take into account sampling frequency conversion
     *
     * @return factor
     */
    protected float getSamplingRateConversionFactor() {
        return originalFormat.getSampleRate() / desiredFormat.getSampleRate();
    }

    protected long getTotalSamples() {
        return track.getTrackData().getTotalSamples();
    }

    public float getDuration(){
        return getSampleCount() / desiredFormat.getSampleRate();
    }


    public AudioFormat getFormat() {
        return desiredFormat;
    }

    public AudioFormat getOriginalFormat() {
        return originalFormat;
    }

    public DecoderInputStream getDecoderInputStream() {
        return decoderInputStream;
    }

    public float getSampleRate(){
        return desiredFormat.getSampleRate();
    }



}
