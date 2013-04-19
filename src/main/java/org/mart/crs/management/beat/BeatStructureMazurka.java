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
 * @version 1.0 7/1/11 4:19 PM
 * @author: Hut
 */
public class BeatStructureMazurka extends BeatStructureText {


    public BeatStructureMazurka(String txtFilePath) {
        super(txtFilePath);
    }

    public BeatStructureMazurka(List<BeatSegment> beatSegmentList) {
        super(beatSegmentList);
    }


    @Override
    public void parseFromSource() {
        List<String> lines = HelperFile.readLinesFromTextFile(sourceFilePath);

        float startTime;
        int beatNumber;

        float offset = 0f;

        for (String line : lines) {
            if (line.startsWith("!!!offset:")) {
                String[] tokens = line.split("\\s+");
                offset = Float.valueOf(tokens[1]) / 1000f;
            }
            if (line.startsWith("4")) {
                String[] tokens = line.split("\\s+");
                if (tokens.length < 4) {
                    throw new IllegalArgumentException(String.format("Could not parse line %s in beatStructure from file %s", line, sourceFilePath));
                } else {
                    startTime = 0;
                    beatNumber = 0;
                    try {
                        startTime = Float.parseFloat(tokens[2]) / 1000f + offset;
                        beatNumber = Integer.parseInt(tokens[1]);
                    } catch (NumberFormatException e) {
                        logger.error(Helper.getStackTrace(e));
                        logger.error(String.format("Could not extract data from file %s line %s", sourceFilePath, line));
                    }
                    beatSegments.add(new BeatSegment(startTime, beatNumber));
                }

            }
        }
        formMeasureStructure();
    }


}
