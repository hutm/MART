package org.mart.crs.audio.musique;

import junit.framework.TestCase;
import org.mart.crs.core.AudioReader;
import org.mart.crs.utils.helper.HelperArrays;

import java.io.IOException;

/**
 * @version 1.0 13-Jul-2010 21:24:09
 * @author: Hut
 */
public class TestAudioSampleInputStream extends TestCase {


    public static void testAudioSampleInputStream() throws IOException {
//        String filePath = "d:/My Documents/!audio/passage.wav";
//        String filePath = "d:/My Documents/!audio/short.wav";
        String filePath = "d:/My Documents/My Music/vkontakte/2.mp3";
        AudioSampleInputStream sampleInputStream = new AudioSampleInputStream(filePath, 44100);
//        sampleInputStream.playAudioStream(0, (int)sampleInputStream.getSampleCount());
        short[] samples = new short[44100 * 2];
        sampleInputStream.seek(44100 * 3);
        sampleInputStream.readSamplesMono(samples);
        AudioReader audioReader = new AudioReader(HelperArrays.getShortAsFloat(samples), 44100);
//        audioReader.play();

//        sampleInputStream.seek(44100 * 20);
//        sampleInputStream.readSamplesMono(samples);
//        audioReader = new AudioReader(HelperArrays.getShortAsFloat(samples), 44100);
//        audioReader.play();


        samples = new short[128];
        sampleInputStream.seek(44100 * 3);
        short[] samplesGlobal = new short[0];
        for (int i = 0; i < 1000; i++) {
            sampleInputStream.readSamplesMono(samples);
            samplesGlobal = HelperArrays.concat(samplesGlobal, samples);
        }
        audioReader = new AudioReader(HelperArrays.getShortAsFloat(samplesGlobal), 44100);
//        audioReader.play();

        sampleInputStream.close();
    }


    public void testReadAllSamples() {
        String filePath = "d:\\My Documents\\!audio\\1.wav";
//        String filePath = "d:\\My Documents\\!audio\\11_-_Mean_Mr_Mustard.wav";
        float samplingRate = 11025;
        AudioReader reader = new AudioReader(filePath, samplingRate);
        reader.setStoreSamplesInMemory(true);
//        reader.play();
        short[] samples = reader.getSamplesShort();
        AudioReader AudioReader = new AudioReader(samples, samplingRate);
//        AudioReader.play();
    }


    public void testReadSamplesIn2Threads() {
        String filePath = "d:\\Beatles\\data\\wav\\01_-_I_Saw_Her_Standing_There.wav";
        String filePath2 = "d:\\Beatles\\data\\wav\\03_-_Anna_(Go_To_Him).wav";
        float samplingRate = 11025;
        final AudioReader reader = new AudioReader(filePath, samplingRate);
        final AudioReader reader2 = new AudioReader(filePath2, samplingRate);
        reader.setStoreSamplesInMemory(true);
        reader2.setStoreSamplesInMemory(true);

        final short[][] samples = new short[1][1];
        Runnable runnable = new Runnable() {
            public void run() {
                final short[] samplesFinal = reader.getSamplesShort();
                samples[0] = samplesFinal;
            }
        };
        Thread thread1 = new Thread(runnable);
        thread1.start();

        short[] samples2 = reader2.getSamplesShort();



        AudioReader AudioReader = new AudioReader(samples2, samplingRate);
//        AudioReader.play();
    }


//    /**
//     * Plays audio from the given audio input stream.
//     */
//    public void playAudioStream(int startFrame, int framesToRead) {
//
//        AudioFormat audioFormat = decoder.getAudioFormat();
//        // Open a data line to play our type of sampled audio.
//        // Use SourceDataLine for play and TargetDataLine for record.
//        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
//        if (!AudioSystem.isLineSupported(info)) {
//            System.out.println("Play.playAudioStream does not handle this type of audio on this system.");
//            return;
//        }
//
//        try {
//            // Create a SourceDataLine for play back (throws LineUnavailableException).
//            SourceDataLine dataLine = (SourceDataLine) AudioSystem.getLine(info);
//            // System.out.println( "SourceDataLine class=" + dataLine.getClass() );
//
//
//            // The line acquires system resources (throws LineAvailableException).
//            dataLine.open(audioFormat);
//
//            // Adjust the volume on the output line.
//            if (dataLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
//                FloatControl volume = (FloatControl) dataLine.getControl(FloatControl.Type.MASTER_GAIN);
////                volume.setValue(2.0F);
//            }
//
//            // Allows the line to move data in and out to a port.
//            dataLine.start();
//
//            // Create a buffer for moving data from the audio stream to the line.
//            int bufferSize = (int) audioFormat.getSampleRate() * audioFormat.getFrameSize();
//            byte[] buffer = new byte[bufferSize];
//
//
//            // Move the data until done or there is an error.
//            try {
//                int bytesRead = 0;
//                int framesWritten = 0;
//
//                //First skip some frames if needed
//                if (startFrame > 0) {
//                    decoder.seekSample(startFrame);
//                }
//
//
//                while (bytesRead >= 0) {
//                    if (framesToRead > 0 && framesWritten >= framesToRead) {
//                        break;
//                    }
//                    bytesRead = decoder.decode(buffer);
//                    if (bytesRead >= 0) {
//                        if (framesToRead > 0) {
//                            framesWritten += dataLine.write(buffer, 0, Math.min(bytesRead, (framesToRead - framesWritten) * audioFormat.getFrameSize())) / audioFormat.getFrameSize();
//                        } else {
//                            framesWritten += dataLine.write(buffer, 0, bytesRead);
//                        }
//                    }
//                } // while
//            } catch (Exception e) {
//                System.out.println("Smth is wrong");
//                System.out.println(Helper.getStackTrace(e));
//            }
//
//            System.out.println("Play.playAudioStream draining line.");
//            // Continues data line I/O until its buffer is drained.
//            dataLine.drain();
//
//            System.out.println("Play.playAudioStream closing line.");
//            // Closes the data line, freeing any resources such as the audio device.
//            dataLine.close();
//        } catch (LineUnavailableException e) {
//            e.printStackTrace();
//        }
//    } // playAudioStream


}
