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

package org.mart.crs.utils;

import junit.framework.Assert;
import org.testng.annotations.Test;

/**
 * @version 1.0 7/11/11 11:19 AM
 * @author: Hut
 */
public class DCTTest {



    @Test
    public void testApplyInverseDCT() throws Exception {
         float[] inVector = new float[]{1, 0, 0, 0.5f, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0.5f, 0, 0, 1, 0, 0, 1, 0, 0};
        Assert.assertEquals(inVector.length, 24);
        float[] out = new float[24];
        DCT.applyDCT(inVector, out);
        float[] origVector = new float[24];
        DCT.applyInverseDCT(out, origVector);
    }

     @Test
    public void testApplyInverseDCTBatch() throws Exception {
         float[][] inVector = new float[][]{{1, 0, 0, 0.5f, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0.5f, 0, 0, 1, 0, 0, 1, 0, 0}};
        float[][] out = DCT.applyInverseDCT(inVector, 16, 1);
    }

}
