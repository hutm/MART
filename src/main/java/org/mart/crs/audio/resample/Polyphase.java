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

package org.mart.crs.audio.resample;

import java.util.Random;

/**
 * Sound Tools rate change effect file.
 *
 * @author K. Bradley, Carnegie Mellon University
 * @author Stan Brooks
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version July 14, 1998 K. Bradley <br>
 *          October 29, 1999 Stan Brooks Various changes, bugfixes, speedups. <br>
 *          2006 nsano ported to java. <br>
 */
public class Polyphase extends CRSResampler{

    /** */
    private static final double ISCALE = 0x10000;
    /** */
    private static final int MF = 30;

    /** */
    private class PolyStage {
        /**
         * up/down conversion factors for this stage
         */
        int up, down;
        /**
         * # coefficients in filter_array
         */
        int filt_len;
        /**
         * filter coefficients
         */
        double[] filt_array;
        /**
         * # samples held in input but not yet processed
         */
        int held;
        /**
         * # samples of past-history kept in lower window
         */
        int hsize;
        /**
         * # samples current data which window can accept
         */
        int size;
        /**
         * this is past_hist[hsize], then input[size]
         */
        double[] window;
    }

    /** */
    private class PolyWork {
        /**
         * least common multiple of rates
         */
        float lcmrate;
        /**
         * LCM increments for I & O rates
         */
        int inskip, outskip;
        /**
         * out_rate/in_rate
         */
        double factor;
        /**
         * number of filter stages
         */
        int total;
        /**
         * output samples to skip at start
         */
        int oskip;
        /**
         * output samples 'in the pipe'
         */
        double inpipe;
        /**
         * array of pointers to polystage structs
         */
        PolyStage[] stage = new PolyStage[MF];
    }

    /** */
    private PolyWork work = new PolyWork();

    /** */
    private int win_type = 0;

    /**
     * Prepare processing.
     */
    private static final short primes[] = {
            2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37,
            41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89,
            97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151,
            157, 163, 167, 173, 179, 181, 191, 193, 197, 199, 211, 223,
            227, 229, 233, 239, 241, 251, 257, 263, 269, 271, 277, 281,
            283, 293, 307, 311, 313, 317, 331, 337, 347, 349, 353, 359,
            367, 373, 379, 383, 389, 397, 401, 409, 419, 421, 431, 433,
            439, 443, 449, 457, 461, 463, 467, 479, 487, 491, 499, 503,
            509, 521, 523, 541, 547, 557, 563, 569, 571, 577, 587, 593,
            599, 601, 607, 613, 617, 619, 631, 641, 643, 647, 653, 659,
            661, 673, 677, 683, 691, 701, 709, 719, 727, 733, 739, 743,
            751, 757, 761, 769, 773, 787, 797, 809, 811, 821, 823, 827,
            829, 839, 853, 857, 859, 863, 877, 881, 883, 887, 907, 911,
            919, 929, 937, 941, 947, 953, 967, 971, 977, 983, 991, 997,
            0
    };

    /** */
    private int prime(int n, int[] q0) {
        int pr;

        int p = 0; // primes
        int q = 0; // q0
//System.err.printf("factors(%d) =", n);
        while (n > 1) {                                                //TODO added >= instead of >
            while ((pr = primes[p]) != 0 && (n % pr) != 0) {
                p++;
            }
            if (pr == 0) {
//System.err.printf("Number %d too large of a prime.\n", n);
                pr = n;
            }
            q0[q++] = pr;
            n /= pr;
        }
        q0[q] = 0;
        for (pr = 0; pr < q; pr++) {
//System.err.printf(" %d", q0[pr]);
        }
//System.err.println();
        return q;
    }

    /** */
    private Random random;

