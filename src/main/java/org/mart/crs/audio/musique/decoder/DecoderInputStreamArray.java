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

package org.mart.crs.audio.musique.decoder;

import org.mart.crs.utils.AudioHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @version 1.0 22-Aug-2010 19:49:48
 * @author: Hut
 */
public class DecoderInputStreamArray extends DecoderInputStream{

    protected byte[] byteDataForStream;

    /**
     * One way to initialize the decoderInputStream from decoder
     *
     * @param samples
     */
    public DecoderInputStreamArray(short[] samples) {
        byteDataForStream = AudioHelper.getDataForStream(samples);
        byteArrayInputStream = new ByteArrayInputStream(byteDataForStream);
    }

    @Override
    public void initializeBuffer() {
        byteArrayInputStream = new ByteArrayInputStream(byteDataForStream);
    }

    @Override
    public int read() throws IOException {
        return byteArrayInputStream.read();
    }
}
