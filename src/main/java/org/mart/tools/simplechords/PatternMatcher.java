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
import org.mart.crs.utils.helper.HelperArrays;

/**
 * @version 1.0 1/11/12 4:02 PM
 * @author: Hut
 */
public class PatternMatcher {

    protected float[][] bassPCP;
    protected float[][] treblePCP;

    protected float[][] chordgram;
    public static final int CHORDGRAM_DIMENSION = 24 + 1;

    public static final float[] MAJ_PATTERN = new float[]{1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0};
    public static final float[] MIN_PATTERN = new float[]{1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0};

    protected static final float n = 0.01f;
    public static final float[] NO_CHORD_PATTERN = new float[]{n, n, n, n, n, n, n, n, n, n, n, n};

    public PatternMatcher(float[][] bassPCP, float[][] treblePCP) {
        this.bassPCP = bassPCP;
        this.treblePCP = treblePCP;
        chordgram = new float[treblePCP.length][CHORDGRAM_DIMENSION];
    }

    protected void process(){
        for(int i = 0; i < chordgram.length; i++){
            for(int j = 0; j < CHORDGRAM_DIMENSION / 2; j++){
                chordgram[i][j] = HelperArrays.calculateDistance(treblePCP[i], PCP.shiftPCP(MAJ_PATTERN, j, true));
            }
            for(int j = CHORDGRAM_DIMENSION / 2; j < CHORDGRAM_DIMENSION - 1; j++){
                chordgram[i][j] = HelperArrays.calculateDistance(treblePCP[i], PCP.shiftPCP(MIN_PATTERN, j, true));
            }
            chordgram[i][CHORDGRAM_DIMENSION - 1] = HelperArrays.calculateDistance(treblePCP[i], NO_CHORD_PATTERN);
        }
    }

    public float[][] getChordgram() {
        return chordgram;
    }
}