    /** */
    private int permute(int[] m, int[] l, int ct, int ct1, int amalg) {

        int p = 0; // l
        int q = 0; // m
        while (ct1 > ct) {
            m[q++] = 1;
            ct++;
        }
        while ((m[q++] = l[p++]) != 0) {
        }
        if (ct <= 1) {
            return ct;
        }

        for (int k = ct; k > 1;) {
            int tmp;
            long j;
            j = Math.abs(random.nextInt() % 32768L) + Math.abs((random.nextInt() % 32768L) << 13); // reasonably big
//Debug.println("j: " + j);
            j = j % k; // non-negative!
            k--;
            if (j != k) {
                tmp = m[k];
                m[k] = m[(int) j];
                m[(int) j] = tmp;
            }
        }
        // now m is a 'random' permutation of l
        p = q = 0; // m
        int n = m[q++];
        int k;
        while ((k = m[q++]) != 0) {
            if ((n * k <= amalg) && (random.nextInt() & 1) != 0) { // TODO cast int OK?
                n *= k;
            } else {
                m[p++] = n;
                n = k;
            }
        }
        if (n != 0) {
            m[p++] = n;
        }
        m[p] = 0;
// for (k = 0; k < p - m; k++) { System.err.printf(" %d", m[k]); }
// System.err.prinln();
        return p;
    }

    /** */
    private int optimize_factors(int numer, int denom, int[] l1, int[] l2) {

        int[] m1 = new int[MF];
        int[] m2 = new int[MF];
        int[] b1 = new int[MF];
        int[] b2 = new int[MF];

        int f_min = numer;
        if (f_min > denom) {
            f_min = denom;
        }
        int c_min = 1 << 30;
        int u_min = 0;

        // Find the prime factors of numer and denom
        int ct1 = prime(numer, l1);
        int ct2 = prime(denom, l2);

        for (int amalg = Math.max(9, l2[0]); amalg <= 9 + l2[ct2 - 1]; amalg++) {
            fail:
            for (int k = 0; k < 100000; k++) {
                int u, u1, u2, j, f, cost;
                cost = 0;
                f = denom;
                u = Math.min(ct1, ct2) + 1;
//System.err.printf("pfacts(%d): ", numer);
                u1 = permute(m1, l1, ct1, u, amalg);
//System.err.printf("pfacts(%d): ", denom);
                u2 = permute(m2, l2, ct2, u, amalg);
                u = Math.max(u1, u2);
                for (j = 0; j < u; j++) {
                    if (j >= u1) {
                        m1[j] = 1;
                    }
                    if (j >= u2) {
                        m2[j] = 1;
                    }
                    f = (f * m1[j]) / m2[j];
                    if (f < f_min) {
                        break fail;
                    }
                    cost += f + m1[j] * m2[j];
                }
                if (c_min > cost) {
                    c_min = cost;
                    u_min = u;
                    System.arraycopy(m1, 0, b1, 0, u);
                    System.arraycopy(m2, 0, b2, 0, u);
                }
            }
            if (u_min != 0) {
                break;
            }
        }
        if (u_min != 0) {
            System.arraycopy(b1, 0, l1, 0, u_min);
            System.arraycopy(b2, 0, l2, 0, u_min);
        }
        l1[u_min] = 0;
        l2[u_min] = 0;
        return u_min;
    }

    /**
     * Calculate a Nuttall window of a given length. Buffer must already be
     * allocated to appropriate size.
     */
    private final void nuttall(double[] buffer, int length) {

        if (buffer == null || length <= 0) {
            throw new IllegalArgumentException("Illegal buffer or length to nuttall.");
        }

        /* Initial variable setups. */
        double N = length;
        int N1 = length / 2;

        for (int j = 0; j < length; j++) {
            buffer[j] = 0.3635819 + 0.4891775 * Math.cos(2 * Math.PI * 1 * (j - N1) / N) + 0.1365995 * Math.cos(2 * Math.PI * 2 * (j - N1) / N) + 0.0106411 * Math.cos(2 * Math.PI * 3 * (j - N1) / N);
        }
    }

    /**
     * Calculate a Hamming window of given length. Buffer must already be
     * allocated to appropriate size.
     */
    private final void hamming(double[] buffer, int length) {

        if (buffer == null || length <= 0) {
            throw new IllegalArgumentException("Illegal buffer or length to hamming.");
        }

        int N1 = length / 2;
        for (int j = 0; j < length; j++) {
            buffer[j] = 0.5 - 0.46 * Math.cos(Math.PI * j / N1);
        }
    }

    /**
     * Calculate the sinc function properly
     */
    private final double sinc(double value) {
        return Math.abs(value) < 1E-50 ? 1.0 : Math.sin(value) / value;
    }

