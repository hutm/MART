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


import org.mart.crs.management.config.Configuration;

import java.util.Arrays;

/**
 * @version 1.0 4/27/11 3:03 PM
 * @author: Hut
 */
public enum ChordType {

    //Chord Dictionary: maj, min, maj6=min7, 7, maj7, dim, aug             N maj min 7 maj6 min7 maj7 9 sus4 sus2 aug dim
    UNKNOWN_CHORD("UNK", new int[]{25}, null),
    NOT_A_CHORD("N",  new int[]{24}, UNKNOWN_CHORD),
    MAJOR_CHORD("maj", new int[]{0, 4, 7}, UNKNOWN_CHORD),
    MINOR_CHORD("min", new int[]{0, 3, 7}, UNKNOWN_CHORD),
    SEVEN_CHORD ("seventh", new int[]{0, 4, 7, 10}, MAJOR_CHORD),
    MAJOR_6_CHORD("maj6",  new int[]{0, 4, 7, 9}, MAJOR_CHORD),
    MINOR_7_CHORD("min7",  new int[]{0, 3, 7, 10}, MINOR_CHORD),  //Inversion of maj6
    MAJOR_7_CHORD("maj7",  new int[]{0, 4, 7, 11}, MAJOR_CHORD),
    NINE_CHORD("ninth", new int[]{0, 2, 4, 7, 10}, SEVEN_CHORD),
    SUS4_CHORD("sus4", new int[]{0, 5, 7}, UNKNOWN_CHORD),
    SUS2_CHORD("sus2", new int[]{0, 2, 7}, UNKNOWN_CHORD), //Inversion of sus4
    AUG_CHORD("aug", new int[]{0, 4, 8}, UNKNOWN_CHORD),
    DIM_CHORD("dim", new int[]{0, 3, 6}, UNKNOWN_CHORD);


    public static final ChordType[] CHORD_DICTIONARY_FULL = new ChordType[]{NOT_A_CHORD, MAJOR_CHORD, MINOR_CHORD, SEVEN_CHORD, MAJOR_6_CHORD, MINOR_7_CHORD, MAJOR_7_CHORD, NINE_CHORD, SUS4_CHORD, SUS2_CHORD, AUG_CHORD, DIM_CHORD};
    public static ChordType[] chordDictionary;

    public static final boolean isToUseChordWrappersToTrainChordChildren = true;


    static{
        String[] chordDictionaryStrings = Configuration.chordDictionary;
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
    
    
    /**
     * Generated chord type from BIAB chord format
     * @param text chord type in biab format
     * @return
     */
    public static ChordType fromBIABString(String text) {
        if (text != null) {
           if(Arrays.asList(BIAB_MAJ).contains(text)){
               return MAJOR_CHORD;
           }
            if(Arrays.asList(BIAB_MIN).contains(text)){
                return MINOR_CHORD;
            }
            if(Arrays.asList(BIAB_MAJ).contains(text)){
                return MAJOR_CHORD;
            }
            if(Arrays.asList(BIAB_SEVEN).contains(text)){
                return SEVEN_CHORD;
            }
            if(Arrays.asList(BIAB_MAJ6).contains(text)){
                return MAJOR_6_CHORD;
            }
            if(Arrays.asList(BIAB_MIN7).contains(text)){
                return MINOR_7_CHORD;
            }
            if(Arrays.asList(BIAB_MAJ7).contains(text)){
                return MAJOR_7_CHORD;
            }
            if(Arrays.asList(BIAB_NINE).contains(text)){
                return NINE_CHORD;
            }
            if(Arrays.asList(BIAB_SUS4).contains(text)){
                return SUS4_CHORD;
            }
            if(Arrays.asList(BIAB_AUG).contains(text)){
                return AUG_CHORD;
            }
            if(Arrays.asList(BIAB_DIM).contains(text)){
                return DIM_CHORD;
            }
        }
        return null;
    }


    public static String[] BIAB_MAJ = new String[]{"", "maj", "5b"};
    public static String[] BIAB_MIN = new String[]{"min", "m", "m6", "maug", "m#5", "mMaj7", "mM7"};
    public static String[] BIAB_SEVEN = new String[]{"7alt","7", "7+","9+", "13+", "13", "7b13", "7#11", "13#11", "7#11b13", "9", "9b13", "9#11", "13#11", "9#11b13", "7b9", "13b9", "7b9b13", "7b9#11", "13b9#11", "7b9#11b13", "7#9", "13#9", "7#9b13", "9#11", "13#9#11", "7#9#11b13",
            "7b5", "13b5", "7b5b13", "9b5", "9b5b13", "7b5b9", "13b5b9", "7b5b9b13", "7b5#9", "13b5#9", "7b5#9b13", "7#5", "13#5", "7#5#11", "13#5#11", "9#5", "9#5#11", "7#5b9", "13#5b9", "7#5b9#11", "13#5b9#11", "7#5#9", "13#5#9#11", "7#5#9#11", "13#5#9#11"
    };
    public static String[] BIAB_MAJ6 = new String[]{"6", "69", "maj6"};
    public static String[] BIAB_MIN7 = new String[]{"m7", "m9", "m11", "m13", "min7"};
    public static String[] BIAB_MAJ7 = new String[]{"maj7", "maj7#5", "11"};
    public static String[] BIAB_NINE = new String[]{"maj9", "maj13", "maj9#11", "maj13#11"};
    public static String[] BIAB_SUS4 = new String[]{"sus", "7sus", "9sus", "13sus", "7susb13", "7sus#11", "13sus#11", "7sus#11b13", "9susb13", "9sus#11", "13sus#11", "9sus#11b13", "7susb9", "13susb9", "7susb9b13", "7susb9#11", "13susb9#11", "7susb9#11b13", "7sus#9", "13sus#9", "7sus#9b13", "9sus#11", "13sus#9#11", "7sus#9#11b13",
            "7susb5", "13susb5", "7susb5b13", "9susb5", "9susb5b13", "7susb5b9", "13susb5b9", "7susb5b9b13", "7susb5#9", "13susb5#9", "7susb5#9b13", "7sus#5", "13sus#5", "7sus#5#11", "13sus#5#11", "9sus#5", "9sus#5#11", "7sus#5b9", "13sus#5b9", "7sus#5b9#11", "13sus#5b9#11", "7sus#5#9", "13sus#5#9#11", "7sus#5#9#11", "13sus#5#9#11"
    };
    public static String[] BIAB_AUG = new String[]{"aug", "+"};
    public static String[] BIAB_DIM = new String[]{"dim", "m7b5"};


    public ChordType getAlternativeInterpretation() {
        return alternativeInterpretation == null ? this : alternativeInterpretation;
    }
}
