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

import org.mart.crs.utils.helper.HelperArrays;

public class FMeasure {

    protected double precision;
    protected double recall;
    protected double fmeasure;

    protected double[] recognized;
    protected double[] gt;
    protected double precisionWindow;

    public FMeasure(double[] recognized, double[] gt, float precisionWindow) {
        this.recognized = recognized;
        this.gt = gt;
        this.precisionWindow = precisionWindow;
        calculateMeasures();
    }

    protected void calculateMeasures() {
        int fp = 0;
        int fn = 0;
        int cd = 0;

        int[] foundArray = new int[gt.length];

        for (int i = 0; i < recognized.length; i++) {
            boolean found = false;
            for (int j = 0; j < gt.length; j++) {
                if (matches(i, j)) {
                    cd++;
                    foundArray[j] = 1;
                    found = true;
                    break;
                }
            }
            if (!found) {
                fp++;
            }
        }

        //Now calculate false positives
        fn = foundArray.length - HelperArrays.sum(foundArray);

        precision = cd / (cd + fp + 0.0f);
        recall = cd / (cd + fn + 0.0f);
        if (precision + recall > 0) {
            fmeasure = 2 * precision * recall / (precision + recall + 0.0f);
        }
    }

    protected boolean matches(int recognizedIndex, int gtIndex){
        return Math.abs(recognized[recognizedIndex] - gt[gtIndex]) < precisionWindow;
    }

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }

    public double getFmeasure() {
        return fmeasure;
    }
}
