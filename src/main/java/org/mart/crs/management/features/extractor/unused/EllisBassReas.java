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

package org.mart.crs.management.features.extractor.unused;

import org.mart.crs.config.Settings;
import org.mart.crs.core.AudioReader;
import org.mart.crs.core.pcp.spectral.PCP;
import org.mart.crs.management.features.extractor.FeaturesExtractorHTK;
import com.jamal.JamalException;
import com.jamal.client.MatlabClient;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @version 1.0 20-Sep-2010 00:59:14
 * @author: Hut
 */
public class EllisBassReas extends FeaturesExtractorHTK {

    protected PCP chromaAll;

    protected static FileWriter writer;

    public void initialize(AudioReader audioReader) {
        super.initialize(audioReader);
        chromaAll = null;
        if(writer == null){
            try {
                writer = new FileWriter("d:/temp/tunings.txt", true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getVectorSize() {
        return Settings.NUMBER_OF_SEMITONES_IN_OCTAVE;
    }

    
    @Override
    public void extractGlobalFeatures(double refFrequency) {
        float[] samples = audioReader.getSamples();
        globalVectors.add(getDataFromMatlab(samples, true));
    }

    protected float[][] getDataFromMatlab(float[] samples, boolean isBass) {

        try {
            MatlabClient matlabClient = new MatlabClient();

            //First we pass an array of integers and calculate sum in Matlab
            Object[] inArgs = new Object[3];
            inArgs[0] = samples;
            inArgs[1] = new Double(audioReader.getSampleRate());
            inArgs[2] = new Integer(execParams.windowLength);

            Object[] outputArgs = matlabClient.executeMatlabFunction("getChroma", inArgs, 3);
            double[] chroma;
            if(isBass){
                chroma = (double[]) outputArgs[0];
            }else{
                chroma = (double[]) outputArgs[1];
            }

            double tuning = ((double[])outputArgs[2])[0];
            try {
                writer.write(String.format("%s %5.3f\n", audioReader.getFilePath(), tuning));
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }


            float[][] out = new float[chroma.length / 12][12];
            for (int i = 0; i < out.length; i++) {
                for (int j = 0; j < 12; j++) {
                    out[i][j] = (float) chroma[i * 12 + j];
                }
            }
            return out;

//            matlabClient.executeMatlabFunction("quit", null, 0);

        } catch (JamalException e) {
            e.printStackTrace();
        }
        return new float[0][];
    }

    @Override
    public float getDuration() {
        return this.audioReader.getDuration();
    }
}
