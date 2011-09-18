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

package org.mart.crs.management.features.extractor.chroma;

import org.mart.crs.config.Settings;
import org.mart.crs.core.pcp.PCPBuilder;
import org.mart.crs.core.pcp.spectral.PCP;
import org.mart.crs.core.spectrum.SpectrumImpl;

/**
 * @version 1.0 20-Oct-2010 01:48:45
 * @author: Hut
 */
public class BassStandard extends BassReas {

    protected PCP producePCP(float[] samples, double refFrequency) {
        SpectrumImpl spectrum = new SpectrumImpl(samples, audioReader.getSampleRate(), execParams);
        PCP pcp = new PCPBuilder(PCPBuilder.BASIC_ALG).setExecParams(execParams).setSpectrum(spectrum).setRefFreq(refFrequency).setInitializationByParts(Settings.initializationByParts).setStartNoteForPCPWrapped(execParams.startMidiNoteBass).setEndNoteForPCPWrapped(execParams.endMidiNoteBass).build();
        return pcp;
    }


}
