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

import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.label.LabelsParser;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.Helper;
import org.apache.log4j.Logger;
import org.mart.crs.management.label.chord.ChordType;
import org.mart.crs.utils.helper.HelperArrays;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static org.mart.crs.config.Settings.LABEL_EXT;

/**
 * @deprecated replaced by  ChordEvaluator
 *             User: hut
 *             Date: Jul 28, 2008
 *             Time: 9:54:46 PM
 *             Evaluates results after ResultsParser
 */
public class EvaluatorOld {

    protected static Logger logger = CRSLogger.getLogger(EvaluatorOld.class);

    private float correctTimeGlobal = 0;
    private float knownChordTimeGlobal = 0;
    private float totalTimeGlobal = 0;

    private List<ChordEvalResult> results;

    public static void makeEvaluation(String recognizedDirPath, String groundTruthFolder, String outTxtFile) {
        EvaluatorOld evaluator = new EvaluatorOld();
        evaluator.evaluate(recognizedDirPath, groundTruthFolder, outTxtFile);
    }

    /**
     * performs evaluation
     *
     * @param recognizedDirPath recognizedFolder
     * @param groundTruthFolder groundTruthFolder
     * @param outTxtFile        outTxtFile
     */
    public void evaluate(String recognizedDirPath, String groundTruthFolder, String outTxtFile) {
        logger.info("Starting evaluation...");
        results = new ArrayList<ChordEvalResult>();
        evaluateFolder(recognizedDirPath, groundTruthFolder);

        //Sort Collection and store results into File
        Collections.sort(results);
        storeResults(results, outTxtFile);
    }


    private void evaluateFolder(String recognizedDirPath, String groundTruthFolder) {
        File recDir = HelperFile.getFile(recognizedDirPath);
        File[] songs = recDir.listFiles(new ExtensionFileFilter(new String[]{LABEL_EXT}));
        List<ChordSegment> chordList, chordListGT;
        for (File song : songs) {
            if (song.isDirectory()) {
                evaluateFolder(song.getAbsolutePath(), groundTruthFolder);
            } else {
                String GTFilePath = HelperFile.getPathForFileWithTheSameName(song.getName(), groundTruthFolder, LABEL_EXT);


                chordList = LabelsParser.getSegments(song.getAbsolutePath(), true);
                chordListGT = LabelsParser.getSegments(GTFilePath, true);

                if (chordListGT == null) {
                    continue;
                }
                results.add(compareLabels(chordList, chordListGT, song.getName()));
            }
        }
    }

    /**
     * return ChordEvalResult structure with evaluation results
     *
     * @param chordList   chordList
     * @param chordListGT chordListGT
     * @param song        song
     * @return EvalResult
     */
    public ChordEvalResult compareLabels(List<ChordSegment> chordList, List<ChordSegment> chordListGT, String song) {
        double duration, totalTime = 0;
        double totalChordsTime = 0;
        double totalKnownChordsTime = 0;
        double correctTime = 0;
        for (ChordSegment csGT : chordListGT) {
            for (ChordSegment cs : chordList) {
                if (cs.getOnset() < csGT.getOffset()) {
                    if (cs.getOffset() > csGT.getOnset()) {
                        if (cs.getChordName().equals(csGT.getChordName()) && !csGT.getChordType().equals(ChordType.NOT_A_CHORD) && !csGT.getChordType().equals(ChordType.UNKNOWN_CHORD)) {
                            correctTime += (Math.min(cs.getOffset(), csGT.getOffset()) - Math.max(cs.getOnset(), csGT.getOnset()));
                        }
                    }
                } else break;
            }
            duration = csGT.getOffset() - csGT.getOnset();

            totalTime += duration;
            totalChordsTime += duration;
            totalKnownChordsTime += duration;

            if (csGT.getChordType().equals(ChordType.UNKNOWN_CHORD)) {
                totalKnownChordsTime -= duration;
            }
            if (csGT.getChordType().equals(ChordType.NOT_A_CHORD)) {
                totalChordsTime -= duration;
                totalKnownChordsTime -= duration;
            }
        }
        this.correctTimeGlobal += correctTime;
        this.knownChordTimeGlobal += totalKnownChordsTime;
        this.totalTimeGlobal += totalTime;
        float recall = 1 - getNumberOfInsertions(chordList, chordListGT) / (float) chordListGT.size();
        float fragmentation = chordList.size() / (float) chordListGT.size();

        float logLikelihood = ChordEvaluator.calculateLogLiklihood(chordList);

        return new ChordEvalResult(song, totalTime, totalChordsTime, totalKnownChordsTime, correctTime, fragmentation, logLikelihood);
    }


