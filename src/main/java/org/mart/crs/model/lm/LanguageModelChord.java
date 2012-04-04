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

package org.mart.crs.model.lm;

import org.apache.log4j.Logger;
import org.mart.crs.config.Extensions;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.label.LabelsSource;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.management.label.chord.ChordType;
import org.mart.crs.management.label.chord.Root;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import static org.mart.crs.management.config.Configuration.NUMBER_OF_SEMITONES_IN_OCTAVE;
import static org.mart.crs.management.label.LabelsParser.END_SENTENCE;
import static org.mart.crs.management.label.LabelsParser.START_SENTENCE;

/**
 * @version 1.0 3/26/12 4:52 PM
 * @author: Hut
 */
public class LanguageModelChord {

    protected static Logger logger = CRSLogger.getLogger(LanguageModelChord.class);

    protected LabelsSource chordLabelsSource;
    protected String wavFileList;
    protected String outTextFilePath;


    public LanguageModelChord(String chordLabelsSoursePath, String wavFileList, String outTextFilePath) {
        this.chordLabelsSource = new LabelsSource(chordLabelsSoursePath, true, "chordGT", Extensions.LABEL_EXT);
        this.wavFileList = wavFileList;
        this.outTextFilePath = outTextFilePath;
    }


    public void process() {
        List<String> wavFilePaths = HelperFile.readLinesFromTextFile(wavFileList);
        logger.debug(String.format("read %d tokens from file %s", wavFilePaths.size(), wavFileList));

        List<String> chordText = createText(wavFilePaths);
        HelperFile.saveCollectionInFile(chordText, outTextFilePath);
    }

    protected List<String> createText(List<String> wavFilePaths) {
        List<String> outList = new ArrayList<String>();

        for (String wavFilePath : wavFilePaths) {
            StringBuilder textBuilder = new StringBuilder();

            String chordFilePath = chordLabelsSource.getFilePathForSong(wavFilePath);
            ChordStructure chordStructure = new ChordStructure(chordFilePath);

            for (ChordSegment cs : chordStructure.getChordSegments()) {
                if (cs.getChordType().equals(ChordType.UNKNOWN_CHORD) || (cs.getChordType().equals(ChordType.NOT_A_CHORD) && !Arrays.asList(ChordType.chordDictionary).contains(ChordType.NOT_A_CHORD))) {
                    //Skip this chord label
                } else {
                    textBuilder.append(cs.getChordName()).append(" ");
                }
            }
            addShiftedversions(textBuilder.toString(), outList);
        }
        return outList;
    }


    protected void addShiftedVersions(String chordString, List<String> outList, boolean includeStartEndSymbols) {
        //It is necessary to shift chord labels 12 times
        for (int i = 0; i < 12; i++) {
            String shiftedString = shiftChords(chordString, i);
            if (includeStartEndSymbols) {
                outList.add(String.format("%s %s %s", START_SENTENCE, additionalTransform(shiftedString), END_SENTENCE));
            } else {
                outList.add(additionalTransform(shiftedString));
            }
        }
    }

    protected void addShiftedversions(String chordString, List<String> outList) {
        addShiftedVersions(chordString, outList, true);
    }

    protected String additionalTransform(String shiftedLine){
        return shiftedLine;
    }
    

    public void createLanguageModel(int lmOrder, String lmFilePath) {
        String command = String.format("ngram-count -text %s -order %d -wbdiscount -lm %s", outTextFilePath, lmOrder, lmFilePath);
        Helper.execCmd(command);
    }


    /**
     * Shift of chord line by <tt>shift<tt> semitones
     *
     * @param inLine Input line containing chords symbols
     * @param shift  Shift in semitones
     * @return Shifted chord String
     */
    public static String shiftChords(String inLine, int shift) {
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
     * Shift of chord line from  keyFrom to  keyTo
     *
     * @param inLine  Input line containing chords symbols
     * @param keyFrom Base key
     * @param keyTo   Target key
     * @return Shifted chord String
     */
    public static String shiftChords(String inLine, Root keyFrom, Root keyTo) {
        int shift = (keyTo.ordinal() - keyFrom.ordinal() + NUMBER_OF_SEMITONES_IN_OCTAVE) % NUMBER_OF_SEMITONES_IN_OCTAVE;
        return LanguageModelChord.shiftChords(inLine, shift);
    }


}
