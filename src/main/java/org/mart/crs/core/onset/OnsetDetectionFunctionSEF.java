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

package org.mart.crs.core.onset;

import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrum;
import org.mart.crs.utils.helper.HelperArrays;

/**
 * Onset detection function based on Spectral Energy Flux (SEF)
 * //TODO complete here everything
 * @version 1.0 1/14/11 2:30 PM
 * @author: Hut
 */
public class OnsetDetectionFunctionSEF extends OnsetDetectionFunction {


    public OnsetDetectionFunctionSEF(ReassignedSpectrum spectrum, float startFreq, float endFreq) {
        super(spectrum, startFreq, endFreq);
    }

    public OnsetDetectionFunctionSEF(ReassignedSpectrum spectrum) {
        super(spectrum);
    }

    public OnsetDetectionFunctionSEF() {
    }

    public void computeOnsetDetection() {
        float[][] magSpectrum = spectrum.getMagSpec();
        detectionFunction = new float[magSpectrum.length];
        for (int i = 0; i < magSpectrum.length - 1; i++) {
            for (int j = spectrum.freq2index(startFreq); j < spectrum.freq2index(endFreq); j++) {
                detectionFunction[i] += magSpectrum[i][j + 1] - magSpectrum[i][j];
            }
        }
        detectionFunction = HelperArrays.normalizeVector(detectionFunction);
    }

    /**
     * calculation of the differentiator filter according to Dvornikov's method
     * based on interpolating polynomials
     *
     * @param filter_order Filter order
     * @return filter coeffs
     */
    public float[] calculateDifferentiator(int filter_order) {
        if (filter_order / 2.0 != filter_order / 2) {
            throw new IllegalArgumentException("Filter order must be even");
        }
        float n = (float) Math.floor(filter_order - 1) / 2;

        int N = 21;
        float r1, r2;
        float[] alpha = new float[N];
        int k1 = 2;
        int k2 = N;
        for (int m = 1; m <= n; m++) {
            r1 = 1;
            for (int k = 1; k <= n; k++) {
                if (k == m) {
                    r2 = 1;
                } else {
                    r2 = 1 - (float) Math.pow(m / (k + 0.0f), 2);
                }
                r1 = r1 * r2;
            }

            r1 = 1.0f / (2 * r1 * m);
            alpha[k1 - 1] = -r1;
            alpha[k2 - 1] = r1;
            k2 = k2 - 1;
        }
        float[] alpha1 = new float[(int) Math.floor(n)];
        for (int i = alpha.length - alpha1.length; i < alpha.length; i++) {
            alpha1[i - (alpha.length - alpha1.length)] = alpha[i];
        }
        float[] out = new float[alpha1.length * 2 + 1];
        for (int i = 0; i < alpha1.length; i++) {
            out[i] = alpha1[i];
            out[out.length - 1 - i] = -alpha1[i];
        }
        return out;

    }


    public double[][] iirFilterDesign(double Fns) {
        double alpha = 0.015f;
        double beta = 0.075f;
        int d = 5;
        int c = 1;
        beta = beta * Fns;
        alpha = alpha * Fns;
        double[] num = new double[2];
        num[0] = d + c;
        num[1] =  -(c * Math.exp(-1 / beta) + d * Math.exp(-1 / alpha));
        double[] den = new double[3];
        den[0] = 1;
        den[1] =  -(Math.exp(-1 / alpha) + Math.exp(-1 / beta));
        den[2] =  Math.exp(-(1 / alpha + 1 / beta));
        double[][] out = new double[2][];
        out[0] = num;
        out[1] = den;
        return out;
    }

}
