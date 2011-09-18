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
 * @version 1.0 Dec 3, 2010 5:26:00 PM
 * @author: Hut
 */
public class MeasureSegment {

    protected List<BeatSegment> beats;


    public MeasureSegment() {
        this.beats = new ArrayList<BeatSegment>();
    }

    public MeasureSegment(BeatSegment downbeatSegment) {
        this();
        beats.add(downbeatSegment);
    }


    public void addBeatSegment(BeatSegment beatSegment){
        beats.add(beatSegment);
    }

    public List<BeatSegment> getBeats() {
        return beats;
    }

    public double getStartTime(){
        return beats.get(0).getTimeInstant();
    }

    public double getEndTime(){
        return beats.get(beats.size() - 1).getNextBeatTimeInstant();
    }


    @Override
    public String toString() {
        return String.format("measure%d", beats.size());
    }
}
