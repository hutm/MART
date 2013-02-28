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

package org.mart.crs.management.label.chord;

import org.mart.crs.management.beat.BeatStructure;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 4/19/12 1:54 AM
 * @author: Hut
 */
public class ChordStructureTest {

    @Test
    public void getMostFrequentString(){
        List<String> list = new ArrayList<String>();
        list.add("A1");
        list.add("B1");
        list.add("C1");
        list.add("A1");
        list.add("A1");
        list.add("B1");
        list.add("A1");
        list.add("B1");
        list.add("A1");
        list.add("C1");
        list.add("A1");
        list.add("D1");
        list.add("D1");
        list.add("B1");
        list.add("B1");
        list.add("B1");



        String outString = ChordStructure.getMostFrequentString(list);
        System.out.println(outString);

    }


    @Test
    public void testReadChordStructureFromCSV() {
        String filePath = this.getClass().getResource("/chordLabels/01-chords.csv").getPath();
        ChordStructure chordStructure = new ChordStructure(filePath, true);
        Assert.assertEquals(chordStructure.getChordSegments().size(), 65);
    }

    @Test
    public void testSynchronizeChordsWithBeats(){
        String filePath = this.getClass().getResource("/chordLabels/01-chords.csv").getPath();
        ChordStructure chordStructure = new ChordStructure(filePath, true);

        filePath = this.getClass().getResource("/chordLabels/01-beats.csv").getPath();
        BeatStructure beatStructure =  BeatStructure.getBeatStructure(filePath);

        ChordStructure syncedChords = chordStructure.getSegmentsPerBeat(beatStructure.getBeats());

        Assert.assertEquals(syncedChords.getChordSegments().size(), 612);

        String chordString = syncedChords.getStringRepresentation();
    }
}
