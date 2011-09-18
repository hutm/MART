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

package org.mart.crs.management.label.chord.type.statistics;

import org.mart.crs.management.label.chord.ChordSegment;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 4/1/11 1:15 AM
 * @author: Hut
 */
public class ChordTypeStatisticalItem implements Comparable<ChordTypeStatisticalItem> {

    protected int[] notes;

    protected List<String> chordLabels;

    protected double duration;

    protected int numberOfOccurences;


    public ChordTypeStatisticalItem(int[] notes) {
        this.notes = notes;
        this.chordLabels = new ArrayList<String>();
    }


    public int[] getNotes() {
        return notes;
    }

    public void addChordSegment(ChordSegment chordSegment){
        this.chordLabels.add(chordSegment.getChordName());
        this.duration += chordSegment.getDuration();
        this.numberOfOccurences++;
    }

    public double getDuration() {
        return duration;
    }

    public int getNumberOfOccurences() {
        return numberOfOccurences;
    }

    public List<String> getChordLabels() {
        return chordLabels;
    }

    public int compareTo(ChordTypeStatisticalItem o) {
        return o.getNumberOfOccurences() - numberOfOccurences;
    }

    @Override
    public String toString() {
        return String.format("%d,%12.0f,%s, ,%s", numberOfOccurences, duration, getNotesAsString(), getLabelsAsString());
    }

    protected String getNotesAsString(){
        StringBuffer out = new StringBuffer();
        for(int i = 0; i < notes.length; i++){
            out.append(notes[i]).append(" ");
        }
        return out.toString();
    }

    protected String getLabelsAsString(){
        StringBuffer out = new StringBuffer();
        for(String label:chordLabels){
            out.append(label).append(" ");
        }
        return out.toString();
    }
}
