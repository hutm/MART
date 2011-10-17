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

package org.mart.crs.exec.operation.models.test.beat;

import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.AbstractCRSOperation;
import org.mart.crs.exec.operation.models.htk.HTKResultsParserBeat;
import org.mart.crs.exec.operation.models.test.onset.RecognizeOnsetOperation;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.mart.crs.config.Settings.EXECUTABLE_EXTENSION;
import static org.mart.crs.exec.scenario.stage.StageParameters.*;
import static org.mart.crs.config.Settings.*;

/**
 * @version 1.0 12/13/10 4:29 PM
 * @author: Hut
 */
public class RecognizeBeatOperation extends RecognizeOnsetOperation {

    public RecognizeBeatOperation(StageParameters stageParameters, ExecParams execParams) {
        super(stageParameters, execParams);
    }

    public void operate() {
        String outFilePath = decodedOutPath + String.format("%d_%2.1f", gaussianNumber, penalty);
        hvite(trainedModelsDir, hmmFolder + "_" + gaussianNumber, penalty, outFilePath);
        String recognizedFolder = resultsDir + File.separator + "-";
        HTKResultsParserBeat.parse(outFilePath, recognizedFolder, ExecParams._initialExecParameters);
    }


    protected void createGrammarFile() {
        try {
            StringBuffer buffer = new StringBuffer();
            FileWriter writer = new FileWriter(HelperFile.getFile(gramFilePath));
            buffer.append("$beats = ");

            for (int beatLength = minNimberOfFramesForBeatSegment; beatLength < maxNimberOfFramesForBeatSegment; beatLength++) {


                buffer.append(String.format("%s%d | ", DOWNBEAT_SYMBOL, beatLength));
                buffer.append(String.format("%s%d | ", BEAT_SYMBOL, beatLength));

            }
            buffer.append(NOTHING_SYMBOL);
            writer.write(buffer.toString());
            writer.write(";\n");
            writer.write("({$beats})");
            writer.close();
        } catch (IOException e) {
            AbstractCRSOperation.logger.error(Helper.getStackTrace(e));
        }
    }


    protected String defineRecognitionOutputRule() {
        return "(beat {$measures} beat)";
    }


    protected void hParse() {
        String command = String.format("%s/HParse%s %s %s", binariesDir, EXECUTABLE_EXTENSION, gramFilePath, netFilePath);
        Helper.execCmd(command);
    }

    protected void hvite(String trainedModelsDir, String hmmFolder, float penalty, String decodedOutPath) {
        String command = String.format("%s/HVite%s -t 250.0 -C %s ", binariesDir, EXECUTABLE_EXTENSION, configPath);
        command = command + String.format(" -H %s/%s/%s ", trainedModelsDir, hmmFolder, hmmDefs);
//        command = command + " -f "; //todo
        if (Settings.isToUseBigramDuringHVite) {
            command = command + " -w " + netHViteFilePath;
        } else {
            command = command + " -w " + netFilePath;
        }
        if (isToOutputLattices) {
            command = command + String.format(" -r %5.2f -s %5.2f -H %s/%s/%s -T 1 -S %s -i %s -z lattice -n %d -q Atvaldmn -p %5.2f %s %s",
                    execParams.acWeight, execParams.lmWeight, trainedModelsDir, hmmFolder, macros, featureFileListTest, decodedOutPath,
                    execParams.NBestCalculationLatticeOrder, penalty, dictFilePath, wordListTestPath);
        } else {
            command = command + String.format(" -r %5.2f -s %5.2f -H %s/%s/%s -T 1 -S %s -i %s -p %5.2f %s %s",
                    execParams.acWeight, execParams.lmWeight, trainedModelsDir, hmmFolder, macros, featureFileListTest, decodedOutPath,
                    penalty, dictFilePath, wordListTestPath);
        }
        Helper.execCmd(command);
    }


}
