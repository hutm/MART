/*
 * Copyright (c) 2008-2013 Maksim Khadkevich and Fondazione Bruno Kessler.
 *
 * This file is part of MART.
 * MART is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2, as published
 * by the Free Software Foundation.
 *
 * MART is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
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
 * BeatStructure to be read in format compatible with sonic visualizer (csv)
 * @version 1.0 2/12/13 4:58 PM
 * @author: Hut
 */
public class BeatStructureCSV extends BeatStructure {

    public BeatStructureCSV(String sourceFilePath) {
        super(sourceFilePath);
    }

    public BeatStructureCSV(List<BeatSegment> beatSegmentList) {
        super(beatSegmentList);
    }


    public void parseFromSource() {
        List<String> lines = HelperFile.readLinesFromTextFile(sourceFilePath);

        for(String line:lines){
            String[] tokens = line.replaceAll("\"", "").split(",");
            if(tokens.length < 2){
                continue;
            }
            float startTime = 0;
            int beatNumber = 0;
            try {
                startTime = Float.parseFloat(tokens[0]);
                beatNumber = Integer.parseInt(tokens[1]);
            } catch (NumberFormatException e) {
                logger.error(Helper.getStackTrace(e));
                logger.error(String.format("Could not extract data from file %s line %s", sourceFilePath, line));
            }
            beatSegments.add(new BeatSegment(startTime, beatNumber));
        }
        formMeasureStructure();
    }


}
