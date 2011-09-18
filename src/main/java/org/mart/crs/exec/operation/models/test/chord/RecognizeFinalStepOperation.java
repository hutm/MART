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

package org.mart.crs.exec.operation.models.test.chord;

import org.mart.crs.config.ExecParams;
import org.mart.crs.exec.operation.models.htk.HTKResultsParser;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.management.features.FeaturesManager;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;

import java.util.List;

import static org.mart.crs.config.Settings.*;
import static org.mart.crs.exec.scenario.stage.StageParameters.*;
import static org.mart.crs.utils.helper.HelperFile.createFileList;
/**
 * @version 1.0 7/10/11 1:44 PM
 * @author: Hut
 */
public class RecognizeFinalStepOperation extends RecognizeOperation {



    public RecognizeFinalStepOperation(StageParameters stageParameters, ExecParams execParams) {
        super(stageParameters, execParams);
    }

    /**
     * Obtain n-best list for each chord segment.
     * At first the song is segmented, then viterbi decoder is applied for each segment
     */
    public void operate(String recognizedFolder) {


        String featuresFolderFilePath = extractedFeaturesDir;
        FeaturesManager.splitAllData(featuresFolderFilePath, recognizedFolder);

        //Now generate N-Best lists for all splited segments
        createFileList(featuresFolderFilePath, featureFileListTest, new ExtensionFileFilter(new String[]{CHROMA_SEC_PASS_EXT}), true);

        createGrammarFile(true);
        hParse();

        String command = String.format("%s/HVite%s -o N -C %s ", binariesDir, EXECUTABLE_EXTENSION, configPath);
        for (int i = 0; i < NUMBER_OF_SEMITONES_IN_OCTAVE; i++) {
            command = command + String.format(" -H %s/%s/%s ", trainedModelsDir, hmmFolder + "_" + gaussianNumber, hmmDefs + i);
        }
        if (isToOutputLattices) {
            command = command + String.format(" -H %s/%s/%s -T 1 -S %s -i %s -z lattice -n %d -q Atvaldmn -w %s -p %5.2f %s %s",
                    trainedModelsDir, hmmFolder + "_" + gaussianNumber, macros, featureFileListTest, decodedOutPath,
                    execParams.NBestCalculationLatticeOrder, netFilePath, penalty, dictFilePath, wordListTestPath);
        } else {
            command = command + String.format(" -H %s/%s/%s -T 1 -S %s -i %s -n 25 25 -w %s -p %5.2f %s %s",
                    trainedModelsDir, hmmFolder + "_" + gaussianNumber, macros, featureFileListTest, decodedOutPath,
                    netFilePath, penalty, dictFilePath, wordListTestPath);
        }
        Helper.execCmd(command);


        //Now parse the hypotheses and compose a lattice
        List<ChordStructure> songList = HTKResultsParser.parseChordHypotheses(decodedOutPath, recognizedFolder);

        String outFolder = recognizedFolder.replace(".00", ".55555");
        HelperFile.createDir(outFolder);
        for (ChordStructure chordStructure : songList) {
            ChordStructure finalChordStructure = chordStructure.getFinalChords();
            finalChordStructure.saveSegmentsInFile(outFolder);
        }

    }

}
