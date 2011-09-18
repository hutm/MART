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

package org.mart.crs.exec.operation.models.htk.parser.beat;

import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.management.beat.BeatStructure;
import org.mart.crs.management.beat.segment.BeatSegment;
import org.mart.crs.management.label.LabelsParser;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.exec.operation.models.htk.parser.chord.ChordHTKParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @version 1.0 5/14/11 3:32 PM
 * @author: Hut
 */
public class BeatHTKParserLattice extends BeatHTKParser {

    //TODO add delay information (look in HTKBeatResultsParser)

    public BeatHTKParserLattice(String htkOutFilePath, String parsedLabelsDir) {
        super(htkOutFilePath, parsedLabelsDir);
    }


    /**
     * parses results
     *
     * @return map
     */
    public void parseResults() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(htkOutFilePath));

            String line;
            String currentSongName = "";
            List<BeatSegment> beatSegments = new ArrayList<BeatSegment>();
            int measure = 0;
            int beatNumber = 1;
            while ((line = reader.readLine()) != null && line.length() > 0) {

                String[] tokens = line.split("\\s+");
                String[] tokensFilePath = tokens[0].split("/");
                String songFileName = tokensFilePath[tokensFilePath.length - 2];

                if (!songFileName.equals(currentSongName)) {
                    if (!currentSongName.equals("")) {
                        beatResults.add(new BeatStructure(beatSegments, songFileName));
                    }
                    currentSongName = songFileName;
                    beatSegments = new ArrayList<BeatSegment>();
                }

                float startTimeSegment = Float.parseFloat(tokens[2]);
                float endTimeSegment = startTimeSegment + Float.parseFloat(tokens[3]);

                String chordNameUnparsed = tokens[4];
                if (chordNameUnparsed.contains(StageParameters.NOTHING_SYMBOL) || chordNameUnparsed.equals(LabelsParser.START_SENTENCE)) {
                    continue;
                }

                if (chordNameUnparsed.contains(StageParameters.DOWNBEAT_SYMBOL)) {
                    measure = 1;
                    beatNumber = 1;
                } else if (chordNameUnparsed.contains(StageParameters.BEAT_SYMBOL)) {
                    measure = 0;
                } else {
                    continue;
                }
                BeatSegment beatSegment = new BeatSegment(startTimeSegment, beatNumber++, measure, 1);
                beatSegments.add(beatSegment);
            }
            if (!currentSongName.equals("")) {
                beatResults.add(new BeatStructure(beatSegments, currentSongName));
            }
            reader.close();
        } catch (Exception e) {
            ChordHTKParser.logger.error("Unexpected Error occured ", e);
            ChordHTKParser.logger.error(Helper.getStackTrace(e));
        }

        Collections.sort(beatResults);
    }


}
