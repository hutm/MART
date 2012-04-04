package org.mart.crs.core;

import junit.framework.TestCase;
import org.mart.crs.utils.AudioHelper;

/**
 * @version 1.0 Nov 26, 2009 6:37:37 PM
 * @author: Maksim Khadkevich
 */
public class TestSamplesExtractor extends TestCase {


    public void testMp3Read(){
        String filePath = "C:\\Documents and Settings\\Administrator\\My Documents\\!!sounds\\09_-_Girl.wav";
        AudioReader samplesExtractor = new AudioReader(filePath, 33075f);
//        samplesExtractor.extractSamples();
        float[] data = samplesExtractor.getSamples();
        AudioReader audioReader = new AudioReader(data, 33075f);
//        audioReader.playAPieceOfMusic(audioReader.getSamples(), audioReader.getAudioFormat());
    }

    public void testMp3Read2(){
        String filePath = "data/serenada.mp3";
//        String filePath = "data/1_.wav";
        AudioReader samplesExtractor = new AudioReader(filePath, 44100f);

        AudioReader newSampesExtractor = new AudioReader(samplesExtractor.getSamples(), 44100);
        AudioHelper.play(newSampesExtractor);
        newSampesExtractor.changeSamplingRate(22050f);

        AudioHelper.play(newSampesExtractor);



//        samplesExtractor.play(0, 5012 * 4);
//        samplesExtractor.play();
//        samplesExtractor.changeSamplingRate(22050f);
//        samplesExtractor.play();
//        samplesExtractor.extractSamples();
    }
}
