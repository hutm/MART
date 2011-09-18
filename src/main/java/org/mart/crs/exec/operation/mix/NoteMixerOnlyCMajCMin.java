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
import org.mart.crs.exec.operation.Operation;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.Helper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @version 1.0 18-Oct-2010 15:07:27
 * @author: Hut
 */
public class NoteMixerOnlyCMajCMin extends NoteMixer {
    public NoteMixerOnlyCMajCMin(String workingDir, int chordNumber) {
        super(workingDir, chordNumber);
    }

    @Override
    public void initialize() {
        noteFiles = new HashMap<Integer, List<String>>();
        File[] noteFileList = (new File(workingDir + noteSubDir)).listFiles(new ExtensionFileFilter(Settings.WAV_EXT));

        for (File noteFile : noteFileList) {
            String note = noteFile.getName().substring(0, noteFile.getName().indexOf("_"));
            int midiNumber = Helper.getMidiNumberForNote(note);
            if (!noteFiles.containsKey(midiNumber)) {
                List<String> list = new ArrayList<String>();
                list.add(noteFile.getPath());
                noteFiles.put(midiNumber, list);
            } else {
                noteFiles.get(midiNumber).add(noteFile.getPath());
            }
        }
    }

    protected void generateChord() {
//        int chordNumber = (int) Math.round(Math.random()) * 12;                                      //TODO refactor notesFromRandomChord
//
//        String chordName = LabelsParser.getChordForNumber(String.valueOf(chordNumber));
//        int[] notes = new int[3];
//        notes[0] = chordNumber % 12;
//        if (chordNumber < 12) {
//            notes[1] = (chordNumber + 4) % 12;
//        } else {
//            notes[1] = (chordNumber + 3) % 12;
//        }
//        notes[2] = (chordNumber + 7) % 12;
//
//
//        String[] fileNamesToMix = new String[3];
//        for (int i = 0; i < fileNamesToMix.length; i++) {
//            int index = (int) Math.round(Math.random() * (noteFiles.get(notes[i] + 60).size() - 1));
//            fileNamesToMix[i] = noteFiles.get(notes[i] + 60).get(index);
//
//        }
//        mixFilesAndSave(fileNamesToMix, chordName);
    }

    public static void main(String[] args) {
        Operation noteMixer = new NoteMixerOnlyCMajCMin("d:/work/notesCMaj", 200);
        noteMixer.initialize();
        noteMixer.operate();
    }
    


}
