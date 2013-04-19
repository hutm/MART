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

package org.mart.crs.management.onset;

import org.mart.crs.management.beat.BeatStructure;
import org.mart.crs.management.beat.segment.BeatSegment;
import org.mart.crs.utils.helper.HelperFile;

import java.util.List;

/**
 * @version 1.0 3/11/11 3:41 PM
 * @author: Hut
 */
public class OnsetStructure extends BeatStructure {


    public OnsetStructure(String sourceFilePath) {
        super(sourceFilePath);
    }

    public OnsetStructure(List<BeatSegment> beatSegmentList) {
        super(beatSegmentList);
    }

    @Override
    public void parseFromSource() {
        float[] onsetTimeInstants = HelperFile.readFloatPerStringFromTextFile(sourceFilePath);
        for(float onsetTime:onsetTimeInstants){
            beatSegments.add(new BeatSegment(onsetTime, 1));
        }
    }



    public void serializeIntoTXT(String outFilePath) {
        double[] onsetData = new double[beatSegments.size()];
        int counter = 0;
        for(BeatSegment beatSegment:beatSegments){
            onsetData[counter++] = beatSegment.getTimeInstant();
        }
        HelperFile.saveDoubleDataInTextFile(onsetData, outFilePath);
    }
}
