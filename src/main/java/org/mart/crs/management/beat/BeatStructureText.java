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

package org.mart.crs.management.beat;

import org.mart.crs.management.beat.segment.BeatSegment;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;

import java.util.List;

/**
 * @version 1.0 5/12/11 6:06 PM
 * @author: Hut
 */
public class BeatStructureText extends BeatStructure {


    public BeatStructureText(String sourceFilePath) {
        super(sourceFilePath);
    }

    public BeatStructureText(List<BeatSegment> beatSegmentList) {
        super(beatSegmentList);
    }


    public void parseFromSource() {
        List<String> lines = HelperFile.readLinesFromTextFile(sourceFilePath);

        for(String line:lines){
            String[] tokens = line.split("\\s+");
            float startTime = 0;
            int beatNumber = 0;
            try {
                startTime = Float.parseFloat(tokens[0]);
                if (tokens.length > 1) {
                    beatNumber = Integer.parseInt(tokens[1]);
                } else{
                    beatNumber = 2; //There is no downbeat/beat labeling information, only beats
                }
            } catch (NumberFormatException e) {
                logger.error(Helper.getStackTrace(e));
                logger.error(String.format("Could not extract data from file %s line %s", sourceFilePath, line));
            }
            beatSegments.add(new BeatSegment(startTime, beatNumber));
        }
        formMeasureStructure();
    }



}
