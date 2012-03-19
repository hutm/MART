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
 * @version 1.0 1/16/12 7:08 PM
 * @author: Hut
 */
public class PatternMatcherBass extends PatternMatcher {
    public PatternMatcherBass(float[][] bassPCP, float[][] treblePCP) {
        super(bassPCP, treblePCP);
    }


    @Override
    protected void process() {
        for(int i = 0; i < chordgram.length; i++){
            for(int j = 0; j < CHORDGRAM_DIMENSION / 2; j++){
                chordgram[i][j] = HelperArrays.calculateDistance(treblePCP[i], PCP.shiftPCP(MAJ_PATTERN, j, true));
                chordgram[i][j] += HelperArrays.calculateDistance(bassPCP[i], PCP.shiftPCP(MAJ_PATTERN, j, true));
            }
            for(int j = CHORDGRAM_DIMENSION / 2; j < CHORDGRAM_DIMENSION - 1; j++){
                chordgram[i][j] = HelperArrays.calculateDistance(treblePCP[i], PCP.shiftPCP(MIN_PATTERN, j, true));
                chordgram[i][j] += HelperArrays.calculateDistance(bassPCP[i], PCP.shiftPCP(MIN_PATTERN, j, true));
            }
            chordgram[i][CHORDGRAM_DIMENSION - 1] = HelperArrays.calculateDistance(treblePCP[i], NO_CHORD_PATTERN);
        }
    }
}
