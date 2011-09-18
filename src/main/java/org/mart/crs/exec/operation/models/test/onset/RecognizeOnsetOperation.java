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

package org.mart.crs.exec.operation.models.test.onset;

import org.mart.crs.config.ExecParams;
import org.mart.crs.exec.operation.models.htk.HTKResultsParserBeat;
import org.mart.crs.exec.operation.models.test.chord.RecognizeOperation;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.utils.helper.Helper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.mart.crs.config.Settings.EXECUTABLE_EXTENSION;
import static org.mart.crs.exec.scenario.stage.StageParameters.*;
import static org.mart.crs.utils.helper.HelperFile.getFile;
/**
 * @version 1.0 Dec 3, 2010 1:01:23 PM
 * @author: Hut
 */
public class RecognizeOnsetOperation extends RecognizeOperation {


    public RecognizeOnsetOperation(StageParameters stageParameters, ExecParams execParams) {
        super(stageParameters, execParams);
    }

    public void operate() {
        String outFilePath = decodedOutPath + String.format("%d_%2.1f", gaussianNumber, penalty);
        hvite(trainedModelsDir, hmmFolder + "_" + gaussianNumber, penalty, outFilePath);
        String recognizedFolder = resultsDir + File.separator + "-";
        HTKResultsParserBeat.parse(outFilePath, recognizedFolder, ExecParams._initialExecParameters);
//        EvaluatorOld evaluator = new EvaluatorOld();
//        evaluator.evaluate(recognizedFolder, Settings.labelsGroundTruthDir, recognizedFolder + ".txt");
    }


    protected void createGrammarFile() {
        try {
            FileWriter writer = new FileWriter(getFile(gramFilePath));
            writer.write("$measures = ");
//            for (int i = 4; i < maxNumberOBeatsInMeasure; i++) {
//                writer.write(String.format("%s%d | ", MEASURE_SYMBOL, i));
//            }
//            writer.write(String.format("%s%d ;\n", MEASURE_SYMBOL, maxNumberOBeatsInMeasure));
            writer.write(String.format("%s %s %s %s;\n", DOWNBEAT_SYMBOL, BEAT_SYMBOL, BEAT_SYMBOL, BEAT_SYMBOL));
            writer.write(defineRecognitionOutputRule());
            writer.close();
        } catch (IOException e) {
            logger.error(Helper.getStackTrace(e));
        }
    }


    protected String defineRecognitionOutputRule() {
        return "({beat} {$measures} {beat})";
    }


    protected void hParse() {
        String command = String.format("%s/HParse%s %s %s", binariesDir, EXECUTABLE_EXTENSION, gramFilePath, netFilePath);
        Helper.execCmd(command);
    }

    protected void hvite(String trainedModelsDir, String hmmFolder, float penalty, String decodedOutPath) {
        String command = String.format("%s/HVite%s -t 250.0 -C %s ", binariesDir, EXECUTABLE_EXTENSION, configPath);
        command = command + String.format(" -H %s/%s/%s ", trainedModelsDir, hmmFolder, hmmDefs);
//        command = command + " -f ";
        if (isToOutputLattices) {
            command = command + String.format(" -H %s/%s/%s -T 1 -S %s -i %s -z lattice -n %d -q Atvaldmn -w %s -p %5.2f %s %s",
                    trainedModelsDir, hmmFolder, macros, featureFileListTest, decodedOutPath,
                    execParams.NBestCalculationLatticeOrder, netFilePath, penalty, dictFilePath, wordListTestPath);   //TODO if FLM wordListLMPath - not sure
        } else {
            command = command + String.format(" -H %s/%s/%s -T 1 -S %s -i %s -w %s -p %5.2f %s %s",
                    trainedModelsDir, hmmFolder, macros, featureFileListTest, decodedOutPath,
                    netFilePath, penalty, dictFilePath, wordListTestPath);
        }
        Helper.execCmd(command);
    }


}
