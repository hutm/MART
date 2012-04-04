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

package org.mart.crs.management.midi;

import edu.columbia.ee.csmit.MidiKaraoke.PianoRoll;
import edu.columbia.ee.csmit.MidiKaraoke.PianoRollViewParser;
import edu.columbia.ee.csmit.MidiKaraoke.TimedNote;
import org.mart.crs.utils.helper.Helper;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @version 1.0 Nov 17, 2009 5:40:27 PM
 * @author: Maksim Khadkevich
 */
public class MidiManager {

    public static void readScoreAndSave(String fileNameIn, String fileNameOut) {
        try {
            File myMidiFile = new File(fileNameIn);
            Sequence mySeq = MidiSystem.getSequence(myMidiFile);

            //Now parse note-based information
            PianoRoll pianoRoll = PianoRollViewParser.parse(mySeq);
            edu.columbia.ee.csmit.MidiKaraoke.TimedNote notes[] = pianoRoll.getNotes();

            saveGroundTruth(notes, fileNameOut);

            long tmp = mySeq.getMicrosecondLength();
            double val = (double) tmp / 1000000D;
            System.out.println((new StringBuilder()).append("Sequence length: ").append(val).toString());
        }
        catch (Exception e) {
            System.out.println("Problem!");
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }


    public static void saveGroundTruth(TimedNote[] notes, String fileName) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

            List<NoteCRS> noteList = new ArrayList<NoteCRS>();
            for (int i = 0; i < notes.length; i++) {
                noteList.add(new NoteCRS(Helper.getFreqForMIDINote(notes[i].getNote()), (float) notes[i].getStartSeconds(), (float) notes[i].getDurationSeconds(), notes[i].getTrackNumber()));
            }
            Collections.sort(noteList);

            for (NoteCRS note : noteList) {
                writer.write(note.toString());
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

class NoteCRS implements Comparable {

    private float frequency;
    private float startTime;
    private float duration;
    private int track;

    public NoteCRS(float frequency, float startTime, float duration, int track) {
        this.frequency = frequency;
        this.startTime = startTime;
        this.duration = duration;
        this.track = track;
    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public float getStartTime() {
        return startTime;
    }

    public void setStartTime(float startTime) {
        this.startTime = startTime;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public int compareTo(Object o) {
        if (((NoteCRS) o).getStartTime() > this.getStartTime()) {
            return -1;
        } else {
            return 1;
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof NoteCRS) {
            NoteCRS note = (NoteCRS) obj;
            return this.getStartTime() == note.getStartTime();
        }
        return false;
    }

    public int getTrack() {
        return track;
    }

    public void setTrack(int track) {
        this.track = track;
    }

    public String toString() {
        return String.format("%5.3f %5.3f %5.3f %d %n", getFrequency(), getStartTime(), getDuration(), getTrack());
    }
}