    //This method checks if two ChordSegments have intersection

    private static boolean isIntersected(ChordSegment a, ChordSegment b) {
        if (a.getOnset() < b.getOffset() && a.getOffset() > b.getOnset()) {
            return true;
        }
        return false;
    }

    /**
     * Stores results into file
     *
     * @param results    ChordEvalResult
     * @param outTxtFile file to store results in
     */
    private void storeResults(List<ChordEvalResult> results, String outTxtFile) {
        try {
            FileWriter writer = new FileWriter(HelperFile.getFile(outTxtFile));
            writer.write("Evaluation Results:\n===================================================\n\n");
            String albumName = "";
            double totalChord, totalKnownChord, totalChord_global, totalKnownChord_global;
            totalChord = totalKnownChord = totalChord_global = totalKnownChord_global = 0;
            double[] statisticalData = new double[results.size()];
            int statisticalCounter = 0;
            int n = 0;
            int n_global = 0;
            double fragmentation_global = 0;
            double chordShiftBorder_global = 0;
            double totalTimeGlobal = 0;
            for (ChordEvalResult result : results) {
//                if (!albumName.equals(result.getAlbum())) {
                //Write total album statistics
                if (!albumName.equals("")) {
                    //writer.write("Average for album:\t" + (totalChord/n)  + "\t" + (totalKnownChord / n) + "\n\n");
                    writer.write("Average for album:\t" + (totalKnownChord / n) + "\n\n");
                }
                albumName = "fake album";
                totalChord = 0;
                totalKnownChord = 0;
                n = 0;
                writer.write("Album " + albumName + " :\n");
//                }
                //writer.write("\t" + result.getSong()  + "\t" + result.getCorrectTime()/result.getTotalChordsTime() + "\t" +result.getCorrectTime()/result.getTotalKnownChordsTime() + "\n");
                writer.write("\t" + result.getSong() + "\t" + result.getCorrectTime() / result.getTotalKnownChordsTime());

                //Write recall
//                writer.write("\t" + "recall: " + result.getRecall() +  "\n");

                //Write fragmentation
                writer.write("\t" + "fragmentation: " + result.getFragmentation());
                writer.write(String.format("\tlogliklihood: %7.2f\n", result.getLogLiklihood()));

                statisticalData[statisticalCounter] = result.getCorrectTime() / result.getTotalKnownChordsTime();
                statisticalCounter++;
                totalChord += result.getCorrectTime() / result.getTotalChordsTime();
                totalKnownChord += result.getCorrectTime() / result.getTotalKnownChordsTime();
                totalChord_global += result.getCorrectTime() / result.getTotalChordsTime();
                totalKnownChord_global += result.getCorrectTime() / result.getTotalKnownChordsTime();
                totalTimeGlobal += result.getCorrectTime() / result.getTotalTime();
                n++;
                n_global++;
                fragmentation_global += result.getFragmentation();
            }
            writer.write("Average for album:\t" + (totalKnownChord / n) + "\n\n");
            ;
            writer.write("=========================================\nAverage: " + (totalKnownChord_global / n_global) + "\n");
            writer.write("=========================================\nAverage timeunit: " + (this.correctTimeGlobal / this.knownChordTimeGlobal) + "\n");
//            writer.write("=========================================\nAverage recall: " + (recall_global / n_global) + "\n");
            writer.write("=========================================\nAverage timeunit global time: " + (this.correctTimeGlobal / this.totalTimeGlobal) + "\n");
            writer.write("=========================================\nAverage fragmentation: " + (fragmentation_global / n_global) + "\n");
            writer.write("=========================================\nAverage chordShiftBorder: " + (chordShiftBorder_global) + "\n");


            logger.info("Average accuracy: " + (totalKnownChord_global / n_global));
            logger.info("Average timeunit accuracy: " + (this.correctTimeGlobal / this.knownChordTimeGlobal));
//            logger.info("Average recall: " + (recall_global / n_global));
            logger.info("Average fragmentation: " + (fragmentation_global / n_global));
            logger.info("Average chordShiftBorder: " + (chordShiftBorder_global));

            writer.write("=========================================\nMean: " + HelperArrays.calculateMeanAndStandardDeviation(HelperArrays.getDoubleAsFloat(statisticalData))[0] + "\n");
            writer.write("=========================================\nSt. Deviation: " + HelperArrays.calculateMeanAndStandardDeviation(HelperArrays.getDoubleAsFloat(statisticalData))[1] + "\n");
            writer.write("=========================================\nMax value: " + HelperArrays.findMax(HelperArrays.getDoubleAsFloat(statisticalData)) + "\n");


            writer.close();
            logger.info("Finished evaluation");
        } catch (IOException e) {
            logger.error("Unexpexted Error occured ");
            logger.error(Helper.getStackTrace(e));
        }
    }


