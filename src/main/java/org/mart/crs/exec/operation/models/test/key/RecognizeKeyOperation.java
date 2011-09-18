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

package org.mart.crs.exec.operation.models.test.key;

import org.mart.crs.config.ConfigSettings;
import org.mart.crs.config.ExecParams;
import org.mart.crs.exec.operation.models.test.chord.RecognizeOperation;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.management.label.chord.ChordType;
import org.mart.crs.utils.helper.HelperFile;
import org.mart.crs.management.label.chord.ChordSegment;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.mart.crs.config.Settings.isMIREX;

/**
 * @version 1.0 11-Jun-2010 16:54:14
 * @author: Hut
 */
public class RecognizeKeyOperation extends RecognizeOperation {


    public RecognizeKeyOperation(StageParameters stageParameters, ExecParams execParams) {
        super(stageParameters, execParams);
    }


    @Override
    public void operate() {
        super.operate();
        if (isMIREX ) {
            String recognizedFolder = resultsDir + File.separator + "-";
            String inFilePath = HelperFile.getFile(recognizedFolder).listFiles()[0].getPath();
            ChordStructure structure = new ChordStructure(inFilePath);
            ChordSegment segment = structure.getChordSegments().get(0);
            String root = segment.getRoot().getName();
            ChordType type = segment.getChordType();
            String modality;
            if(type.equals(ChordType.MAJOR_CHORD)){
                modality = "major";
            } else{
                modality = "minor";
            }
            List<String> transcriptionDataToSave = new ArrayList<String>();
            transcriptionDataToSave.add(String.format("%s\t%s", root, modality));
            HelperFile.saveCollectionInFile(transcriptionDataToSave, ConfigSettings.outPathExternal, false);
            HelperFile.getFile(inFilePath).delete();
        }
    }


    @Override
    protected String defineRecognitionOutputRule() {
        return "($chords)";
    }

}
