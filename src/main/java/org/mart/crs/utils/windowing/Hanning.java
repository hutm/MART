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

package org.mart.crs.utils.windowing;

/**
 * @version 1.0 25-Aug-2010 11:29:23
 * @author: Hut
 */
public class Hanning extends WindowFunction {
    @Override
    public float getFunction(int i, int offset, int width) {
        return (float) (0.5f - 0.5f * Math.cos(2 * Math.PI * (i - offset) / width));
    }

    @Override
    public float getFunctionDerivative(int i, int offset, int width) {
        return (float) (0.5f * (2 * Math.PI / width) * Math.sin(2 * Math.PI * (i - offset) / width));
    }


}
