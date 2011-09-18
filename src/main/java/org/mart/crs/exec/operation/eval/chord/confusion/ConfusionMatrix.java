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

package org.mart.crs.exec.operation.eval.chord.confusion;

import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.management.label.chord.ChordType;
import org.mart.crs.management.label.chord.Root;
import org.mart.crs.utils.helper.HelperArrays;

/**
 * @version 1.0 5/2/11 5:27 PM
 * @author: Hut
 */
public class ConfusionMatrix {

    protected String separator = ",";

    protected ChordType chordType;

    protected double[][] confisionMatrix;


    public ConfusionMatrix(ChordType chordType) {
        this.chordType = chordType;
        this.confisionMatrix = new double[ChordType.chordDictionary.length][Root.values().length]; //The last column will be Not_A_Chord
    }


    public void addConfusion(ChordSegment recognizedChord, ChordSegment gtchord) {
        double intersection = recognizedChord.getIntersection(gtchord);
        int rootIndex;
        int typeIndex;

        if (recognizedChord.getChordType().equals(ChordType.NOT_A_CHORD)) {
            rootIndex = 0;
        } else {
            if (gtchord.getChordType().equals(ChordType.NOT_A_CHORD)) {
                rootIndex = recognizedChord.getRoot().ordinal();
            }else{
                rootIndex = HelperArrays.transformIntValueToBaseRange(recognizedChord.getRoot().ordinal() - gtchord.getRoot().ordinal(), ChordSegment.SEMITONE_NUMBER);
            }
        }
        typeIndex = getChordTypeIndex(recognizedChord.getChordType());
        if (typeIndex >= 0) {
            confisionMatrix[typeIndex][rootIndex] += intersection;
        }
    }


    public int getChordTypeIndex(ChordType chordType) {
        for (int i = 0; i < ChordType.chordDictionary.length; i++) {
            if (chordType.equals(ChordType.chordDictionary[i])) {
                return i;
            }
        }
        return -1;
    }


    public double[][] getConfisionMatrix() {
        return confisionMatrix;
    }


    public double[] getConfusionRow() {
        double[] out = new double[confisionMatrix.length];
        for (int i = 0; i < confisionMatrix.length; i++) {
            for (int j = 0; j < confisionMatrix[0].length; j++) {
                out[i] += confisionMatrix[i][j];
            }
        }
        return out;
    }


    public void appendConfusionMatrixStringData(StringBuffer out) {
        out.append(separator);
        for (int i = 0; i < ChordType.chordDictionary.length; i++) {
            out.append(ChordType.chordDictionary[i]).append(separator);
        }
        out.append("\r\n");
        for (int i = 0; i < confisionMatrix[0].length; i++) {
            out.append(this.chordType.getName()).append(":").append(Root.values()[i]).append(separator);
            for (int j = 0; j < confisionMatrix.length; j++) {
                out.append(String.format("%5.3f", confisionMatrix[j][i])).append(separator);
            }
            out.append("\r\n");
        }

    }


    public void appendConfusionRowStringData(StringBuffer out) {
        out.append(this.chordType.getName()).append(separator);
        double[] confisionRow = getConfusionRow();
        for (int i = 0; i < confisionRow.length; i++) {
            out.append(String.format("%5.3f", confisionRow[i])).append(separator);
        }
        out.append("\r\n");
    }

}


