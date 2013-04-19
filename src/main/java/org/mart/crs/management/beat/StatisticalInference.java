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

package org.mart.crs.management.beat;

import org.mart.crs.config.Extensions;
import org.mart.crs.management.beat.segment.BeatSegment;
import org.mart.crs.utils.helper.HelperArrays;
import org.mart.crs.utils.helper.HelperFile;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides functionality to collect statistics on the downbeat features respect to the beat features. Needed for futher
 * discrimination
 *
 * @version 1.0 10/11/10 11:28
 * @author: Hut
 */
public class StatisticalInference {

    public static final String fileListPath = "d:\\dev\\Matlab\\beat\\corpus\\listAll.txt";

    public static final String gtDirPath = "d:\\dev\\Matlab\\beat\\corpus\\6.2.2_Beat\\Annotation_names";

    protected float[][] data;
    protected float sampleRate;
    protected float delay;


    public void getStatistics() {
        List<String> files = HelperFile.readLinesFromTextFile(fileListPath);
        for (String filePath : files) {
            initializeData(filePath);
            String labelsPath = HelperFile.getPathForFileWithTheSameName(filePath, gtDirPath, Extensions.BEAT_EXT);
            BeatStructure beatStructure = BeatStructure.getBeatStructure(labelsPath);
            List<BeatSegment> downbeats = beatStructure.getDownBeatPositions(true);
            List<BeatSegment> nonDownbeats = beatStructure.getDownBeatPositions(false);

            float[][] beatEnergies = getStatisticsOnEnergies(downbeats);
            float[][] nonBeatEnergies = getStatisticsOnEnergies(nonDownbeats);

            System.out.println(String.format("%5.3f %5.3f", beatEnergies[0][0], nonBeatEnergies[0][0]));
        }
    }

    protected float[][] getStatisticsOnEnergies(List<BeatSegment> beats) {
        List<float[]> means = new ArrayList<float[]>();

        int counter = 0;
        for (BeatSegment downBeat : beats) {
            int startIndex = getIndexForTime(downBeat.getTimeInstant());
            int endIndex = getIndexForTime(downBeat.getNextBeatTimeInstant());
            float[][] beatData = HelperArrays.cut(data, Math.min(startIndex, data.length - 1), Math.min(endIndex, data.length - 1));
            if(beatData.length == 0){
                break;
            }
            float[][] meanAndStdDeviation = HelperArrays.calculateMeanAndStandardDeviationVectors(beatData);
            means.add(meanAndStdDeviation[0]);
            counter++;
        }
        float[][] arrayDara = new float[means.size()][];
        for(int i = 0; i < arrayDara.length; i++){
            arrayDara[i] = means.get(i);
        }

        float[][] meanAndStdDeviation = HelperArrays.calculateMeanAndStandardDeviationVectors(arrayDara);
        return meanAndStdDeviation;
    }


    protected float[][] getDownbeatData(Float[] downbeats) {
        float[][] out = new float[downbeats.length][];
        int counter = 0;
        for (Float downBeat : downbeats) {
            out[counter] = data[getIndexForTime(downBeat)];
        }
        return out;
    }


    protected int getIndexForTime(double time) {
        time = time - delay;
        int outIndex = (int)Math.round(time * sampleRate);
        return outIndex;
    }


    //TODO: needs refactoring
    public void initializeData(String audioFilePath) {
        /*AudioReader reader = new AudioReader(audioFilePath);
        SpectrumImpl spectrum = new SpectrumImpl(reader, ExecParams._initialExecParameters);
        float[][] magSpec = spectrum.getMagSpec();
        float[][] spectralData = new float[magSpec.length][1];
        for (int i = 0; i < magSpec.length; i++) {
            spectralData[i][0] = HelperArrays.sum(magSpec[i], 0, Math.round(spectrum.freq2index(1000)));
        }

//        PCP pcp = new PCPBuilder(PCPBuilder.BASIC_ALG).setSpectrum(spectrum).build();
//        float[][] pcpData = pcp.getPCP();
//        float sampleRate = pcp.getSampleRatePCP();
//        float delay = 0;

        this.data = spectralData;
        this.sampleRate = spectrum.getSampleRateSpectrum();
        this.delay = 0;*/
    }


    public static void main(String[] args) {
        StatisticalInference inference = new StatisticalInference();
        inference.getStatistics();
    }


}
