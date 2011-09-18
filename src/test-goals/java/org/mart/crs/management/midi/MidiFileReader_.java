package org.mart.crs.management.midi;

import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;


/**
 * @version 1.0 Jul 3, 2009 3:29:43 PM
 * @author: Maksim Khadkevich
 */
public class MidiFileReader_ extends TestCase {

    public static void testExtractGT() {
        MidiManager.readScoreAndSave("data/MIDI/1.mid", "data/MIDI/1.txt");
    }


    public static void testExtractGTfromFolder(){
        String folderPath = "D:\\midi\\AllZipped\\brahmpno";

        File folder = new File(folderPath);
        File[] midiFiles = folder.listFiles(new ExtensionFileFilter(new String[]{".mid"}));

        for(File midiFile:midiFiles){
            try {
                String midiFilePath = midiFile.getCanonicalPath();
                String txtFilePath = midiFilePath.replaceAll("\\.mid", ".txt");
                txtFilePath = txtFilePath.replaceAll("\\.MID", ".txt");
                System.out.println("Extracting ground-truth for file " + midiFile.getName());
                MidiManager.readScoreAndSave(midiFilePath, txtFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