    /**
     * Design a low-pass FIR filter using window technique. Length of filter is
     * in length, cutoff frequency in cutoff. 0 < cutoff <= 1.0 (normalized
     * frequency)
     * <p/>
     * buffer must already be allocated.
     */
    private final void fir_design(double[] buffer, int length, double cutoff) {

        if (buffer == null || length < 0 || cutoff < 0 || cutoff > Math.PI) {
            throw new IllegalArgumentException("Illegal buffer length or cutoff.");
        }

        // Use the user-option of window type
        if (win_type == 0) {
            nuttall(buffer, length); // Design Nuttall window: ** dB cutoff
        } else {
            hamming(buffer, length); // Design Hamming window: 43 dB cutoff
        }

//System.err.printf("# fir_design length=%d, cutoff=%8.4f\n", length, cutoff);
        // Design filter: windowed sinc function
        double sum = 0.0;
        for (int j = 0; j < length; j++) {
            buffer[j] *= sinc((Math.PI * cutoff * (j - length / 2))); // center at length / 2
//System.err.printf("%.1f %.6f\n", (double) j, buffer[j]);
            sum += buffer[j];
        }
        sum = 1.0 / sum;
        // Normalize buffer to have gain of 1.0: prevent roundoff error
        for (int j = 0; j < length; j++) {
            buffer[j] *= sum;
        }
//System.err.printf("# end\n\n");
    }

    /** */
//  private static final int RIBLEN = 2048;

    /** */
    private final float st_gcd(float a, float b) {
        if (b == 0) {
            return a;
        } else {
            return st_gcd(b, a % b);
        }
    }

    /**
     * parenthesize this way to avoid st_sample_t overflow in product term
     */
    private final float st_lcm(float a, float b) {
        return a * (b / st_gcd(a, b));
    }

    /** */
    public Polyphase(float inrate, float outrate) {
        this(inrate, outrate, 0, 1024, 0.95f);
    }

    /**
     * Process options Options:
     * <p/>
     * <pre>
     *  -w &lt;nut / ham&gt;        : window type
     *  -width &lt;short / long&gt; : window width
     *         short = 128 samples
     *         long  = 1024 samples
     *         &lt;num&gt; num      : explicit number
     * -cutoff &lt;float&gt;        : frequency cutoff for base bandwidth.
     *         Default = 0.95 = 95%
     * </pre>
     *
     * @param win_type  0: nuttall 1: hamming, default is 0
     * @param win_width short:128, long:1024, default is 1024
     * @param cutoff    frequency cutoff of base bandwidth in percentage. default is 0.95f (95%)
     */
    public Polyphase(float inrate, float outrate, int win_type, int win_width, float cutoff) {
        int[] l1 = new int[MF];
        int[] l2 = new int[MF];

        //
        this.win_type = win_type;

        // start

        double skip = 0;
// moved to #resample()
        int k;

        if (inrate == outrate) {
            throw new IllegalArgumentException("Input and Output rate must not be the same to use polyphase effect");
        }

        random = new Random(System.currentTimeMillis());

        work.lcmrate = st_lcm(inrate, outrate);

        // Cursory check for LCM overflow. If both rate are below 65k, there
        // should be no problem. 16 bits x 16 bits = 32 bits, which we can
        // handle.

        work.inskip = (int) (work.lcmrate / inrate);
        work.outskip = (int) (work.lcmrate / outrate);
        work.factor = (double) work.inskip / (double) work.outskip;
        work.inpipe = 0;
// moved to #resample()

        // Find the prime factors of inskip and outskip
        int total = optimize_factors(work.inskip, work.outskip, l1, l2);
        work.total = total;
        // l1 and l2 are now lists of the up/down factors for conversion

//System.err.printf("Poly:  input rate %.1f, output rate %.1f.  %d stages.\n", inrate, outrate, total);
//System.err.printf("Poly:  window: %s  size: %d  cutoff: %.2f.\n", (win_type == 0) ? "nut" : "ham", win_width, cutoff);

        // Create an array of filters and past history
        float uprate = inrate;
        for (k = 0; k < total; k++) {
            int prod, f_cutoff, f_len;
            PolyStage s;

            work.stage[k] = s = new PolyStage();
            s.up = l1[k];
            s.down = l2[k];
            f_cutoff = Math.max(s.up, s.down);
            f_len = Math.max(20 * f_cutoff, win_width);
            prod = s.up * s.down;
            if (prod > 2 * f_len) {
                prod = s.up;
            }
            f_len = ((f_len + prod - 1) / prod) * prod; // reduces rounding-errors in polyphase()
//          s.size = size;
            s.hsize = f_len / s.up; // this much of window is past-history
            s.held = 0;
//System.err.printf("Poly:  stage %d:  Up by %d, down by %d,  i_samps %d, hsize %d\n", k + 1, s.up, s.down, -1/* size */, s.hsize);
            s.filt_len = f_len;
            s.filt_array = new double[f_len];
//          s.window = new double[s.hsize + size];
            uprate *= s.up;
//System.err.printf("Poly:         :  filt_len %d, cutoff freq %.1f\n", f_len, uprate * cutoff / f_cutoff);
            uprate /= s.down;
            fir_design(s.filt_array, f_len, cutoff / f_cutoff);
            // s.filt_array[f_len - 1] = 0;

            skip *= s.up;
            skip += f_len;
            skip /= s.down;

//          size = (size * s.up) / s.down; // this is integer
        }
        work.oskip = (int) (skip / 2);
        { // bogus last stage is for output buffering
            PolyStage s = new PolyStage();
            work.stage[k] = s;
            s.up = s.down = 0;
//          s.size = size;
            s.hsize = 0;
            s.held = 0;
            s.filt_len = 0;
            s.filt_array = null;
//          s.window = new double[size];
        }
//System.err.printf("Poly:  output samples %d, oskip %d\n", -1 /* size */, work.oskip);
    }

