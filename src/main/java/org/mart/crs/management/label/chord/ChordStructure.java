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

package org.mart.crs.management.label.chord;

import org.apache.log4j.Logger;
import org.mart.crs.config.Extensions;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.beat.BeatStructure;
import org.mart.crs.management.label.LabelsSource;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;
import org.mart.crs.utils.helper.HelperFile;

import java.io.*;
import java.util.*;

import static org.mart.crs.config.Extensions.LABEL_EXT;

/**
 * @version 1.0 3/29/11 11:38 AM
 * @author: Hut
 */
public class ChordStructure implements Comparable<ChordStructure> {


    protected static Logger logger = CRSLogger.getLogger(ChordStructure.class);

    protected List<ChordSegment> chordSegments;
    protected String songName;

    public ChordStructure(String labelFilePath) {
        this(labelFilePath, false);
    }

    public ChordStructure(String labelFilePath, boolean isCVS) {
        this.chordSegments = new ArrayList<ChordSegment>();
        this.songName = HelperFile.getNameWithoutExtension(labelFilePath);
        if (!isCVS) {
            parseChordLabels(labelFilePath);
        } else {
            parseChordLabelsCsv(labelFilePath);
        }
    }

    public ChordStructure(List<ChordSegment> chordSegments, String songName) {
        this.chordSegments = chordSegments;
        this.songName = songName;
    }


