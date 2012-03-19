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

import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;
import org.apache.log4j.Logger;
import org.imirsel.nema.model.NemaChord;
import org.imirsel.nema.model.util.ChordConversionUtil;

import java.io.Serializable;
import java.util.*;

import static org.mart.crs.config.Settings.NUMBER_OF_SEMITONES_IN_OCTAVE;
import static org.mart.crs.management.label.chord.ChordType.NOT_A_CHORD;
import static org.mart.crs.management.label.chord.ChordType.UNKNOWN_CHORD;

/**
 * User: Hut
 * Date: 10.05.2008
 * Time: 11:03:38
 * Data structure representing chord segment
 */
public class ChordSegment extends NemaChord implements Serializable {

    protected static Logger logger = CRSLogger.getLogger(ChordSegment.class);


    public static final int SEMITONE_NUMBER = 12;

    protected String chordNameOriginal;
    protected ChordType chordType;
    protected Root root;


    protected double logLikelihood;
    protected int durationInBeats;
    protected List<ChordSegment> hypotheses;


    public ChordSegment() {
        hypotheses = new ArrayList<ChordSegment>();
    }


    public ChordSegment(String chordString) {
        this();
        String[] comps = chordString.trim().split("\\s+");
        if (comps.length < 3) {
            logger.debug("Couldn't parse NemaChord from String: " + chordString);
        }
        double onset = Double.valueOf(comps[0]);
        double offset = Double.valueOf(comps[1]);


        this.onset = onset;
        this.offset = offset;
        this.notes = getNotesFromLabel(comps[2]);

        this.chordNameOriginal = comps[2];

        setRootAndType(notes);

        //Now parse loglikelihood
        if (comps.length > 3) {
            double logLikelihood = Helper.parseDouble(comps[3]);
            if (logLikelihood > Double.MIN_VALUE) {
                this.logLikelihood = logLikelihood;
            }
        }
    }


    protected int[] getNotesFromLabel(String chordLabel) {
        int[] notes = null;
        try {
            notes = ChordConversionUtil.getInstance().convertShorthandToNotenumbers(preprocessChordLabel(chordLabel));
        } catch (Exception e) {
            logger.debug("Couldn't parse NemaChord from Chord String: " + chordLabel + " from " + preprocessChordLabel(chordLabel));
            this.chordType = ChordType.UNKNOWN_CHORD;
        }
        if (notes == null) {
            try {
                notes = ChordConversionUtil.getInstance().convertIntervalsToNotenumbers(preprocessChordLabel(chordLabel));
            } catch (Exception e) {
                logger.debug("Couldn't parse NemaChord from Interval String: " + chordLabel);
                this.chordType = ChordType.UNKNOWN_CHORD;
                notes = ChordType.UNKNOWN_CHORD.getNotes();
            }
        }
        return notes;
    }


    public float[] getChordGram() {
        float[] output = new float[NUMBER_OF_SEMITONES_IN_OCTAVE * 2];

//        //If confidence is low, output zeros   //TODO remove this - it did not work
//        if(hypotheses.get(0).getLogLikelihood() < 0){
//            return output;
//        }

        for (ChordSegment hypo : hypotheses) {
            if (!hypo.hasRoot()) {
                continue;
            }
            int modality = 0;
            //Only Major and minor chirds are assumed
            if (hypo.getChordType().equals(ChordType.MINOR_CHORD)) {
                modality = 1;
            }
            int index = NUMBER_OF_SEMITONES_IN_OCTAVE * modality + hypo.getRoot().ordinal();
            output[index] = (float) hypo.getLogLikelihood();
        }


        float maxvalue = HelperArrays.findMax(output);
        for (int i = 0; i < output.length; i++) {
            output[i] = 1 / (float) Math.pow(1.3d, maxvalue - output[i]);
        }

        output = HelperArrays.normalizeVector(output);

//        for (int i = 0; i < output.length; i++) {
//            if (output[i] < 1) {
//                output[i] = 0;
//            }
//        }


        return output;
    }

    /**
     * Constructor
     *
     * @param startTime onset
     * @param endTime   offset
     * @param chordName chordName
     */
    public ChordSegment(double startTime, double endTime, String chordName) {
        this(startTime, endTime, chordName, 0f);
    }


    /**
     * Constructor
     *
     * @param startTime    onset
     * @param endTime      offset
     * @param chordName    chordName
     * @param logliklihood logliklihood
     */
    public ChordSegment(double startTime, double endTime, String chordName, double logliklihood) {
        this();
        this.onset = startTime;
        this.offset = endTime;
        this.chordNameOriginal = chordName;
        this.logLikelihood = logliklihood;
        this.notes = getNotesFromLabel(chordName);
        setRootAndType(this.notes);
    }


    protected void setRootAndType(int[] notes) {
        for (ChordType chordType : ChordType.values()) {
            int[] maptemplate = chordType.getNotes();
            if (isNoteArraysBelongToTheSameChord(maptemplate, notes)) {
                //The following condition checks if the correct inversion is still not found and continues search
                if ((chordType.equals(ChordType.MAJOR_6_CHORD) && this.chordNameOriginal.contains(ChordType.MINOR_7_CHORD.getOriginalName())) ||
                        (chordType.equals(ChordType.SUS4_CHORD) && this.chordNameOriginal.contains(ChordType.SUS2_CHORD.getOriginalName()))) {
                    continue;
                }
                this.chordType = chordType;
                if(this.chordType.getName().equals(UNKNOWN_CHORD.getName())){
                    this.chordType = UNKNOWN_CHORD;
                }
                if ((this.chordType != NOT_A_CHORD && this.chordType != UNKNOWN_CHORD)) {
                    this.root = Root.values()[getShift(maptemplate, notes)];
                }
                return;
            }
        }
        this.chordType = ChordType.UNKNOWN_CHORD;
    }


