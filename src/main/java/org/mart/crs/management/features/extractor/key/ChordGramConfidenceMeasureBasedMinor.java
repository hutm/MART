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

package org.mart.crs.management.features.extractor.key;

import org.mart.crs.management.label.chord.ChordSegment;

/**
 * @version 1.0 5/9/11 7:42 PM
 * @author: Hut
 */
public class ChordGramConfidenceMeasureBasedMinor extends ChordGramConfidenceMeasureBasedMajor {



    protected float[] extractModalityData(float[] fullVector){
        float[] output = new float[ChordSegment.SEMITONE_NUMBER];
        System.arraycopy(fullVector, ChordSegment.SEMITONE_NUMBER , output, 0, output.length);
        return output;
    }


}
