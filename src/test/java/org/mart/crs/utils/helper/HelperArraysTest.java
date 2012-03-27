package org.mart.crs.utils.helper;


import org.testng.annotations.Test;

/**
 * @version 1.0 15-Oct-2010 18:42:56
 * @author: Hut
 */
public class HelperArraysTest {

    @Test
    public void testDeltaCoeffs(){
        float[][] inData = new float[100][1];
        for(int i = 0; i < inData.length; i++){
            inData[i][0] = i;
        }
        float[][] deltas = HelperArrays.getDeltaCoefficients(inData, 10);
    }


}
