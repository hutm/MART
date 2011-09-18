package org.mart.crs.core.decoder;

import org.mart.crs.core.AudioReader;
import org.mart.crs.utils.AudioHelper;
import org.junit.Test;

import java.io.IOException;

/**
 * @version 1.0 Nov 26, 2009 2:39:58 PM
 * @author: Maksim Khadkevich
 */
public class Mp3DecoderTest {

    @Test
    public void testReadMp3() throws IOException {
        String filePath = this.getClass().getResource("/1.mp3").getPath();
        AudioReader reader = new AudioReader(filePath);
        AudioHelper.play(reader);
    }

}
