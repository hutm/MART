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

package org.mart.crs.exec.operation.models.htk;

import org.mart.crs.config.ExecParams;
import org.mart.crs.exec.operation.models.htk.parser.chord.ChordHTKParser;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.beat.BeatStructure;
import org.mart.crs.management.beat.segment.BeatSegment;
import org.mart.crs.management.beat.segment.BeatSegmentState;
import org.mart.crs.management.features.FeaturesManagerOnset;
import org.mart.crs.management.label.LabelsParser;
import org.mart.crs.utils.helper.Helper;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static org.mart.crs.config.Settings.BEAT_EXT;
import static org.mart.crs.utils.helper.HelperFile.getFile;

/**
 * @version 1.0 12/4/10 2:50 PM
 * @author: Hut
 */
public class HTKResultsParserBeat {


    protected static Logger logger = CRSLogger.getLogger(HTKResultsParserBeat.class);


    /**
     * Parses HTKOut and writes results to label file
     *
     * @param HTKOutFile       HTKOutFile
     * @param recognizedFolder recognizedFolder
     */
    public static void parse(String HTKOutFile, String recognizedFolder, ExecParams execParams) {
        Map<String, BeatStructure> map = parseResults(HTKOutFile, execParams);
        storeResults(map, recognizedFolder);
    }

    public static void parseLM(String HTKOutFile, String recognizedFolder, ExecParams execParams) {
        Map<String, BeatStructure> map = parseResultsLM(HTKOutFile, execParams);
        storeResults(map, recognizedFolder);
    }


    /**
     * stores results in a file
     *
     * @param map    map
     * @param outDir outDir
     */
    public static void storeResults(Map<String, BeatStructure> map, String outDir) {
        //First create outDir
        logger.info("Storing data into folder " + outDir);
        (getFile(outDir)).mkdirs();

        String folder, filename;
        for (String s : map.keySet()) {
            filename = outDir + File.separator + s + BEAT_EXT;
            BeatStructure beatStructure = map.get(s);
            beatStructure.serializeIntoXML(filename);

        }

    }

    /**
     * parses results
     *
     * @param HTKout HTKout
     * @return map
     */
    public static Map<String, BeatStructure> parseResults(String HTKout, ExecParams execParams) {
        Map<String, BeatStructure> map = new HashMap<String, BeatStructure>();

        double delay = FeaturesManagerOnset.getTimePeriodForFramesNumber(execParams.statesBeat - 2, execParams);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(HTKout));

            String line, beatName, songFileName, logLik;
            double startTime, endTime, logliklihood;
            int number;
            StringTokenizer tokenizer;

