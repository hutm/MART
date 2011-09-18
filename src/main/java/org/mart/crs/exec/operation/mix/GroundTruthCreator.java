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

package org.mart.crs.exec.operation.mix;

import org.mart.crs.config.Settings;
import org.mart.crs.core.AudioReader;
import org.mart.crs.exec.operation.Operation;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 18-Oct-2010 15:22:55
 * @author: Hut
 */
public class GroundTruthCreator extends Operation {

    protected String inChordDir;
    protected String outLabelsDir;

    public GroundTruthCreator(String workingDir) {
        super(workingDir);
    }


    @Override
    public void initialize() {
        inChordDir = String.format("%s/%s", workingDir, "chords");
        outLabelsDir = String.format("%s/%s", workingDir, "labels");
        HelperFile.createDir(outLabelsDir);
    }

    @Override
    public void operate() {
        File[] inFiles = HelperFile.getFile(inChordDir).listFiles(new ExtensionFileFilter(Settings.WAV_EXT));
        for(File inFile:inFiles){
            String name = HelperFile.getNameWithoutExtension(inFile.getName());
            String chord = name.substring(0, name.indexOf("_"));
            AudioReader reader = new AudioReader(inFile.getPath());
            float endTime = reader.getDuration();
            List<ChordSegment> segments = new ArrayList<ChordSegment>();
            segments.add(new ChordSegment(0, endTime, chord));
            ChordStructure chordStructure = new ChordStructure(segments, name);
            chordStructure.saveSegmentsInFile(outLabelsDir);
        }
    }

    public static void main(String[] args) {
//        GroundTruthCreator creator = new GroundTruthCreator("d:\\work\\notesCmaj");
//        creator.initialize();
//        creator.operate();

        GroundTruthCreator creator1 = new GroundTruthCreator("d:\\work\\notes");
        creator1.initialize();
        creator1.operate();
    }

}
