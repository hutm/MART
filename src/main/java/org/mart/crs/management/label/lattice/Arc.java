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

package org.mart.crs.management.label.lattice;

import org.imirsel.nema.model.NemaSegment;
import org.mart.crs.management.label.chord.ChordSegment;

public class Arc extends ChordSegment {

    protected int startIndex;
    protected int endIndex;

    protected double acWeigth;
    protected double lmWeigth;


    public Arc(double startTime, double endTime, String name) {
        super(startTime, endTime, name);
    }

    public Arc(int startIndex, int endIndex, String name, double acWeigth, double lmWeigth) {
        super(startIndex, endIndex, name);
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.chordNameOriginal = name;
        this.acWeigth = acWeigth;
        this.lmWeigth = lmWeigth;
    }

    public Arc() {
    }


    public double getAcWeigth() {
        return acWeigth;
    }

    public void setAcWeigth(float acWeigth) {
        this.acWeigth = acWeigth;
    }

    public double getLmWeigth() {
        return lmWeigth;
    }

    public void setLmWeigth(float lmWeigth) {
        this.lmWeigth = lmWeigth;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    @Override
    public int compareTo(NemaSegment o) {
        if(!(o.getOnset() == getOnset())){
            if (getOnset() > o.getOnset()) {
                return 1;
            } else{
                return -1;
            }
        } else{
            if (getOffset() > o.getOffset()) {
                return 1;
            } else{
                return -1;
            }
        }
    }
}
