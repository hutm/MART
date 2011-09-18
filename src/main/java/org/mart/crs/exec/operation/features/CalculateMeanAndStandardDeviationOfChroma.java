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

import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.Operation;
import org.mart.crs.management.features.FeatureVector;
import org.mart.crs.management.features.FeaturesManager;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.HelperArrays;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 08-Oct-2010 02:27:00
 * @author: Hut
 */
public class CalculateMeanAndStandardDeviationOfChroma extends Operation {


    public CalculateMeanAndStandardDeviationOfChroma(String workingDir) {
        super(workingDir);
        
    }


    @Override
    public void initialize() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void operate() {
        File[] fileList = (new File(workingDir)).listFiles(new ExtensionFileFilter(Settings.CHROMA_EXT));
        List<float[][]> features = new ArrayList<float[][]>();
        for(File aFile:fileList){
            FeatureVector featureVector = FeaturesManager.readFeatureVector(aFile.getPath());
            features.add(featureVector.getVectors().get(0));
        }

        int length = 0;
        for(float[][] vectors: features){
            length += vectors.length;
        }

        float[][] arrayData = new float[length][];

        int counter = 0;
        for(float[][] vectors: features){
            for (int i = 0 ; i < vectors.length; i++) {
                arrayData[counter++] = vectors[i];
            }
        }

        float[][] meanAndDevData = HelperArrays.calculateMeanAndStandardDeviationVectors(arrayData);
        System.out.println("");
    }


    public static void main(String[] args) {
        CalculateMeanAndStandardDeviationOfChroma ofChroma = new CalculateMeanAndStandardDeviationOfChroma("d:\\dev\\CHORDS4\\chords\\reas_false_true_5_1024_0.50\\mean\\");
        ofChroma.operate();
    }


}
