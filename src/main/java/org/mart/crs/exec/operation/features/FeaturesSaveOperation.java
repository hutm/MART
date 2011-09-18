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

package org.mart.crs.exec.operation.features;

import org.mart.crs.config.ExecParams;
import org.mart.crs.exec.operation.AbstractCRSOperation;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.management.features.FeatureVector;
import org.mart.crs.management.features.FeaturesManager;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.util.Map;

/**
 * @version 1.0 08-Jul-2010 21:07:48
 * @author: Hut
 */
public class FeaturesSaveOperation extends AbstractCRSOperation {

    protected Map<Integer, FeatureVector> featuresMap;

    protected String resultsDir;


    public FeaturesSaveOperation(StageParameters stageParameters, ExecParams execParams, Map<Integer, FeatureVector> featuresMap) {
        super(stageParameters, execParams);
        this.featuresMap = featuresMap;
        this.resultsDir = String.format("%s/%s/%s", stageParameters.getWorkingDir(), StageParameters.DATA_DIR_NAME, StageParameters.RESULTS_DIR_NAME);
    }

    @Override
    public void operate() {
        HelperFile.deleteDirectory(this.resultsDir);

        String outDirPath = resultsDir + File.separator + StageParameters.FEATURES_DIR_NAME;
        File outDir = HelperFile.getFile(outDirPath);
        outDir.mkdirs();
        for(Integer hash:featuresMap.keySet()){
            FeatureVector featureVector = featuresMap.get(hash);
            File songDir = HelperFile.getFile(outDirPath + File.separator + hash);
            songDir.mkdirs();
            String filenameToSave = outDirPath + File.separator + hash + File.separator + featureVector.getFileNameToStoreTestVersion();
            FeaturesManager.storeDataInHTKFormatStatic(filenameToSave, featureVector);
        }
    }
}
