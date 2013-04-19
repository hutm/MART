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

package org.mart.crs.management.beat.segment;

/**
 * @version 1.0 12/4/10 2:57 PM
 * @author: Hut
 */
public class BeatSegmentState implements Comparable{

    protected double startTime;
    protected double endTime;

    protected int stateNumber;


    public BeatSegmentState(double startTime, double endTime, int stateNumber) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.stateNumber = stateNumber;
    }


    public double getStartTime() {
        return startTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public int getStateNumber() {
        return stateNumber;
    }

   public int compareTo(Object o) {
        if (((BeatSegmentState) o).getStartTime() > this.getStartTime()) {
            return -1;
        } else {
            return 1;
        }
    }

    public boolean equals(Object obj) {
        BeatSegmentState beatSegmentState;
        if (obj instanceof BeatSegmentState) {
            beatSegmentState = (BeatSegmentState) obj;
            return this.startTime == beatSegmentState.startTime && this.endTime == beatSegmentState.endTime;
        }
        return false;
    }
}
