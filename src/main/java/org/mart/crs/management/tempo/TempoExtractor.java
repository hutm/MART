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

package org.mart.crs.management.tempo;

import org.apache.commons.math.stat.clustering.Cluster;
import org.apache.commons.math.stat.clustering.EuclideanFloatPoint;
import org.apache.commons.math.stat.clustering.KMeansPlusPlusClusterer;
import org.mart.crs.management.beat.BeatStructure;
import org.mart.crs.management.beat.segment.BeatSegment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Extracts tempo value from already extracted beat structure
 *
 * @version 1.0 1/20/11 3:33 PM
 * @author: Hut
 */
public class TempoExtractor {

    public static final int numberOfCluster = 3;
    public static final boolean isToNormalizeByDuration = false; //if set to false, normalization

    protected BeatStructure beatStructure;

    protected float tempo;


    public TempoExtractor(BeatStructure beatStructure) {
        this.beatStructure = beatStructure;
        extractTempo();
    }

    public TempoExtractor(String beatStructureXMLFilePath) {
        this.beatStructure = BeatStructure.getBeatStructure(beatStructureXMLFilePath);
        extractTempo();
    }


    protected void extractTempo() {
        List<EuclideanFloatPoint> pointList = new ArrayList<EuclideanFloatPoint>();
        for (BeatSegment beatSegment : beatStructure.getBeatSegments()) {
            pointList.add(new EuclideanFloatPoint(new double[]{beatSegment.getDuration()}));
        }

        KMeansPlusPlusClusterer clusterer = new KMeansPlusPlusClusterer(new Random(2039091238l));
        List<Cluster<EuclideanFloatPoint>> cluster = clusterer.cluster(pointList, numberOfCluster, 100);


        int maxIndex = 0;
        int maxNumberOfPoints = 0;
        for (int i = 0; i < numberOfCluster; i++) {
            int numberOfPoints = cluster.get(i).getPoints().size();
            if (numberOfPoints > maxNumberOfPoints) {
                maxIndex = i;
                maxNumberOfPoints = numberOfPoints;
            }
        }

        float beatLength = (float) cluster.get(maxIndex).getCenter().getPoint()[0];
        this.tempo = 60 / beatLength;

    }

    public float getTempo() {
        return tempo;
    }

    public static void main(String[] args) {
        String testFileName = "/home/hut/prg/Matlab/beat/code/results_transformed_fixed/results_4_7.0_lm_10.00_ac_1.00_p_11.00_factored_false/0308_-_Nena_-_99_Luftballons.xml";
        TempoExtractor tempoExtractor = new TempoExtractor(testFileName);
        float tempo = tempoExtractor.getTempo();
        System.out.println(tempo);

//        List<String> fileNumbers = HelperFile.readLinesFromTextFile("/home/hut/prg/Matlab/beatTrackingAlonso/data/list.txt", true);
//
//        Map<String, String> tempos = new HashMap<String, String>();
//        TempoExtractor tempoExtractor;
//        for (String fileNimber : fileNumbers) {
//            tempoExtractor = new TempoExtractor(String.format("/home/hut/prg/Matlab/beatTrackingAlonso/data/signal%s.wav", fileNimber));
//            tempos.put(fileNimber, String.valueOf(tempoExtractor.getTempo()));
//        }
//
//        HelperFile.saveMapInTextFile(tempos, "/home/hut/prg/Matlab/beatTrackingAlonso/results.txt");


    }


}
