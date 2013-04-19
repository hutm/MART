/*
 * Copyright (c) 2008-2013 Maksim Khadkevich and Fondazione Bruno Kessler.
 *
 * This file is part of MART.
 * MART is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2, as published
 * by the Free Software Foundation.
 *
 * MART is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with MART; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.mart.crs.management.matlab;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLStructure;
import org.apache.log4j.Logger;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;

/**
 * @version 1.0 Mar 4, 2010 1:02:29 PM
 * @author: Maksim Khadkevich
 */
public class MatlabManager {

    protected static Logger logger = CRSLogger.getLogger(MatlabManager.class);

    private MatFileReader matFileReader;

    public MatlabManager(String filePath) {
        this.matFileReader = getMatFileReader(filePath);
    }

    public float[][] getStoredPCP() {
        double[][] data = ((MLDouble) matFileReader.getMLArray("f_chroma")).getArray();
        return HelperArrays.getDoubleAsFloat(data);
    }

    public float getSamplingRate(){
        double[][] data = ((MLDouble)((MLStructure)((MLStructure)matFileReader.getMLArray("sideinfo")).getField("pitchSTMSP")).getField("featureRate")).getArray();
        return (float)data[0][0];
    }


    private MatFileReader getMatFileReader(String filePath) {
        MatFileReader mfr = null;
        try {
            mfr = new MatFileReader(filePath);
        } catch (java.io.IOException e) {
            logger.error(Helper.getStackTrace(e));
        }
        if (mfr == null) {
            logger.warn(String.format("Could not read matlab data from file %s", filePath));
        }
        return mfr;
    }


}