    public void parseChordLabels(String labelFilePath) {
        File file;
        try {

            file = HelperFile.getFile(labelFilePath);

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = reader.readLine()) != null && line.length() > 1) {
                chordSegments.add(new ChordSegment(line));
            }
            reader.close();
        } catch (FileNotFoundException e) {
            logger.info("Could not find label file " + labelFilePath);
        } catch (Exception e) {
            logger.error("Unexpected Error occured ");
            logger.error(Helper.getStackTrace(e));
        }
    }

    public void parseChordLabelsCsv(String labelFilePath) {
        File file;
        try {

            file = HelperFile.getFile(labelFilePath);

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            String[] previousLine = null;
            while ((line = reader.readLine()) != null && line.length() > 1) {
                String[] comps = line.trim().replaceAll("\"", "").split(",");
                comps[1] = comps[1].indexOf("/") > 0 ? comps[1].substring(0, comps[1].indexOf("/")) : comps[1];
                if (previousLine != null) {                                                                                                        //TODO chords from nnls-chroma may have the following format: C:maj/A which is not parsed correctly
                    chordSegments.add(new ChordSegment(String.format("%s %s %s", previousLine[0], comps[0], previousLine[1])));
                }
                previousLine = comps;
            }
            reader.close();
        } catch (FileNotFoundException e) {
            logger.info("Could not find label file " + labelFilePath);
        } catch (Exception e) {
            logger.error("Unexpected Error occured ");
            logger.error(Helper.getStackTrace(e));
        }
    }


    public void saveSegmentsInFile(String outDirectory, boolean isChordNameOriginal) {
        Collections.sort(chordSegments);
        try {
            FileWriter writer = new FileWriter(HelperFile.getFile(String.format("%s/%s%s", outDirectory, songName, Extensions.LABEL_EXT)));
            for (ChordSegment cs : chordSegments) {
                if (!isChordNameOriginal) {
                    writer.write(cs + "\n");
                } else {
                    writer.write(String.format("%7.5f\t%7.5f\t%s\n", cs.getOnset(), cs.getOffset(), cs.getChordNameOriginal()));
                }
            }
            writer.close();

        } catch (IOException e) {
            logger.error("Unexpected error occured: ");
            logger.error(Helper.getStackTrace(e));
        }
    }

    public void saveSegmentsInFile(String outDirectory) {
        this.saveSegmentsInFile(outDirectory, false);
    }

    public void exportToFileInOriginalChordFormat(String filePath) {
        List<String> lines = new ArrayList<String>();
        for (ChordSegment cs : chordSegments) {
            lines.add(String.format("%5.5f\t%5.5f\t%s", cs.getOnset(), cs.getOffset(), cs.getChordNameOriginal()));
        }
        HelperFile.saveCollectionInFile(lines, filePath);
    }


    public void saveHypothesesInFile(String outFilePath) {
        StringBuffer buffer = new StringBuffer();
        for (ChordSegment cs : chordSegments) {
            buffer.append(cs).append(" | ");
            for (ChordSegment hypo : cs.getHypotheses()) {
                buffer.append(String.format("%s %5.3f ", ChordSegment.preprocessChordLabel(hypo.getChordName()), hypo.getLogLikelihood()));
            }
            buffer.append("\r\n");
        }
        try {
            FileWriter writer = new FileWriter(HelperFile.getFile(outFilePath));
            writer.write(buffer.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static ChordStructure readHypothesesFromFile(String filePath) {
        String songName = HelperFile.getNameWithoutExtension(filePath);
        List<String> lines = HelperFile.readLinesFromTextFile(filePath);
        List<ChordSegment> chordSegments = new ArrayList<ChordSegment>();
        for (String line : lines) {
            ChordSegment chordSegment = new ChordSegment(line);
            String hypothesesLine = line.substring(line.indexOf("|") + 1);
            StringTokenizer tokenizer = new StringTokenizer(hypothesesLine);
            while (tokenizer.hasMoreTokens()) {
                chordSegment.addHypothesis(new ChordSegment(0, 0, tokenizer.nextToken(), Helper.parseDouble(tokenizer.nextToken())));
            }
            chordSegments.add(chordSegment);
        }
        return new ChordStructure(chordSegments, songName);
    }


    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongName() {
        return songName;
    }

    @Override
    public boolean equals(Object o) {
        if (((ChordStructure) o).getSongName().equals(getSongName())) {
            return true;
        } else {
            return false;
        }
    }


    public ChordStructure getFinalChords() {
        List<ChordSegment> chordSegments = new ArrayList<ChordSegment>();
        for (ChordSegment chordSegment : this.chordSegments) {
            ChordSegment firstHypo = chordSegment.getHypotheses().get(0);
            ChordSegment secondHypo = chordSegment.getHypotheses().get(1);
            int notes1[] = firstHypo.getNotes();
            int notes2[] = secondHypo.getNotes();
            int[] concatedNotes = HelperArrays.concat(notes1, notes2);
            concatedNotes = ChordSegment.getNotesWithoutDuplicates(concatedNotes);
            ChordSegment newSegment = new ChordSegment(chordSegment.getOnset(), chordSegment.getOffset(), ChordType.NOT_A_CHORD.getName());
            newSegment.setRootAndType(concatedNotes);
            if (newSegment.hasRoot()) {
                chordSegments.add(newSegment);
            } else {
                chordSegments.add(chordSegment);
            }
        }

        ChordStructure chordStructure = new ChordStructure(chordSegments, this.songName);
        return chordStructure;

    }


    /**
     * post-processing step, when silence segments are added to the final labels
     *
     * @param inDirPath
     * @param outDirPath
     * @param intervals
     * @param isForMIREX
     */
    @Deprecated
    public static void addNonChordSegments(String inDirPath, String outDirPath, Map<String, List<float[]>> intervals, boolean isForMIREX, String labelExtension) {
        File outDir = HelperFile.getFile(outDirPath);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        Map<String, List<ChordSegment>> mapForStorage = new HashMap<String, List<ChordSegment>>();
        for (String fileName : intervals.keySet()) {
            List<ChordSegment> inSegments = (new ChordStructure(HelperFile.getPathForFileWithTheSameName(fileName, inDirPath, LABEL_EXT))).getChordSegments();
            List<ChordSegment> tempListAdd = new ArrayList<ChordSegment>();
            List<ChordSegment> tempListRemove = new ArrayList<ChordSegment>();
            List<float[]> silenceIntervals = intervals.get(fileName);
            ChordSegment silenceSegment;
            for (ChordSegment segment : inSegments) {
                for (float[] silence : silenceIntervals) {
                    silenceSegment = new ChordSegment(silence[0], silence[1], ChordType.NOT_A_CHORD.getName());
                    if (segment.intersects(silenceSegment)) {
                        double endTimeTemp = segment.getOffset();
                        if (segment.getOnset() < silenceSegment.getOnset()) {
                            segment.setOffset(silenceSegment.getOnset());
                            if (endTimeTemp > silenceSegment.getOffset()) {
                                ChordSegment newSegment = new ChordSegment(silenceSegment.getOffset(), endTimeTemp, segment.getChordName());
                                tempListAdd.add(newSegment);
                            }
                        } else {
                            if (segment.getOffset() > silenceSegment.getOffset()) {
                                segment.setOnset(silenceSegment.getOffset());
                            } else {
                                tempListRemove.add(segment);
                            }
                        }
                    }
                }
            }

            for (float[] silence : silenceIntervals) {
                inSegments.add(new ChordSegment(silence[0], silence[1], ChordType.NOT_A_CHORD.getName()));
            }
            for (ChordSegment segment : tempListAdd) {
                inSegments.add(segment);
            }
            for (ChordSegment segment : tempListRemove) {
                inSegments.remove(segment);
            }

            boolean changed = true;

            while (changed) {
                changed = false;
                Collections.sort(inSegments);

                tempListRemove = new ArrayList<ChordSegment>();
                for (ChordSegment segment : inSegments) {
                    if (segment.getOffset() - segment.getOnset() < 0.3f && segment.getChordName() != ChordType.NOT_A_CHORD.getName()) {
                        tempListRemove.add(segment);
                        int index = inSegments.indexOf(segment);
                        if (index > 0 && index < (inSegments.size() - 1)) {
                            if (inSegments.get(index - 1).getChordName() == inSegments.get(index + 1).getChordName()) {
                                inSegments.get(index - 1).setOffset(inSegments.get(index + 1).getOffset());
                                tempListRemove.add(inSegments.get(index + 1));
                            } else {
                                inSegments.get(index - 1).setOffset(inSegments.get(index + 1).getOnset());
                            }
                        }
                        changed = true;
                        break;
                    }
                }

                for (ChordSegment segment : tempListRemove) {
                    inSegments.remove(segment);
                }

            }


            Collections.sort(inSegments);

            mapForStorage.put(fileName, inSegments);
        }

        LABEL_EXT = labelExtension;
//        HTKResultsParser.storeResults(mapForStorage, outDirPath);    //TODO: needs refactoring
        LABEL_EXT = ".lab";

    }


    public ChordStructure transpose(int numberOfSemitones) {
        List<ChordSegment> outList = new ArrayList<ChordSegment>();
        for (ChordSegment chordSegment : chordSegments) {
            if (chordSegment.hasRoot()) {
                int rootIndex = HelperArrays.transformIntValueToBaseRange(chordSegment.getRoot().ordinal() + numberOfSemitones, 12);
                outList.add(new ChordSegment(chordSegment.getOnset(), chordSegment.getOffset(), Root.values()[rootIndex] + chordSegment.getChordType().getName()));
            } else {
                outList.add(chordSegment);
            }
        }
        return new ChordStructure(outList, getSongName());
    }

    public String getChordSequenceWithoutTimings() {
        StringBuilder builder = new StringBuilder();
        for (ChordSegment cs : chordSegments) {
            builder.append(cs.getChordName()).append(" ");
        }
        return builder.toString();
    }

    public double[] getTimeGrid() {
        double[] out = new double[chordSegments.size() + 1];
        for (int i = 0; i < chordSegments.size(); i++) {
            out[i] = chordSegments.get(i).getOnset();
        }
        out[out.length - 1] = chordSegments.get(chordSegments.size() - 1).getOffset();
        return out;
    }


    /**
     * returns aray of n-best logliklihoods as a fuction of time
     *
     * @return
     */
    public float[][] getLogLikSampleArray() {

        //First count number of Hypos
        int counter = 1;
        String currentChord = chordSegments.get(0).getChordName();
        for (ChordSegment chordHypo : chordSegments.get(0).getHypotheses()) {
            if (!currentChord.equals(chordHypo.getChordName())) {
                counter++;
            }
            currentChord = chordHypo.getChordName();
        }

        List<List<Float>> outData = new ArrayList<List<Float>>();
        for (int i = 0; i < counter; i++) {
            outData.add(new ArrayList<Float>());
        }

        for (ChordSegment chordSegment : chordSegments) {
            currentChord = "";//chordSegment.getChordName();
            int currentIndex = -1;
            for (ChordSegment chordHypo : chordSegment.getHypotheses()) {
                if (!currentChord.equals(chordHypo.getChordName())) {
                    currentIndex++;
                    currentChord = chordHypo.getChordName();
                    continue;
                }
                outData.get(currentIndex).add((float) chordHypo.getLogLikelihood());
            }
        }
        float[][] out = new float[counter][outData.get(0).size()];
        for (int i = 0; i < counter; i++) {
            for (int j = 0; j < out[0].length; j++) {
                out[i][j] = outData.get(i).get(j);
            }
        }
        return out;
    }


    protected double[] getBeatsForSong(String beatLabelsGroundTruthDir) {
        LabelsSource beatLabelSource = new LabelsSource(beatLabelsGroundTruthDir, true, "beatGT", Extensions.BEAT_EXTENSIONS);
        BeatStructure beatStructure = BeatStructure.getBeatStructure(beatLabelSource.getFilePathForSong(getSongName()));
        beatStructure.fixBeatStructure(getSongDuration());
        return beatStructure.getBeats();
    }


    public void refineHypothesesUsingBeats(String beatLabelsGroundTruthDir) {
        refineHypothesesUsingBeats(getBeatsForSong(beatLabelsGroundTruthDir));
    }


    /**
     * remove hierarchical structure and leave only one chord per frame
     *
     * @param beats beats
     */
    public void refineHypothesesUsingBeats(double beats[]) {
        List<ChordSegmentBeatSync> beatSyncChordSegments = getSegmentsBeatSync(getChordSegments(), beats);
        List<ChordSegment> outChordSegments = new ArrayList<ChordSegment>();
        for (int i = 0; i < beats.length - 1; i++) {
            List<String> intersections = new ArrayList<String>();
            for (ChordSegmentBeatSync beatSyncChordSegment : beatSyncChordSegments) {
                if (beatSyncChordSegment.getOnsetBeat() <= i && beatSyncChordSegment.getOffsetBeat() >= i + 1) {
                    intersections.add(beatSyncChordSegment.getChordName());
                }
            }
            String newLabel = getMostFrequentString(intersections);
            outChordSegments.add(new ChordSegment(beats[i], beats[i + 1], newLabel));
        }
        this.chordSegments = outChordSegments;
    }

    public static String getMostFrequentString(List<String> arrayList) {
        Map<String, Integer> hm = new LinkedHashMap<String, Integer>();
        int maxCount = 0;
        String maxString = "";
        for (String item : arrayList) {
            Integer count = hm.get(item);
            int currentCount = count == null ? 1 : count + 1;
            if (currentCount > maxCount) {
                maxCount = currentCount;
                maxString = item;
            }
            hm.put(item, currentCount);
        }
        return maxString;
    }


    public void refineHypothesesLeavingOrder(int order, String beatLabelsGroundTruthDir) {
        refineHypothesesLeavingOrder(order, getBeatsForSong(beatLabelsGroundTruthDir));
    }


    /**
     * Removes
     *
     * @param order
     * @param beats
     */
    public void refineHypothesesLeavingOrder(int order, double[] beats) {
        List<ChordSegmentBeatSync> beatSyncsSegments = getSegmentsBeatSync(getChordSegments(), beats);

        List<ChordSegment> outList = new ArrayList<ChordSegment>();
        for (ChordSegmentBeatSync chordSegmentBeatSync : beatSyncsSegments) {
            if (chordSegmentBeatSync.getDurationInBeats() == order) {
                outList.add(chordSegmentBeatSync);
            }
        }
        this.chordSegments = outList;
    }


    /**
     * Removes
     *
     * @param order
     * @param beats
     */
    public void refineHypothesesLeavingOrderWithoutIntersection(int order, double[] beats) {
        refineHypothesesLeavingOrder(order, beats);
        List<ChordSegment> refinedList = new ArrayList<ChordSegment>();
        //TODO add first and last beats
        for (int i = 0; i < chordSegments.size(); i++) {
            for (int j = i + 1; j < chordSegments.size(); j++) {
                if (chordSegments.get(i).intersects(chordSegments.get(j))) {
                    String chordSymbol = chordSegments.get(i).getLogLikelihood() > chordSegments.get(j).getLogLikelihood() ? chordSegments.get(i).getChordName() : chordSegments.get(j).getChordName();
                    ChordSegmentBeatSync outSegment = ((ChordSegmentBeatSync) chordSegments.get(i)).getIntersectionInBeatsSegment((ChordSegmentBeatSync) chordSegments.get(j));
                    refinedList.add(new ChordSegment(outSegment.getOnset(), outSegment.getOffset(), chordSymbol));
                }
            }
        }
        this.chordSegments = refinedList;
    }


    public static List<ChordSegmentBeatSync> getSegmentsBeatSync(List<ChordSegment> chordSegments, double[] beats) {
        List<ChordSegmentBeatSync> beatSyncsSegments = new ArrayList<ChordSegmentBeatSync>();
        for (ChordSegment cs : chordSegments) {
            beatSyncsSegments.add(new ChordSegmentBeatSync(cs, beats));
        }
        return beatSyncsSegments;
    }

    /**
     * Split chords into beat segments, so that each beat is one chord segment
     *
     * @param beats beats to sync chords with
     * @return new ChordStructure
     */
    public ChordStructure getSegmentsPerBeat(double[] beats) {
        List<ChordSegment> beatSyncsSegments = new ArrayList<ChordSegment>();
        int currentChordSegmentIndex = 0;
        ChordSegment currentChordSegment = chordSegments.get(currentChordSegmentIndex);
        ChordSegment currentBeatSegment;
        for (int i = 0; i < beats.length - 1; i++) {
            double currentBeat = beats[i];
            double nextBeat = beats[i + 1];
            currentBeatSegment = new ChordSegment(currentBeat, nextBeat, "N");

            while (nextBeat > currentChordSegment.getOnset() && !currentChordSegment.intersects(currentBeatSegment) && currentChordSegmentIndex < chordSegments.size() - 1) {
                currentChordSegment = chordSegments.get(++currentChordSegmentIndex);
            }
            //here we found first segment that intersects with our beat segment
            Map<ChordSegment, Float> intersectionMap = new HashMap<ChordSegment, Float>();
            do {
                intersectionMap.put(currentChordSegment, currentChordSegment.getIntersection(currentBeatSegment));
                if (currentChordSegmentIndex < chordSegments.size() - 1) {
                    currentChordSegment = chordSegments.get(++currentChordSegmentIndex);
                } else {
                    break;
                }
            } while (currentChordSegment.intersects(currentBeatSegment));
            currentChordSegment = chordSegments.get(--currentChordSegmentIndex);
            //Now we have all intersections in a map. Let's find the longest
            ChordSegment longestIntersection = null;
            float longestValue = -1;
            for (ChordSegment cs : intersectionMap.keySet()) {
                if (intersectionMap.get(cs) > longestValue) {
                    longestIntersection = cs;
                    longestValue = intersectionMap.get(cs);
                }
            }

            String label = longestIntersection != null ? longestIntersection.getLabel() : ChordType.NOT_A_CHORD.getName();
            beatSyncsSegments.add(new ChordSegment(currentBeat, nextBeat, label));
        }
        return new ChordStructure(beatSyncsSegments, getSongName());
    }


    public String getStringRepresentation() {
        StringBuilder builder = new StringBuilder();
        for (ChordSegment cs : chordSegments) {
            char letter = (char) (97 + cs.getNumberForSimplpeChord());
            builder.append(letter);
        }
        return builder.toString();
    }




    public void shiftSegmentsInTime(float shiftInsecs) {
        for (ChordSegment cs : chordSegments) {
            cs.setOnset(cs.getOnset() + shiftInsecs);
            cs.setOffset(cs.getOffset() + shiftInsecs);
        }
    }

    public void shiftKey(int semitones) {
        for (ChordSegment cs : chordSegments) {
            cs.shiftRoot(semitones);
        }
    }

    /**
     * Returns a copy of ChordStructure with shifted chords.
     *
     * @param semitones Shift (in semitones)
     * @return Shifted ChordStructure
     */
    public ChordStructure getShiftedChordStructure(int semitones) {
        List<ChordSegment> newSegments = new ArrayList<ChordSegment>(chordSegments.size());
        for (ChordSegment cs : chordSegments) {
            newSegments.add(new ChordSegment(cs.getOnset(), cs.getOffset(), cs.getChordNameOriginal()));
        }
        ChordStructure outChordStructure = new ChordStructure(newSegments, songName);
        outChordStructure.shiftKey(semitones);
        return outChordStructure;
    }

    public void correctStartEndTimings(float duration) {
        chordSegments.get(0).setOnset(0);
        chordSegments.get(chordSegments.size() - 1).setOffset(duration);
    }


    public List<ChordSegment> getChordSegments() {
        return chordSegments;
    }

    public double getSongDuration() {
        Collections.sort(chordSegments);
        return chordSegments.get(chordSegments.size() - 1).getOffset();
    }

    public int compareTo(ChordStructure o) {
        return this.songName.compareTo(o.getSongName());
    }
}
