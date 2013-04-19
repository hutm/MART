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


import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * User: Hut
 * Date: 16.06.2008
 * Time: 0:27:22
 */
public class AudioReaderTest {

    @Test
    public void testAudioReader(){
        AudioReader reader = new AudioReader(this.getClass().getResource("/audio/1.wav").getPath());
        Assert.assertEquals(reader.getSamples().length, 222514);

    }

    @Test(groups = {"resample"})
    public void testAudioReaderResample(){
        AudioReader reader = new AudioReader(this.getClass().getResource("/audio/1.wav").getPath(), 11025);
        Assert.assertEquals(reader.getSamples().length, 55628);
    }
}
