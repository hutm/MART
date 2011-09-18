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

package org.mart.crs.management.beat.segment;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 07/11/10 22:30
 * @author: Hut
 */
public class BeatSegment {

    protected double timeInstant;
    protected double nextBeatTimeInstant;

    protected int beat;
    protected int measure;
    protected int tatum;

    protected List<BeatSegmentState> beatSegmentStates;


    public BeatSegment(double timeInstant, int beat) {
        this.timeInstant = timeInstant;
        this.beat = beat;
        this.beatSegmentStates = new ArrayList<BeatSegmentState>();
    }

    public BeatSegment(double timeInstant, int beat, int measure, int tatum) {
        this(timeInstant, beat);
        this.measure = measure;
        this.tatum = tatum;
    }

    public BeatSegment(double timeInstant, int beat, int measure, int tatum, double nextBeatTimeInstant) {
        this(timeInstant, beat, measure, tatum);
        this.nextBeatTimeInstant = nextBeatTimeInstant;
    }

    public double getTimeInstant() {
        return timeInstant;
    }

    public void setTimeInstant(double timeInstant) {
        this.timeInstant = timeInstant;
    }

    public int getBeat() {
        return beat;
    }

    public void setBeat(int beat) {
        this.beat = beat;
    }

    public int getMeasure() {
        return measure;
    }

    public void setMeasure(int measure) {
        this.measure = measure;
    }

    public int getTatum() {
        return tatum;
    }

    public void setTatum(int tatum) {
        this.tatum = tatum;
    }

    public double getNextBeatTimeInstant() {
        return nextBeatTimeInstant;
    }

    public void setNextBeatTimeInstant(double nextBeatTimeInstant) {
        this.nextBeatTimeInstant = nextBeatTimeInstant;
    }


    public void addBeatSegmentState(BeatSegmentState beatSegmentState) {
        this.beatSegmentStates.add(beatSegmentState);
    }

    public List<BeatSegmentState> getBeatSegmentStates() {
        return beatSegmentStates;
    }

    public boolean isDownBeat() {
        return (beat == 1);
    }

    @Override
    public String toString() {
        String beatType;
        if (beat == 1) {
            beatType = "downbeat";
        } else {
            beatType = "beat";
        }

        return beatType;

    }


    public double getDuration(){
        return nextBeatTimeInstant - timeInstant;
    }


}
