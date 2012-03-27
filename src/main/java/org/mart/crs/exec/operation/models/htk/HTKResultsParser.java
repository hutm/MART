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

import org.apache.log4j.Logger;
import org.mart.crs.exec.operation.models.htk.parser.chord.ChordHTKParser;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.beat.BeatsManager;
import org.mart.crs.management.label.LabelsParser;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.utils.helper.Helper;
import org.mart.tools.svf.Synchronizer;

import java.io.*;
import java.util.*;

import static org.mart.crs.config.Settings.*;
import static org.mart.crs.utils.helper.HelperFile.getFile;
import static org.mart.crs.utils.helper.HelperFile.getPathForFileWithTheSameName;
/**
 * User: hut
 * Date: Jul 28, 2008
 * Time: 8:05:21 PM
 * Performs parsing of HTK recognition output
 */
public class HTKResultsParser {

    protected static Logger logger = CRSLogger.getLogger(HTKResultsParser.class);

    /**
     * Parses HTKOut and writes results to label file
     *
     * @param HTKOutFile       HTKOutFile
     * @param recognizedFolder recognizedFolder
     */
    public static void parse(String HTKOutFile, String recognizedFolder) {
        Map<String, List<ChordSegment>> map = parseResults(HTKOutFile);
        storeResults(map, recognizedFolder);
    }


    /**
     * Parses HTKOut generated using output of FLM lattice rescoring and writes results to label file
     *
     * @param HTKOutFile       HTKOutFile
     * @param recognizedFolder recognizedFolder
     */
    public static void parseLM(String HTKOutFile, String recognizedFolder, boolean isOneVectorPerBeatVersion) {
        Map<String, List<ChordSegment>> map = parseResultsLM(HTKOutFile, isOneVectorPerBeatVersion);
        storeResults(map, recognizedFolder);
    }


    /**
     * Parses HTKOutFile and returns Song list that contain chord sequence with different chord hypotheses inside
     *
     * @param HTKOutFile HTKOutFile
     * @param firstPassRecognizedLabelsFolder
     *                   Folder with labels after first pass
     * @return Song list
     */
    public static List<ChordStructure> parseChordHypotheses(String HTKOutFile, String firstPassRecognizedLabelsFolder) {

        List<ChordStructure> outSongsList = new ArrayList<ChordStructure>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(HTKOutFile));

            String line, chordName, songFileName, songDirectory;
            float score;
            int number, chordOrder;
            StringTokenizer tokenizer;

            line = reader.readLine();
            if (!line.equals("#!MLF!#")) {
                logger.error("File " + HTKOutFile + " does not seem to be label file");
                throw new IOException();
            }

