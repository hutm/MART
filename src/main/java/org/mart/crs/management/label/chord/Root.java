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

import org.mart.crs.utils.helper.HelperArrays;

/**
 * @version 1.0 4/27/11 1:25 PM
 * @author: Hut
 */
public enum Root {


    C("C"),
    C_SHARP("C#"),
    D("D"),
    D_SHARP("D#"),
    E("E"),
    F("F"),
    F_SHARP("F#"),
    G("G"),
    G_SHARP("G#"),
    A("A"),
    A_SHARP("A#"),
    B("B");

    public static final String BEMOL_SYMBOL = "b";
    public static final String BEMOL_DOUBLE_SYMBOL = "bb";
    public static final String SHARP_SYMBOL = "#";
    public static final String SHARP_DOUBLE_SYMBOL = "##";



    private String name;

    Root(String name) {
        this.name = name;
    }

    public static Root fromString(String text) {                    //TODO maybe a possible bug if bemol symbol is not just after root note
        if (text != null) {
            String root;
            int ordinalOut = 0;
            boolean isBemol = text.indexOf(BEMOL_SYMBOL) > 0;              //TODO   && text.indexOf(BEMOL_SYMBOL) <= 2
            boolean isBemolDouble = text.indexOf(BEMOL_DOUBLE_SYMBOL) > 0;
            boolean isSharp = text.indexOf(SHARP_SYMBOL) > 0;
            boolean isSharpDouble = text.indexOf(SHARP_DOUBLE_SYMBOL) > 0;

            Root tempRoot = null;
            root = text.substring(0, 1);
            for (Root b : Root.values()) {
                if (root.contains(b.getName())) {
                    tempRoot = b;
                }
            }
            if (tempRoot != null) {
                if(isBemolDouble){
                    ordinalOut = tempRoot.ordinal() - 2;
                } else  if(isBemol){
                    ordinalOut = tempRoot.ordinal() - 1;
                } else  if(isSharpDouble){
                    ordinalOut = tempRoot.ordinal() + 2;
                } else  if(isSharp){
                    ordinalOut = tempRoot.ordinal() + 1;
                }

                ordinalOut = HelperArrays.transformIntValueToBaseRange(ordinalOut, ChordSegment.SEMITONE_NUMBER);
                return Root.values()[ordinalOut];
            }
        }
        return null;
    }


    public String getName() {
        return this.name;
    }


    @Override
    public String toString() {
        return getName();
    }
}
