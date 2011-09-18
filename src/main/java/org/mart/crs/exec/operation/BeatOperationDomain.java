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

import org.apache.log4j.Logger;
import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.eval.AbstractCRSEvaluator;
import org.mart.crs.exec.operation.eval.beat.BeatEvaluator;
import org.mart.crs.exec.operation.models.test.beat.RecognizeBeatConventionalVersionWithLMOperation;
import org.mart.crs.exec.operation.models.test.beat.RecognizeBeatOperation;
import org.mart.crs.exec.operation.models.training.beat.TrainingBeatLanguageModelsOperation;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.features.FeaturesManager;
import org.mart.crs.management.features.FeaturesManagerBeat;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mart.crs.exec.scenario.stage.StageParameters.*;
import static org.mart.crs.management.label.LabelsParser.END_SENTENCE;
import static org.mart.crs.management.label.LabelsParser.START_SENTENCE;
/**
 * @version 1.0 2/22/11 11:53 PM
 * @author: Hut
 */
public class BeatOperationDomain extends OperationDomain {

    protected static Logger logger = CRSLogger.getLogger(BeatOperationDomain.class);

    public BeatOperationDomain() {
        Settings.labelsGroundTruthDir = Settings.beatLabelsGroundTruthDir;
    }

    public void createWordLists() {
        //TODO init language models
        int totalNumberOfWordsForBeatType = maxNimberOfFramesForBeatSegment - minNimberOfFramesForBeatSegment;
        beatLMArrayForTrain = new String[2 * totalNumberOfWordsForBeatType];
        for (int i = minNimberOfFramesForBeatSegment; i < maxNimberOfFramesForBeatSegment; i++) {
            beatLMArrayForTrain[i - minNimberOfFramesForBeatSegment] = BEAT_SYMBOL + i;
            beatLMArrayForTrain[i - minNimberOfFramesForBeatSegment + 1] = DOWNBEAT_SYMBOL + i;
        }


        try {
            crsOperation.wordArrayForTrain = beatJumpArrayForTrain;
            FileWriter fileWriterTrain = new FileWriter(HelperFile.getFile(crsOperation.wordListTrainPath));
            FileWriter fileWriterTest = new FileWriter(HelperFile.getFile(crsOperation.wordListTestPath));
            for (String beat : crsOperation.wordArrayForTrain) {
                fileWriterTrain.write(String.format("%s\n", beat));
                fileWriterTest.write(String.format("%s\n", beat));
            }

            fileWriterTest.write(String.format("%s\n", NOTHING_SYMBOL));

            fileWriterTrain.close();
            fileWriterTest.close();

        } catch (IOException e) {
            logger.error("Problems: ");
            logger.error(Helper.getStackTrace(e));
        }

        try {

            FileWriter fileWriterLM = new FileWriter(HelperFile.getFile(crsOperation.wordListLMPath));

            for (int beatLength = minNimberOfFramesForBeatSegment; beatLength < maxNimberOfFramesForBeatSegment; beatLength++) {
                fileWriterLM.write(String.format("%s%d\n", DOWNBEAT_SYMBOL, beatLength));
                fileWriterLM.write(String.format("%s%d\n", BEAT_SYMBOL, beatLength));
            }
            fileWriterLM.write(String.format("%s\n", NOTHING_SYMBOL));
            fileWriterLM.write(START_SENTENCE + "\n");
            fileWriterLM.write(END_SENTENCE + "\n");
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
            fileWriter.write(String.format("\"*%s.lab\"\n%s\n%s\n.\n", BEAT_SYMBOL, BEAT_START, BEAT_END));
            fileWriter.write(String.format("\"*%s.lab\"\n%s\n%s\n.\n", DOWNBEAT_SYMBOL, DOWNBEAT_START, DOWNBEAT_END));

            fileWriter.write(String.format("\"*%s.lab\"\n%s\n.\n", BEAT_START, BEAT_START));
            fileWriter.write(String.format("\"*%s.lab\"\n%s\n.\n", BEAT_END, BEAT_END));
            fileWriter.write(String.format("\"*%s.lab\"\n%s\n.\n", DOWNBEAT_START, DOWNBEAT_START));
            fileWriter.write(String.format("\"*%s.lab\"\n%s\n.\n", DOWNBEAT_END, DOWNBEAT_END));
            fileWriter.write(String.format("\"*%s.lab\"\n%s\n.\n", NO_BEAT, NO_BEAT));

            for (int i = minNimberOfFramesForBeatSegment; i <= maxNimberOfFramesForBeatSegment; i++) {
                fileWriter.write(String.format("\"*%s%d.lab\"\n%s\n%s\n%s.\n", BEAT_SYMBOL, i, BEAT_START, BEAT_END, getMlfStringForBeat(i)));
                fileWriter.write(String.format("\"*%s%d.lab\"\n%s\n%s\n%s.\n", DOWNBEAT_SYMBOL, i, DOWNBEAT_START, DOWNBEAT_END, getMlfStringForBeat(i)));
            }

            fileWriter.close();
        } catch (IOException e) {
            logger.error("Problems: ");
            logger.error(Helper.getStackTrace(e));
        }
    }