    public static boolean isNoteArraysBelongToTheSameChord(int[] notes1, int[] notes2) {
        for (int i = 0; i < SEMITONE_NUMBER; i++) {
            int[] shifted = shiftNotes(notes2, i);
            Arrays.sort(shifted);
            if (Arrays.equals(notes1, shifted)) {
                return true;
            }
        }
        return false;
    }

    public static int[] shiftNotes(int[] notes, int shift) {
        if (notes.length == 1 && notes[0] == 24) {
            return notes;
        }

        int out[] = new int[notes.length];
        for (int i = 0; i < notes.length; i++) {
            out[i] = (notes[i] + shift) % 12;
        }
        return out;
    }

    public static int getShift(int[] notes1, int[] notes2) {
        for (int i = 0; i < SEMITONE_NUMBER; i++) {
            int[] shifted = shiftNotes(notes2, i);
            Arrays.sort(shifted);
            if (Arrays.equals(notes1, shifted)) {
                return (SEMITONE_NUMBER - i) % 12;
            }
        }
        return -1;
    }


    public boolean isValidChord() {
        return !chordType.equals(UNKNOWN_CHORD);
    }

    public boolean hasRoot() {
        return !(chordType.equals(UNKNOWN_CHORD) || (chordType.equals(NOT_A_CHORD)));
    }


    public boolean intersects(ChordSegment cs1) {
        return onset < cs1.getOffset() && offset > cs1.getOnset();
    }

    public float getIntersection(ChordSegment cs2) {
        if (!intersects(cs2)) {
            return 0;
        } else {
            return (float) (Math.min(getOffset(), cs2.getOffset()) - Math.max(getOnset(), cs2.getOnset()));
        }
    }

    public static int[] getNotesWithoutDuplicates(int[] notes) {
        int[] temp = new int[notes.length];
        System.arraycopy(notes, 0, temp, 0, notes.length);
        int nonZeroCounter = temp.length;
        for (int i = 0; i < notes.length; i++) {
            for (int j = 0; j < i; j++) {
                if (notes[i] == notes[j]) {
                    temp[i] = 0;
                    nonZeroCounter--;
                    break;
                }
            }
        }
        int[] out = new int[nonZeroCounter];
        int index = 0;
        for (int i : temp) {
            if (i != 0) {
                out[index++] = i;
            }
        }
        Arrays.sort(out);
        return out;
    }

    public int[] getNotesWithoutDuplicates(){
        return getNotesWithoutDuplicates(this.notes);

    }


    public String getChordName() {
        if (chordType.equals(NOT_A_CHORD) || chordType.equals(ChordType.UNKNOWN_CHORD)) {
            return chordType.toString();
        } else {
            return String.format("%s%s", root, chordType);

        }
    }


    public static String preprocessChordLabel(String chordName) {
        String outChordLabel;
        if (chordName.length() <= 2 || chordName.indexOf(":") > 0 || chordName.indexOf("/") > 0 || chordName.equals(ChordType.NOT_A_CHORD.getName()) || chordName.equals(ChordType.UNKNOWN_CHORD.getName())) {
            outChordLabel = chordName;
        } else {
            if (chordName.charAt(1) == '#') {
                outChordLabel = chordName.substring(0, 2) + ":" + chordName.substring(2);
            } else {
                outChordLabel = chordName.substring(0, 1) + ":" + chordName.substring(1);
            }
        }
        outChordLabel = outChordLabel.replace(ChordType.SEVEN_CHORD.getOriginalName(), "7");
        outChordLabel = outChordLabel.replace(ChordType.NINE_CHORD.getOriginalName(), "9");
        return outChordLabel;
    }


    public void shiftRoot(int shift) {
        this.root = shiftRoot(this.root, shift);
    }


    public static Root shiftRoot(Root root, int shift) {
        int newIndex = root.ordinal() + shift;
        newIndex = HelperArrays.transformIntValueToBaseRange(newIndex, NUMBER_OF_SEMITONES_IN_OCTAVE);
        return Root.values()[newIndex];
    }


    /**
     * tostring() method
     *
     * @return
     */
    public String toString() {
        return String.format("%7.3f\t%7.3f\t%s\t%7.2f", onset, offset, preprocessChordLabel(getChordName()), logLikelihood);   //TODO remove preprocessChordLabel method
    }


    public boolean equals(Object obj) {
        ChordSegment cs;
        if (obj instanceof ChordSegment) {
            cs = (ChordSegment) obj;
            return this.onset == cs.onset && this.offset == cs.offset && this.notes.equals(cs.notes);
        }
        return false;
    }

    public double getDuration() {
        return offset - onset;
    }

    public void addHypothesis(ChordSegment chordSegmentHypothesis) {
        hypotheses.add(chordSegmentHypothesis);
    }

    public List<ChordSegment> getHypotheses() {
        return hypotheses;
    }

    public int getDurationInBeats() {
        return durationInBeats;
    }

    public void setDurationInBeats(int durationInBeats) {
        this.durationInBeats = durationInBeats;
    }

    public double getLogLikelihood() {
        return logLikelihood;
    }

    public void setLogLikelihood(float logLikelihood) {
        this.logLikelihood = logLikelihood;
    }

    public ChordType getChordType() {
        return chordType;
    }

    public Root getRoot() {
        return root;
    }

    public String getChordNameOriginal() {
        return chordNameOriginal;
    }
}

