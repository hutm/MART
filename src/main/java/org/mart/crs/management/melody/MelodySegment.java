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

package org.mart.crs.management.melody;

import org.mart.crs.utils.helper.Helper;

/**
 * Time segment of a melody
 * @version 1.0 2/21/13 11:25 AM
 * @author: Hut
 */
public class MelodySegment {

    protected float startTime;
    protected float endTime;
    protected float pitch;
    protected int midiNote;
    protected boolean voiced;

    public MelodySegment(float startTime, float endTime, float pitch) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.pitch = Math.abs(pitch);
        this.midiNote = Math.round(Helper.getMidiNoteForFreq(this.pitch));
        this.voiced = pitch > 0;
    }


    public float getStartTime() {
        return startTime;
    }

    public float getEndTime() {
        return endTime;
    }

    public float getPitch() {
        return pitch;
    }

    public boolean isVoiced() {
        return voiced;
    }

    public void setStartTime(float startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(float endTime) {
        this.endTime = endTime;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setVoiced(boolean voiced) {
        this.voiced = voiced;
    }

    public int getMidiNote() {
        return Math.max(0, midiNote);
    }
}
