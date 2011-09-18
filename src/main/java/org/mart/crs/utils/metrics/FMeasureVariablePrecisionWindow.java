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

package org.mart.crs.utils.metrics;

/**
 * @version 1.0 1/25/11 4:10 PM
 * @author: Hut
 */
public class FMeasureVariablePrecisionWindow extends FMeasure {

    protected double[] downbeatPrecisionWindows;

    public FMeasureVariablePrecisionWindow(double[] recognized, double[] gt, float precisionWindow) {
        super(recognized, gt, precisionWindow);
    }


    protected boolean matches(int recognizedIndex, int gtIndex){
        if(downbeatPrecisionWindows == null){
            generateDownbeatPrecisionWindows();
        }
        return Math.abs(recognized[recognizedIndex] - gt[gtIndex]) < downbeatPrecisionWindows[recognizedIndex];
    }

    protected void generateDownbeatPrecisionWindows(){
        downbeatPrecisionWindows = new double[recognized.length];
        for(int i = 0; i < recognized.length-1; i++){
            downbeatPrecisionWindows[i+1] = (recognized[i+1] - recognized[i]) * precisionWindow;
        }
        if (downbeatPrecisionWindows.length > 1) {
            downbeatPrecisionWindows[0] = downbeatPrecisionWindows[1];
        }
    }

}
