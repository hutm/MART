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

package org.mart.crs.exec.operation.mix;

import org.mart.crs.config.Settings;
import org.mart.crs.core.AudioReader;
import org.mart.crs.exec.operation.Operation;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NoteMixer provides functionality for mixing 3 notes into a chord and save into a separate file
 * @version 1.0 21-Sep-2010 00:04:12
 * @author: Hut
 */
public class NoteMixer extends Operation{

    protected Map<Integer, List<String>> noteFiles;

    protected String noteSubDir = "/notes";
    protected String chordSubDir = "/chords";


    /**
     * Number of output chords to generate
     */
    protected int chordNumber;

    public NoteMixer(String workingDir, int chordNumber) {
        super(workingDir);
        this.chordNumber = chordNumber;
    }

    @Override
    public void initialize() {
        noteFiles = new HashMap<Integer, List<String>>();
        File[] noteFileList = (new File(workingDir + noteSubDir)).listFiles(new ExtensionFileFilter(Settings.WAV_EXT));

        for(File noteFile:noteFileList){
            String note = noteFile.getName().substring(0, noteFile.getName().indexOf("_"));
            int midiNumber = Helper.getMidiNumberForNote(note) % 12;
            if(!noteFiles.containsKey(midiNumber)){
                List<String> list = new ArrayList<String>();
                list.add(noteFile.getPath());
                noteFiles.put(midiNumber, list);
            } else{
                noteFiles.get(midiNumber).add(noteFile.getPath());
            }
        }
    }

    @Override
    public void operate() {
        for(int i = 0; i < chordNumber; i++){
            generateChord();
        }
    }

    protected void generateChord(){
        int chordNumber = (int)Math.round(Math.random()*23);

//        String  chordName = LabelsParser.getChordForNumber(String.valueOf(chordNumber));          //TODO refactor notesFromRandomChord
//        int[] notes = new int[3];
//        notes[0] = chordNumber % 12;
//        if (chordNumber < 12) {
//            notes[1] = (chordNumber + 4) % 12;
//        } else{
//            notes[1] = (chordNumber + 3) % 12;
//        }
//        notes[2] = (chordNumber + 7) % 12;
//
//
//        String[] fileNamesToMix = new String[3];
//        for(int i = 0; i < fileNamesToMix.length; i++){
//            int index = (int)Math.round(Math.random()*(noteFiles.get(notes[i]).size() - 1));
//            fileNamesToMix[i] = noteFiles.get(notes[i]).get(index);
//        }
//        mixFilesAndSave(fileNamesToMix, chordName);
    }

    


    protected void mixFilesAndSave(String[] files, String chordName){
        AudioReader[] readers = new AudioReader[files.length];
        float[][] samples = new float[files.length][];
        int minSamples = Integer.MAX_VALUE;
        for(int i = 0; i < files.length; i++){
            readers[i] = new AudioReader(files[i]);
            samples[i] = readers[i].getSamples();
            if(minSamples > samples[i].length ){
                minSamples = samples[i].length;
            }
        }
        float[] outSamples = new float[minSamples];
        for(int i = 0; i < readers.length; i++){
            for (int j = 0; j < minSamples; j++) {
                if (j < samples[i].length) {
                    outSamples[j] += samples[i][j] / readers.length;
                }
            }
        }
        StringBuilder filesStringData = new StringBuilder();
        for(int i = 0; i < files.length; i++){
            filesStringData.append(HelperFile.getNameWithoutExtension((new File(files[i])).getName())).append("_");
        }
        String fileName = String.format("%s/%s/%s_%s", workingDir, chordSubDir, chordName, filesStringData);
//        readers[0].setPlayInOriginalFormat(true);
        AudioReader.storeAPieceOfMusicAsWav(outSamples, readers[0].getAudioFormat(), fileName + Settings.WAV_EXT);

    }


    public static void main(String[] args) {
        Operation noteMixer = new NoteMixer("d:/work/notes", 1000);
        noteMixer.initialize();
        noteMixer.operate();
    }

}
