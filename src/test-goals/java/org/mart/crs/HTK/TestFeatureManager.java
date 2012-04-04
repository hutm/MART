package org.mart.crs.HTK;

import junit.framework.TestCase;
import org.mart.crs.config.Settings;
import org.mart.crs.management.features.FeaturesManager;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.management.label.chord.ChordStructure;

import java.util.List;

/**
 * @version 1.0 Nov 10, 2009 3:43:54 PM
 * @author: Maksim Khadkevich
 */
public class TestFeatureManager extends TestCase {

    public void testSegmentFeatureVectorStream() {
        String featureVectorFilePath = "data/FeatureSegment/yesterday.chr";
        List<ChordSegment> chordSegments = (new ChordStructure("data/FeatureSegment/yesterday.lab")).getChordSegments();

        int partsNumber = FeaturesManager.splitFeatureVectorsInSegments(featureVectorFilePath, chordSegments, "data/FeatureSegment/out");

        //Now check that all parts can be assembled together
        List<float[][]> pcp;
        List<float[][]> originalPCP = FeaturesManager.readFeatureVector("data/FeatureSegment/yesterday.chr").getVectors();
        int currentIndex = 0;
        for (int i = 0; i < partsNumber; i++) {
            pcp = FeaturesManager.readFeatureVector("data/FeatureSegment/out/" + i + Settings.CHROMA_SEC_PASS_EXT).getVectors();
            //Now compare contents
            for (int j = 0; j < pcp.size(); j++) {
                for (int k = 0; k < Settings.NUMBER_OF_SEMITONES_IN_OCTAVE; k++) {
// TODO rewrite this test                   
// assertTrue(originalPCP[currentIndex][k] == pcpList[j][k]);
                }
                currentIndex ++;
            }
        }


    }


//    public void testDirTraform(){
//        String featuresDir = "D:\\Beatles\\test\\features";
//        String recognizedLabelsDir = "D:\\Beatles\\test\\results";
//        FeaturesManager.splitAllData(featuresDir, recognizedLabelsDir);
//    }


}
