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

package org.mart.tools.simplechords;

import org.mart.crs.management.label.chord.ChordSegment;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 1/12/12 8:15 PM
 * @author: Hut
 */
public class ChordStructureParser {

    public static final String[] CHORD_LABELS = new String[]{
            "C:maj", "C#:maj", "D:maj", "D#:maj", "E:maj", "F:maj", "F#:maj",
            "G:maj", "G#:maj", "A:maj", "A#:maj", "B:maj",
            "C:min", "C#:min", "D:min", "D#:min", "E:min", "F:min", "F#:min",
            "G:min", "G#:min", "A:min", "A#:min", "B:min", "N"};
    

    public static List<ChordSegment> parseChords(int[] path, float frameStep){
        List<ChordSegment> chordList = new ArrayList<ChordSegment>();
        int currentChord = path[0];
        float currentChordStartTime = 0;
        for(int i = 1; i < path.length; i++){
            if(path[i] != currentChord || i == path.length - 1){
                chordList.add(new ChordSegment(currentChordStartTime, frameStep * i, CHORD_LABELS[path[i-1]]));
                currentChord = path[i];
                currentChordStartTime = frameStep * i;
            } 
        }
         
        return chordList;
    }


}
