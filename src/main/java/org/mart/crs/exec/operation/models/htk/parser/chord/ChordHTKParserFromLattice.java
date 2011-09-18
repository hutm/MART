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

package org.mart.crs.exec.operation.models.htk.parser.chord;

import org.mart.crs.management.label.LabelsParser;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.utils.helper.Helper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * @version 1.0 5/14/11 2:43 PM
 * @author: Hut
 */
public class ChordHTKParserFromLattice extends ChordHTKParser {

    protected float multiplicationCoeff;


    public ChordHTKParserFromLattice(String htkOutFilePath, String parsedLabelsDir) {
        super(htkOutFilePath, parsedLabelsDir);
    }


    /**
     * parses results
     *
     * @return map
     */
    public void parseResults() {
        setupMultiplicationCoeff();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(htkOutFilePath));

            String line;
            String currentSongName = "";
            List<ChordSegment> chordSegments = new ArrayList<ChordSegment>();
            while ((line = reader.readLine()) != null && line.length() > 0) {

                String[] tokens = line.split("\\s+");
                String[] tokensFilePath = tokens[0].split("/");
                String songFileName = tokensFilePath[tokensFilePath.length - 2];

                if (!songFileName.equals(currentSongName)) {
                    if (!currentSongName.equals("")) {
                        results.add(new ChordStructure(chordSegments, currentSongName));
                    }
                    currentSongName = songFileName;
                    chordSegments = new ArrayList<ChordSegment>();
                }

                float startTimeSegment = Float.parseFloat(tokens[2]) / multiplicationCoeff;
                float endTimeSegment = startTimeSegment + Float.parseFloat(tokens[3]) / multiplicationCoeff;

                String chordNameUnparsed = tokens[4];
                if (chordNameUnparsed.equals(LabelsParser.START_SENTENCE) || chordNameUnparsed.equals(LabelsParser.END_SENTENCE)) {
                    continue;
                }
                String chordName = parseChordName(chordNameUnparsed);
                ChordSegment chordSegment = new ChordSegment(startTimeSegment, endTimeSegment, chordName);
                chordSegments.add(chordSegment);
            }
            if (!currentSongName.equals("")) {
                results.add(new ChordStructure(chordSegments, currentSongName));
            }
            reader.close();
        } catch (Exception e) {
            logger.error("Unexpected Error occured ", e);
            logger.error(Helper.getStackTrace(e));
        }

        Collections.sort(results);
    }

    protected void setupMultiplicationCoeff(){
        this.multiplicationCoeff = PRECISION_COEFF_LATTICE;
    }


    protected String parseChordName(String chordNameUnparsed) {
        return chordNameUnparsed;
    }

}
