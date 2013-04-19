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

    @Test(groups = {"static"})
    public void testReadMp3() throws IOException {
        String filePath = this.getClass().getResource("/audio/1.mp3").getPath();
        AudioReader reader = new AudioReader(filePath);
        AudioHelper.play(reader);
    }


}
