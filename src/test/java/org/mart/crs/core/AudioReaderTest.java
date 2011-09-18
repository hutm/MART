package org.mart.crs.core;

import org.junit.Assert;
import org.junit.Test;

/**
 * User: Hut
 * Date: 16.06.2008
 * Time: 0:27:22
 */
public class AudioReaderTest {

    @Test
    public void testAudioReader(){
        AudioReader reader = new AudioReader(this.getClass().getResource("/1.wav").getPath());
        Assert.assertEquals(reader.getSamples().length, 222514);

    }
}
