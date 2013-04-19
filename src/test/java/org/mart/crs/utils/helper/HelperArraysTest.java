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
