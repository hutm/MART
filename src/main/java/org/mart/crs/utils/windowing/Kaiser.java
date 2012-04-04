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

package org.mart.crs.utils.windowing;


/**
 * @version 1.0 25-Aug-2010 11:30:35
 * @author: Hut
 */
public class Kaiser extends WindowFunction{

    public static float kaiserWindowAlpha = 16;
    
    @Override
    public float getFunction(int i, int offset, int width) {

        float alpha = kaiserWindowAlpha;
        int m = width;

        float I0alpha = I0(alpha);
        return I0(alpha * (float) Math.sqrt(1.0f - ((float) 2*i/m - 1) * ((float) 2*i / m - 1))) / I0alpha;
    }

      private static float I0(float x) {
        // zero order Bessel function of the first kind
        float eps = 1.0e-6f;   // accuracy parameter
        float fact = 1.0f;
        float x2 = 0.5f * x;
        float p = x2;
        float t = p * p;
        float s = 1.0f + t;
        for (int k = 2; t > eps; k++) {
            p *= x2;
            fact *= k;
            t = (p / fact) * (p / fact);
            s += t;
        }
        return s;
    }

    @Override
    public float getFunctionDerivative(int i, int offset, int width) {
        //TODO
        throw new RuntimeException("Derivative function is not implemented yet");
    }

}


