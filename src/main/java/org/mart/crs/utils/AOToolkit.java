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

package org.mart.crs.utils;

public class AOToolkit {

    //******** real <-> dB (amplitude) conversion ********

    /**
     * real to dB
     */
    public static float todB(float r) {
        return (float) Math.max((8.685890 * Math.log(Math.abs(r))), -200);
    }


    /**
     * dB to real
     */
    public static float fromdB(float dB) {
        return (float) (Math.exp(dB / 8.685890));
    }


    //******** real <-> halftone factor conversion ********

    private static final double halfToneFactor = 1. / Math.log(Math.pow(2., 1. / 12.));


    /**
     * real to halftone
     */
    public static float toHalfTone(float r) {
        return (float) (halfToneFactor * Math.log(Math.abs(r)));
    }


    /**
     * halftone to real
     */
    public static float fromHalfTone(float halfTone) {
        return (float) (Math.exp(halfTone / halfToneFactor));
    }


    //******** real <-> octave factor conversion ********

    private static final double octaveFactor = 1. / Math.log(2);


    /**
     * real to octave
     */
    public static float toOctave(float r) {
        return (float) (octaveFactor * Math.log(Math.abs(r)));
    }


    /**
     * octave to real
     */
    public static float fromOctave(float octave) {
        return (float) (Math.exp(octave / octaveFactor));
    }


    //******** interpolation of aequidistant data samples ********


