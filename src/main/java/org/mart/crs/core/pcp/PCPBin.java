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

package org.mart.crs.core.pcp;

import org.mart.crs.utils.helper.Helper;

/**
 * @version 1.0 31-May-2010 18:23:41
 * @author: Hut
 */
public class PCPBin implements Comparable {

    private String tone;
    private int subBin;
    private int octave;

    public PCPBin(String tone, int subBin, int octave) {
        this.tone = tone;
        this.subBin = subBin;
        this.octave = octave;
    }


    public int compareTo(Object o) {
        String note = String.format("%s%d", tone, octave);
        String noteToCompare =  String.format("%s%d", ((PCPBin) o).getTone(), ((PCPBin) o).getOctave());
        int midiNote = Helper.getMidiNumberForNote(note);
        int midiNoteToCompare = Helper.getMidiNumberForNote(noteToCompare);

        if(midiNote != midiNoteToCompare){
            return midiNote - midiNoteToCompare;
        } else{
            return subBin - ((PCPBin) o).getSubBin();
        }
    }


    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public int getSubBin() {
        return subBin;
    }

    public void setSubBin(int subBin) {
        this.subBin = subBin;
    }

    public int getOctave() {
        return octave;
    }

    public void setOctave(int octave) {
        this.octave = octave;
    }

    @Override
    public boolean equals(Object obj) {
        return this.toString().equals( obj.toString());
    }

    @Override
    public String toString() {
        if (subBin < 0) {
            return String.format("%s%d", tone, octave);
        }
        return String.format("%d%s%d", subBin, tone, octave);
    }
}