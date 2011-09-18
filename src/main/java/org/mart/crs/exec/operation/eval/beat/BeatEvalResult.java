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

package org.mart.crs.exec.operation.eval.beat;

import org.mart.crs.management.beat.BeatStructure;
import org.mart.crs.utils.metrics.FMeasure;
import org.mart.crs.utils.metrics.FMeasureVariablePrecisionWindow;

/**
 * @version 1.0 1/24/11 3:07 PM
 * @author: Hut
 */
public class BeatEvalResult implements Comparable {

    protected BeatStructure recognizedBeatStructure;
    protected BeatStructure gtBeatStructure;

    protected float precisionWindow;

    protected FMeasureVariablePrecisionWindow beatMeasure;
    protected FMeasure downBeatMeasure;


    public BeatEvalResult(BeatStructure recognizedBeatStructure, BeatStructure gtBeatStructure, float precisionWindow) {
        this.recognizedBeatStructure = recognizedBeatStructure;
        this.gtBeatStructure = gtBeatStructure;
        this.precisionWindow = precisionWindow;
        evaluate();
    }



    protected void evaluate() {
        double[] beats = recognizedBeatStructure.getBeats();
        double[] beatsGT = gtBeatStructure.getBeats();

        double[] downBeats = recognizedBeatStructure.getDownBeats();
        double[] downBeatsGT = gtBeatStructure.getDownBeats();

        beatMeasure = new FMeasureVariablePrecisionWindow(beats, beatsGT, precisionWindow);
        downBeatMeasure = new FMeasureVariablePrecisionWindow(downBeats, downBeatsGT, precisionWindow);
    }


    public FMeasure getBeatMeasure() {
        return beatMeasure;
    }

    public FMeasure getDownBeatMeasure() {
        return downBeatMeasure;
    }

    public String getSongName(){
        return  recognizedBeatStructure.getSongName();
    }

    public int compareTo(Object o) {
        return getSongName().compareTo(((BeatEvalResult) o).getSongName());
    }
}


