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

import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.utils.helper.Helper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @version 1.0 5/18/11 4:18 PM
 * @author: Hut
 */
public class ChordHTKParserHypotheses extends ChordHTKParser {
    public ChordHTKParserHypotheses(String htkOutFilePath, String parsedLabelsDir) {
        super(htkOutFilePath, parsedLabelsDir);
    }

    public void parseResults() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(htkOutFilePath));

            String line = reader.readLine();
            if (!line.equals("#!MLF!#")) {
                logger.error("File " + htkOutFilePath + " does not seem to be label file");
                throw new IOException();
            }
            ChordStructure chordStructure = null;
            while ((line = reader.readLine()) != null && line.length() > 0) {

                String[] tokensFilePath = line.split("/");
                String songFileName = tokensFilePath[tokensFilePath.length - 2];

                String[] tokensTimeInstants = tokensFilePath[tokensFilePath.length - 1].split("_"); //TODO use for beatSync parsing
                float startTimeSegment = Float.parseFloat(tokensTimeInstants[0]);
                float endTimeSegment = Float.parseFloat(tokensTimeInstants[1]);

                //Check if the song is already in map
                List<ChordSegment> chordList = new ArrayList<ChordSegment>();
                if (!results.contains(new ChordStructure(null, songFileName))) {
                    chordStructure = (new ChordStructure(chordList, songFileName));
                    results.add(chordStructure);
                } else {
                    for (ChordStructure cs : results) {
                        if (cs.equals(chordStructure)) {
                            chordStructure = cs;
                            break;
                        }
                    }
                }

                //Now read recognized chords
                ChordSegment chordSegment = null;
                while ((line = reader.readLine()) != null && !line.equals(".")) {
                    StringTokenizer tokenizer = new StringTokenizer(line);
                    if (line.startsWith("///")) {
                        continue;
                    }

                    tokenizer.nextToken();
                    tokenizer.nextToken();

                    String chordName = tokenizer.nextToken().trim();
                    float logliklihood = Float.parseFloat(tokenizer.nextToken().trim());

                    if (chordSegment == null) {
                        chordSegment = new ChordSegment(startTimeSegment, endTimeSegment, chordName, logliklihood);
                        chordSegment.addHypothesis(new ChordSegment(startTimeSegment, endTimeSegment, chordName, logliklihood));
                    } else {
                        chordSegment.addHypothesis(new ChordSegment(startTimeSegment, endTimeSegment, chordName, logliklihood));
                    }
                }
                chordStructure.getChordSegments().add(chordSegment);
            }
            reader.close();
        } catch (Exception e) {
            logger.error("Unexpexted Error occured ");
            logger.error(Helper.getStackTrace(e));
        }

        Collections.sort(results);
        for (ChordStructure aResult : results) {
            Collections.sort(aResult.getChordSegments());
        }
    }


}
