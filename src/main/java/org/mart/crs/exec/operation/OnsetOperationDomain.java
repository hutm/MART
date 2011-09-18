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
import org.mart.crs.exec.operation.models.test.onset.RecognizeOnsetConventionalVersionWithLMOperation;
import org.mart.crs.exec.operation.models.test.onset.RecognizeOnsetOperation;
import org.mart.crs.exec.operation.models.training.onset.TrainingOnsetLanguageModelsOperation;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.features.FeaturesManager;
import org.mart.crs.management.features.FeaturesManagerOnset;
import org.mart.crs.utils.helper.Helper;
import org.apache.log4j.Logger;
import org.mart.crs.management.label.LabelsParser;
import org.mart.crs.utils.helper.HelperFile;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @version 1.0 2/22/11 11:52 PM
 * @author: Hut
 */
public class OnsetOperationDomain extends OperationDomain {

    protected static Logger logger = CRSLogger.getLogger(OnsetOperationDomain.class);


    public OnsetOperationDomain() {
        Settings.labelsGroundTruthDir = Settings.onsetLabelsGroundTruthDir;
    }

    public void createWordLists() {
        crsOperation.wordArrayForTrain = StageParameters.beatArrayForTrain;

        StageParameters.beatLMArrayForTrain = new String[2 * 20];
        for (int i = 1; i < 20; i++) {
            StageParameters.beatLMArrayForTrain[i] = StageParameters.BEAT_SYMBOL + i;
            StageParameters.beatLMArrayForTrain[i + 1] = StageParameters.DOWNBEAT_SYMBOL + i;
        }


        try {
            FileWriter fileWriterTrain = new FileWriter(HelperFile.getFile(crsOperation.wordListTrainPath));
            FileWriter fileWriterTest = new FileWriter(HelperFile.getFile(crsOperation.wordListTestPath));
            for (String beat : StageParameters.beatArrayForTrain) {
                fileWriterTrain.write(String.format("%s\n", beat));
                fileWriterTest.write(String.format("%s\n", beat));
            }
            fileWriterTrain.close();
            fileWriterTest.close();

        } catch (IOException e) {
            logger.error("Problems: ");
            logger.error(Helper.getStackTrace(e));
        }

        try {

            FileWriter fileWriterLM = new FileWriter(HelperFile.getFile(crsOperation.wordListLMPath));

            for (String tone : StageParameters.beatLMArrayForTrain) {
                fileWriterLM.write(tone + "\n");
            }
            fileWriterLM.write(LabelsParser.START_SENTENCE + "\n");
            fileWriterLM.write(LabelsParser.END_SENTENCE + "\n");

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
            for (int i = 2; i <= StageParameters.maxNumberOBeatsInMeasure; i++) {
                fileWriter.write(String.format("\"*%s%d.lab\"\n%s\n", StageParameters.MEASURE_SYMBOL, i, StageParameters.DOWNBEAT_SYMBOL));
                for (int curBeat = 2; curBeat <= i; curBeat++) {
                    fileWriter.write(String.format("%s\n", StageParameters.BEAT_SYMBOL));
                }
                fileWriter.write(".\n");
            }
            fileWriter.write(String.format("\"*%s.lab\"\n%s\n.\n", StageParameters.DOWNBEAT_SYMBOL, StageParameters.DOWNBEAT_SYMBOL));
            fileWriter.write(String.format("\"*%s.lab\"\n%s\n.\n", StageParameters.BEAT_SYMBOL, StageParameters.BEAT_SYMBOL));
            fileWriter.close();
        } catch (IOException e) {
            logger.error("Problems: ");
            logger.error(Helper.getStackTrace(e));
        }
    }


    public void createDictionaryFile() {
        try {
            FileWriter writer = new FileWriter(HelperFile.getFile(crsOperation.dictFilePath));
//                for (int i = 2; i <= maxNumberOBeatsInMeasure; i++) {
//                    writer.write(String.format("%s%d %s", MEASURE_SYMBOL, i, DOWNBEAT_SYMBOL));
//                    for (int beatNumber = 0; beatNumber < i - 1; beatNumber++) {
//                        writer.write(String.format(" %s", BEAT_SYMBOL));
//                    }
//                    writer.write("\n");
//                }
            writer.write(String.format("%s %s\n", StageParameters.DOWNBEAT_SYMBOL, StageParameters.DOWNBEAT_SYMBOL));
            writer.write(String.format("%s %s\n", StageParameters.BEAT_SYMBOL, StageParameters.BEAT_SYMBOL));


            writer.close();
        } catch (IOException e) {
            logger.error("Problems: ");
            logger.error(Helper.getStackTrace(e));
        }
    }

    @Override
    public List<AbstractCRSEvaluator> getEvaluators() {
        throw new IllegalArgumentException("Not implemented yet");
    }

    @Override
    public AbstractCRSOperation getRecognizeOperation(StageParameters stageParameters, ExecParams execParams) {
        return new RecognizeOnsetOperation(stageParameters, execParams);
    }

    @Override
    public AbstractCRSOperation getRecognizeLanguageModelOperation(StageParameters stageParameters, ExecParams execParams) {
        return new RecognizeOnsetConventionalVersionWithLMOperation(stageParameters, execParams);
    }

    @Override
    public AbstractCRSOperation getTrainLanguageModelsOperation(StageParameters stageParameters, ExecParams execParams) {
        return new TrainingOnsetLanguageModelsOperation(stageParameters, execParams);
    }

    @Override
    public FeaturesManager getFeaturesManager(String songFilePath, String outDirPath, boolean isForTraining, ExecParams execParams) {
        return new FeaturesManagerOnset(songFilePath, outDirPath, isForTraining, execParams);
    }
}