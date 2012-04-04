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

import org.apache.log4j.Logger;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * @version 1.0 Feb 6, 2010 5:21:34 PM
 * @author: Maksim Khadkevich
 */
@Deprecated
public class KeyManager {

    protected static Logger logger = CRSLogger.getLogger(KeyManager.class);

    protected String dataFileName;

    private Map<String, String> keysMap;
    private Map<String, String> keysMapGT;

    public void KeyManager(String keyDataFile, String keysGroudTruthFilePath) {
        keysMap = initializeMap(keyDataFile);
        keysMapGT = initializeMap(keysGroudTruthFilePath);
    }


    protected Map<String, String> initializeMap(String dataFileName) {
        this.dataFileName = dataFileName;

        FileReader reader;
        Map<String, String> destMap = new HashMap<String, String>();
        try {
            reader = new FileReader(dataFileName);
            destMap = HelperFile.readMapFromReader(reader);
            reader.close();
        } catch (FileNotFoundException e) {
            logger.info("Could not read keys from file " + dataFileName);
        } catch (IOException e) {
            logger.error(Helper.getStackTrace(e));
        }
        return destMap;
    }


    public boolean containKey(String song) {
        String songName = HelperFile.getShortFileName(song);
        return keysMap.containsKey(songName);
    }


    public String getKeyForSong(String song) {
        String songName = HelperFile.getShortFileName(song);
        if (keysMap.containsKey(songName)) {
            return keysMap.get(songName);
        }
        logger.warn("There is no information on key for file " + songName);
        return null;
    }

    public String getKeyForSong(String songName, boolean isGT) {
        if (!isGT) {
            return getKeyForSongDetected(songName);
        } else {
            return getKeyForSongGT(songName);
        }
    }


    private String getKeyForSongGT(String songName) {
        if (keysMapGT != null) {
            if (keysMapGT.containsKey(songName)) {
                return keysMapGT.get(songName);
            }
            int dotIndex = songName.lastIndexOf(".");
            if (dotIndex > 0 && keysMapGT.containsKey(songName.substring(0, dotIndex))) {
                return keysMapGT.get(songName.substring(0, dotIndex));
            }
        }
        return null;

    }

    protected String getKeyForSongDetected(String songName) {
        if (keysMap != null) {
            if (keysMap.containsKey(songName)) {
                return keysMap.get(songName);
            }
            int dotIndex = songName.lastIndexOf(".");
            if (dotIndex > 0 && keysMap.containsKey(songName.substring(0, dotIndex))) {
                return keysMap.get(songName.substring(0, dotIndex));
            }
        }
        logger.error("There is no information in Key Manager about keys");
        return null;
    }


}

