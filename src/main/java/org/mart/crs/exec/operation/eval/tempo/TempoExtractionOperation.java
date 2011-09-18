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

package org.mart.crs.exec.operation.eval.tempo;

import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.Operation;
import org.mart.crs.management.tempo.TempoExtractor;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

/**
 * @version 1.0 3/10/11 5:51 PM
 * @author: Hut
 */
public class TempoExtractionOperation extends Operation {

    protected String extractedTempoDirectory;


    public TempoExtractionOperation(String workingDir, String extractedTempoDirectory) {
        super(workingDir);
        this.extractedTempoDirectory = extractedTempoDirectory;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void operate() {
        File extractedDir = HelperFile.getFile(extractedTempoDirectory);
        File[] files = extractedDir.listFiles(new ExtensionFileFilter(Settings.BEAT_EXT));
        TempoExtractor tempoExtractor;
        Map<String, Float> extractedTempos = new TreeMap<String, Float>();
        for(File songName:files){
            tempoExtractor = new TempoExtractor(songName.getPath());
            float tempo = tempoExtractor.getTempo();
            extractedTempos.put(songName.getName().replaceAll("signal", "").replaceAll(Settings.BEAT_EXT, ""), tempo);
        }

        //save in the txt file
        String outFileName = String.format("%s%s", extractedDir, Settings.TXT_EXT);
        HelperFile.saveMapInTextFile(extractedTempos, outFileName);
    }






}
