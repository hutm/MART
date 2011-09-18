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
import org.mart.crs.management.features.extractor.FeaturesExtractorHTK;
import org.mart.crs.management.label.LabelsSource;
import org.mart.crs.management.label.chord.Root;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.mart.crs.exec.scenario.stage.StageParameters.maxNimberOfFramesForBeatSegment;
import static org.mart.crs.exec.scenario.stage.StageParameters.minNimberOfFramesForBeatSegment;

/**
 * @version 1.0 Dec 3, 2010 6:30:33 PM
 * @author: Hut
 */
public class FeaturesManagerBeat extends FeaturesManager {

    protected LabelsSource labelsSource;


    public FeaturesManagerBeat(String songFilePath, String outDirPath, boolean isForTraining, ExecParams execParams) {
        super( songFilePath, outDirPath, isForTraining, execParams);
        labelsSource = new LabelsSource(Settings.labelsGroundTruthDir, true, "gt", Settings.BEAT_EXTENSIONS);
    }

    public void exctractForTraining(float refFrequency, String dirName) {
        String lblFilePath = labelsSource.getFilePathForSong(songFilePath);

//        lblFilePath = HelperFile.getPathForFileWithTheSameName(songFilePath, Settings.labelsGroundTruthDir, Settings.BEAT_EXT);

        if (lblFilePath == null) {
            logger.warn(String.format("Lablels for file %s were not found in directory %s", songFilePath, Settings.labelsGroundTruthDir));
            return;
        }

        extractBeatsInOneSegment(lblFilePath, refFrequency, dirName);
    }




    protected void extractBeatsInOneSegment(String lblFilePath, float refFrequency, String dirName) {
        List<BeatSegment> segments = BeatStructure.getBeatStructure(lblFilePath).getBeatSegments();

        for (int i = 0; i < segments.size() - 1; i++) {    //The last beat in each song is not included in training set

            BeatSegment curSegment = segments.get(i);

            double startTime = segments.get(i).getTimeInstant() - getTimePeriodForFramesNumber(execParams.statesBeat - 2, execParams);
            if(startTime < 0){
                continue;
            }
            double endTime = segments.get(i).getNextBeatTimeInstant() - getTimePeriodForFramesNumber(execParams.statesBeat - 2, execParams);

            int startIndex = getIndexForTimeInstant(startTime, execParams);
            int endIndex = getIndexForTimeInstant(endTime, execParams);

            int numberOframes = endIndex - startIndex + 1 - 2 * (execParams.statesBeat - 2);

            if (numberOframes < minNimberOfFramesForBeatSegment || numberOframes > maxNimberOfFramesForBeatSegment) {
                continue;
            }

            String filename = String.format("%5.3f_%5.3f_%s%d%s", startTime, endTime, curSegment.toString(), numberOframes, Settings.CHROMA_EXT);
            extractAndSave(startTime, endTime, numberOframes, refFrequency, filename, dirName);
        }
    }

    protected void extractAndSave(double startTime, double endTime, int numberOfFrames, double refFrequency, String filename, String dirName) {
        List<float[][]> features = new ArrayList<float[][]>();
        for (FeaturesExtractorHTK featuresExtractor : featureExtractorToWorkWith) {
            float[][] feature = featuresExtractor.extractFeatures(startTime, endTime, refFrequency);
            features.add(feature);
        }

        String fileNameToStore = new StringBuilder().append(dirName).append(File.separator).append(filename).toString();
        if (features.get(0).length == numberOfFrames + 2 * (execParams.statesBeat - 2)) {
            storeDataInHTKFormat(fileNameToStore, new FeatureVector(features, chrSamplingPeriod));
        } else{
            logger.warn(String.format("Segment %s is not eligible for training", filename));
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
