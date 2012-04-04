package org.mart.crs.core;

import junit.framework.TestCase;
import org.mart.crs.core.pcp.PCP;
import org.mart.crs.management.features.FeatureVector;
import org.mart.crs.management.features.FeaturesManager;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.mart.crs.utils.helper.HelperData.*;

/**
 * User: Hut
 * Date: 28.06.2008
 * Time: 15:52:23
 */
public class TestReadHTKFormat extends TestCase {

    public void testReadHTK() {
        read_storeData("D:\\temp\\0.00_153.14_Hammer.chr");
    }


    private static void read_storeData(String fileNameToRead) {
        int nSamples, samplingPeriod;
        short sampSize, paramKind;
        System.out.println(fileNameToRead);
        try {
            FileInputStream inputStream = new FileInputStream(fileNameToRead);
            BufferedInputStream in = new BufferedInputStream(inputStream);
            nSamples = readInt(in);
            samplingPeriod = readInt(in);
            sampSize = readShort(in);
            paramKind = readShort(in);
            float[][] pcp = new float[(int)nSamples][(sampSize*8/ Float.SIZE)];
            for (int i = 0; i < (int)nSamples; i++) {
                for (int j = 0; j < (sampSize*8/ Float.SIZE); j++) {
                    pcp[i][j] = readFloat(in);
                }
            }

            //Now store read data to check identity
            for(int i = 1; i<2; i++){
                FeaturesManager.storeDataInHTKFormatStatic(fileNameToRead + "_" + i, new FeatureVector(PCP.shiftPCP(pcp, i), samplingPeriod));
            }


            in.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }


    }



}
