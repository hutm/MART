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

/**
 * @version 1.0 1/14/11 6:03 PM
 * @author: Hut
 */
public abstract class AbstractOnsetDetectionFunction {

    protected float[] detectionFunction;

    protected abstract void computeOnsetDetection();

    public float[] getDetectionFunction() {
        if (detectionFunction == null) {
            computeOnsetDetection();
        }
        return detectionFunction;
    }

}