    /**
     * zeroth order interpolation
     *
     * @param data seen as circular buffer when array out of bounds
     */
    public static float interpolate0(float[] data, float index) {
        try {
            return data[((int) index) % data.length];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }


    /**
     * first order interpolation
     *
     * @param data seen as circular buffer when array out of bounds
     */
    public static float interpolate1(float[] data, float index) {
        try {
            int ip = ((int) index);
            float fp = index - ip;

            return data[ip % data.length] * (1 - fp) + data[(ip + 1) % data.length] * fp;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }

    //ayy  (a comment of moustique...)

    /**
     * second order interpolation
     *
     * @param data seen as circular buffer when array out of bounds
     */
    public static float interpolate2(float[] data, float index) {
        try {
            //Newton's 2nd order interpolation
            int ip = ((int) index);
            float fp = index - ip;

            float d0 = data[ip % data.length];
            float d1 = data[(ip + 1) % data.length];
            float d2 = data[(ip + 2) % data.length];

            float a0 = d0;
            float a1 = d1 - d0;
            float a2 = (d2 - d1 - a1) / 2;

            return a0 + a1 * fp + a2 * fp * (fp - 1);

        }
        catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }


    /**
     * third order interpolation
     *
     * @param data seen as circular buffer when array out of bounds
     */
    public static float interpolate3(float[] data, float index) {
        try {
            //cubic hermite interpolation
            int ip = (int) index;
            float fp = index - ip;

            float dm1 = data[(ip - 1) % data.length];
            float d0 = data[ip % data.length];
            float d1 = data[(ip + 1) % data.length];
            float d2 = data[(ip + 2) % data.length];

            float a = (3 * (d0 - d1) - dm1 + d2) / 2;
            float b = 2 * d1 + dm1 - (5 * d0 + d2) / 2;
            float c = (d1 - dm1) / 2;

            return (((a * fp) + b) * fp + c) * fp + data[ip % data.length];


/*
			//3rd order spline interpolation
			int ip = (int)index;
			float fp = index - ip;
			float l1 = data[ip];
			float l0 = data[ip+1];
			float h0 = data[ip+2];
			float h1 = data[ip]+3;

			return 	l0 +
 		   			.5f *
						fp * (h0 - l1 +
						fp * (h0 + l0 * (-2) + l1 +
						fp * ((h0 - l0) * 9 + (l1 - h1) * 3 +
						fp * ((l0 - h0) * 15 + (h1 - l1) * 5 +
						fp * ((h0 - l0) * 6 + (l1 - h1) * 2)))));

*/
        }
        catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }


    //******** interpolation of variable distance data samples ********


    /**
     * zeroth order interpolation. the x-values must be ordered in rising manner.
     * x- and y-length must be equal.
     */
    public static float interpolate0(float[] x, float[] y, float index) {
        //min value ?
        if (index < x[0]) {
            return y[0];
        }

        //search segment that contains the index...
        for (int i = 1; i < x.length; i++) {
            //inside segment value...
            if (x[i] > index) {
                return y[i - 1];
            }
        }
        //max value...
        return y[y.length - 1];
    }


    /**
     * first order interpolation. the x-values must be ordered in rising manner.
     * x- and y-length must be equal.
     */
    public static float interpolate1(float[] x, float[] y, float index) {
        float dx, dy;

        //min value ?
        if (index < x[0]) {
            return y[0];
        }

        //search segment that contains the index...
        for (int i = 1; i < x.length; i++) {
            //inside segment value...
            if (x[i] > index) {
                dx = x[i] - x[i - 1];
                dy = y[i] - y[i - 1];
                return y[i - 1] + ((index - x[i - 1]) / dx * dy);
            }
        }
        //max value...
        return y[y.length - 1];
    }


    /**
     * second order interpolation. the x-values must be ordered in rising manner.
     * x- and y-length must be equal.
     */
    public static float interpolate2(float[] x, float[] y, float index) {
        //search segment that contains the index...
        int i = 0;
        for (i = x.length - 1; i >= 0; i--) {
            if (x[i] < index)
                break;
        }
        if (i > x.length - 3) {
            i = x.length - 3;
        }
        if (i < 0) {
            i = 0;
        }


        //Newton's 2nd order interpolation
        float x0, x1, x2, y0, y1, y2, a10, a11, a20;
        x0 = x1 = x2 = y0 = y1 = y2 = a10 = a11 = a20 = 0;
        try {
            x0 = x[i];
            x1 = x[i + 1];
            x2 = x[i + 2];
            y0 = y[i];
            y1 = y[i + 1];
            y2 = y[i + 2];
        }
        catch (Exception e) {
        }
        a10 = (y1 - y0) / (x1 - x0);
        a11 = (y2 - y1) / (x2 - x1);
        a20 = (a11 - a10) / (x2 - x0);
        return y0 + a10 * (index - x0) + a20 * (index - x0) * (index - x1);
    }


    /**
     * third order interpolation. the x-values must be ordered in rising manner.
     * x- and y-length must be equal.
     */
    public static float interpolate3(float[] x, float[] y, float index) {
        //search segment that contains the index...
        int i = 0;
        for (i = x.length - 1; i >= 0; i--) {
            if (x[i] < index)
                break;
        }
        i--;
        if (i > x.length - 4) {
            i = x.length - 4;
        }
        if (i < 0) {
            i = 0;
        }


        //Newton's 3rd order interpolation
        float x0, x1, x2, x3, y0, y1, y2, y3, a10, a11, a12, a20, a21, a30;
        x0 = x1 = x2 = x3 = y0 = y1 = y2 = y3 = a10 = a11 = a12 = a20 = a21 = a30 = 0;
        try {
            x0 = x[i];
            x1 = x[i + 1];
            x2 = x[i + 2];
            x3 = x[i + 3];
            y0 = y[i];
            y1 = y[i + 1];
            y2 = y[i + 2];
            y3 = y[i + 3];
        }
        catch (Exception e) {
        }
        a10 = (y1 - y0) / (x1 - x0);
        a11 = (y2 - y1) / (x2 - x1);
        a12 = (y3 - y2) / (x3 - x2);
        a20 = (a11 - a10) / (x2 - x0);
        a21 = (a12 - a11) / (x3 - x1);
        a30 = (a21 - a20) / (x3 - x0);
        return y0 +
                a10 * (index - x0) +
                a20 * (index - x0) * (index - x1) +
                a30 * (index - x0) * (index - x1) * (index - x2);
    }


    //******** cubic spline interpolation ********


    public static AOSpline createSpline() {
        return new AOSpline();
    }


    //******** convolution, FIR filter ********


    /**
     * convolution
     */
    public static float convolve(float[] data, int index, float[] kernel, int m) {
        float y = 0;
        try {
            for (int i = 0; i < m; i++) {
                y += data[index - i] * kernel[i];
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
        }
        return y;
    }


    /**
     * creates a lowpass filter kernel. WARNING: right-to-left processing
     * required.
     */
    public static void setLowPassKernel(float[] kernel, int m, float fc) {
        int m2 = m / 2;
        float summ = 0;

        for (int i = 0; i < m; i++) {
            //sinc...
            if (i == m2) {
                kernel[i] = 2 * (float) Math.PI * fc;
            } else {
                kernel[i] = (float) Math.sin(2 * (float) Math.PI * fc * (i - m2)) / (i - m2);
            }
            //blackman window...
            kernel[i] *= (0.42 -
                    0.5 * Math.cos(2 * Math.PI * i / m) +
                    0.08 * Math.cos(4 * Math.PI * i / m));
            summ += kernel[i];
        }
        //normalize...
        for (int i = 0; i < m; i++) {
            kernel[i] /= summ;
        }
    }


    /**
     * creates a highpass filter kernel. WARNING: right-to-left processing
     * required. this kernel doesn't work correctly yet!!!
     */
    public static void setHighPassKernel(float[] kernel, int m, float fc) {
        int m2 = m / 2;
        float summ = 0;

        for (int i = 0; i < m; i++) {
            //sinc...
            if (i != m2) {
                kernel[i] = (float) Math.sin(2 * (float) Math.PI * fc * (i - m2)) / (i - m2);
                //blackman window...
                kernel[i] *= (0.42 -
                        0.5 * Math.cos(2 * Math.PI * i / m) +
                        0.08 * Math.cos(4 * Math.PI * i / m));
                summ += kernel[i];
            }
        }

        //normalize...
        for (int i = 0; i < m; i++) {
            kernel[i] /= -summ;
        }

        kernel[m2] = 1;
    }


    //******** IIR filters ********


    /**
     * add a band of x to y. offset is considered in x only.
     */
    public static void addIirBandPass(float[] x, float[] y, int o, int l,
                                      float f, float q, float gain) {
        //coefficients
        float theta = (float) (2. * Math.PI * f);
        float beta = (float) (0.5 * (1. - Math.tan(theta / (2. * q))) /
                (1. + Math.tan(theta / (2. * q))));
        float alpha = (float) ((0.5 - beta) / 2);
        float gamma = (float) ((0.5 + beta) * Math.cos(theta));

        //x
        float x0, x1, x2;
        x0 = x1 = x2 = 0;

        //y
        float y0, y1, y2;
        y0 = y1 = y2 = 0;

        //y
        for (int i = o; i < (o + l); i++) {
            x2 = x1;
            x1 = x0;
            x0 = x[i];

            y2 = y1;
            y1 = y0;
            y0 = 2 * (alpha * (x0 - x2) + gamma * y1 - beta * y2);

            y[i - o] += gain * y0;
        }
    }


    /**
     * processes high pass to data.
     */
    public static void setIirHighPass(float[] data, int o, int l,
                                      float dry, float wet, float freq) {
        //coefficients
        float omega = (float) (2. * Math.PI * freq);
        float cs = (float) Math.cos(omega);
        float alpha = (float) (Math.sin(omega) / 2.);

        float a0 = 1 + alpha;
        float b0 = (1 + cs) / 2 / a0;
        float b1 = -(1 + cs) / a0;
        float b2 = b0;
        float a1 = -2 * cs / a0;
        float a2 = (1 - alpha) / a0;

        //x
        float x0, x1, x2;
        x0 = x1 = x2 = 0;

        //y
        float y0, y1, y2;
        y0 = y1 = y2 = 0;

        //y
        for (int i = o; i < (o + l); i++) {
            x2 = x1;
            x1 = x0;
            x0 = data[i];

            y2 = y1;
            y1 = y0;
            y0 = b0 * x0 + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2;

            data[i] = data[i] * dry + y0 * wet;
        }
    }


    /**
     * processes low pass to data.
     */
    public static void setIirLowPass(float[] data, int o, int l,
                                     float dry, float wet, float freq) {
        //coefficients
        float omega = (float) (2. * Math.PI * freq);
        float cs = (float) Math.cos(omega);
        float alpha = (float) (Math.sin(omega) / 2.);

        float a0 = 1 + alpha;
        float b0 = (1 - cs) / 2 / a0;
        float b1 = (1 - cs) / a0;
        float b2 = b0;
        float a1 = -2 * cs / a0;
        float a2 = (1 - alpha) / a0;

        //x
        float x0, x1, x2;
        x0 = x1 = x2 = 0;

        //y
        float y0, y1, y2;
        y0 = y1 = y2 = 0;

        //y
        for (int i = o; i < (o + l); i++) {
            x2 = x1;
            x1 = x0;
            x0 = data[i];

            y2 = y1;
            y1 = y0;
            y0 = b0 * x0 + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2;

            data[i] = data[i] * dry + y0 * wet;
        }
    }


    /**
     * processes notch filter to data.
     */
    public static void setIirNotch(float[] data, int o, int l,
                                   float freq, float q, float gain) {
        //coefficients
        float omega = (float) (2. * Math.PI * freq);
        float cs = (float) Math.cos(omega);
        float alpha = (float) (Math.sin(omega) / (2 * q));

        float a0 = 1 + alpha;
        float b0 = 1 / a0;
        float b1 = -2 * cs / a0;
        float b2 = b0;
        float a1 = -2 * cs / a0;
        float a2 = (1 - alpha) / a0;

        //x
        float x0, x1, x2;
        x0 = x1 = x2 = 0;

        //y
        float y0, y1, y2;
        y0 = y1 = y2 = 0;

        //y
        for (int i = o; i < (o + l); i++) {
            x2 = x1;
            x1 = x0;
            x0 = data[i];

            y2 = y1;
            y1 = y0;
            y0 = b0 * x0 + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2;

            data[i] = y0 * gain;
        }
    }


    //******** FFT ********


    /**
     * performs complex FFT
     */
    public static void complexFft(float[] re, float[] im) {
        baseFft(false, re, im);
    }


    /**
     * performs complex IFFT
     */
    public static void complexIfft(float[] re, float[] im) {
        baseFft(true, re, im);
    }


    /**
     * performs complex FFT/IFFT
     */
    private static void baseFft(boolean isInvers, float[] re, float[] im) {
        int n = re.length;
        float scale = (float) Math.sqrt(1.0f / n);
        int sign;

        if (isInvers) {
            sign = -1;
        } else {
            sign = 1;
        }

        int i, j;
        for (i = j = 0; i < n; ++i) {
            if (j >= i) {
                float tempr = re[j] * scale;
                float tempi = im[j] * scale;
                re[j] = re[i] * scale;
                im[j] = im[i] * scale;
                re[i] = tempr;
                im[i] = tempi;
            }
            int m = n / 2;
            while (m >= 1 && j >= m) {
                j -= m;
                m /= 2;
            }
            j += m;
        }

        int mmax, istep;
        //GProgressViewer.entrySubProgress();
        for (mmax = 1, istep = 2 * mmax; mmax < n; mmax = istep, istep = 2 * mmax) {
            //GProgressViewer.setProgress(mmax * 100 / n);
            float delta = (float) sign * (float) Math.PI / (float) mmax;
            for (int m = 0; m < mmax; ++m) {
                float w = (float) m * delta;
                float wr = (float) Math.cos(w);
                float wi = (float) Math.sin(w);
                for (i = m; i < n; i += istep) {
                    j = i + mmax;
                    float tr = wr * re[j] - wi * im[j];
                    float ti = wr * im[j] + wi * re[j];
                    re[j] = re[i] - tr;
                    im[j] = im[i] - ti;
                    re[i] += tr;
                    im[i] += ti;
                }
            }
            mmax = istep;
        }
        //GProgressViewer.exitSubProgress();


        //adaptor...
        if (isInvers) {
        } else {
            for (int k = 0; k < n; k++) {
                re[k] *= 2;
                im[k] *= 2;
            }
            for (int k = n / 2; k < n; k++) {
                re[k] = 0;
                im[k] = 0;
            }
        }
    }


    /**
     * performs power spectrum FFT
     */
    public static void powerFft(float[] tRe, float[] pRe) {
    }


    /**
     * applies a window to the given data
     */
    public static void applyRectangularWindow(float[] data, int width) {
        for (int i = 0; i < data.length; i++) {
            if (i < width) {
            } else {
                data[i] = 0;
            }
        }
    }


    /**
     * applies a window to the given data
     */
    public static void applyHammingWindow(float[] data, int width) {
        for (int i = 0; i < data.length; i++) {
            if (i < width) {
                data[i] *= (float) (0.54 - 0.46 * Math.cos(2 * Math.PI * i / width));
            } else {
                data[i] = 0;
            }
        }
    }


    /**
     * applies a window to the given data
     */
    public static void applyBlackmanWindow(float[] data, int width) {
        for (int i = 0; i < data.length; i++) {
            if (i < width) {
                double a = 2 * Math.PI * i / width;
                data[i] *= (float) (0.42 - 0.5 * Math.cos(a) + 0.08 * Math.cos(2 * a));
            } else {
                data[i] = 0;
            }
        }
    }


    /**
     * applies a window to the given data
     */
    public static void applyFlattopWindow(float[] data, int width) {
        //setLowPassKernel(data, );
    }


    //******** correlation... *********


    /**
     * performs autocorrelation
     */
    public void autoCorrelation(float[] source, float[] result) {

    }


    //******** averages... ********


    /**
     * average
     */
    public static float average(float[] s, int o, int l) {
        float a = 0;
        for (int i = 0; i < l; i++) {
            a += s[o + i];
        }
        a /= l;

        return a;
    }


    /**
     * RMS average
     */
    public static float rmsAverage(float[] s, int o, int l) {
        float a = 0;
        for (int i = 0; i < l; i++) {
            a += s[o + i] * s[o + i];
        }
        a = (float) Math.sqrt(a / l);

        return a;
    }


    //******** moving averages... ********


    /**
     * moving average
     */
    public static float movingAverage(float average, float newData, float weight) {
        return ((average * weight) + newData) / (weight + 1);
    }


    /**
     * moving RMS average
     */
    public static float movingRmsAverage(float average, float newData, float weight) {
        return (float) Math.sqrt(((average * average * weight) + (newData * newData)) /
                (weight + 1));
    }


    //******** smoothing curves... ********


    /**
     * smooth using mean algorithm
     */
    public static void smooth(float s[], int offset, int length, float weight) {
        //smooth freehand drawing in both directions, to avoid x-shift...
        float av = s[offset];
        for (int i = offset; i < offset + length; i++) {
            av = AOToolkit.movingAverage(av, s[i], weight);
            s[i] = av;
        }
        av = s[offset + length - 1];
        for (int i = offset + length - 1; i >= offset; i--) {
            av = AOToolkit.movingAverage(av, s[i], weight);
            s[i] = av;
        }
    }


    /**
     * smooth using RMS algorithm
     */
    public static void smoothRms(float s[], int offset, int length, float weight) {
        //smooth freehand drawing in both directions, to avoid x-shift...
        float av = s[offset];
        for (int i = offset; i < offset + length; i++) {
            av = AOToolkit.movingRmsAverage(av, s[i], weight);
            s[i] = av;
        }
        av = s[offset + length - 1];
        for (int i = offset + length - 1; i >= offset; i--) {
            av = AOToolkit.movingRmsAverage(av, s[i], weight);
            s[i] = av;
        }
    }


    //******** finder... ********


    /**
     * return the index of the most positive peak
     */
    public static int getPositivePeakIndex(float[] s, int offset, int length) {
        float p = Float.MIN_VALUE;
        int index = 0;

        for (int i = 0; i < length; i++) {
            if (s[offset + i] > p) {
                p = s[offset + i];
                index = offset + i;
            }
        }
        return index;
    }


    /**
     * return the index of the most negative peak
     */
    public static int getNegativePeakIndex(float[] s, int offset, int length) {
        float p = Float.MAX_VALUE;
        int index = 0;

        for (int i = 0; i < length; i++) {
            if (s[offset + i] < p) {
                p = s[offset + i];
                index = offset + i;
            }
        }
        return index;
    }


    /**
     * return the nearest positive peak index from center
     * or -1 if no peak found.
     * the search ends after reaching length in both directions.
     */
    public static int getNearestPositivePeakIndex(float[] s, int center, int length) {
        //range check...
        if (center < 0) {
            center = 0;
        } else if (center >= s.length) {
            center = s.length - 1;
        }

        float peakValue = Float.MIN_VALUE;
        int peakIndex = center;
        int x;

        //search...
        for (int i = 0; i < length; i++) {
            //right side...
            x = center + i;

            if (x < s.length) {
                if (s[x] > peakValue) {
                    peakValue = s[x];
                    peakIndex = x;
                }
            }

            //left side...
            x = center - i;

            if (x >= 0) {
                if (s[x] > peakValue) {
                    peakValue = s[x];
                    peakIndex = x;
                }
            }
        }

        //is really peak ?
        if ((s[peakIndex + 1] <= s[peakIndex]) && (s[peakIndex - 1] <= s[peakIndex])) {
            return peakIndex;
        } else {
            return center;
        }
    }


    /**
     * return the nearest negative peak index from center
     * or -1 if no peak found.
     * the search ends after reaching length in both directions.
     */
    public static int getNearestNegativePeakIndex(float[] s, int center, int length) {
        //range check...
        if (center < 0) {
            center = 0;
        } else if (center >= s.length) {
            center = s.length - 1;
        }

        float peakValue = Float.MAX_VALUE;
        int peakIndex = center;
        int x;

        //search...
        for (int i = 0; i < length; i++) {
            //right side...
            x = center + i;

            if (x < s.length) {
                if (s[x] < peakValue) {
                    peakValue = s[x];
                    peakIndex = x;
                }
            }

            //left side...
            x = center - i;

            if (x >= 0) {
                if (s[x] < peakValue) {
                    peakValue = s[x];
                    peakIndex = x;
                }
            }
        }

        //is really peak ?
        if ((s[peakIndex + 1] >= s[peakIndex]) && (s[peakIndex - 1] >= s[peakIndex])) {
            return peakIndex;
        } else {
            return center;
        }
    }


    /**
     * return the nearest zero-cross index from center or -1 if no zero-cross found.
     * the search ends after reaching length in both directions.
     */
    public static int getNearestZeroCrossIndex(float[] s, int center, int length) {
        //range check...
        if (center < 0) {
            center = 0;
        } else if (center >= s.length) {
            center = s.length - 1;
        }

        boolean sign = s[center] > 0;
        int x;

        //search...
        for (int i = 0; i < length; i++) {
            //right side...
            x = center + i;

            if (x < s.length) {
                if (sign != (s[x] > 0)) {
                    return x;
                }
            }

            //left side...
            x = center - i;

            if (x >= 0) {
                if (sign != (s[x] > 0)) {
                    return x;
                }
            }
        }
        return -1;
    }


    /**
     * return the number of samples greater than silenceThreshold in the given
     * range.
     */
    public static int getNumberOfNoiseSamples(float[] s, int offset, int length,
                                              float silenceThreshold) {
        int counter = 0;

        for (int i = 0; i < length; i++) {
            if (Math.abs(s[offset + i]) > silenceThreshold) {
                counter++;
            }
        }
        return counter;
    }


    /**
     * return the number of samples lower than silenceThreshold in the given
     * range.
     */
    public static int getNumberOfSilenceSamples(float[] s, int offset, int length,
                                                float silenceThreshold) {
        int counter = 0;

        for (int i = 0; i < length; i++) {
            if (Math.abs(s[offset + i]) <= silenceThreshold) {
                counter++;
            }
        }
        return counter;
    }


    /**
     * return the index of the begin of the next noise-area on right side,
     * or -1 if no noise found. noise is defined by threshold and minimum
     * length.
     */
    public static int getNextUpperNoiseIndex(float[] s, int offset, int length,
                                             float silenceThreshold, int minWidth) {
        int widthCounter = 0;

        for (int i = 1; i < length; i++) {
            if (Math.abs(s[offset + i]) > silenceThreshold) {
                widthCounter++;
            } else {
                widthCounter = 0;
            }

            //width reached ?
            if (widthCounter >= minWidth) {
                return offset + i - widthCounter;
            }
        }
        return -1;
    }


    /**
     * return the index of the begin of the next noise-area on left side,
     * or -1 if no noise found. noise is defined by threshold and minimum
     * length.
     */
    public static int getNextLowerNoiseIndex(float[] s, int offset, int length,
                                             float silenceThreshold, int minWidth) {
        int widthCounter = 0;

        for (int i = 1; i < length; i++) {
            if (Math.abs(s[offset - i]) > silenceThreshold) {
                widthCounter++;
            } else {
                widthCounter = 0;
            }

            //width reached ?
            if (widthCounter >= minWidth) {
                return offset - i + widthCounter;
            }
        }
        return -1;
    }


    /**
     * return the index of the begin of the next silence-area on right side,
     * or -1 if no silence found. silence is defined by threshold and minimum
     * length.
     */
    public static int getNextUpperSilenceIndex(float[] s, int offset, int length,
                                               float silenceThreshold, int minWidth) {
        int widthCounter = 0;

        for (int i = 1; i < length; i++) {
            if (Math.abs(s[offset + i]) <= silenceThreshold) {
                widthCounter++;
            } else {
                widthCounter = 0;
            }

            //width reached ?
            if (widthCounter >= minWidth) {
                return offset + i - widthCounter;
            }
        }
        return -1;
    }


    /**
     * return the index of the begin of the next silence-area on left side,
     * or -1 if no silence found. silence
     * +++++++++++++++++ is defined by threshold and minimum
     * length.
     */
    public static int getNextLowerSilenceIndex(float[] s, int offset, int length,
                                               float silenceThreshold, int minWidth) {
        int widthCounter = 0;

        for (int i = 1; i < length; i++) {
            if (Math.abs(s[offset - i]) <= silenceThreshold) {
                widthCounter++;
            } else {
                widthCounter = 0;
            }

            //width reached ?
            if (widthCounter >= minWidth) {
                return offset - i + widthCounter;
            }
        }
        return -1;
    }


    //******** coordinat transform... ********

    /**
     * coordinate trnsform: cartesian to polar magnitude
     */
    public static float cartesianToMagnitude(float x, float y) {
        return (float) Math.sqrt(x * x + y * y);
    }


    /**
     * coordinate trnsform: cartesian to polar phase
     */
    public static float cartesianToPhase(float x, float y) {
        return (float) (Math.atan2(y, x));
    }


    /**
     * coordinate trnsform: polar to cartesian x
     */
    public static float polarToX(float mag, float phas) {
        return (float) (mag * Math.cos(phas));
    }


    /**
     * coordinate trnsform: polar to cartesian y
     */
    public static float polarToY(float mag, float phas) {
        return (float) (mag * Math.sin(phas));
    }


    //******** zero-cross boundary... ********


    /**
     * applies a zero-cross boundary: filters clipping, which occur on editing
     * (copy, paste, cut...) and some effects, when the phase of the signal
     * makes a step. works in circular mode (end-begin)
     *
     * @param sample     data samples of a channel
     * @param crossIndex points on the middle of the crosspoint
     * @param crossWidth number of affected points
     */
    public static void applyZeroCross(float[] sample, int crossIndex, int crossWidth) {
        try {
            smooth(sample, crossIndex - crossWidth / 2, crossWidth, 1.5f);    //gives better results???
/*
			int effectiveCrossWidth = crossWidth / 2;

			if (effectiveCrossWidth > 0)
			{
				float f0 = 1.f / effectiveCrossWidth;
				float mean = average(sample, crossIndex-crossWidth/2, crossWidth);

				for (int i=0; i<effectiveCrossWidth; i++)
				{
					float f = f0 * i;
					int iLeft = (crossIndex - i + sample.length) % sample.length;
					int iRight = (crossIndex + i) % sample.length;

					sample[iLeft] = sample[iLeft] * f + mean * (1 - f);
					sample[iRight] = sample[iRight] * f + mean * (1 - f);
				}
			}
*/
        }
        catch (Exception e) {
        }
    }


    public static int zeroCrossWidth = 30;


    /**
     * global setting of zerocross-width
     */
    public static void setZeroCrossWidth(int zc) {
        zeroCrossWidth = zc;
    }


    /**
     * global setting of zerocross-width
     */
    public static int getZeroCrossWidth() {
        return zeroCrossWidth;
    }


    /**
     * same as above, but with default width and global enable
     */
    public static void applyZeroCross(float[] sample, int crossIndex) {
        applyZeroCross(sample, crossIndex, zeroCrossWidth);
    }


    //******** math... ********

    /**
     * constant addition
     */
    public static void add(float[] s, int o, int l, float value) {
        for (int i = 0; i < l; i++) {
            s[o + i] += value;
        }
    }


    /**
     * variable addition
     */
    public static void add(float[] s, float[] value, int o, int l) {
        for (int i = 0; i < l; i++) {
            s[o + i] += value[o + i];
        }
    }


    /**
     * constant subtraction
     */
    public static void subtract(float[] s, int o, int l, float value) {
        for (int i = 0; i < l; i++) {
            s[o + i] -= value;
        }
    }


    /**
     * variable subtraction
     */
    public static void subtract(float[] s, float[] value, int o, int l) {
        for (int i = 0; i < l; i++) {
            s[o + i] -= value[o + i];
        }
    }


    /**
     * constant multiplication
     */
    public static void multiply(float[] s, int o, int l, float factor) {
        for (int i = 0; i < l; i++) {
            s[o + i] *= factor;
        }
    }


    /**
     * variable multiplication
     */
    public static void multiply(float[] s, float[] factor, int o, int l) {
        for (int i = 0; i < l; i++) {
            s[o + i] *= factor[o + i];
        }
    }


    /**
     * constant division
     */
    public static void divide(float[] s, int o, int l, float divisor) {
        for (int i = 0; i < l; i++) {
            s[o + i] /= divisor;
        }
    }


    /**
     * variable division
     */
    public static void divide(float[] s, float[] divisor, int o, int l) {
        for (int i = 0; i < l; i++) {
            if (divisor[o + i] != 0) {
                s[o + i] /= divisor[o + i];
            }
        }
    }


    /**
     * first order derivation
     */
    public static void derivate(float[] s, int o, int l) {
        float sOld = s[o];
        float d = 0;
        for (int i = 0; i < l; i++) {
            //derivate...
            d = s[o + i] - sOld;
            sOld = s[o + i];
            s[o + i] = d;
        }
    }


    /**
     * first order integral
     */
    public static void integrate(float[] s, int o, int l) {
        float summ = 0;
        for (int i = 0; i < l; i++) {
            summ += s[o + i];
            s[o + i] = summ;
        }
    }


    /**
     * invers 1 / x
     */
    public static void invers(float[] s, int o, int l) {
        for (int i = 0; i < l; i++) {
            if (s[o + i] != 0) {
                s[o + i] = 1.f / s[o + i];
            }
        }
    }


    /**
     * neg +/-
     */
    public static void neg(float[] s, int o, int l) {
        for (int i = 0; i < l; i++) {
            s[o + i] = -s[o + i];
        }
    }


    /**
     * power
     */
    public static void pow(float[] s, int o, int l, float exponent) {
        for (int i = 0; i < l; i++) {
            s[o + i] = (float) Math.pow(s[o + i], exponent);
        }
    }


    /**
     * square root
     */
    public static void sqrt(float[] s, int o, int l) {
        for (int i = 0; i < l; i++) {
            s[o + i] = (float) Math.sqrt(s[o + i]);
        }
    }


    /**
     * exponential
     */
    public static void exp(float[] s, int o, int l) {
        for (int i = 0; i < l; i++) {
            s[o + i] = (float) Math.exp(s[o + i]);
        }
    }


    /**
     * logarithm
     */
    public static void log(float[] s, int o, int l) {
        for (int i = 0; i < l; i++) {
            s[o + i] = (float) Math.log(s[o + i]);
        }
    }

    /**
     * to decibel
     */
    public static void todB(float[] s, int o, int l) {
        for (int i = 0; i < l; i++) {
            s[o + i] = todB(s[o + i]);
        }
    }


    /**
     * from decibel
     */
    public static void fromdB(float[] s, int o, int l) {
        for (int i = 0; i < l; i++) {
            s[o + i] = fromdB(s[o + i]);
        }
    }


    /**
     * returns max absolut value
     */
    public static float max(float[] s, int o, int l) {
        float max = 0;
        for (int i = 0; i < l; i++) {
            if (Math.abs(s[o + i]) > max) {
                max = Math.abs(s[o + i]);
            }
        }
        return max;
    }


}








