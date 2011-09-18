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

package org.mart.crs.exec.scenario.stage;

import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @version 1.0 3/6/11 11:31 AM
 * @author: Hut
 */
public class PersistenceManager {


    public static final String PERSISTENT_FILE_NAME = "persistence.txt";

    protected Map<String, String> persistentMap;

    protected Random random;

    protected String dirPath;

    protected String filePath;

    private static PersistenceManager instance;

    private static String persistentFileDirPath;

    public static PersistenceManager getInstance(){
        if(instance == null){
            if (persistentFileDirPath != null) {
                instance = new PersistenceManager(persistentFileDirPath);
            } else{
                throw new IllegalArgumentException(String.format("PersistentFileDirPath was not set. Cannot initialize PersisnentManager"));
            }
        }
        return instance;
    }


    private PersistenceManager(String dirPath) {
        this.dirPath = dirPath;
        initialize();
    }

    protected void initialize(){
        random = new Random();
        this.filePath = String.format("%s/%s", dirPath, PERSISTENT_FILE_NAME);
        try {
            File persistentFile = HelperFile.getFile(this.filePath);
            if (persistentFile.exists()) {
                this.persistentMap = HelperFile.readMapFromReader(new FileReader(persistentFile));
            } else {
                this.persistentMap = new HashMap<String, String>();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void setPersistentFileDirPath(String persistentFileDirPath) {
        PersistenceManager.persistentFileDirPath = persistentFileDirPath;
        HelperFile.createDir(persistentFileDirPath);
    }

    public String getMappingOfWorkingDirectory(String workingDirectory) {
        initialize();
        if (!persistentMap.containsKey(workingDirectory)) {
            persistentMap.put(workingDirectory, getRandomString());
            HelperFile.saveMapInTextFile(persistentMap, this.filePath);
        }
        return persistentMap.get(workingDirectory);
    }

    public String getMappedDirectoryNameFromNumber(String numberString){
        initialize();
        if(persistentMap.containsValue(numberString)){
            for(String key:persistentMap.keySet()){
                if(persistentMap.get(key).equals(numberString)){
                    return key;
                }
            }
        }
        return null;
    }


    protected String getRandomString() {
        String randomString = String.valueOf(Math.abs(random.nextInt()));
        if (persistentMap.containsValue(randomString)) {
            return getRandomString();
        }
        return randomString;
    }

}
