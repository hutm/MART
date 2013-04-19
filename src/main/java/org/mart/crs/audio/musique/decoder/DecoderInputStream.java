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

package org.mart.crs.audio.musique.decoder;

import com.tulskiy.musique.audio.Decoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @version 1.0 15-Jul-2010 17:52:23
 * @author: Hut
 */
public class DecoderInputStream extends InputStream {

    protected Decoder decoder;
    protected ByteArrayInputStream byteArrayInputStream;
    

    /**
     * One way to initialize the decoderInputStream from decoder
     * @param decoder
     */
    public DecoderInputStream(Decoder decoder) {
        this.decoder = decoder;
        initializeBuffer();
    }

    protected DecoderInputStream() {
    }


    public void initializeBuffer(){
        this.byteArrayInputStream = new ByteArrayInputStream(new byte[0]);
    }


    @Override
    public int read() throws IOException {
        if (byteArrayInputStream.available() <= 0) {
            byte[] newData = new byte[65536];
            int bytesRead = decoder.decode(newData);
            if (bytesRead <= 0) {
                return -1;
            }
            byteArrayInputStream = new ByteArrayInputStream(newData, 0, bytesRead);
        }
        return byteArrayInputStream.read();
    }
}
