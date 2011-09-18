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

import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.models.htk.parser.chord.ChordHTKParser;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.management.beat.BeatStructure;
import org.mart.crs.management.beat.segment.BeatSegment;
import org.mart.crs.management.label.LabelsParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static org.mart.crs.utils.helper.HelperFile.getFile;

/**
 * @version 1.0 5/14/11 3:31 PM
 * @author: Hut
 */
public class BeatHTKParser extends ChordHTKParser {

    protected List<BeatStructure> beatResults;

    public BeatHTKParser(String htkOutFilePath, String parsedLabelsDir) {
        super(htkOutFilePath, parsedLabelsDir);
        this.beatResults = new ArrayList<BeatStructure>();
    }


    protected void parseSongTranscription(BufferedReader reader, String songFileName) throws IOException {
        //Now read recognized chords
        String line;
        List<BeatSegment> segments = new ArrayList<BeatSegment>();
        int measure = 0;
        int beatNumber = 1;
        while ((line = reader.readLine()) != null && !line.equals(".")) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            float startTime = Float.parseFloat(tokenizer.nextToken()) / ChordHTKParser.FEATURE_SAMPLE_RATE;
            float endTime = Float.parseFloat(tokenizer.nextToken()) / ChordHTKParser.FEATURE_SAMPLE_RATE;
            String beatName = tokenizer.nextToken().trim();
            float logliklihood = Float.parseFloat(tokenizer.nextToken().trim());

            if (beatName.contains(StageParameters.NOTHING_SYMBOL) || beatName.equals(LabelsParser.START_SENTENCE)) {
                continue;
            }

            if (beatName.contains(StageParameters.DOWNBEAT_SYMBOL)) {
                measure = 1;
                beatNumber = 1;
            } else if (beatName.contains(StageParameters.BEAT_SYMBOL)) {
                measure = 0;
            } else {
                continue;
            }
            BeatSegment beatSegment = new BeatSegment(startTime, beatNumber++, measure, 1);
            segments.add(beatSegment);
        }
        beatResults.add(new BeatStructure(segments, songFileName));
    }




    public void storeResults() {
        //First create outDir
        logger.info("Storing data into folder " + parsedLabelsDir);
        (getFile(parsedLabelsDir)).mkdirs();

        for (BeatStructure beatStructure : beatResults) {
            beatStructure.serializeIntoXML(String.format("%s/%s%s", parsedLabelsDir, beatStructure.getSongName(), Settings.BEAT_EXT));
        }
    }

}
