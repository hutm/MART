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

package org.mart.crs.analysis.filter.QMF;

/**
 * @version 1.0 21.04.2009 10:06:42
 * @author: Maksim Khadkevich
 */
public interface QMFCoeff {

    /**
     * QMF (Quadratic Mirror Filter) table
     */
    public static final float[] h0_64 = {
            3.596189e-05f, -0.0001123515f,
            -0.0001104587f, 0.0002790277f,
            0.0002298438f, -0.0005953563f,
            -0.0003823631f, 0.00113826f,
            0.0005308539f, -0.001986177f,
            -0.0006243724f, 0.003235877f,
            0.0005743159f, -0.004989147f,
            -0.0002584767f, 0.007367171f,
            -0.0004857935f, -0.01050689f,
            0.001894714f, 0.01459396f,
            -0.004313674f, -0.01994365f,
            0.00828756f, 0.02716055f,
            -0.01485397f, -0.03764973f,
            0.026447f, 0.05543245f,
            -0.05095487f, -0.09779096f,
            0.1382363f, 0.4600981f,
            0.4600981f, 0.1382363f,
            -0.09779096f, -0.05095487f,
            0.05543245f, 0.026447f,
            -0.03764973f, -0.01485397f,
            0.02716055f, 0.00828756f,
            -0.01994365f, -0.004313674f,
            0.01459396f, 0.001894714f,
            -0.01050689f, -0.0004857935f,
            0.007367171f, -0.0002584767f,
            -0.004989147f, 0.0005743159f,
            0.003235877f, -0.0006243724f,
            -0.001986177f, 0.0005308539f,
            0.00113826f, -0.0003823631f,
            -0.0005953563f, 0.0002298438f,
            0.0002790277f, -0.0001104587f,
            -0.0001123515f, 3.596189e-05f
    };

    /**
     * QMF (Quadratic Mirror Filter) table
     */
    public static final float[] h1_64 = {
            3.596189e-05f, 0.0001123515f,
            -0.0001104587f, -0.0002790277f,
            0.0002298438f, 0.0005953563f,
            -0.0003823631f, -0.00113826f,
            0.0005308539f, 0.001986177f,
            -0.0006243724f, -0.003235877f,
            0.0005743159f, 0.004989147f,
            -0.0002584767f, -0.007367171f,
            -0.0004857935f, 0.01050689f,
            0.001894714f, -0.01459396f,
            -0.004313674f, 0.01994365f,
            0.00828756f, -0.02716055f,
            -0.01485397f, 0.03764973f,
            0.026447f, -0.05543245f,
            -0.05095487f, 0.09779096f,
            0.1382363f, -0.4600981f,
            0.4600981f, -0.1382363f,
            -0.09779096f, 0.05095487f,
            0.05543245f, -0.026447f,
            -0.03764973f, 0.01485397f,
            0.02716055f, -0.00828756f,
            -0.01994365f, 0.004313674f,
            0.01459396f, -0.001894714f,
            -0.01050689f, 0.0004857935f,
            0.007367171f, 0.0002584767f,
            -0.004989147f, -0.0005743159f,
            0.003235877f, 0.0006243724f,
            -0.001986177f, -0.0005308539f,
            0.00113826f, 0.0003823631f,
            -0.0005953563f, -0.0002298438f,
            0.0002790277f, 0.0001104587f,
            -0.0001123515f, -3.596189e-05f
    };


    public static final float[] h0_32 = {
            2.2451390e-03f, -3.9711520e-03f,
            -1.9696720e-03f, 8.1819410e-03f,
            8.4268330e-04f, -1.4228990e-02f,
            2.0694700e-03f, 2.2704150e-02f,
            -7.9617310e-03f, -3.4964400e-02f,
            1.9472180e-02f, 5.4812130e-02f,
            -4.4524230e-02f, -9.9338590e-02f,
            1.3297250e-01f, 4.6367410e-01f,
            4.6367410e-01f, 1.3297250e-01f,
            -9.9338590e-02f, -4.4524230e-02f,
            5.4812130e-02f, 1.9472180e-02f,
            -3.4964400e-02f, -7.9617310e-03f,
            2.2704150e-02f, 2.0694700e-03f,
            -1.4228990e-02f, 8.4268330e-04f,
            8.1819410e-03f, -1.9696720e-03f,
            -3.9711520e-03f, 2.2451390e-03f
    };

    public static final float[] h1_32 = {
            2.2451390e-03f, 3.9711520e-03f,
            -1.9696720e-03f, -8.1819410e-03f,
            8.4268330e-04f, 1.4228990e-02f,
            2.0694700e-03f, -2.2704150e-02f,
            -7.9617310e-03f, 3.4964400e-02f,
            1.9472180e-02f, -5.4812130e-02f,
            -4.4524230e-02f, 9.9338590e-02f,
            1.3297250e-01f, -4.6367410e-01f,
            4.6367410e-01f, -1.3297250e-01f,
            -9.9338590e-02f, 4.4524230e-02f,
            5.4812130e-02f, -1.9472180e-02f,
            -3.4964400e-02f, 7.9617310e-03f,
            2.2704150e-02f, -2.0694700e-03f,
            -1.4228990e-02f, -8.4268330e-04f,
            8.1819410e-03f, 1.9696720e-03f,
            -3.9711520e-03f, -2.2451390e-03f
    };


    public static final float[] h0_16 = {
            0.65256660e-02f, -0.20487510e-01f,
            0.19911500e-02f, 0.46476740e-01f,
            -0.26275600e-01f, -0.99295500e-01f,
            0.11786660e-00f, 0.47211220e-00f,
            0.47211220e-00f, 0.11786660e-00f,
            -0.99295500e-01f, -0.26275600e-01f,
            0.46476740e-01f, 0.19911500e-02f,
            -0.20487510e-01f, 0.65256660e-02f
    };

    public static final float[] h1_16 = {
            0.65256660e-02f, 0.20487510e-01f,
            0.19911500e-02f, -0.46476740e-01f,
            -0.26275600e-01f, 0.99295500e-01f,
            0.11786660e-00f, -0.47211220e-00f,
            0.47211220e-00f, -0.11786660e-00f,
            -0.99295500e-01f, 0.26275600e-01f,
            0.46476740e-01f, -0.19911500e-02f,
            -0.20487510e-01f, -0.65256660e-02f
    };
}