    /**
     * Processed signed long samples from ibuf to obuf. Return number of samples
     * processed.
     * <p>
     * REMARK: putting this in a separate subroutine improves gcc's optimization
     * </p>
     */
    private final double st_prod(final double[] q, int qP, int qstep, final double[] p, int pP, int n) {
//System.err.printf("qP: %d, qstep: %d, pP: %d, n: %d, (%d)\n", qP, qstep, pP, n, q.length);
        double sum = 0;
        int p0 = pP - n; // p
        while (pP > p0) {
//System.err.printf("p[%d]: %f, q[%d]: %f\n", pP, p[pP], qP, q[qP]);
            sum += p[pP] * q[qP];
            qP += qstep;
            pP -= 1;
        }
//System.err.printf("sum: %f\n", sum);
        return sum;
    }

    /** */
    private final void polyphase(double[] output, int oP, PolyStage s) {
        int up = s.up;
        int down = s.down;
        int f_len = s.filt_len;

        int in = s.hsize; // s.window
//for (mm = 0; mm < s.filt_len; mm++) { System.err.printf("cf_%d %f\n", mm, s.filt_array[mm]); }
        // assumes s.size divisible by down (now true)
        int o_top = (s.size * up) / down; // output
//System.err.printf(" isize %d, osize %d, up %d, down %d, N %d", s.size, o_top-output, up, down, f_len);
        for (int mm = 0, o = 0; o < o_top; mm += down, o++) { // o: output pointer
            int q = mm % up; // decimated coef pointer, s.filt_array
            int p = in + (mm / up);
            double sum = st_prod(s.filt_array, q, up, s.window, p, f_len / up);
            output[oP + o] = sum * up;
//System.err.printf("%f\n", output[oP + o]);
        }
    }

    /** */
    private final void update_hist(double[] hist, int hist_size, int in_size) {
        int p = 0; // hist;
        int p1 = hist_size;
        int q = in_size;
        while (p < p1) {
            hist[p++] = hist[q++];
        }
    }

    /** */
    private static final int ST_SAMPLE_MAX = 2147483647;
    /** */
    private static final int ST_SAMPLE_MIN = (-ST_SAMPLE_MAX - 1);

    /**
     * TODO check
     */
    private final int clipfloat(double sample) {
//System.err.printf("%f\n", sample);
        if (sample > ST_SAMPLE_MAX) {
            return ST_SAMPLE_MAX;
        }
        if (sample < ST_SAMPLE_MIN) {
            return ST_SAMPLE_MIN;
        }
        return (int) sample;
    }

