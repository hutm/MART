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

package org.mart.crs.management.features;

import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.management.beat.BeatStructure;
import org.mart.crs.management.beat.segment.BeatSegment;
import org.mart.crs.management.beat.segment.MeasureSegment;
import org.mart.crs.management.features.extractor.FeaturesExtractorHTK;
import org.mart.crs.management.label.chord.Root;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.mart.crs.config.Settings.CHROMA_EXT;

/**
 * @version 1.0 Dec 3, 2010 6:30:33 PM
 * @author: Hut
 */
public class FeaturesManagerOnset extends FeaturesManager {

    public FeaturesManagerOnset(String songFilePath, String outDirPath, boolean isForTraining, ExecParams execParams) {
        super(songFilePath, outDirPath, isForTraining, execParams);
    }

    public void exctractForTraining(float refFrequency, String dirName) {
        String lblFilePath;
        lblFilePath = HelperFile.getPathForFileWithTheSameName(songFilePath, Settings.labelsGroundTruthDir, Settings.BEAT_EXT);


        if (lblFilePath == null) {
            logger.warn(String.format("Lablels for file %s were not found in directory %s", songFilePath, Settings.labelsGroundTruthDir));
            return;
        }

//        extractMeasures(lblFilePath, refFrequency, dirName);
        extractBeats(lblFilePath, refFrequency, dirName);

    }


    protected void extractMeasures(String lblFilePath, float refFrequency, String dirName){
        List<MeasureSegment> segments = BeatStructure.getBeatStructure(lblFilePath).getMeasureSegments();

        for (int i = 0; i < segments.size(); i++) {

            MeasureSegment curSegment = segments.get(i);
            String filename = new StringBuilder().append((String.valueOf(curSegment.getStartTime())).substring(0, Math.min((String.valueOf(curSegment.getStartTime())).length(), 6))).append("_").append((String.valueOf(curSegment.getEndTime())).substring(0, Math.min((String.valueOf(curSegment.getEndTime())).length(), 6))).append("_").append(curSegment.toString()).append(CHROMA_EXT).toString();

            double startTime = segments.get(i).getStartTime();
            double endTime = segments.get(i).getEndTime();


            List<float[][]> features = new ArrayList<float[][]>();
            for (FeaturesExtractorHTK featuresExtractor : featureExtractorToWorkWith) {
                float[][] feature = featuresExtractor.extractFeatures(startTime, endTime, refFrequency);
                features.add(feature);
            }

            String fileNameToStore = new StringBuilder().append(dirName).append(File.separator).append(filename).toString();
            storeDataInHTKFormat(fileNameToStore, new FeatureVector(features, chrSamplingPeriod));

        }
    }


    protected void extractBeats(String lblFilePath, float refFrequency, String dirName){
        List<BeatSegment> segments = BeatStructure.getBeatStructure(lblFilePath).getBeatSegments();

        for (int i = 0; i < segments.size(); i++) {

            BeatSegment curSegment = segments.get(i);

            double startTime = segments.get(i).getTimeInstant();
            double endTime = segments.get(i).getNextBeatTimeInstant();

            //TODO use here formatted string instead
            String filename = new StringBuilder().append((String.valueOf(startTime)).substring(0, Math.min((String.valueOf(startTime)).length(), 6))).append("_").append((String.valueOf(endTime)).substring(0, Math.min((String.valueOf(endTime)).length(), 6))).append("_").append(curSegment.toString()).append(CHROMA_EXT).toString();

            List<float[][]> features = new ArrayList<float[][]>();
            for (FeaturesExtractorHTK featuresExtractor : featureExtractorToWorkWith) {
                float[][] feature = featuresExtractor.extractFeatures(startTime, endTime, refFrequency);
                features.add(feature);
            }

            String fileNameToStore = new StringBuilder().append(dirName).append(File.separator).append(filename).toString();
            storeDataInHTKFormat(fileNameToStore, new FeatureVector(features, chrSamplingPeriod));

        }
    }


    public void extractForTest(float refFrequency, String dirName) {
        FeatureVector featureVector = extractFeatureVectorForTest(refFrequency);

        String filenameToSave = new StringBuilder().append(dirName).append(File.separator).append(featureVector.getFileNameToStoreTestVersion()).toString();
        storeDataInHTKFormat(filenameToSave, featureVector);
    }


    public FeatureVector extractFeatureVectorForTest(float refFrequency) {
        List<float[][]> features = new ArrayList<float[][]>();
        for (FeaturesExtractorHTK featuresExtractor : featureExtractorToWorkWith) {
            float[][] feature = featuresExtractor.extractFeatures(refFrequency, Root.C);
            features.add(feature);
        }

        float duration = featureExtractorToWorkWith.get(0).getDuration();

        FeatureVector outFeatureVector;
        //The following lines make transform from the given key to the base (C or Am)
        outFeatureVector = new FeatureVector(features, chrSamplingPeriod);
        outFeatureVector.setDuration(duration);

        return outFeatureVector;
    }
}
