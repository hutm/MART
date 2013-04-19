/*
 * Copyright (c) 2008-2013 Maksim Khadkevich and Fondazione Bruno Kessler.
 *
 * This file is part of MART.
 * MART is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2, as published
 * by the Free Software Foundation.
 *
 * MART is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with MART; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.mart.crs.management.label.chord.type.statistics;

import org.mart.crs.config.Extensions;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @version 1.0 3/31/11 11:15 PM
 * @author: Hut
 */
public class ChordTypeStatistics {

    protected List<ChordTypeStatisticalItem> chordTypes;

    protected String gtFilePath;

    public ChordTypeStatistics(String gtFilePath) {
        this.gtFilePath = gtFilePath;
        this.chordTypes = new ArrayList<ChordTypeStatisticalItem>();
    }


    public void fillStatistics() {
        File[] gtFiles = HelperFile.getFile(gtFilePath).listFiles(new ExtensionFileFilter(Extensions.LABEL_EXT, false));
        for (File gtFile : gtFiles) {
            ChordStructure chordStructure = new ChordStructure(gtFile.getPath());
            for (ChordSegment chordSegment : chordStructure.getChordSegments()) {
                processChordSegment(chordSegment);
            }
        }
        Collections.sort(chordTypes);
        exportStatistics("/home/hut/work/statistics.csv");
        System.out.println("Done!!!");
    }

    protected void processChordSegment(ChordSegment chordSegment) {
        int[] notes = chordSegment.getNotesWithoutDuplicates();
        ChordTypeStatisticalItem existingChordType = findChordInMap(notes);
        if (existingChordType == null) {
            ChordTypeStatisticalItem chordType = new ChordTypeStatisticalItem(notes);
            chordType.addChordSegment(chordSegment);
            chordTypes.add(chordType);
        } else {
            existingChordType.addChordSegment(chordSegment);
        }
    }

    protected ChordTypeStatisticalItem findChordInMap(int[] notes) {
        for (ChordTypeStatisticalItem chordType : chordTypes) {
            int[] maptemplate = chordType.getNotes();
            if(ChordSegment.isNoteArraysBelongToTheSameChord(maptemplate, notes)){
                return chordType;
            }
        }
        //Nothing has been found
        return null;
    }




    protected void exportStatistics(String filePath) {
        List<String> outList = new ArrayList<String>();
        for (ChordTypeStatisticalItem chordType : chordTypes) {
            outList.add(chordType.toString());
        }
        HelperFile.saveCollectionInFile(outList, filePath, false);
    }


    //TODO: move to tests
/*    public static void main(String[] args) {
        ChordTypeStatistics chordTypeStatistics = new ChordTypeStatistics(Settings.chordLabelsGroundTruthDir);
        chordTypeStatistics.fillStatistics();
    }*/


}
