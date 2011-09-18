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

package org.mart.crs.utils;

/* LPC */


public class LPC {

    public static void doLPC(double [] input, double [] output, double [] error, int p) {

        //Note input is assumed to be windowed.. ie: input.length = N

        if(p <= 0)
            throw new IllegalArgumentException("P should be > 0");

        if(output.length != p)
            throw new IllegalArgumentException("Output array should be of length p!");

        if(error.length != p)
            throw new IllegalArgumentException("Error array should be of length p!");

        //double [] error = new double[p];
        double [] k = new double[p];

        double [][] A = new double[p][p];

        error[0] = autocorrelation(input, 0);
        A[0][0] = k[0] = 0.0;

        for(int m=1; m<p; m++) {

            //calculate k[m]
            double tmp = autocorrelation(input, m);
            for(int i=1; i<m; i++)
                tmp -= A[m-1][i] * autocorrelation(input, m-i);

            k[m] = tmp / error[m-1];

            //update A[m][*]
            for(int i=0; i<m; i++)
                A[m][i] = A[m-1][i] - k[m]*A[m-1][m-i];

            A[m][m] = k[m];

            //update error[m]
            error[m] = (1 - (k[m]*k[m])) * error[m-1];
        }

        for(int i=0; i<p; i++)
            output[i] = A[p-1][i];

    }

    public static double autocorrelation(double [] input, int x) {

        double ret = 0.0;

        for(int i=x; i<input.length; i++)
            ret += input[i] * input[i-x];

        return ret;
    }
}

