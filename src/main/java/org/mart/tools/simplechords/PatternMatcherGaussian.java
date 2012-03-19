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

package org.mart.tools.simplechords;

import org.mart.crs.core.pcp.spectral.PCP;

/**
 * @version 1.0 1/13/12 3:47 PM
 * @author: Hut
 */
public class PatternMatcherGaussian extends PatternMatcher {

    public static final float[] MAJ_MEAN = new float[]{5.908757e-01f, 2.390884e-01f, 3.830926e-01f, 2.541426e-01f, 4.995563e-01f, 2.807787e-01f, 2.770872e-01f, 6.285381e-01f, 2.315999e-01f, 2.972363e-01f, 3.030220e-01f, 3.747688e-01f};
    public static final float[] MIN_MEAN = new float[]{5.743682e-01f, 2.245141e-01f, 3.725727e-01f, 4.674375e-01f, 2.714382e-01f, 2.922004e-01f, 2.464965e-01f, 5.905380e-01f, 2.067079e-01f, 2.421832e-01f, 3.972981e-01f, 2.687592e-01f};
    public static final float[] N_MEAN = new float[]{3.528795e-01f, 3.528795e-01f, 3.528795e-01f, 3.528795e-01f, 3.528795e-01f, 3.528795e-01f, 3.528795e-01f, 3.528795e-01f, 3.528795e-01f, 3.528795e-01f, 3.528795e-01f, 3.528795e-01f};




    public static final float[] MAJ_COV = new float[]{1.010811e-01f, 4.599659e-02f, 7.919874e-02f, 5.257366e-02f, 9.332642e-02f, 6.394540e-02f, 5.574838e-02f, 9.529032e-02f, 4.353090e-02f, 7.213372e-02f, 6.852291e-02f, 7.480779e-02f};
    public static final float[] MIN_COV = new float[]{1.037234e-01f, 4.731061e-02f, 8.081701e-02f, 9.997191e-02f, 5.810704e-02f, 8.108612e-02f, 5.212210e-02f, 1.022761e-01f, 4.147693e-02f, 5.581133e-02f, 9.066258e-02f, 5.580973e-02f};
    public static final float[] N_COV = new float[]{1.059016e-01f, 1.059016e-01f, 1.059016e-01f, 1.059016e-01f, 1.059016e-01f, 1.059016e-01f, 1.059016e-01f, 1.059016e-01f, 1.059016e-01f, 1.059016e-01f, 1.059016e-01f, 1.059016e-01f};


    public PatternMatcherGaussian(float[][] bassPCP, float[][] treblePCP) {
        super(bassPCP, treblePCP);
    }

    protected void process(){
        for(int i = 0; i < chordgram.length; i++){
            for(int j = 0; j < CHORDGRAM_DIMENSION / 2; j++){
                chordgram[i][j] = getGaussianDistance(treblePCP[i], PCP.shiftPCP(MAJ_MEAN, j, true), PCP.shiftPCP(MAJ_COV, j, true));
            }
            for(int j = CHORDGRAM_DIMENSION / 2; j < CHORDGRAM_DIMENSION - 1; j++){
                chordgram[i][j] = getGaussianDistance(treblePCP[i], PCP.shiftPCP(MIN_MEAN, j, true), PCP.shiftPCP(MIN_COV, j, true));
            }
            chordgram[i][CHORDGRAM_DIMENSION - 1] = getGaussianDistance(treblePCP[i], N_MEAN, N_COV);
        }
    }
    
    
    
    protected float getGaussianDistance(float[] vector, float[] mean, float[] variance){
        float sum = 0;
        for(int i = 0; i < vector.length; i++){
            float dist = vector[i] - mean[i];
            sum += dist * dist * variance[i];
        }

        return sum;
    }





}
