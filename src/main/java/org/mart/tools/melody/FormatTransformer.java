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

package org.mart.tools.melody;

import org.mart.crs.utils.helper.HelperFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import static org.mart.crs.utils.helper.HelperFile.saveCollectionInFile;

/**
 * @version 1.0 Feb 26, 2010 4:03:57 PM
 * @author: Maksim Khadkevich
 */
public class FormatTransformer {

    public void transformOutputFormat(String inFilePath, String outFilePath) {

        List<String> lines = HelperFile.readLinesFromTextFile(inFilePath);

        List<NoteFrame> outList = new ArrayList<NoteFrame>();


        NoteFrame currenNoteFrame = parseNoteFromLine(lines.get(0));
        NoteFrame noteFrame = null;
        for (String line : lines) {
            noteFrame = parseNoteFromLine(line);
            if (noteFrame != null) {
                if (noteFrame.getNote() != currenNoteFrame.getNote()) {
                    currenNoteFrame = getReadyFrame(currenNoteFrame, noteFrame, outList);
                }
            }
        }
        if (noteFrame != null) {
            currenNoteFrame = getReadyFrame(currenNoteFrame, noteFrame, outList);
        }

        List<String> textToSave = new ArrayList<String>();

        for (NoteFrame note : outList) {
            textToSave.add(note.toString());
        }

        saveCollectionInFile(textToSave, outFilePath, false);
    }

    private NoteFrame getReadyFrame(NoteFrame currenNoteFrame, NoteFrame noteFrame, List<NoteFrame> outList) {
        currenNoteFrame.setEndTime(noteFrame.getTime());
        outList.add(currenNoteFrame);
        currenNoteFrame = new NoteFrame(noteFrame.getTime(), noteFrame.getNote());
        return currenNoteFrame;
    }


    private NoteFrame parseNoteFromLine(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line);
        try {
            float time = Float.parseFloat(tokenizer.nextToken());
            int midiNote = Integer.parseInt(tokenizer.nextToken());
            return new NoteFrame(time, midiNote);
        } catch (Exception e) {

        }
        return null;
    }


    public static void main(String[] args) {
        FormatTransformer transformer = new FormatTransformer();
        Map<String, String> inFiles = HelperFile.readMapFromTextFile(args[0]);
        for(String s:inFiles.keySet()){
            System.out.println(String.format("%s > %s", s, inFiles.get(s)));
            transformer.transformOutputFormat(s, inFiles.get(s));
        }

//        transformer.transformOutputFormat("testFormat.txt", "testFormatOut.txt");
    }


}

class NoteFrame {

    private float time;
    private float endTime;
    private int note;

    NoteFrame(float time, int note) {
        this.time = time;
        this.note = note;
    }

    public float getTime() {
        return time;
    }

    public int getNote() {
        return note;
    }

    public float getEndTime() {
        return endTime;
    }

    public void setEndTime(float endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return String.format("%5.2f\t%5.2f\t%d", time, endTime, note);
    }
}
