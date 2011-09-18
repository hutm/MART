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

package org.mart.tools.hps;

import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.core.AudioReader;
import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.utils.helper.HelperArrays;
import org.mart.crs.utils.helper.HelperFile;

/**
 * @version 1.0 3/24/11 2:20 PM
 * @author: Hut
 */
public class HPS2 {

    public static final String WORKING_DIR = "/home/hut/temp/hps";




    private static final int maxIterations = 200;
    private static final float alpha = 0.5f;


    protected String audioFilePath;
    protected SpectrumImpl spectrum;

    protected boolean[][] separationMatrix;



    public HPS2(String audioFilePath) {
        this.audioFilePath = audioFilePath;
    }


    public void separateParts() {
        HelperFile.createDir(WORKING_DIR);
        AudioReader reader = new AudioReader(audioFilePath);

        Settings.saveHPSRelatedInformation = true;
        spectrum = createSpecrumFromAurioReader(reader);

        separationMatrix = getSeparationMatrixHarmonicPart(spectrum);


        float[] samples = spectrum.inverseFFT(separationMatrix, true);
        AudioReader save = new AudioReader(samples, reader.getSampleRate());
        save.exportWavFile(WORKING_DIR + "/test_har.wav", 0, 1000);


        separationMatrix = getSeparationMatrixPercussivePart();


        samples = spectrum.inverseFFT(separationMatrix, true);
        save = new AudioReader(samples, reader.getSampleRate());
        save.exportWavFile(WORKING_DIR + "/test_per.wav", 0, 1000);


        samples = spectrum.inverseFFT();
        save = new AudioReader(samples, reader.getSampleRate());
        save.exportWavFile(WORKING_DIR + "/test.wav", 0, 1000);
    }


    protected SpectrumImpl createSpecrumFromAurioReader(AudioReader reader){
        spectrum = new SpectrumImpl(reader, ExecParams._initialExecParameters);
        return spectrum;
    }



    protected boolean[][] getSeparationMatrixHarmonicPart(SpectrumImpl spectrum) {
        float[][] magnitude = spectrum.getMagSpec();

        int searchStepTime = 3;
        int searchStepFreq = 1;

        int maxT = magnitude.length;
        int maxF = magnitude[0].length;
        boolean[][] out = new boolean[maxT][maxF];

        //initialization
        float[][] p = new float[maxT][maxF];
        float[][] h = new float[maxT][maxF];
        for (int i = 0; i < maxT; i++) {
            for (int j = 0; j < maxF; j++) {
                p[i][j] = h[i][j] = magnitude[i][j] / 2;
            }
        }

        //start iterations
        float[][] delta = new float[maxT][maxF];
        for (int k = 0; k < maxIterations; k++) {
            //System.out.println("Iteration " + k);

            //calculate deltas
            for (int i = searchStepTime; i < maxT - searchStepTime; i++) {
                for (int j = searchStepFreq; j < maxF - searchStepFreq; j++) {
                    delta[i][j] = 0.25f * (alpha * (h[i - searchStepTime][j] - 2 * h[i][j] + h[i + searchStepTime][j]) - (1 - alpha) * (p[i][j - searchStepFreq] - 2 * p[i][j] + p[i][j + searchStepFreq]));
                }
            }

            //update h and p
            for (int i = 1; i < maxT - 1; i++) {
                for (int j = 1; j < maxF - 1; j++) {
                    h[i][j] = Math.min(Math.max(h[i][j] + delta[i][j], 0), magnitude[i][j]);
                    p[i][j] = magnitude[i][j] - h[i][j];
                }
            }
        }

        //make binarization
        for (int i = 1; i < maxT - 1; i++) {
            for (int j = 1; j < maxF - 1; j++) {
                out[i][j] = h[i][j] > p[i][j];
            }
        }

        return out;
    }


    protected boolean[][] getSeparationMatrixPercussivePart(){
         return HelperArrays.inverseBooleans(separationMatrix);
    }


    public static void main(String[] args) {
        String audioPath = "/home/hut/Documents/!audio/hps/225_BillieJean.wav";
//        String audioPath = "./data/audio/test.wav";
        HPS2 hps2 = new HPS2(audioPath);
        hps2.separateParts();
    }

}
