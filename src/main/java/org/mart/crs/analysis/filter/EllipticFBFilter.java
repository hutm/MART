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

package org.mart.crs.analysis.filter;

import com.jamal.JamalException;
import com.jamal.client.MatlabClient;
import org.apache.log4j.Logger;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.xml.Tags;
import org.mart.crs.management.xml.XMLManager;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;
import org.w3c.dom.Element;


/**
 * @version 1.0 16-Apr-2010 17:50:47
 * @author: Maksim Khadkevich
 */
public class EllipticFBFilter implements Filter{

    protected static Logger logger = CRSLogger.getLogger(EllipticFBFilter.class);


    private String matlabFunction;
    private float midiNote;

    public EllipticFBFilter(String matlabFunction, int midiNote) {
        this.matlabFunction = matlabFunction;
        this.midiNote = midiNote;
    }

    public EllipticFBFilter(Element element) {
        try {
            this.matlabFunction = XMLManager.getStringData(element, Tags.MATLAB_FUNCTION_TAG);
            this.midiNote = Float.valueOf(XMLManager.getStringData(element, Tags.MIDI_NOTE_TAG));
        } catch (Exception e) {
            logger.error(Helper.getStackTrace(e));
        }
    }


    public float[] process(float[] samples) {

        float[] filteredSamples = new float[0];
        try {
            MatlabClient matlabClient = new MatlabClient();
            double[] samplesDouble = HelperArrays.getFloatAsDouble(samples);

            Object[] args = new Object[]{new Double(this.midiNote), samplesDouble};
            Object[] output = matlabClient.executeMatlabFunction(this.matlabFunction, args, 1);

            filteredSamples = HelperArrays.getDoubleAsFloat((double[]) output[0]);
        } catch (JamalException e) {
            logger.error(e.getMessage());
            logger.error(Helper.getStackTrace(e));
        }

        return filteredSamples;
    }


    public float[] process(float[] input, int delayInSamples) {
        logger.warn("Currently managing delay is not supported");
        return process(input);  //To change body of implemented methods use File | Settings | File Templates.
    }

}