    /**
     * Some attemts were made to introduce recall measure for the task, but it failed
     *
     * @param chordList   chordList
     * @param chordListGT chordListGT
     * @return insertions
     */
    private int getNumberOfInsertions(List<ChordSegment> chordList, List<ChordSegment> chordListGT) {
        ChordSegment chord;
        Set<ChordSegment> insertions = new HashSet<ChordSegment>();
        for (ChordSegment cs : chordList) {
            for (ChordSegment csGT : chordListGT) {
                if (cs.getOnset() < csGT.getOffset()) {
                    if (cs.getOffset() > csGT.getOnset()) {
                        if (!cs.getChordName().equals(csGT.getChordName())) {
                            //Check, if there is a segment from GT that has intersection with the chord and has the same chordName
                            boolean found = false;
                            for (ChordSegment segmentGT : chordListGT) {
                                if (cs.getOnset() < segmentGT.getOffset() && cs.getOffset() > segmentGT.getOnset()) {
                                    if (cs.getChordName().equals(segmentGT.getChordName())) {
                                        found = true;
                                    }
                                }
                            }
                            //If not found - and the chordSegment in the list of insertions
                            if (!found) {
                                insertions.add(cs);
                            }
                        }
                    }
                }
            }
        }
        return insertions.size();
    }


//public static void main(String[] args) {
//        EvaluatorOld.makeEvaluation("/home/hut/temp/test/-", "/home/hut/Beatles/labels", "/home/hut/temp/test/-_newVersion.txt");
//        EvaluatorOld.makeEvaluation("/home/hut/temp/test/lmWeight_9.00#acWeight_1.00#wip_-3.00", "/home/hut/Beatles/labels", "/home/hut/temp/test/lmWeight_9.00#acWeight_1.00#wip_-3.00_newVersion.txt");
//    }


/**
     * |Main class for test
     *
     * @param args
     */
    public static void main(String[] args) {
        EvaluatorOld.makeEvaluation("/home/hut/temp/test/-", "/home/hut/Beatles/labels", "/home/hut/temp/test/-Refactored.txt");
        EvaluatorOld.makeEvaluation("/home/hut/temp/test/lmWeight_9.00#acWeight_1.00#wip_-3.00", "/home/hut/Beatles/labels", "/home/hut/temp/test/lmWeight_9.00#acWeight_1.00#wip_-3.00Refactored.txt");
    }


}


