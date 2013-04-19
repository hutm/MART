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

package org.mart.crs.core.pcp;

import com.jamal.JamalException;
import com.jamal.client.MatlabClient;
import org.mart.crs.core.spectrum.SpectrumImpl;


/**
 * @version 1.0 24-May-2010 18:03:38
 * @author: Hut
 */
public class PCPEnhenced extends PCPBasic {


    protected PCPEnhenced() {
    }

    protected PCPEnhenced(double refFreq, int averagingFactor, int numberOfBinsPerSemitone, boolean toNormalize, int startNoteForPCPWrapped, int endNoteForPCPWrapped, float chromaSpectrumRate) {
        super(refFreq, averagingFactor, numberOfBinsPerSemitone, toNormalize, startNoteForPCPWrapped, endNoteForPCPWrapped, chromaSpectrumRate);
    }

    @Override
    public void initSpectrum(SpectrumImpl spectrum) {
        super.initSpectrum(spectrum);
        performnmfPCP();
    }

    @Override
    public void initMatrixData(float[][] pcp, boolean isUnwrapped, float frameSize) {
        super.initMatrixData(pcp, isUnwrapped, frameSize);
        performnmfPCP();
    }


    protected void performnmfPCP(){
        try {
            MatlabClient matlabClient = new MatlabClient();

            //First we pass an array of integers and calculate sum in Matlab
            Object[] inArgs = new Object[1];
            inArgs[0] = this.pcpUnwrapped;

            Object[] outputArgs = matlabClient.executeMatlabFunction("nmfPCP", inArgs, 1);
            double[] outPCP = (double[]) outputArgs[0];

            int frameNumber = pcpUnwrapped.length;
            int frameLength = pcpUnwrapped[0].length;

            float[][] newPCPUnwrapped = new float[pcpUnwrapped.length][pcpUnwrapped[0].length];

            for (int i = 0; i < frameNumber; i++) {
                for (int j = 0; j < frameLength; j++) {
                    float value = (float) outPCP[i * frameLength + j];
                    newPCPUnwrapped[i][j] = value;
                }
            }

            setPcpUnwrapped(newPCPUnwrapped);

            System.out.println("");
//            matlabClient.executeMatlabFunction("quit", null, 0);

        } catch (JamalException e) {
            e.printStackTrace();
        }
    }


}
