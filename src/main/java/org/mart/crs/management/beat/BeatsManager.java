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

package org.mart.crs.management.beat;

import at.ofai.music.beatroot.BeatRoot;
import org.apache.log4j.Logger;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.CRSThreadPoolExecutor;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.util.List;

import static org.mart.crs.config.Extensions.BEAT_EXT;
import static org.mart.crs.config.Extensions.WAV_EXT;
import static org.mart.crs.utils.helper.HelperFile.*;

/**
 * Extracts beats from audio files and saves to text file using BeatRoot software by S. Dixon
 *
 * @version 1.0 01.04.2009 19:18:25
 * @author: Maksim Khadkevich
 */
@Deprecated
public class BeatsManager {

    public static int threadsNumberForFeatureExtraction = 2;
    
    public static boolean isToDetectBeats;
    public static boolean forceReDetectBeats;

    protected static Logger logger = CRSLogger.getLogger(BeatsManager.class);

    public static final String BEATS_DIR = "D:/Beatles/beats";

    protected String beatsDir;


    public BeatsManager(String beatsDir) {
        this.beatsDir = beatsDir;
    }

    public BeatsManager() {
        this.beatsDir = BEATS_DIR;
    }


    public void batchExtractBeats(String fileOrDir, boolean isDirectory) {
        if (isDirectory) {
            String fileList = System.getProperty("java.io.tmpdir") + File.separator + "wavList";
            createFileList(fileOrDir, fileList, new ExtensionFileFilter(new String[]{WAV_EXT}), true);
            batchExtractBeats(fileList);
        } else {
            extractBeatsFromFile(fileOrDir);
        }
    }


    public void batchExtractBeats(String fileList) {
        File out = getFile(beatsDir);
        out.mkdirs();
        final List<String> filesToProcessList = HelperFile.readTokensFromTextFile(fileList, 1);

        CRSThreadPoolExecutor poolExecutor = new CRSThreadPoolExecutor(threadsNumberForFeatureExtraction);

        for (final String filePath : filesToProcessList) {
            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        extractBeatsFromFile(filePath);
                    } catch (Exception e1) {
                        logger.warn("File " + filePath + " was processed with errors");
                        logger.warn(e1.getStackTrace());
                    }
                }
            };
            poolExecutor.runTask(runnable);
        }
        poolExecutor.waitCompletedAndshutDown();

    }

    protected void extractBeatsFromFile(String filePath) {
        if (forceReDetectBeats) {
            extractBeatsForFile(filePath);
        } else {
            String beatFilePath = getPathForFileWithTheSameName(getFile(filePath).getName(), beatsDir, BEAT_EXT);
            if (beatFilePath == null) {
                extractBeatsForFile(filePath);
            }
        }
    }

    protected void extractBeatsForFile(String filePath) {
        File audioFile = getFile(filePath);
        if (!audioFile.exists()) {
            return;
        }
        String outFile, shortName;
        logger.info("Extracting beat structure for file " + filePath);
        shortName = ((getFile(filePath)).getName());
        getNameWithoutExtension(filePath);
        outFile = beatsDir + File.separator + getNameWithoutExtension(shortName) + BEAT_EXT;
        BeatRoot.main(new String[]{"-b", "-o", outFile, filePath});
    }

    public double[] getBeatsForWavFile(String filePath) {
        String fileName = getFile(filePath).getName();

        //First try to find already extracted beats
        String beatFilePath = getPathForFileWithTheSameName(fileName, beatsDir, BEAT_EXT);
        if (beatFilePath == null) {
            extractBeatsForFile(filePath);
            beatFilePath = getPathForFileWithTheSameName(fileName, beatsDir, BEAT_EXT);
            if (beatFilePath == null) {
                return null;
            }
        }
        return parseBeats(beatFilePath);
    }


    public static double[] parseBeats(String beatFilePath) {
        double[] beatsFloatValues;

        List<String> beats = HelperFile.readTokensFromTextFile(beatFilePath, 1);
        String[] beatsStingValues = new String[beats.size()];
        beats.toArray(beatsStingValues);
        beatsFloatValues = new double[beatsStingValues.length];
        for (int i = 0; i < beatsStingValues.length; i++) {
            beatsFloatValues[i] = Float.parseFloat(beatsStingValues[i].replaceAll("\\,", "."));
        }
        return beatsFloatValues;
    }


    public static void main(String[] args) {
        (new BeatsManager(BEATS_DIR)).batchExtractBeats("D:/temp/wav", true);
    }

}
