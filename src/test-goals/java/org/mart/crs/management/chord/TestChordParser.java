package org.mart.crs.management.chord;

import org.mart.crs.config.ConfigSettings;
import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.models.lm.TextForLMCreator;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.utils.helper.HelperFile;
import junit.framework.TestCase;

import java.io.File;

/**
 * @version 1.0 6/14/11 1:16 AM
 * @author: Hut
 */
public class TestChordParser extends TestCase {

    public void testParseAllChords() {
        String filePath = "/home/hut/Beatles/labels";
        for (File file : HelperFile.getFile(filePath).listFiles()) {
            ChordStructure chordStructure = new ChordStructure(file.getPath());
        }
    }

    public void testCreateTextForLM() {
        ConfigSettings.CONFIG_FILE_PATH = "./cfg/configChords.cfg";
        Settings.initialize();
        String wavFileList = "/home/hut/Beatles/list/all.txt";
        TextForLMCreator.process(wavFileList, "temp/LmTest/text", false);
    }

}
