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

import org.mart.crs.config.Settings;
import org.mart.crs.utils.helper.Helper;

import java.io.FileWriter;
import java.io.IOException;

import static org.mart.crs.exec.scenario.stage.StageParameters.*;
import static org.mart.crs.config.Settings.*;
import static org.mart.crs.management.label.LabelsParser.END_SENTENCE;
import static org.mart.crs.management.label.LabelsParser.START_SENTENCE;
import static org.mart.crs.utils.helper.HelperFile.getFile;
/**
 * @version 1.0 7/27/11 2:11 PM
 * @author: Hut
 */
public class BeatOnlyOperationDomain extends BeatOperationDomain {


    public void createWordLists() {
        int totalNumberOfWordsForBeatType = maxNimberOfFramesForBeatSegment - minNimberOfFramesForBeatSegment;
        beatLMArrayForTrain = new String[totalNumberOfWordsForBeatType];
        for (int i = minNimberOfFramesForBeatSegment; i < maxNimberOfFramesForBeatSegment; i++) {
            beatLMArrayForTrain[i - minNimberOfFramesForBeatSegment] = BEAT_SYMBOL + i;
        }


        try {
            crsOperation.wordArrayForTrain = beatOnlyArrayForTrain;
            FileWriter fileWriterTrain = new FileWriter(getFile(crsOperation.wordListTrainPath));
            FileWriter fileWriterTest = new FileWriter(getFile(crsOperation.wordListTestPath));
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

            FileWriter fileWriterLM = new FileWriter(getFile(crsOperation.wordListLMPath));

            for (int beatLength = minNimberOfFramesForBeatSegment; beatLength < maxNimberOfFramesForBeatSegment; beatLength++) {
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
            FileWriter fileWriter = new FileWriter(getFile(crsOperation.mlfFilePath));
            fileWriter.write("#!MLF!#\n");
            fileWriter.write(String.format("\"*%s.lab\"\n%s\n%s\n.\n", BEAT_SYMBOL, BEAT_START, BEAT_END));
            fileWriter.write(String.format("\"*%s.lab\"\n%s\n%s\n.\n", DOWNBEAT_SYMBOL, BEAT_START, BEAT_END));

            fileWriter.write(String.format("\"*%s.lab\"\n%s\n.\n", BEAT_START, BEAT_START));
            fileWriter.write(String.format("\"*%s.lab\"\n%s\n.\n", BEAT_END, BEAT_END));
            fileWriter.write(String.format("\"*%s.lab\"\n%s\n.\n", DOWNBEAT_START, BEAT_START));
            fileWriter.write(String.format("\"*%s.lab\"\n%s\n.\n", DOWNBEAT_END, BEAT_END));
            fileWriter.write(String.format("\"*%s.lab\"\n%s\n.\n", NO_BEAT, NO_BEAT));

            for (int i = minNimberOfFramesForBeatSegment; i <= maxNimberOfFramesForBeatSegment; i++) {
                fileWriter.write(String.format("\"*%s%d.lab\"\n%s\n%s\n%s.\n", BEAT_SYMBOL, i, BEAT_START, BEAT_END, getMlfStringForBeat(i)));
                fileWriter.write(String.format("\"*%s%d.lab\"\n%s\n%s\n%s.\n", DOWNBEAT_SYMBOL, i, BEAT_START, BEAT_END, getMlfStringForBeat(i)));
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
            FileWriter writer = new FileWriter(getFile(crsOperation.dictFilePath));
            for (int beatLength = minNimberOfFramesForBeatSegment; beatLength < maxNimberOfFramesForBeatSegment; beatLength++) {
                //This flexibility parameter allows for different duration for a beat "word"
                StringBuilder downbeatBuilder = new StringBuilder(String.format("%s%d %s %s", DOWNBEAT_SYMBOL, beatLength, BEAT_START, BEAT_END));
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


}
