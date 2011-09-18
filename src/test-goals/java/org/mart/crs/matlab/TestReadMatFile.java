package org.mart.crs.matlab;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLStructure;
import junit.framework.TestCase;

/**
 * @version 1.0 Mar 3, 2010 10:21:01 AM
 * @author: Maksim Khadkevich
 */
public class TestReadMatFile extends TestCase {


    public static void main(String[] args) {
        MatFileReader mfr = null;
        try {
            mfr = new MatFileReader("01_-_A_Hard_Day's_Night_chroma.mat");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        if (mfr != null) {
            double[][] data = ((MLDouble) mfr.getMLArray("f_chroma")).getArray();
            double[][] data_ = ((MLDouble)((MLStructure)((MLStructure)mfr.getMLArray("sideinfo")).getField("pitchSTMSP")).getField("featureRate")).getArray();

            System.out.println("data.length: " + data.length + ".");
            
        }
    }
}