            List<ChordSegment> chordSegments = null;
            while ((line = reader.readLine()) != null && line.length() > 0) {

                //First read song name, start and end time
                tokenizer = new StringTokenizer(line, "/");
                number = tokenizer.countTokens();
                //Skip unnesesary tokens
                for (int j = 0; j < (number - 3); j++) {
                    tokenizer.nextToken();
                }
                songFileName = tokenizer.nextToken();


                songDirectory = line.replaceAll("\"", "");
                songDirectory = songDirectory.substring(0, songDirectory.lastIndexOf("/out"));

                //Now read chord order
                tokenizer.nextToken();
                String fileName = tokenizer.nextToken();
                chordOrder = Integer.parseInt(fileName.substring(0, fileName.indexOf(".")));


                //Read previously recognized labels

                //Check if the song is already in map
                List<ChordSegment> chordList;
                if (!outSongsList.contains(new ChordStructure(chordSegments, songFileName))) {
                    chordSegments = (new ChordStructure(getPathForFileWithTheSameName(songFileName + WAV_EXT, firstPassRecognizedLabelsFolder, LABEL_EXT))).getChordSegments();
                    outSongsList.add(new ChordStructure(chordSegments, songFileName));
                }

                //Now read hypotheses about chords
                while ((line = reader.readLine()) != null && !line.equals(".")) {
                    if (line.startsWith("///")) {
                        continue;
                    }

                    tokenizer = new StringTokenizer(line);
                    //skip start and end time
                    tokenizer.nextToken();
                    tokenizer.nextToken();

                    chordName = tokenizer.nextToken().trim();
                    score = Float.parseFloat(tokenizer.nextToken());

                    ChordSegment currentSegment = chordSegments.get(chordOrder);
                    if (currentSegment.getHypotheses().size() == 0 && !currentSegment.getChordName().equals(chordName)) {
                        //There is some parsing error
                        logger.error("previously recognized labels are wrong: " + songFileName + " for segment " + chordOrder);
                        continue;
                    } else {
                        currentSegment.addHypothesis(new ChordSegment(0, 0, chordName, score));
                    }

                }
            }
            reader.close();
        } catch (Exception e) {
            logger.error("Unexpexted Error occured ");
            logger.error(Helper.getStackTrace(e));

        }

        return outSongsList;
    }





    /**
     * stores results in a file
     *
     * @param map    map
     * @param outDir outDir
     */
    public static void storeResults(Map<String, List<ChordSegment>> map, String outDir) {
        //First create outDir
        logger.info("Storing data into folder " + outDir);
        (getFile(outDir)).mkdirs();

        String filename;
        for (String s : map.keySet()) {
            filename = outDir + File.separator + s + LABEL_EXT;
            try {
                FileWriter writer = new FileWriter(getFile(filename));
                for (ChordSegment cs : map.get(s)) {
                    writer.write(cs + "\n");
                }
                writer.close();

            } catch (IOException e) {
                logger.error("Unexpexted Error occured ");
                logger.error(Helper.getStackTrace(e));
            }
        }

    }

    /**
     * parses results
     *
     * @param HTKout HTKout
     * @return map
     */
    public static Map<String, List<ChordSegment>> parseResults(String HTKout) {
        Map<String, List<ChordSegment>> map = new HashMap<String, List<ChordSegment>>();
        Map<String, double[]> beatsMap = new HashMap<String, double[]>();


        try {
            double[] beats;

            BufferedReader reader = new BufferedReader(new FileReader(HTKout));

            String line, chordName, songFileName, temp, logLik;
            ChordSegment chordSegment;
            float startTimeSegment, endTimeSegment, startTime, endTime, logliklihood;
            int number, underscoreIndex;
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


                temp = tokenizer.nextToken();
                underscoreIndex = temp.indexOf("_");
                startTimeSegment = Float.parseFloat(temp.substring(0, underscoreIndex));
                endTimeSegment
                        = Float.parseFloat(temp.substring(underscoreIndex + 1, temp.indexOf("_", underscoreIndex + 1)));

                //Check if the song is already in map
                List<ChordSegment> chordList;
                if (!map.containsKey(songFileName)) {
                    map.put(songFileName, new ArrayList<ChordSegment>());
                }
                chordList = map.get(songFileName);

                //Now read recognized chords
                while ((line = reader.readLine()) != null && !line.equals(".")) {
                    tokenizer = new StringTokenizer(line);
                    startTime = Float.parseFloat(tokenizer.nextToken()) / ChordHTKParser.FEATURE_SAMPLE_RATE + startTimeSegment;
                    endTime = Float.parseFloat(tokenizer.nextToken()) / ChordHTKParser.FEATURE_SAMPLE_RATE + startTimeSegment;
                    chordName = tokenizer.nextToken();
                    logLik = tokenizer.nextToken().trim();
                    logliklihood = Float.parseFloat(logLik);

                    chordSegment = new ChordSegment(startTime, endTime, chordName, logliklihood);
                    chordList.add(chordSegment);
                }
                (chordList.get(chordList.size() - 1)).setOffset(endTimeSegment);

            }
            reader.close();
        } catch (Exception e) {
            logger.error("Unexpexted Error occured ");
            logger.error(Helper.getStackTrace(e));
        }


        // Before return sort each List
        for (String s : map.keySet()) {
            Collections.sort(map.get(s));
        }
        return map;
    }


    /**
     * parses results
     *
     * @param HTKout HTKout
     * @return map
     */
    public static Map<String, List<ChordSegment>> parseResultsLM(String HTKout, boolean isOneVectorPerChordVersion) {
        Map<String, List<ChordSegment>> map = new HashMap<String, List<ChordSegment>>();
        Map<String, double[]> beatsMap = new HashMap<String, double[]>();


        try {
            BufferedReader reader = new BufferedReader(new FileReader(HTKout));

            String line, chordName, chordName_unparsed, filePath, songFileName = "";
            ChordSegment chordSegment;
            int startTimeIndex, endTimeIndex;
            int number;
            StringTokenizer tokenizerFileName, tokenizerChordName;
            double[] beats;

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

                if (beatsMap.containsKey(songFileName)) {
                    beats = beatsMap.get(songFileName);
                } else {
                    beats = (new BeatsManager()).getBeatsForWavFile(songFileName + WAV_EXT);
                    beatsMap.put(songFileName, beats);
                }
                //Check if the song is already in map
                List<ChordSegment> chordList;
                if (!map.containsKey(songFileName)) {
                    map.put(songFileName, new ArrayList<ChordSegment>());
                }
                chordList = map.get(songFileName);

                String token = tokenizer.nextToken(" \t\n\r\f");
                token = tokenizer.nextToken();

                startTimeIndex = Math.round(Float.parseFloat(token) * ChordHTKParser.PRECISION_COEFF_LATTICE);

                token = tokenizer.nextToken();
                endTimeIndex = startTimeIndex + Math.round(Float.parseFloat(token) * ChordHTKParser.PRECISION_COEFF_LATTICE);

                chordName_unparsed = tokenizer.nextToken();
                if (chordName_unparsed.equals(LabelsParser.START_SENTENCE) || chordName_unparsed.equals(LabelsParser.END_SENTENCE)) {
                    continue;
                }

                try {
                    if (IS_FACTORED_LM) {
                        tokenizerChordName = new StringTokenizer(chordName_unparsed, ":");
                        chordName = tokenizerChordName.nextToken().substring(2);
                    } else {
                        chordName = chordName_unparsed;
                    }
                } catch (Exception e) {
                    chordName = "!NULL";
                    logger.error("Error while parsing token " + chordName);
                    logger.error(Helper.getStackTrace(e));
                }


                if (!chordName.equalsIgnoreCase("!NULL")) {
                    if (isOneVectorPerChordVersion) {
                        chordSegment = new ChordSegment(beats[startTimeIndex / (int) ChordHTKParser.PRECISION_COEFF_LATTICE], beats[endTimeIndex / (int) ChordHTKParser.PRECISION_COEFF_LATTICE], chordName);
                    } else {
                        chordSegment = new ChordSegment(startTimeIndex / ChordHTKParser.PRECISION_COEFF_LATTICE, endTimeIndex / ChordHTKParser.PRECISION_COEFF_LATTICE, chordName);
                    }
                    chordList.add(chordSegment);
                }
            }
            reader.close();
        } catch (Exception e) {
            logger.error("Unexpected Error occured ", e);
            logger.error(Helper.getStackTrace(e));

        }


        // Before return sort each List
        for (String s : map.keySet()) {
            Collections.sort(map.get(s));
        }
        return map;
    }

    /**
     * Performs synchronization with beat structure of recognized chord boundaries
     *
     * @param segments chordSegments
     * @param beatsMap beatsMap
     */
    private static void synchronizeWithBeats(Map<String, List<ChordSegment>> segments, Map<String, double[]> beatsMap) {
        for (String song : segments.keySet()) {
            Synchronizer.synchronizeChordLabelsWithBeats(segments.get(song), beatsMap.get(song));
        }
    }

    public static void main(String[] args) {
        parseLM("D:\\dev\\CHORDS\\CRS_MODULE\\script\\rescoring\\lm10.0_ac1.0_penalty-15.0.txt_out",
                "D:\\dev\\CHORDS\\CRS_MODULE\\script\\rescoring\\results", true);
    }


}
