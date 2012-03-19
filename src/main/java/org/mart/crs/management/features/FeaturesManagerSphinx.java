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
import org.mart.crs.utils.helper.Helper;

import java.io.*;
import java.util.List;

import static org.mart.crs.utils.helper.HelperData.writeFloat;
import static org.mart.crs.utils.helper.HelperData.writeInt;
import static org.mart.crs.utils.helper.HelperData.writeShort;

/**
 * @version 1.0 2/23/12 2:37 PM
 * @author: Hut
 */
public class FeaturesManagerSphinx extends FeaturesManager {

    public FeaturesManagerSphinx(String songFilePath, String outDirPath, boolean isForTraining, ExecParams execParams) {
        super(songFilePath, outDirPath, isForTraining, execParams);
    }

    protected boolean isToSaveRotatedFeatures(){
        return true;
    }

    protected void storeDataInHTKFormat(String fileNameToStore, FeatureVector featureVector) {

        List<float[][]> vectors = featureVector.getVectors();

        if (vectors.size() == 0 || vectors.get(0).length == 0 || vectors.get(0)[0].length == 0) {
            return;
        }



        try {
            DataOutputStream outStream = new DataOutputStream(new FileOutputStream(fileNameToStore));


            int vectorSize = 0;
            for (float[][] vector : vectors) {
                vectorSize += vector[0].length;
            }

            int numberOfPoints = vectorSize * vectors.get(0).length;

            outStream.writeInt(numberOfPoints);


            //Write Pcp data
            for (int i = 0; i < vectors.get(0).length; i++) {
                for (float[][] vector : vectors) {
                    for (int j = 0; j < vector[0].length; j++) {
                        outStream.writeFloat(vector[i][j]);
                    }
                }
            }

            outStream.close();

        } catch (FileNotFoundException e) {
            logger.error("Cannot open stream to write data to file " + fileNameToStore);
            logger.error(Helper.getStackTrace(e));
        } catch (IOException e) {
            logger.error("Some strange error");
            logger.error(Helper.getStackTrace(e));
        }
    }


}
