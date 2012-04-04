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

package org.mart.crs.analysis.filter.PQMF;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @version 1.0 3/27/12 9:22 PM
 * @author: Hut
 */
public class PQMFTest {
    @Test
    public void testProcess() throws Exception {
        float[] data = new float[44100];
        for(int i = 0; i < data.length;i++){
            data[i] = (float)Math.sin(Math.PI*2/44100 * 100 * i);
        }
        PQMF pqmf = new PQMF(1, 44100);
        pqmf.init();
        float[] out = pqmf.process(data);
        Assert.assertTrue(out.length == data.length);
    }
}
