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

package org.mart.crs.management.label.lattice;

import org.mart.crs.management.label.chord.ChordSegment;

import java.util.List;

/**
 * @version 1.0 Nov 11, 2009 11:39:52 AM
 * @author: Maksim Khadkevich
 */
public class Song {

    protected String name;
    protected String songDirectory;
    protected List<ChordSegment> chordList;

    public Song(String name, List<ChordSegment> chordList) {
        this.name = name;
        this.chordList = chordList;
    }

    public Song(String name, String songFirectory, List<ChordSegment> chordList) {
        this.name = name;
        this.songDirectory = songFirectory;
        this.chordList = chordList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ChordSegment> getChordList() {
        return chordList;
    }

    public void setChordList(List<ChordSegment> chordList) {
        this.chordList = chordList;
    }

    public boolean equals(Object o) {
        if (((Song) o).getName().equals(name)) {
            return true;
        } else {
            return false;
        }
    }
}