    /**
     * Returns String, containing numberOfNoBeatFrames noBeat phonemes
     *
     * @param numberOfNoBeatFrames
     * @return
     */
    private String getMlfStringForBeat(int numberOfNoBeatFrames) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < numberOfNoBeatFrames; i++) {
            stringBuilder.append(NO_BEAT).append("\n");
        }
        return stringBuilder.toString();
    }


    public void createDictionaryFile() {
        try {
            FileWriter writer = new FileWriter(HelperFile.getFile(crsOperation.dictFilePath));
            for (int beatLength = minNimberOfFramesForBeatSegment; beatLength < maxNimberOfFramesForBeatSegment; beatLength++) {
                //This flexibility parameter allows for different duration for a beat "word"
                StringBuilder downbeatBuilder = new StringBuilder(String.format("%s%d %s %s", DOWNBEAT_SYMBOL, beatLength, DOWNBEAT_START, DOWNBEAT_END));
                StringBuilder beatBuilder = new StringBuilder(String.format("%s%d %s %s", BEAT_SYMBOL, beatLength, BEAT_START, BEAT_END));

                //It is necessary to subtract (2 * (Settings.statesBeat - 2)) to remove duration of beatStart and beatEnd phonemes
                for (int currentFrame = 0; currentFrame < Math.max(beatLength - 2 * (crsOperation.getExecParams().statesBeat - 2), 1); currentFrame++) {
                    downbeatBuilder.append(" ").append(NO_BEAT);
                    beatBuilder.append(" ").append(NO_BEAT);
                }

                downbeatBuilder.append("\n");
                beatBuilder.append("\n");

                writer.write(downbeatBuilder.toString());
                writer.write(beatBuilder.toString());
            }
            writer.write(String.format("%s %s\n", NOTHING_SYMBOL, NOTHING_SYMBOL));
            if (Settings.isToUseBigramDuringHVite) {
                writer.write(START_SENTENCE + "\n");
                writer.write(END_SENTENCE + "\n");
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
        outList.add(new BeatEvaluator());
        return outList;

    }

    @Override
    public AbstractCRSOperation getRecognizeOperation(StageParameters stageParameters, ExecParams execParams) {
        return new RecognizeBeatOperation(stageParameters, execParams);
    }

    @Override
    public AbstractCRSOperation getRecognizeLanguageModelOperation(StageParameters stageParameters, ExecParams execParams) {
        return new RecognizeBeatConventionalVersionWithLMOperation(stageParameters, execParams);
    }

    @Override
    public AbstractCRSOperation getTrainLanguageModelsOperation(StageParameters stageParameters, ExecParams execParams) {
        return new TrainingBeatLanguageModelsOperation(stageParameters, execParams);
    }

    @Override
    public FeaturesManager getFeaturesManager(String songFilePath, String outDirPath, boolean isForTraining, ExecParams execParams) {
        return new FeaturesManagerBeat(songFilePath, outDirPath, isForTraining, execParams);
    }
}