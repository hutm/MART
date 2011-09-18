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

package org.mart.crs.management.features;

import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.utils.helper.HelperFile;

/**
 * //TODO remove this class
 * @version 1.0 5/9/11 11:00 AM
 * @author: Hut
 */
public class FeaturesManagerKey extends FeaturesManager {

    /**
     * Recognized chord structure extracted on previous steps
     */
    protected ChordStructure chordStructure;


    public FeaturesManagerKey(String songFilePath, String outDirPath, boolean isForTraining, ExecParams execParams) {
        super(songFilePath, outDirPath, isForTraining, execParams);
        String songName = HelperFile.getPathForFileWithTheSameName(songFilePath, Settings.chordRecognizedDirectory, Settings.LABEL_EXT);
        this.chordStructure = new ChordStructure(songName);
    }


    /**
     * Remove segments with low likelihood and then save features
     * @param fileNameToStore
     * @param featureVector
     */
    protected void storeDataInHTKFormat(String fileNameToStore, FeatureVector featureVector) {

        storeDataInHTKFormatStatic(fileNameToStore, featureVector);
    }





}
