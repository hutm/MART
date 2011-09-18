package org.mart.crs.management.chord;

import org.mart.crs.config.Settings;
import org.mart.crs.core.AudioReader;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.utils.helper.HelperFile;
import junit.framework.TestCase;

import java.io.File;
import java.util.Arrays;

/**
 * @version 1.0 6/7/11 4:58 PM
 * @author: Hut
 */
public class TestFindDurationConsistency extends TestCase {


    public void testFindDurationInconsistency() {
        String labDir = "/home/hut/Beatles/data/tempLab";
        String audioDir = "/home/hut/Beatles/data/flac2";
        File[] audioFiles = HelperFile.getFile(audioDir).listFiles();
        Arrays.sort(audioFiles);
        for(File audio:audioFiles){
            ChordStructure structure = new ChordStructure(HelperFile.getPathForFileWithTheSameName(audio.getName(), labDir, Settings.LABEL_EXT));
            AudioReader reader = new AudioReader(audio.getPath());
            double duration = reader.getDuration();
            double lastchordOffset = structure.getChordSegments().get(structure.getChordSegments().size() - 1).getOffset();
            System.out.println(String.format("%7.5f %s %5.5f %5.5f" , duration - lastchordOffset, audio.getName(), duration, lastchordOffset));
        }
    }

}
