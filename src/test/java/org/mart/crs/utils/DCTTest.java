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
