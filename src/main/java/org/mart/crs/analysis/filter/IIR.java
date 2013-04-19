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

package org.mart.crs.analysis.filter;

import org.apache.log4j.Logger;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.xml.XMLManager;
import org.mart.crs.utils.helper.Helper;
import org.w3c.dom.Element;

import static org.mart.crs.management.xml.Tags.*;


public class IIR implements Filter {

    protected static Logger logger = CRSLogger.getLogger(IIR.class);

    /**
     * The a coefficients.
     */
    protected float[] a;
    /**
     * The b coefficients.
     */
    protected float[] b;

    protected int defaultDelayInSamples = 0;

    public IIR(float[] a, float[] b) {
        this.a = a;
        this.b = b;
    }


    public IIR(Element element){
        try {
            String aCoeff = XMLManager.getStringData(element, A_COEFF_TAG);
            this.a = Helper.getStringValuesAsFloats(aCoeff);
            String bCoeff = XMLManager.getStringData(element, B_COEFF_TAG);
            this.b = Helper.getStringValuesAsFloats(bCoeff);
            String delay = XMLManager.getStringData(element, DELAY_TAG);
            if (Helper.isInt(delay)) {
                this.defaultDelayInSamples = Integer.parseInt(delay);
            }
        } catch (Exception e) {
            logger.error(Helper.getStackTrace(e));
        }
    }


    public float[] process(float[] data, int delayInSamples) {
        float[] output = new float[data.length];
        for (int index = 0; index < data.length; index++) {
            float sum = 0;
            for (int j = index; j >= Math.max(0, index - a.length + 1); j--) {
                sum += a[index - j] * data[j];
            }
            for (int j = index - 1; j >= Math.max(0, index - b.length + 1); j--) {
                sum -= b[index - j] * output[j];
            }
            sum = b[0] * sum;
            if (index - delayInSamples >= 0) {
                output[index - delayInSamples] = sum;
            }
        }

        return output;

    }

    public float[] process(float[] input) {
        return process(input, defaultDelayInSamples);
    }


}