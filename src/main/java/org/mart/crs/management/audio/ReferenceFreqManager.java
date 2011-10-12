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

package org.mart.crs.management.audio;

import org.mart.crs.config.Settings;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


/**
 * @version 1.0 Feb 6, 2010 5:21:34 PM
 * @author: Maksim Khadkevich
 */
public class ReferenceFreqManager {

    protected Logger logger = CRSLogger.getLogger(ReferenceFreqManager.class);

    protected String refFreqFile;
                                                   
    protected Map<String, Float> refFreqMap = new HashMap<String, Float>();


    private static Map<String, ReferenceFreqManager> instances = new HashMap<String, ReferenceFreqManager>();

    public static ReferenceFreqManager getReferenceFreqManager(String refFreqFilePath) {
        if (!instances.containsKey(refFreqFilePath)) {
            instances.put(refFreqFilePath, new ReferenceFreqManager(refFreqFilePath));
        }
        return instances.get(refFreqFilePath);
    }


    public ReferenceFreqManager(String refFreqFile) {
        this.refFreqFile = refFreqFile;

        FileReader reader;
        Map<String, String> stringMap = null;
        if (refFreqFile == null || !HelperFile.getFile(refFreqFile).exists()) {
            logger.info(String.format("Reference frequency file %s does not exist", refFreqFile));
            return;
        }

        try {
            reader = new FileReader(refFreqFile);
            stringMap = HelperFile.readMapFromReader(reader);
            reader.close();
        } catch (FileNotFoundException e) {
            logger.info("Could not read from reference frequencies from file " + refFreqFile);
        } catch (IOException e) {
//            logger.error(Helper.getStackTrace(e));
        }
        if (stringMap != null) {
            for (String key : stringMap.keySet()) {
                try {
                    refFreqMap.put(key, Helper.parseFloat(stringMap.get(key)));
                } catch (Exception e) {
                    logger.debug("Problems with parsing float " + stringMap.get(key));
                }
            }
        }
    }

    public void addExtractedRefFreq(String filePath, float refFreq) {
        String shortFileName = HelperFile.getShortFileName(filePath);
        if (refFreqMap.containsKey(shortFileName)) {
            refFreqMap.remove(shortFileName);
        }
        refFreqMap.put(shortFileName, refFreq);
    }


    public boolean containSong(String song) {
        String songName = HelperFile.getShortFileName(song);
        return refFreqMap.containsKey(songName);
    }

    public void saveRefFreqs() {
        File outFile = HelperFile.getFile(refFreqFile);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
            for (String key : refFreqMap.keySet()) {
                writer.write(String.format("%s\t%8.4f", key, refFreqMap.get(key)));
                writer.write("\n");
                writer.flush();
            }
            writer.close();
        } catch (IOException e) {
            logger.error(String.format("Could not save reference frequencies in file %s", outFile));
            logger.error(Helper.getStackTrace(e));
        }
    }


    public float getRefFreqForSong(String song) {
        String songName = HelperFile.getShortFileName(song);
        if (Settings.isToUseRefFreq && refFreqMap.containsKey(songName)) {
            return refFreqMap.get(songName);
        }
        return Settings.REFERENCE_FREQUENCY;
    }

}


