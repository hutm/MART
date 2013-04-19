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


/**
 * ********************************************************
 * <p/>
 * This file is part of LAoE.
 * <p/>
 * LAoE is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 * <p/>
 * LAoE is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with LAoE; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * <p/>
 * <p/>
 * Class:			AOSpline
 * Autor:			olivier gumann, neuchtel (switzerland)
 * JDK:				1.3
 * <p/>
 * Desctription:	cubic spline interpolation
 * <p/>
 * History:
 * Date:			Description:									Autor:
 * 28.07.01		first draft										oli4
 * 01.12.01		bugfix wrong offset of first point		oli4
 * <p/>
 * *********************************************************
 */
public class AOSpline {
    public AOSpline() {
    }


    private float u[], d[], p[], w[], x[], y[];


    public void load(float[] xx, float[] yy) {
        int N = xx.length;

        if (N <= 0)
            return;


        if ((u == null) || (u.length < N + 1)) {
            x = new float[N + 1];
            y = new float[N + 1];

            u = new float[N + 1];
            d = new float[N + 1];
            p = new float[N + 1];
            w = new float[N + 1];
        }

        /**
         *	bugfix wrong offset of first point:
         *	i had to increase arrays x and y of one element, the first point
         *	is added a second time. this results in a spline, where the curve
         *	passes also through the first point. (this was not the case before,
         *	the curve had another wrong offset... this bugfix was found by
         *	try and error.
         */
        x[0] = xx[0];
        y[0] = yy[0];
        for (int i = 0; i < xx.length; i++) {
            x[i + 1] = xx[i];
            y[i + 1] = yy[i];
        }

        for (int i = 2; i < N; i++) {
            d[i] = 2 * (x[i + 1] - x[i - 1]);
        }
        for (int i = 1; i < N; i++) {
            u[i] = x[i + 1] - x[i];
        }
        for (int i = 2; i < N; i++) {
            w[i] = 6 * ((y[i + 1] - y[i]) / u[i] - (y[i] - y[i - 1]) / u[i - 1]);
        }
        p[1] = 0;
        p[N] = 0;
        for (int i = 2; i < N - 1; i++) {
            w[i + 1] -= w[i] * u[i] / d[i];
            d[i + 1] -= u[i] * u[i] / d[i];
        }
        for (int i = N - 1; i > 1; i--) {
            p[i] = (w[i] - u[i] * p[i + 1]) / d[i];
        }

    }


    private float xPow3minusX(float x) {
        return x * x * x - x;
    }


    /**
     * cubic spline interpolation. when deltaX is zero, the y result is buggy!!!
     */
    public float getResult(float index) {
        int i;
        for (i = 1; i < x.length - 2; i++) {
            //no interpolation ?
            if (index == x[i + 1]) {
                return y[i + 1];
            }
            //interpolation ?
            if (index > x[i + 1]) {
            } else {
                break;
            }
        }
        float t = (index - x[i]) / u[i];
        return t * y[i + 1] +
                (1 - t) * y[i] +
                u[i] * u[i] * (xPow3minusX(t) * p[i + 1] + xPow3minusX(1 - t) * p[i]) / 6;
    }


}






