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

import org.mart.crs.config.Settings;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.management.label.chord.Root;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;
import org.apache.log4j.Logger;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.management.label.chord.ChordType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version 1.0 11-Mar-2010 14:47:58
 * @author: Maksim Khadkevich
 */
public class ConfusionChordManager {
    protected static Logger logger = CRSLogger.getLogger(ConfusionChordManager.class);


    protected Map<ChordType, ConfusionMatrix> confusionMatrixMap;


    public ConfusionChordManager() {
        confusionMatrixMap = new HashMap<ChordType, ConfusionMatrix>();
        for (ChordType chordType : ChordType.chordDictionary) {
            confusionMatrixMap.put(chordType, new ConfusionMatrix(chordType));
        }
    }

    public void extractConfusion(String recognizedFolder, String groundTruthFolder, String outFile) {
        List<File> fileList = HelperFile.findFiles(recognizedFolder, Settings.LABEL_EXT);
        for (File file : fileList) {
            List<ChordSegment> recognizedSegments = (new ChordStructure(file.getPath())).getChordSegments();
            String labelFilePath = HelperFile.getPathForFileWithTheSameName(file.getName(), groundTruthFolder, Settings.LABEL_EXT);
            List<ChordSegment> groundTruthSegments = (new ChordStructure(labelFilePath)).getChordSegments();
            if (groundTruthSegments != null) {
                contributeToConfusion(recognizedSegments, groundTruthSegments);
            }
        }
        writeToAFile(outFile);
    }

    private void writeToAFile(String outFile) {
        StringBuffer stringBuffer = new StringBuffer();
        for (ChordType chordType : ChordType.chordDictionary) {
            confusionMatrixMap.get(chordType).appendConfusionMatrixStringData(stringBuffer);
        }

        stringBuffer.append("\r\n");

        for (ChordType chordType : ChordType.chordDictionary) {
            confusionMatrixMap.get(chordType).appendConfusionRowStringData(stringBuffer);
        }

        try {
            FileWriter writer = new FileWriter(outFile);
            writer.write(stringBuffer.toString());
            writer.close();
        } catch (IOException e) {
            logger.error(Helper.getStackTrace(e));
        }

        //write maj minor confusions for matlab


    }


    public void contributeToConfusion(List<ChordSegment> detectedChords, List<ChordSegment> groundTruthChords) {
        for (ChordSegment chordSegment : detectedChords) {
            for (ChordSegment groundTruthSegment : groundTruthChords) {
                if (chordSegment.intersects(groundTruthSegment) && groundTruthSegment.isValidChord()) {
                    ConfusionMatrix confusionMatrix = confusionMatrixMap.get(groundTruthSegment.getChordType());
                    if (confusionMatrix != null && !groundTruthSegment.getChordType().equals(ChordType.UNKNOWN_CHORD) && !chordSegment.getChordType().equals(ChordType.UNKNOWN_CHORD)) {
                        confusionMatrix.addConfusion(chordSegment, groundTruthSegment);
                    }
                }
            }
        }
    }


}
