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

package org.mart.crs.exec.operation.eval.chord;

import org.apache.log4j.Logger;
import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.eval.AbstractCRSEvaluator;
import org.mart.crs.exec.operation.eval.chord.confusion.ConfusionChordManager;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.label.LabelsParser;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.management.label.chord.ChordType;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mart.crs.config.Settings.LABEL_EXT;
import static org.mart.crs.management.label.chord.ChordType.NOT_A_CHORD;
import static org.mart.crs.management.label.chord.ChordType.UNKNOWN_CHORD;
import static org.mart.crs.utils.helper.HelperFile.getFile;
import static org.mart.crs.utils.helper.HelperFile.getPathForFileWithTheSameName;

/**
 * @version 1.0 3/31/11 9:41 AM
 * @author: Hut
 */
public class ChordEvaluator extends AbstractCRSEvaluator {

    protected static Logger logger = CRSLogger.getLogger(ChordEvaluator.class);

    public static final String CHORD_RECOGNITION_RATE_TIME_NAME = "CRRTime";
    public static final String CHORD_RECOGNITION_RATE_NAME = "CRR";
    public static final String FRAGMENTATION_NAME = "fragmentation";

    protected String recognizedDirPath;
    protected String groundTruthFolder;
    protected String outTxtFile;
    protected String outTxtFileConfusion;

    protected File outputDirectory;


    protected float correctTimeGlobal;
    protected float knownChordTimeGlobal;
    protected float totalTimeGlobal;
    protected float chordRecognitionRateGlobal;
    protected float chordRecognitionRateTimeBasedGlobal;
    protected float fragmentationGlobal;

    protected List<ChordEvalResult> chordEvalResults;


