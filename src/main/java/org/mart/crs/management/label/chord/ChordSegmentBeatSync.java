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

package org.mart.crs.management.label.chord;

/**
 * @version 1.0 4/19/12 1:29 PM
 * @author: Hut
 */
public class ChordSegmentBeatSync extends ChordSegment {

    protected int onsetBeat;
    protected int offsetBeat;

    public ChordSegmentBeatSync(String chordString) {
        super(chordString);
    }

    public ChordSegmentBeatSync(ChordSegment chordSegment, double[] beats) {
        super(chordSegment.getOnset(), chordSegment.getOffset(), chordSegment.getChordName(), chordSegment.getLogLikelihood());
        initializeBeats(beats);
    }

    public ChordSegmentBeatSync(double onset,  double offset, String chordName, int onsetbeat, int offsetBeat) {
        super(onset, offset, chordName);
        this.onsetBeat = onsetbeat;
        this.offsetBeat = offsetBeat;
    }


    protected void initializeBeats(double[] beats){
        onsetBeat = -1;
        offsetBeat = -1;
        for(int i = 0; i < beats.length; i++){
            if(Math.abs(getOnset() - beats[i]) < 0.005){
                onsetBeat = i;
            }
            if(Math.abs(getOffset() - beats[i]) < 0.005){
                offsetBeat = i;
            }

        }
        if(onsetBeat < 0){
            logger.error(String.format("Cannot assign onset for %s", this.toString()));
        }
        if(offset < 0){
            logger.error(String.format("Cannot assign offset for %s", this.toString()));
        }
    }

    public boolean intersectsBeats(ChordSegmentBeatSync cs1) {
        return onsetBeat < cs1.getOffsetBeat() && offsetBeat > cs1.getOnsetBeat();
    }


    public float getIntersectionInBeats(ChordSegmentBeatSync cs2) {
        if (!intersects(cs2)) {
            return 0;
        } else {
            return (float) (Math.min(getOffsetBeat(), cs2.getOffsetBeat()) - Math.max(getOnsetBeat(), cs2.getOnsetBeat()));
        }
    }

    public ChordSegmentBeatSync getIntersectionInBeatsSegment(ChordSegmentBeatSync cs2) {
        if (!intersects(cs2)) {
            return null;
        } else {
            int  offsetBeat = Math.min(getOffsetBeat(), cs2.getOffsetBeat());
            int  onsetBeat = Math.max(getOnsetBeat(), cs2.getOnsetBeat());

            double  offset = Math.min(getOffset(), cs2.getOffset());
            double  onset = Math.max(getOnset(), cs2.getOnset());

            return new ChordSegmentBeatSync(onset, offset, ChordType.NOT_A_CHORD.getName(), onsetBeat, offsetBeat);
        }
    }






    public int getDurationInBeats(){
        return offsetBeat - onsetBeat;
    }


    public int getOnsetBeat() {
        return onsetBeat;
    }

    public void setOnsetBeat(int onsetBeat) {
        this.onsetBeat = onsetBeat;
    }

    public int getOffsetBeat() {
        return offsetBeat;
    }

    public void setOffsetBeat(int offsetBeat) {
        this.offsetBeat = offsetBeat;
    }



}
