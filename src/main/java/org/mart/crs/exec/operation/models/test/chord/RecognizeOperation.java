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
import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.AbstractCRSOperation;
import org.mart.crs.exec.operation.ChordFullTrainingOperationDomain;
import org.mart.crs.exec.operation.OperationType;
import org.mart.crs.exec.operation.models.htk.HTKResultsParser;
import org.mart.crs.exec.operation.models.htk.parser.chord.ChordHTKParser;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.exec.scenario.stage.TestFeaturesStage;
import org.mart.crs.exec.scenario.stage.TestRecognizeStage;
import org.mart.crs.exec.scenario.stage.TrainModelsStage;
import org.mart.crs.management.label.chord.ChordType;
import org.mart.crs.management.label.chord.Root;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import static org.mart.crs.config.Settings.*;
import static org.mart.crs.exec.scenario.stage.StageParameters.*;
import static org.mart.crs.management.label.chord.ChordType.NOT_A_CHORD;
import static org.mart.crs.management.label.chord.ChordType.chordDictionary;
import static org.mart.crs.utils.helper.HelperFile.createFileList;
import static org.mart.crs.utils.helper.HelperFile.getFile;
/**
 * @version 1.0 11-Jun-2010 16:49:09
 * @author: Hut
 */
public class RecognizeOperation extends AbstractCRSOperation {

    protected String resultsDir;
    protected String trainedModelsDir;
    protected String extractedFeaturesDir;
    protected String lmDir;
    protected String netHViteFilePath;

    protected String gramFilePath;
    protected String decodedOutPath;
    protected String featureFileListTest;
    protected String netFilePath;


    protected int gaussianNumber;
    protected float penalty;

    protected boolean isToOutputLattices;

    public RecognizeOperation(StageParameters stageParameters, ExecParams execParams) {
        super(stageParameters, execParams);
    }


    @Override
    public void initialize() {
        super.initialize();

        TestRecognizeStage testRecognizeStage = (TestRecognizeStage) stageParameters.getStage(TestRecognizeStage.class);
        TrainModelsStage trainModelsStage = (TrainModelsStage) stageParameters.getStage(TrainModelsStage.class);
        TestFeaturesStage testFeaturesStage = ((TestFeaturesStage) stageParameters.getStage(TestFeaturesStage.class));

        this.resultsDir = testRecognizeStage.getResultsDirPath();
        this.trainedModelsDir = trainModelsStage.getHMMsDirPath();
        this.lmDir = trainModelsStage.getLanguageModelsDirPath();

        String featuresDirLocalCopy = String.format("%s/%s", this.tempDirPath, FEATURES_DIR_NAME);
        HelperFile.copyDirectory(testFeaturesStage.getFeaturesDirPath(), featuresDirLocalCopy);
        this.extractedFeaturesDir = featuresDirLocalCopy;

        netHViteFilePath = trainModelsStage.getNetHViteFilePath();

        this.gaussianNumber = execParams.gaussianNumber;
        this.penalty = execParams.penalty;


        gramFilePath = String.format("%s/%s", tempDirPath, GRAM_FILE);
        decodedOutPath = String.format("%s/%s", tempDirPath, DECODED_OUT);
        featureFileListTest = String.format("%s/%s", tempDirPath, FEATURE_FILELIST_TEST);
        netFilePath = String.format("%s/%s", tempDirPath, NET_FILE);

        isToOutputLattices = false;
        createFileList(extractedFeaturesDir, featureFileListTest, new ExtensionFileFilter(CHROMA_EXT), true);
        operationDomain.createDictionaryFile();
        createGrammarFile();
        hParse();
    }


    public void operate() {
        String outFilePath = decodedOutPath + String.format("%d_%2.1f", gaussianNumber, penalty);
        hvite(trainedModelsDir, hmmFolder + "_" + gaussianNumber, penalty, outFilePath);
        String recognizedFolder = resultsDir + File.separator + "-";
        ChordHTKParser parser = new ChordHTKParser(outFilePath, recognizedFolder);
        parser.run();
        HTKResultsParser.parse(outFilePath, recognizedFolder);
//        EvaluatorOld evaluator = new EvaluatorOld();
//        evaluator.evaluate(recognizedFolder, Settings.labelsGroundTruthDir, recognizedFolder + ".txt");
    }

    protected void createGrammarFile(boolean isOnlyOneChord) {
        try {
            StringBuffer buffer = new StringBuffer();
            FileWriter writer = new FileWriter(getFile(gramFilePath));
            buffer.append("$chords = ");
            for (ChordType modality : chordDictionary) {
                if (!modality.equals(ChordType.NOT_A_CHORD)) {
                    for (Root root : Root.values()) {
                        buffer.append(String.format("%s%s | ", root, modality));
                    }
                }
            }
            if (Arrays.asList(chordDictionary).contains(NOT_A_CHORD)) {
                buffer.append(ChordType.NOT_A_CHORD.getOriginalName());
            } else {
                buffer.deleteCharAt(buffer.length() - 2);
            }
            buffer.append(";\n");
            writer.write(buffer.toString());
            if (!isOnlyOneChord) {
                writer.write(defineRecognitionOutputRule());
            } else {
                writer.write("($chords)");
            }
            writer.close();
        } catch (IOException e) {
            logger.error(Helper.getStackTrace(e));
        }
    }

    protected void createGrammarFile() {
        createGrammarFile(false);
    }


    protected String defineRecognitionOutputRule() {
        return "({$chords})";
    }


    protected void hParse() {
        String command = String.format("%s/HParse%s %s %s", binariesDir, EXECUTABLE_EXTENSION, gramFilePath, netFilePath);
        Helper.execCmd(command);
    }

    protected void hvite(String trainedModelsDir, String hmmFolder, float penalty, String decodedOutPath) {
        String command = String.format("%s/HVite%s -o N -t 250.0 -C %s ", binariesDir, EXECUTABLE_EXTENSION, configPath);

        //In case of full train, there are no rotated hmm def files
        int numberOfHmms;
        if (Settings.operationType.equals(OperationType.CHORD_OPERATION_FULL_TRAIN)) {
            numberOfHmms = ChordFullTrainingOperationDomain.getNumberOfTrainedHmms();
//            command = command + String.format(" -H %s/%s/%s ", trainedModelsDir, hmmFolder, hmmDefs);
        } else {
            numberOfHmms = NUMBER_OF_SEMITONES_IN_OCTAVE;
        }
        for (int i = 0; i < numberOfHmms; i++) {
            command = command + String.format(" -H %s/%s/%s ", trainedModelsDir, hmmFolder, hmmDefs + i);
        }
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