    public void initializeDirectories(String recognizedDirPath, String groundTruthFolder, String outTxtFile) {
        this.recognizedDirPath = recognizedDirPath;
        this.groundTruthFolder = groundTruthFolder;
        this.outTxtFile = outTxtFile.replaceAll(Settings.TXT_EXT, String.format("%s-%s", this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".") + 1), Settings.TXT_EXT));
        this.outTxtFileConfusion = this.outTxtFile.replaceAll(Settings.TXT_EXT, String.format("-confusions%s", Settings.TXT_EXT));

        chordEvalResults = new ArrayList<ChordEvalResult>();

        correctTimeGlobal = 0;
        knownChordTimeGlobal = 0;
        totalTimeGlobal = 0;
        chordRecognitionRateGlobal = 0;
        chordRecognitionRateTimeBasedGlobal = 0;
        fragmentationGlobal = 0;

        outputDirectory = getFile(HelperFile.getFilePathWithoutExtension(this.outTxtFile.replace(Settings.TXT_EXT, "")));
        outputDirectory.mkdirs();
    }


    public void evaluate() {

        ChordType[] tempDictionary = ChordType.chordDictionary;
        ChordType.chordDictionary = ChordType.CHORD_DICTIONARY_FULL;

        logger.info("Starting evaluation...");
        processFolder(recognizedDirPath, groundTruthFolder);

        //Sort Collection and store chordEvalResults into File
        Collections.sort(chordEvalResults);
        saveResults();

        ChordType.chordDictionary = tempDictionary;

        ConfusionChordManager confusionChordManager = new ConfusionChordManager();
        confusionChordManager.extractConfusion(this.recognizedDirPath, this.groundTruthFolder, this.outTxtFileConfusion);
    }


    protected void processFolder(String recognizedDirPath, String groundTruthFolder) {
        File recDir = getFile(recognizedDirPath);
        File[] songs = recDir.listFiles(new ExtensionFileFilter(LABEL_EXT, false));
        List<ChordSegment> chordList, chordListGT;
        for (File song : songs) {
            String GTFilePath = getPathForFileWithTheSameName(song.getName(), groundTruthFolder, LABEL_EXT);
            if (GTFilePath == null) {
                continue;
            }
//            chordList = (new ChordStructure(song.getAbsolutePath())).getChordSegments();
//            chordListGT = (new ChordStructure(GTFilePath)).getChordSegments();
            chordList = LabelsParser.getSegments(song.getAbsolutePath(), true);
            chordListGT = LabelsParser.getSegments(GTFilePath, true);
            chordEvalResults.add(compareLabels(chordList, chordListGT, song.getName()));
        }
    }

    /**
     * return ChordEvalResult structure with evaluation chordEvalResults
     *
     * @param chordList   chordList
     * @param chordListGT chordListGT
     * @param song        song
     * @return EvalResult
     */
    protected ChordEvalResult compareLabels(List<ChordSegment> chordList, List<ChordSegment> chordListGT, String song) {
        double duration, totalTime = 0;
        double totalChordsTime = 0;
        double totalKnownChordsTime = 0;
        double correctTime = 0;
        for (ChordSegment csGT : chordListGT) {
            for (ChordSegment cs : chordList) {
                if (cs.getOnset() < csGT.getOffset()) {
                    if (cs.getOffset() > csGT.getOnset()) {
                        if (cs.getChordName().equals(csGT.getChordName()) && !csGT.getChordType().equals(NOT_A_CHORD) && !csGT.getChordType().equals(ChordType.UNKNOWN_CHORD)) {
                            correctTime += (Math.min(cs.getOffset(), csGT.getOffset()) - Math.max(cs.getOnset(), csGT.getOnset()));
                        }
                    }
                } else break;
            }
            duration = csGT.getOffset() - csGT.getOnset();

            totalTime += duration;
            totalChordsTime += duration;
            totalKnownChordsTime += duration;

            if (csGT.getChordType().getName().equals(UNKNOWN_CHORD.getName())) {
                totalKnownChordsTime -= duration;
            }
            if (csGT.getChordType().getName().equals(NOT_A_CHORD.getName())) {
                totalChordsTime -= duration;
                totalKnownChordsTime -= duration;
            }
        }

        float fragmentation = chordList.size() / (float) chordListGT.size();
        float logLikelihood = calculateLogLiklihood(chordList);

        return new ChordEvalResult(song, totalTime, totalChordsTime, totalKnownChordsTime, correctTime, fragmentation, logLikelihood);
    }


    public static float calculateLogLiklihood(List<ChordSegment> chordSegments) {
        float out = 0;
        for (ChordSegment cs : chordSegments) {
            out += cs.getLogLikelihood() / (cs.getOffset() - cs.getOnset());
        }
        return out / chordSegments.size();
    }


    protected void saveResults() {
        try {
            FileWriter writer = new FileWriter(outTxtFile);
            int length = chordEvalResults.size();
            if (Settings.numberOfTestMaterial > 0) {
                length = Settings.numberOfTestMaterial;     //Sometimes output does not contain labels for all the songs. In this way the score will be lower.
            }
            Collections.sort(chordEvalResults);
            for (ChordEvalResult evalResult : chordEvalResults) {

                this.correctTimeGlobal += evalResult.getCorrectTime();
                this.knownChordTimeGlobal += evalResult.getTotalKnownChordsTime();
                this.totalTimeGlobal += evalResult.getTotalTime();
                this.chordRecognitionRateGlobal += evalResult.getChordrecognitionRate();
                this.fragmentationGlobal += evalResult.getFragmentation();


                String songName = Helper.getStringPadded(evalResult.getSong(), 90);
                writer.write(String.format("%s %5.5f %5.5f %5.5f\r\n", songName,
                        evalResult.getChordrecognitionRate(), evalResult.getFragmentation(), evalResult.getLogLiklihood()));
            }

            writer.write("----------------------------------------\r\n");
            chordRecognitionRateTimeBasedGlobal = correctTimeGlobal / knownChordTimeGlobal;
            writer.write(String.format("%5.5f %5.5f %5.5f\r\n",
                    chordRecognitionRateTimeBasedGlobal, chordRecognitionRateGlobal /= length, fragmentationGlobal /= length));

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String getResultsHeadersCommaSeparated() {
        return String.format("%s,%s,%s,", CHORD_RECOGNITION_RATE_TIME_NAME, CHORD_RECOGNITION_RATE_NAME, FRAGMENTATION_NAME);
    }

    public String getResultsValuesCommaSeparated() {
        return String.format("%5.5f,%5.5f,%5.5f,", chordRecognitionRateTimeBasedGlobal, chordRecognitionRateGlobal, fragmentationGlobal);
    }

}
