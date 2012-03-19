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

package org.mart.crs.exec.operation;

import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.eval.AbstractCRSEvaluator;
import org.mart.crs.exec.operation.eval.chord.ChordEvaluator;
import org.mart.crs.exec.operation.eval.chord.ChordEvaluatorNema;
import org.mart.crs.exec.operation.eval.chord.ChordEvaluatorNemaFullDictionary;
import org.mart.crs.exec.operation.models.test.chord.RecognizeBeatSynchronousOperation;
import org.mart.crs.exec.operation.models.test.chord.RecognizeConventionalVersionWithLMOperation;
import org.mart.crs.exec.operation.models.test.chord.RecognizeOperation;
import org.mart.crs.exec.operation.models.training.chord.TrainingLanguageModelsOperation;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.features.FeaturesManager;
import org.mart.crs.management.features.FeaturesManagerSphinx;
import org.mart.crs.management.label.chord.ChordType;
import org.mart.crs.management.label.chord.Root;
import org.mart.crs.utils.helper.Helper;
import org.apache.log4j.Logger;
import org.mart.crs.management.label.LabelsParser;
import org.mart.crs.utils.helper.HelperFile;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @version 1.0 2/22/11 11:35 PM
 * @author: Hut
 */

public class ChordOperationDomain extends OperationDomain {

    protected static Logger logger = CRSLogger.getLogger(ChordOperationDomain.class);

    public ChordOperationDomain() {
        Settings.labelsGroundTruthDir = Settings.chordLabelsGroundTruthDir;
    }

    public void createWordLists() {
        try {
            crsOperation.wordArrayForTrain = ChordType.getStringValues();
            FileWriter fileWriter = new FileWriter(HelperFile.getFile(crsOperation.wordListTrainPath));
            for (String chord : crsOperation.wordArrayForTrain) {
                fileWriter.write(chord + "\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            logger.error("Problems: ");
            logger.error(Helper.getStackTrace(e));
        }

        try {
            FileWriter fileWriter = new FileWriter(HelperFile.getFile(crsOperation.wordListTestPath));
            FileWriter fileWriterLM = new FileWriter(HelperFile.getFile(crsOperation.wordListLMPath));

            for (ChordType modality : ChordType.chordDictionary) {
                if (!modality.equals(ChordType.NOT_A_CHORD)) {
                    for (Root root : Root.values()) {
                        fileWriter.write(String.format("%s%s\n", root, modality));
                        fileWriterLM.write(String.format("%s%s\n", root, modality));
                    }
                } else {
                    if (Arrays.asList(ChordType.chordDictionary).contains(ChordType.NOT_A_CHORD)) {
                        fileWriter.write(ChordType.NOT_A_CHORD.getName() + "\n");
                        fileWriterLM.write(ChordType.NOT_A_CHORD.getName() + "\n");
                    }
                }
            }
            fileWriterLM.write(LabelsParser.START_SENTENCE + "\n");
            fileWriterLM.write(LabelsParser.END_SENTENCE + "\n");


            fileWriter.close();
            fileWriterLM.close();
        } catch (IOException e) {
            logger.error("Problems: ");
            logger.error(Helper.getStackTrace(e));
        }
    }


    public void createMLF() {
        try {
            FileWriter fileWriter = new FileWriter(HelperFile.getFile(crsOperation.mlfFilePath));
            fileWriter.write("#!MLF!#\n");
            for (ChordType modality : ChordType.chordDictionary) {
                fileWriter.write(String.format("\"*%s.lab\"\n%s\n.\n", modality.getName(), modality.getName()));
            }
            fileWriter.close();
        } catch (IOException e) {
            logger.error("Problems: ");
            logger.error(Helper.getStackTrace(e));
        }
    }


    public void createDictionaryFile() {
        try {
            FileWriter writer = new FileWriter(HelperFile.getFile(crsOperation.dictFilePath));
            for (ChordType modality : ChordType.chordDictionary) {
                if (!modality.equals(ChordType.NOT_A_CHORD)) {
                    for (Root root : Root.values()) {
                        writer.write(String.format("%s%s %s%s\n", root, modality, root, modality));
                    }
                }
                if (Arrays.asList(ChordType.chordDictionary).contains(ChordType.NOT_A_CHORD)) {
                    writer.write(String.format("%s %s\n", ChordType.NOT_A_CHORD, ChordType.NOT_A_CHORD));
                }
            }
            writer.close();
        } catch (IOException e) {
            logger.error("Problems: ");
            logger.error(Helper.getStackTrace(e));
        }
    }

    @Override
    public List<AbstractCRSEvaluator> getEvaluators() {
        List<AbstractCRSEvaluator> outList = new ArrayList<AbstractCRSEvaluator>();
        outList.add(new ChordEvaluator());
        outList.add(new ChordEvaluatorNema());                   //TODO for more detailed evaluation metrics
        outList.add(new ChordEvaluatorNemaFullDictionary());
        return outList;
    }

    @Override
    public AbstractCRSOperation getRecognizeOperation(StageParameters stageParameters, ExecParams execParams) {
        return new RecognizeOperation(stageParameters, execParams);
    }

    @Override
    public AbstractCRSOperation getRecognizeLanguageModelOperation(StageParameters stageParameters, ExecParams execParams) {
        if (!Settings.isBeatSynchronousDecoding) {
            return new RecognizeConventionalVersionWithLMOperation(stageParameters, execParams);
        } else {
            return new RecognizeBeatSynchronousOperation(stageParameters, execParams);
        }
    }

    @Override
    public AbstractCRSOperation getTrainLanguageModelsOperation(StageParameters stageParameters, ExecParams execParams) {
        return new TrainingLanguageModelsOperation(stageParameters, execParams);
    }

    @Override
    public FeaturesManager getFeaturesManager(String songFilePath, String outDirPath, boolean isForTraining, ExecParams execParams) {
        if (Settings.isSphinx) {
            return new FeaturesManagerSphinx(songFilePath, outDirPath, isForTraining, execParams);
        }
        return new FeaturesManager(songFilePath, outDirPath, isForTraining, execParams);
    }
}


