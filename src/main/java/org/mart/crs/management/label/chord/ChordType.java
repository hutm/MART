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

import org.mart.crs.config.Settings;

import java.util.Arrays;

/**
 * @version 1.0 4/27/11 3:03 PM
 * @author: Hut
 */
public enum ChordType {

    //Chord Dictionary: maj, min, maj6=min7, 7, maj7, dim, aug             N maj min 7 maj6 min7 maj7 9 sus4 sus2 aug dim
    UNKNOWN_CHORD("UNK", new int[]{25}, null),
    NOT_A_CHORD("N",  new int[]{24}, UNKNOWN_CHORD),
    MAJOR_CHORD("maj", new int[]{0, 4, 7}, NOT_A_CHORD),
    MINOR_CHORD("min", new int[]{0, 3, 7}, NOT_A_CHORD),
    SEVEN_CHORD ("seventh", new int[]{0, 4, 7, 10}, MAJOR_CHORD),
    MAJOR_6_CHORD("maj6",  new int[]{0, 4, 7, 9}, MAJOR_CHORD),
    MINOR_7_CHORD("min7",  new int[]{0, 3, 7, 10}, MINOR_CHORD),  //Inversion of maj6
    MAJOR_7_CHORD("maj7",  new int[]{0, 4, 7, 11}, MAJOR_CHORD),
    NINE_CHORD("ninth", new int[]{0, 2, 4, 7, 10}, SEVEN_CHORD),
    SUS4_CHORD("sus4", new int[]{0, 5, 7}, UNKNOWN_CHORD),
    SUS2_CHORD("sus2", new int[]{0, 2, 7}, UNKNOWN_CHORD), //Inversion of sus4
    AUG_CHORD("aug", new int[]{0, 4, 8}, UNKNOWN_CHORD),
    DIM_CHORD("dim", new int[]{0, 3, 6}, UNKNOWN_CHORD);


    public static ChordType[] chordDictionary;

    public static final ChordType[] CHORD_DICTIONARY_FULL = new ChordType[]{NOT_A_CHORD, MAJOR_CHORD, MINOR_CHORD, SEVEN_CHORD, MAJOR_6_CHORD, MINOR_7_CHORD, MAJOR_7_CHORD, NINE_CHORD, SUS4_CHORD, SUS2_CHORD, AUG_CHORD, DIM_CHORD};


    static{
        String[] chordDictionaryStrings = Settings.chordDictionary;
        chordDictionary = new ChordType[chordDictionaryStrings.length];
        for(int i = 0; i < chordDictionaryStrings.length; i++){
            chordDictionary[i] = fromString(chordDictionaryStrings[i]);
        }
    }

    private String name;
    private int[] notes;

    /**
     * If chord type is not included into dictionary, this chord type is treated as  alternativeInterpretation
     */
    private ChordType alternativeInterpretation;



    ChordType(String name, int[] notes, ChordType alternativeInterpretation) {
        this.name = name;
        this.notes = notes;
        this.alternativeInterpretation = alternativeInterpretation;
    }


    public String getName() {
        if (this.equals(UNKNOWN_CHORD) || Arrays.asList(chordDictionary).contains(this)) {
            return this.name;
        } else{
            return this.alternativeInterpretation.getName();
        }
    }

    public String getOriginalName() {
        return this.name;
    }

    public int[] getNotes(){
        return this.notes;
    }

     public static ChordType fromString(String text) {
        if (text != null) {
            for (ChordType b : ChordType.values()) {
                if (text.equalsIgnoreCase(b.getOriginalName())) {
                    return b;
                }
            }
        }
        return null;
    }


    @Override
    public String toString() {
        return getName();
    }

    public static String[] getStringValues(){
        String[] out = new String[chordDictionary.length];
        int counter = 0;
        for(ChordType chordType: chordDictionary){
            out[counter++] = chordType.getOriginalName();
        }
        return out;
    }

}
