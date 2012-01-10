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

package org.mart.crs.analysis.filter.PQMF;

import org.mart.crs.analysis.filter.FIR;
import org.mart.crs.analysis.filter.Filter;
import org.mart.crs.management.xml.XMLManager;
import org.w3c.dom.Element;

import static org.mart.crs.management.xml.Tags.CHANNEL_NUMBER_TAG;


/**
 * performs PQMF filtering of the input signal
 *
 * @version 1.0 Jun 8, 2009 6:53:19 PM
 * @author: Maksim Khadkevich
 */
public class PQMF implements Filter {

    public static final int ORDER = 512;

    public static final int CHANNELS_NUMBER = 32;

    public static float[] h_PQMF = PQMF_Coeff.h_Coeff;


    //Fields
    private int channelNumber;
    private Filter filter;

    public PQMF(int channelNumber, float samplingRate) {
        this.channelNumber = channelNumber;
        init();
    }


    public PQMF(Element rootElement) {
        this.channelNumber = Integer.valueOf(XMLManager.getStringData(rootElement, CHANNEL_NUMBER_TAG));
        init();
    }

    protected void init() {
        float[] h = calculateFilterImpulseResponce(channelNumber);
        this.filter = new FIR(h);
    }

    public float[] process(float[] samples) {
        return filter.process(samples);
    }

    public float[] process(float[] samples, int delay) {
        return filter.process(samples, delay);
    }


    private static float[] calculateFilterImpulseResponce(int channellNumber) {
        float[] h = new float[ORDER];
        for (int i = 0; i < ORDER; i++) {
            h[i] = h_PQMF[i] * (float) Math.cos((channellNumber + 0.5f) * (i - CHANNELS_NUMBER / 2) * Math.PI / CHANNELS_NUMBER);
        }
        return h;
    }


    public static void main(String[] args) {
        calculateFilterImpulseResponce(1);
        calculateFilterImpulseResponce(2);
    }
}