            line = reader.readLine();
            if (!line.equals("#!MLF!#")) {
                logger.error("File " + HTKout + " does not seem to be label file");
                throw new IOException();
            }
            while ((line = reader.readLine()) != null && line.length() > 0) {

                //First read song name, start and end time
                tokenizer = new StringTokenizer(line, "/");
                number = tokenizer.countTokens();
                //Skip unnesesary tokens
                for (int j = 0; j < (number - 2); j++) {
                    tokenizer.nextToken();
                }
                songFileName = tokenizer.nextToken();


                //Check if the song is already in map
                BeatStructure beatStructure;
                if (!map.containsKey(songFileName)) {
                    map.put(songFileName, new BeatStructure(new ArrayList<BeatSegment>()));
                }
                beatStructure = map.get(songFileName);
                beatStructure.setSongName(songFileName);

                //Now read recognized chords
                List<BeatSegmentState> beatSegmentStateList = new ArrayList<BeatSegmentState>();
                BeatSegmentState beatSegmentState;
                BeatSegment beatSegment = null;
                int measure = 0;
                int beatNumber = 1;
                while ((line = reader.readLine()) != null && !line.equals(".")) {

                    tokenizer = new StringTokenizer(line);
                    //TODO remove the first nobeat event
                    startTime = Float.parseFloat(tokenizer.nextToken()) / ChordHTKParser.FEATURE_SAMPLE_RATE + delay;
                    endTime = Float.parseFloat(tokenizer.nextToken()) / ChordHTKParser.FEATURE_SAMPLE_RATE + delay;
                    beatName = tokenizer.nextToken();
                    logLik = tokenizer.nextToken().trim();
                    logliklihood = Float.parseFloat(logLik);

                    if (beatName.indexOf("[") > 0) {  //IT means the transcription has state information
                        String beatType = beatName.substring(0, beatName.indexOf("["));
                        int stateNumber = Integer.parseInt(beatName.substring(beatName.indexOf("[") + 1, beatName.indexOf("[") + 2));
                        if (stateNumber == 2) {
                            if (beatSegment != null) {
                                beatStructure.addBeatSegment(beatSegment);
                            }
                            if (beatType.equalsIgnoreCase("downbeat")) {
                                measure = 1;
                                beatNumber = 1;
                            } else {
                                measure = 0;
                            }
                            beatSegment = new BeatSegment(startTime, beatNumber++, measure, 1);
                        }

                        beatSegmentState = new BeatSegmentState(startTime, endTime, stateNumber);
                        beatSegment.addBeatSegmentState(beatSegmentState);
                    } else {                           //IT means the transcription does not have state information
                        if (beatName.contains(StageParameters.NOTHING_SYMBOL) || beatName.equals(LabelsParser.START_SENTENCE)) {
                            continue;
                        }
                        if (beatName.contains(StageParameters.DOWNBEAT_SYMBOL)) {
                            measure = 1;
                            beatNumber = 1;
                        } else if (beatName.contains(StageParameters.BEAT_SYMBOL)) {
                            measure = 0;
                        } else {
                            continue;
                        }
                        beatSegment = new BeatSegment(startTime, beatNumber++, measure, 1);
                        beatStructure.addBeatSegment(beatSegment);
                    }

                }

            }
            reader.close();
        } catch (Exception e) {
            logger.error("Unexpexted Error occured ");
            logger.error(Helper.getStackTrace(e));
        }
        return map;
    }


    /**
     * parses results
     *
     * @param HTKout HTKout
     * @return map
     */
    public static Map<String, BeatStructure> parseResultsLM(String HTKout, ExecParams execParams) {
        Map<String, BeatStructure> map = new HashMap<String, BeatStructure>();

        double delay = FeaturesManagerOnset.getTimePeriodForFramesNumber(execParams.statesBeat - 2, execParams);


        try {
            BufferedReader reader = new BufferedReader(new FileReader(HTKout));

            String line, beatNameUnparsed, filePath, songFileName = "";
            BeatSegment beatSegment;
            float startTimeIndex, endTimeIndex;
            int number;
            StringTokenizer tokenizerFileName, tokenizerChordName;
            Float[] beats;

            int measure;
            int beatNumber = 1;
            while ((line = reader.readLine()) != null && line.length() > 0) {

                //First read song name, start and end time
                StringTokenizer tokenizer = new StringTokenizer(line, " \t\n\r\f");
                filePath = tokenizer.nextToken();

                tokenizerFileName = new StringTokenizer(filePath, "/");
                number = tokenizerFileName.countTokens();
                //Skip unnesesary tokens
                for (int j = 0; j < (number - 2); j++) {
                    tokenizerFileName.nextToken();
                }
                songFileName = tokenizerFileName.nextToken();


                //Check if the song is already in map
                BeatStructure beatStructure;
                if (!map.containsKey(songFileName)) {
                    map.put(songFileName, new BeatStructure(new ArrayList<BeatSegment>()));
                }
                beatStructure = map.get(songFileName);
                beatStructure.setSongName(songFileName);

                tokenizer.nextToken(" \t\n\r\f");
                String token = tokenizer.nextToken();
                if (token.equals("?")) {
                    continue;
                }

                startTimeIndex = Float.parseFloat(token) * ChordHTKParser.PRECISION_COEFF_LATTICE;

                token = tokenizer.nextToken();
                endTimeIndex = startTimeIndex + Float.parseFloat(token) * ChordHTKParser.PRECISION_COEFF_LATTICE;

                beatNameUnparsed = tokenizer.nextToken();
                if (beatNameUnparsed.equals(LabelsParser.START_SENTENCE) || beatNameUnparsed.equals(LabelsParser.END_SENTENCE) || beatNameUnparsed.equals("!NULL")) {
                    continue;
                }


                if (beatNameUnparsed.contains(StageParameters.NOTHING_SYMBOL) || beatNameUnparsed.equals(LabelsParser.START_SENTENCE)) {
                    continue;
                }
                if (beatNameUnparsed.contains(StageParameters.DOWNBEAT_SYMBOL)) {
                    measure = 1;
                    beatNumber = 1;
                } else if (beatNameUnparsed.contains(StageParameters.BEAT_SYMBOL)) {
                    measure = 0;
                } else {
                    continue;
                }
                beatSegment = new BeatSegment(startTimeIndex / ChordHTKParser.PRECISION_COEFF_LATTICE + delay, beatNumber++, measure, 1);
                beatStructure.addBeatSegment(beatSegment);
            }
            reader.close();
        } catch (Exception e) {
            logger.error("Unexpected Error occured ", e);
            logger.error(Helper.getStackTrace(e));

        }

        return map;
    }


    public static void main(String[] args) {
        parse("/home/hut/work/work_test/testSimpleBeats/btemp/out32_-65.0",
                "/home/hut/work/work_test/testSimpleBeats/btemp/tempTest", ExecParams._initialExecParameters);
        BeatStructure beatStructure = BeatStructure.getBeatStructure("/home/hut/work/work_test/testSimpleBeats/btemp/tempTest/0400 - George Michael - Careless Whisper_.xml");
        System.out.println("");
    }

}
