package org.mart.crs.core.decoder;

import org.mart.crs.core.AudioReader;
import org.mart.crs.utils.AudioHelper;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * @version 1.0 Nov 26, 2009 2:39:58 PM
 * @author: Maksim Khadkevich
 */
public class Mp3DecoderTest {

    @Test(enabled = false)
    public void testReadMp3() throws IOException {
        String filePath = this.getClass().getResource("/audio/1.mp3").getPath();
        AudioReader reader = new AudioReader(filePath);
        AudioHelper.play(reader);
    }

}
