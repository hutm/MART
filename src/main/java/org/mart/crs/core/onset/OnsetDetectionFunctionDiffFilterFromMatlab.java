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

package org.mart.crs.core.onset;

import com.jamal.JamalException;
import com.jamal.client.MatlabClient;
import org.mart.crs.audio.resample.Polyphase;
import org.mart.crs.utils.helper.HelperArrays;

/**
 * @version 1.0 1/14/11 6:01 PM
 * @author: Hut
 */
public class OnsetDetectionFunctionDiffFilterFromMatlab extends AbstractOnsetDetectionFunction {


    protected String audioFilePath;
    protected float desiredSampleRate;

    protected float inSamplingRate;


    public OnsetDetectionFunctionDiffFilterFromMatlab(String audioFilePath, float desiredSampleRate) {
        this.audioFilePath = audioFilePath;
        this.desiredSampleRate = desiredSampleRate;
    }

    protected void computeOnsetDetection() {
        float[][] df;
        int numberOFChannels = 8;
        try {
            MatlabClient matlabClient = new MatlabClient();

            //First we pass an array of integers and calculate sum in Matlab
            Object[] inArgs = new Object[1];
            inArgs[0] = audioFilePath;

            Object[] outputArgs = matlabClient.executeMatlabFunction("a_getDF_orig", inArgs, 3);
            double[] rawFunction = (double[]) outputArgs[0];
            inSamplingRate = (float) (((double[]) outputArgs[2])[0]);
            int delayInFrames = 5;
            int frameNumber = rawFunction.length / numberOFChannels;

            df = new float[numberOFChannels][frameNumber];
            detectionFunction = new float[frameNumber];
            for (int i = 0; i < numberOFChannels; i++) {
                for (int j = 0; j < frameNumber-delayInFrames; j++) {
                    df[i][j] = (float) rawFunction[i * frameNumber + j];
                    detectionFunction[j+delayInFrames] += df[i][j];
                }
            }
            detectionFunction =  HelperArrays.normalizeVector(detectionFunction);
            resampleDetectionFunction(inSamplingRate);

        } catch (JamalException e) {
            e.printStackTrace();
        }
    }

    protected void resampleDetectionFunction(float inSampleRate) {
        Polyphase polyphase = new Polyphase(inSampleRate, desiredSampleRate);
        int[] inData = new int[detectionFunction.length];
        for (int i = 0; i < detectionFunction.length; i++) {
            inData[i] = Math.round(Integer.MAX_VALUE * detectionFunction[i]);
        }
        int[] outData = polyphase.resample(inData);
        float[] newDetectionFunction = new float[outData.length];
        for (int i = 0; i < outData.length; i++) {
            newDetectionFunction[i] = (float)outData[i] ;
        }
        this.detectionFunction = HelperArrays.normalizeVector(newDetectionFunction);
    }


    public float getInSamplingRate() {
        return inSamplingRate;
    }
}
