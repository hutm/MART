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

package org.mart.crs.exec.operation.models.lm;

import org.apache.log4j.Logger;
import org.mart.crs.config.Settings;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.beat.BeatsManager;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.management.label.chord.ChordType;
import org.mart.crs.management.label.chord.Root;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;

import java.io.*;
import java.util.*;

import static org.mart.crs.config.Settings.*;
import static org.mart.crs.management.label.LabelsParser.END_SENTENCE;
import static org.mart.crs.management.label.LabelsParser.START_SENTENCE;
import static org.mart.crs.utils.helper.HelperFile.*;

/**
 * User: hut
 * Date: Jul 7, 2008
 * Time: 11:39:31 PM
 * This class creates chord text for building language model
 */
public class TextForLMCreator {

    protected static Logger logger = CRSLogger.getLogger(TextForLMCreator.class);

    public void process(String wavFileList, String outText, boolean isFactored) {
        //wavFileList = "/home/hut/Beatles/list/all.txt";      //TODO Remove this this is just a stub
        List<String> wavFilePaths = HelperFile.readTokensFromTextFile(wavFileList, 1);
        logger.debug(String.format("read %d tokens from file %s", wavFilePaths.size(), wavFileList));
        List<String> labelFilePathList = new ArrayList<String>();
        for (String wavFilePath : wavFilePaths) {
            if (!Settings.isMIREX) {
                labelFilePathList.add(getPathForFileWithTheSameName(wavFilePath, labelsGroundTruthDir, LABEL_EXT));
            } else {
                labelFilePathList.add(wavFilePath + ".txt");
            }
        }
        logger.debug(String.format("added %d tokens from file %s", labelFilePathList.size(), wavFileList));

        if (isFactored) {

            String tempFileName = System.getProperty("java.io.tmpdir") + File.separator + "textForFLM.tmp";
            try {
                createTextForFactoredLM(labelFilePathList, tempFileName);
                createTextInFLMFormat(tempFileName, outText);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            createText(labelFilePathList, outText);
        }
    }

    /**
     * Creating text for training language modelling, using only label files and not including the information
     * on chord duration.
     *
     * @param labelsDir labelsDir
     * @param outFile   outFile
     */
    public void createText(String labelsDir, String outFile) {
        String fileList = System.getProperty("java.io.tmpdir") + File.separator + "labelsList";
        createFileList(labelsDir, fileList, new ExtensionFileFilter(new String[]{LABEL_EXT}), true);
        List<String> labelFilePathList = HelperFile.readTokensFromTextFile(fileList, 1);

        createText(labelFilePathList, outFile);

    }


    public void createText(List<String> labelFilePaths, String outFile) {
        logger.debug("Creating output file " + outFile);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(getFile(outFile)));
            List<ChordSegment> chordList;
            String chordSentence;

            for (String file : labelFilePaths) {
                StringBuilder stringBuilder = new StringBuilder();
                chordList = (new ChordStructure(file)).getChordSegments();


                //Remove duplicate chords from sequence
                boolean changed = true;
                while (changed) {
                    if (chordList.size() <= 1) {
                        break;
                    }
                    for (int i = 1; i < chordList.size(); i++) {
                        changed = false;
                        //First remove unknown chords
                        if (chordList.get(i).getChordType().equals(ChordType.UNKNOWN_CHORD)) {
                            chordList.remove(i);
                            changed = true;
                            break;
                        }
//                        if (chordList.get(i).getChordName().equals(chordList.get(i - 1).getChordName())) {
//                            chordList.remove(i);
//                            changed = true;
//                            break;
//                        }
                    }
                }


                for (ChordSegment cs : chordList) {
                    if (cs.getChordType().equals(ChordType.UNKNOWN_CHORD) || (cs.getChordType().equals(ChordType.NOT_A_CHORD) && !Arrays.asList(ChordType.chordDictionary).contains(ChordType.NOT_A_CHORD))) {
                        //Skip this chord label
                    } else {
                        stringBuilder.append(cs.getChordName()).append(" ");
                    }
                }
                chordSentence = stringBuilder.toString();

                //It is necessary to shift chord labels 12 times
                writer.write(START_SENTENCE + " " + chordSentence + " " + END_SENTENCE + " \n");
                for (int i = 1; i < NUMBER_OF_SEMITONES_IN_OCTAVE; i++) {
                    writer.write(START_SENTENCE + " " + shiftChords(chordSentence, i) + " " + END_SENTENCE + " \n");
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
     * Shift of chord line from  keyFrom to  keyTo
     *
     * @param inLine
     * @param keyFrom
     * @param keyTo
     * @return
     */
    public String shiftChords(String inLine, Root keyFrom, Root keyTo) {
        int shift = (keyTo.ordinal() - keyFrom.ordinal() + NUMBER_OF_SEMITONES_IN_OCTAVE) % NUMBER_OF_SEMITONES_IN_OCTAVE;
        return shiftChords(inLine, shift);
    }


    /**
     * Shift of chord line by <tt>shift<tt> semitones
     *
     * @param inLine
     * @param shift
     * @return
     */
    public String shiftChords(String inLine, int shift) {
        StringBuffer out = new StringBuffer();
        StringTokenizer tokenizer = new StringTokenizer(inLine);
        String chordLabel;
        while (tokenizer.hasMoreTokens()) {
            chordLabel = tokenizer.nextToken();
            ChordSegment chordSegment = new ChordSegment(0, 0, chordLabel, 0);
            if (!chordSegment.hasRoot()) {
                //Can be other symbol, i.e. <s> or </s>
                out.append(chordLabel).append(" ");
            } else {
                chordSegment.shiftRoot(shift);
                out.append(chordSegment.getChordName()).append(" ");
            }
        }
        return out.toString();
    }


    /**
     * Creates text for FLM with later extraction of chord duration
     *
     * @param labelsDir Dir with labels files
     */
    public void createTextForFactoredLM(String labelsDir, String textFilePath) throws IOException {

        String fileList = System.getProperty("java.io.tmpdir") + File.separator + "labelsList";
        createFileList(labelsDir, fileList, new ExtensionFileFilter(new String[]{LABEL_EXT}), true);
        List<String> labelFilePathList = HelperFile.readTokensFromTextFile(fileList, 1);

        createTextForFactoredLM(labelFilePathList, textFilePath);

    }


    public void createTextForFactoredLM(List<String> labelsFilePaths, String textFilePath) throws IOException {
        FileWriter writer = new FileWriter(getFile(textFilePath));

        String beatFilePath;
        File labelFile, beatFile;
        String chordSentence;
        String key, baseKey;
        for (String labelFilePath : labelsFilePaths) {
            labelFile = getFile(labelFilePath);
            beatFilePath = getPathForFileWithTheSameName(labelFile.getName(), BeatsManager.BEATS_DIR, BEAT_EXT);


            if (beatFilePath != null) {
                List<ChordSegment> chordSegments = (new ChordStructure(labelFilePath)).getChordSegments();
                List<String> beats = HelperFile.readTokensFromTextFile(beatFilePath, 1);
                String[] beatsArray = new String[beats.size()];
                beats.toArray(beatsArray);
                chordSentence = parseChordSentence(beatsArray, chordSegments);

                writer.write(chordSentence);
            }
        }

        writer.close();
    }


    public String parseChordSentence(String[] beatsArray, List<ChordSegment> chordSegments) {
        StringBuilder chordSentence = new StringBuilder();

        float startTime, endTime;
        Map<Double, String> candidates;
        for (int i = 0; i < beatsArray.length - 1; i++) {
            candidates = new HashMap<Double, String>();
            startTime = Float.parseFloat(beatsArray[i].replaceAll("\\,", "."));
            endTime = Float.parseFloat(beatsArray[i + 1].replaceAll("\\,", "."));
            for (ChordSegment cs : chordSegments) {
                if (cs.getOnset() <= startTime && cs.getOffset() > startTime ||
                        cs.getOnset() > startTime && cs.getOffset() <= endTime ||
                        cs.getOnset() <= endTime && cs.getOffset() > endTime
                        ) {
                    candidates.put((Math.min(endTime, cs.getOffset()) - Math.max(startTime, cs.getOnset())), cs.getChordName());
                }
                //TODO optimize segment search in the future (if you are not too lazy)
            }

            //Now choose the most probable candidate
            double maxValue = 0;
            String chordName = "";
            for (Double fValue : candidates.keySet()) {
                if (fValue > maxValue) {
                    maxValue = fValue;
                    chordName = candidates.get(fValue);
                }
            }
            chordSentence.append(chordName).append(" ");
        }
        chordSentence.append("\n");

        return chordSentence.toString();
    }


    public void createTextInFLMFormat(String parsedTextFilePath, String outFilePath) throws IOException {
        File inFile = getFile(parsedTextFilePath);
        File outFile = getFile(outFilePath);

        FileWriter writer = new FileWriter(outFile);

        if (inFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(inFile));
                String line;
                while ((line = reader.readLine()) != null && line.length() > 1) {
                    writer.write(START_SENTENCE + " ");
                    writer.write(compactLine(line, IS_QUANTIZED_DURATION, IS_INCLUDE_OOV_INTO_LM));
                    writer.write(END_SENTENCE + "\n");
                }
                reader.close();
                writer.close();
            } catch (IOException e) {
                logger.error("Cannot read data from fileListTrain file ");
                logger.error(Helper.getStackTrace(e));
            }
        }
    }

    /**
     * Transforms chord sequence into FLM representation (W=C:D=4), where W is ChordName, D - duration in beats
     *
     * @param line line
     * @return compactLine
     */
    public String compactLine(String line, boolean isToQuantize, boolean isToIncludeOOVChords) {
        String currentChord, token;
        int currentChordCount = 1;
        StringBuffer stringBuffer = new StringBuffer();
        StringTokenizer tokenizer = new StringTokenizer(line);
        currentChord = tokenizer.nextToken();
        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            if (token.equals(currentChord)) {
                currentChordCount++;
            } else {
                if (isToQuantize) {
                    currentChordCount = Helper.quantizeDuration(currentChordCount);
                }
                if (!isToIncludeOOVChords && (currentChord.equals(ChordType.NOT_A_CHORD.getName()) || currentChord.equals(ChordType.UNKNOWN_CHORD.getName()))) {

                } else {
                    stringBuffer.append("W-" + currentChord + ":D-" + currentChordCount + " ");
                }
                currentChord = token;
                currentChordCount = 1;
            }
        }

        return stringBuffer.toString();
    }


    /**
     * parses chord line in FLM originalFormat and returns List
     *
     * @param line input line
     * @return Map
     */
    public List<ChordSegment> parseCompactRepresentation(String line) {
        List<ChordSegment> out = new ArrayList<ChordSegment>();
        StringTokenizer chordTokenizer, tokenizer = new StringTokenizer(line);
        String token, chordToken, chordName, chordDuration;
        int duration;
        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            chordTokenizer = new StringTokenizer(token, ":");
            chordToken = chordTokenizer.nextToken();
            chordName = chordToken.substring(2);
            chordToken = chordTokenizer.nextToken();
            chordDuration = chordToken.substring(2);
            duration = Integer.parseInt(chordDuration);
            out.add(new ChordSegment(0, duration, chordName));
        }

        return out;
    }


    /**
     * Parses input string and returns the resu
     *
     * @param beatsArray
     * @param segments
     * @return
     */
    public List<ChordSegment> getChordSegmentsList(String[] beatsArray, List<ChordSegment> segments) {
        String chordSentence = parseChordSentence(beatsArray, segments);
        String chordSentenceCompact = compactLine(chordSentence, false, true);
        List<ChordSegment> chordDurations = parseCompactRepresentation(chordSentenceCompact);
        return chordDurations;
    }

}
