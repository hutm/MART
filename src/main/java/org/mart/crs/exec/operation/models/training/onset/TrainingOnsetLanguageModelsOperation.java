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

package org.mart.crs.exec.operation.models.training.onset;

import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.models.htk.parser.chord.ChordHTKParser;
import org.mart.crs.exec.operation.models.training.chord.TrainingLanguageModelsOperation;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.management.beat.BeatStructure;
import org.mart.crs.management.beat.segment.BeatSegment;
import org.mart.crs.management.features.FeaturesManager;
import org.mart.crs.management.label.LabelsParser;
import org.mart.crs.management.label.LabelsSource;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mart.crs.exec.scenario.stage.StageParameters.*;

/**
 * @version 1.0 Dec 3, 2010 1:00:04 PM
 * @author: Hut
 */
public class TrainingOnsetLanguageModelsOperation extends TrainingLanguageModelsOperation {

    public TrainingOnsetLanguageModelsOperation(StageParameters stageParameters, ExecParams execParams) {
        super(stageParameters, execParams);
    }


    protected void createLanguageModels() throws IOException {

        HelperFile.createDir(lmDir);

        //First create text for standard language model
        String textFilePath = lmDir + File.separator + "text_lan_model_standard";
        parseLabelsToTextFile(wavFileList, textFilePath, false);

        String command = binariesDir + File.separator + "ngram-count -text " + textFilePath + " -order " + execParams.standardLmOrder + " -wbdiscount -lm " + (lmDir + File.separator + LMModelStandardVersion);
        Helper.execCmd(command);
    }


    public void parseLabelsToTextFile(String wavFileList, String outText, boolean isFactored) {
        String fileList = outText + "_labelsList";
        List<String> wavFilePaths = HelperFile.readLinesFromTextFile(wavFileList);
        logger.debug(String.format("read %d tokens from file %s", wavFilePaths.size(), wavFileList));
        List<String> labelFilePathList = new ArrayList<String>();

        LabelsSource labelsSource = new LabelsSource(Settings.labelsGroundTruthDir, true, "gt", Settings.BEAT_EXTENSIONS);
        for (String wavFilePath : wavFilePaths) {
            labelFilePathList.add(labelsSource.getFilePathForSong(wavFilePath));
        }
        logger.debug(String.format("added %d tokens from file %s", labelFilePathList.size(), wavFileList));

        createText(labelFilePathList, outText);
    }


    public void createText(List<String> labelFilePaths, String outFile) {
        logger.debug("Creating output file " + outFile);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(HelperFile.getFile(outFile)));
            List<BeatSegment> beatSequence;
            String beatSentence;

            for (String file : labelFilePaths) {

                BeatStructure beatStructure = BeatStructure.getBeatStructure(file);
                beatSequence = beatStructure.getBeatSegments();

                float stretchStart = Settings.stretchCoeffForBeatLMBuildingStart;
                float stretchEnd = Settings.stretchCoeffForBeatLMBuildingEnd;
                if(HelperFile.getExtension(file).equalsIgnoreCase(Settings.BEAT_MAZURKA_EXT)){
                    stretchStart = 0.8f;                                    //TODO remove hardcoded values
                    stretchEnd = 1.2f;
                }

                for (float stretch = stretchStart; stretch <= stretchEnd; stretch += 0.02) {
                    StringBuilder stringBuilder = new StringBuilder();

                    boolean acceptedString = true;
                    for (BeatSegment beatSegment : beatSequence) {
                        if (appendBuild(stringBuilder, beatSegment, stretch)) {
                            acceptedString = false;
                        }
                    }
                    if (acceptedString || HelperFile.getExtension(file).equalsIgnoreCase(Settings.BEAT_MAZURKA_EXT)) {
                        beatSentence = stringBuilder.toString();
                        writer.write(String.format("%s %s %s %s %s\n", LabelsParser.START_SENTENCE, NOTHING_SYMBOL, beatSentence, NOTHING_SYMBOL, LabelsParser.END_SENTENCE));
                    }

                }

                logger.debug("Text from " + file + " was successfully extracted ");
            }
            writer.close();

        } catch (IOException e) {
            logger.error("Unexpected error occured " + e);
            logger.error(Helper.getStackTrace(e));
        }
    }

    /**
     * Appends beat word. Returns whether the duration is out of allowed range
     * Returns true
     *
     * @param builder
     * @param beatSegment
     * @param stretch
     * @return is duration out of range
     */
    protected boolean appendBuild(StringBuilder builder, BeatSegment beatSegment, double stretch) {
        int duration = (int)Math.round(stretch * beatSegment.getDuration() / (FeaturesManager.getChrSamplingPeriod(execParams) / ChordHTKParser.FEATURE_SAMPLE_RATE)); //in FeaturesManager.chrSamplingPeriod units
        if (duration <= 0) {
            return false;
        }

        builder.append(String.format("%s%d ", beatSegment.toString(), duration));

        return (duration <= minNimberOfFramesForBeatSegment || duration >= maxNimberOfFramesForBeatSegment);
    }

}