    /** */
    public int[] resample(int[] ibuf) {

        int j;
        int size;
        {
            int f = ibuf.length / Math.max(work.inskip, work.outskip);
            if (f == 0) {
                f = 1;
            }
            size = ibuf.length;
//            size = f * work.outskip; // reasonable input block size
//Debug.println("size 0: " + size);
        }
        for (j = 0; j < work.total; j++) {
            PolyStage s = work.stage[j];
            s.size = size;
            s.window = new double[s.hsize + size];
            size = (size * s.up) / s.down; // this is integer
//Debug.println("size [" + j + "]: " + size);
        }
        {
            PolyStage s = work.stage[j];
            s.size = size;
//Debug.println("size [" + j + "]: " + size);
            s.window = new double[size];
        }

        //----

        int osamp = (int) (ibuf.length / work.factor);
        int[] obuf;

        //----

        // Sanity check: how much can we tolerate?
//System.err.printf("isamp=%d osamp=%d\n", isamp[0], osamp[0]); System.err.flush();
        PolyStage s0 = work.stage[0]; // the first stage
        PolyStage s1 = work.stage[work.total]; // the 'last' stage is output buffer
        {
            int in_size = ibuf.length;
            int gap = s0.size - s0.held; // space available in this 'input' buffer
            if ((in_size > gap) || (ibuf == null)) {
                in_size = gap;
            }
            if (in_size > 0) {
                int q = s0.hsize; // s0.window
                if (s0 != s1) {
                    q += s0.held; // the last (output) buffer doesn't shift history
                }
                if (ibuf != null) {
                    work.inpipe += work.factor * in_size;
                    for (int k = 0; k < in_size; k++) {
//if (k < 300) {
// System.err.printf("%d\n", ibuf[k]);
//}
                        s0.window[q++] = ibuf[k] / ISCALE;
                    }
                } else { // ibuf == null is draining
                    for (int k = 0; k < in_size; k++) {
                        s0.window[q++] = 0.0;
                    }
                }
                s0.held += in_size;
            }
        }

        if (s0.held == s0.size && s1.held == 0) {
            // input buffer full, output buffer empty, so do process

            for (int k = 0; k < work.total; k++) {
                PolyStage s = work.stage[k];
                int out = work.stage[k + 1].hsize; // rate.stage[k + 1].window

//System.err.printf("stage: %d insize=%d\n", k + 1, s.window.length); System.err.flush();
//for (int l = 0; l < s.window.length; l++) {
// if (l < 300) {
//  System.err.printf("%f\n", s.window[l]);
// }
//}
                polyphase(work.stage[k + 1].window, out, s);

                // copy input history into lower portion of rate.window[k]
                update_hist(s.window, s.hsize, s.size);
                s.held = 0;
            }

            s1.held = s1.size;
            s1.hsize = 0;
        }

        {
            int q;
            int k;

            int oskip = work.oskip;
            int out_size = s1.held;
//Debug.println("out_size 0: " + out_size);
            int out_buf = s1.hsize; // s1.window

            if (ibuf == null && out_size > Math.ceil(work.inpipe)) {
                out_size = (int) Math.ceil(work.inpipe);
//Debug.println("out_size 1: " + out_size);
            }

//TODO this code was commented
//            if (out_size > oskip + osamp) {
//                out_size = oskip + osamp;
////Debug.println("out_size 2: " + out_size);
//            }

            obuf = new int[out_size];
//Debug.println("out_size: " + out_size);
            for (q = 0, k = oskip; k < out_size; k++) {
                obuf[q++] = clipfloat(s1.window[out_buf + k] * ISCALE); // should clip - limit
            }

            osamp = q;
            work.inpipe -= osamp;
            oskip -= out_size - osamp;
            work.oskip = oskip;

            s1.hsize += out_size;
            s1.held -= out_size;
            if (s1.held == 0) {
                s1.hsize = 0;
            }
        }

        return obuf;
    }

    /**
     * Process tail of input samples.
     */
    public int[] drain() {
        // Call "flow" with null input.
        return resample(new int[0]);
    }
}

/* */