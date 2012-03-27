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

import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.models.htk.HTKResultsParser;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;
import org.mart.crs.utils.helper.HelperFile;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

import static org.mart.crs.config.Settings.LABEL_EXT;

/**
 * @version 1.0 3/29/11 11:38 AM
 * @author: Hut
 */
public class ChordStructure implements Comparable<ChordStructure> {


    protected static Logger logger = CRSLogger.getLogger(ChordStructure.class);

    protected List<ChordSegment> chordSegments;
    protected String songName;

    public ChordStructure(String labelFilePath) {
        this.chordSegments = new ArrayList<ChordSegment>();
        this.songName = HelperFile.getNameWithoutExtension(labelFilePath);
        parseChordLabels(labelFilePath);
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


    public void saveSegmentsInFile(String outDirectory, boolean isChordNameOriginal) {
        Collections.sort(chordSegments);
        try {
            FileWriter writer = new FileWriter(HelperFile.getFile(String.format("%s/%s%s", outDirectory, songName, Settings.LABEL_EXT)));
            for (ChordSegment cs : chordSegments) {
                if (!isChordNameOriginal) {
                    writer.write(cs + "\n");
                }     else{
                    writer.write(String.format("%7.5f\t%7.5f\t%s\n",cs.getOnset(), cs.getOffset(), cs.getChordNameOriginal()));
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


    public void saveHypothesesInFile(String outFilePath) {
        StringBuffer buffer = new StringBuffer();
        for (ChordSegment cs : chordSegments) {
            buffer.append(cs).append(" | ");
            for (ChordSegment hypo : cs.getHypotheses()) {
                buffer.append(hypo.getChordName()).append(" ").append(hypo.getLogLikelihood()).append(" ");
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
            if(newSegment.hasRoot()){
                chordSegments.add(newSegment);
            } else{
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
     * @deprecated needs refactoring
     */
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
        HTKResultsParser.storeResults(mapForStorage, outDirPath);
        LABEL_EXT = ".lab";

    }


    public ChordStructure transpose(int numberOfSemitones){
        List<ChordSegment> outList = new ArrayList<ChordSegment>();
        for(ChordSegment chordSegment:chordSegments){
            if (chordSegment.hasRoot()) {
                int rootIndex = HelperArrays.transformIntValueToBaseRange(chordSegment.getRoot().ordinal() + numberOfSemitones, 12);
                outList.add(new ChordSegment(chordSegment.getOnset(), chordSegment.getOffset(), Root.values()[rootIndex] + chordSegment.getChordType().getName()));
            } else{
                outList.add(chordSegment);
            }
        }
        return new ChordStructure(outList, getSongName());
    }
    
    public String getChordSequenceWithoutTimings(){
        StringBuilder builder = new StringBuilder();
        for(ChordSegment cs:chordSegments){
            builder.append(cs.getChordName()).append(" ");
        }
        return builder.toString();
    }


    public List<ChordSegment> getChordSegments() {
        return chordSegments;
    }

    public int compareTo(ChordStructure o) {
        return this.songName.compareTo(o.getSongName());
    }
}
