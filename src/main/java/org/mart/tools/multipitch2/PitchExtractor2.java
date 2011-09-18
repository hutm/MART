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

package org.mart.tools.multipitch2;

import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrum;

/**
 * @version 1.0 23-Jun-2010 16:56:55
 * @author: Hut
 */
public class PitchExtractor2 {

    protected ReassignedSpectrum spectrum;


    public PitchExtractor2(ReassignedSpectrum spectrum) {
        this.spectrum = spectrum;
        initialize();
    }


    protected void initialize(){
        
    }


}
