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

package org.mart.crs.management.features.extractor.key;

import org.mart.crs.config.Settings;
import org.mart.crs.management.features.FeaturesManager;
import org.mart.crs.management.features.extractor.FeaturesExtractorHTK;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.utils.helper.HelperFile;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.management.label.chord.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 5/9/11 6:53 PM
 * @author: Hut
 */
public class ChordGramConfidenceMeasureBasedMajor extends FeaturesExtractorHTK {

    @Override
    protected void extractGlobalFeatures(double refFrequency) {
        String chordGramFilePath = HelperFile.getPathForFileWithTheSameName(this.songFilePath, Settings.chordRecognizedDirectory, Settings.CHROMA_EXT);
        ChordStructure chordStructure = ChordStructure.readHypothesesFromFile(chordGramFilePath);
        List<ChordSegment> chordSegments = chordStructure.getChordSegments();
        int endIndexGlobal = FeaturesManager.getIndexForTimeInstant(chordSegments.get(chordSegments.size() - 1).getOffset(), execParams);
        float[][] chordGram = new float[endIndexGlobal][ChordSegment.SEMITONE_NUMBER];
        for (ChordSegment cs : chordSegments) {
            int startIndex = FeaturesManager.getIndexForTimeInstant(cs.getOnset(), execParams);
            int endIndex = FeaturesManager.getIndexForTimeInstant(cs.getOffset(), execParams);
            float[] chordGramData = extractModalityData(cs.getChordGram());
            for(int i = startIndex; i < endIndex; i++){
                for(int j = 0; j < chordGramData.length; j++){
                    chordGram[i][j] = chordGramData[j];
                }
            }
        }
        this.globalVectors.add(chordGram);
    }


    public float[][] extractFeatures(double startTime, double endTime, double refFrequency, Root chordFrom) {
        float[][] features = super.extractFeatures(startTime, endTime,refFrequency, chordFrom);
        List<float[]> featuresRemovedZeros = new ArrayList<float[]>();
        for(float[] vector:features){
            boolean allZeros = true;
            for(int i = 0; i < vector.length; i++){
                if(vector[i] != 0){
                    allZeros = false;
                }
            }
            if(!allZeros){
                featuresRemovedZeros.add(vector);
            }
        }
        float[][] out = new float[featuresRemovedZeros.size()][];
        for(int i = 0 ; i < featuresRemovedZeros.size(); i++){
            out[i] = featuresRemovedZeros.get(i);
        }
        return out;
    }

    protected float[] extractModalityData(float[] fullVector){
        float[] output = new float[ChordSegment.SEMITONE_NUMBER];
        System.arraycopy(fullVector, 0, output, 0, output.length);
        return output;
    }


    @Override
    public int getVectorSize() {
        return 12;
    }
}
