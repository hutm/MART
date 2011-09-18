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
import org.mart.crs.core.spectrum.ReassignedFromMatlabFitz;
import org.mart.crs.core.spectrum.ReassignedSpectrumPhaseChange;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrumPercussivePart;
import org.mart.crs.management.features.extractor.ReassignedSpectrogramType;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrum;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrumHarmonicPart;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrumLimitDistance;

/**
 * @version 1.0 20-Jun-2010 15:27:55
 * @author: Hut
 */
public class TrebleReas extends SpectrumBased {

    protected PCP producePCP(float[] samples, double refFrequency) {
        ReassignedSpectrum spectrum = getReassignedSpectrum(samples, audioReader.getSampleRate());
        PCP pcp = new PCPBuilder(PCPBuilder.BASIC_ALG).setExecParams(execParams).setSpectrum(spectrum).setRefFreq(refFrequency).setInitializationByParts(Settings.initializationByParts).build();
        return pcp;
    }

    protected ReassignedSpectrum getReassignedSpectrum(float[] samples, float samplingRate) {
        ReassignedSpectrum spectrum = null;
        switch (ReassignedSpectrogramType.values()[execParams.reassignedSpectrogramType]) {
            case STANDARD:
                spectrum = new ReassignedSpectrum(samples, samplingRate, execParams);
                break;
            case HARMONIC:
                spectrum = new ReassignedSpectrumHarmonicPart(samples, samplingRate, execParams.reassignedSpectrogramThreshold, execParams);
                break;
            case PERCUSSIVE:
                spectrum = new ReassignedSpectrumPercussivePart(samples, samplingRate, execParams.reassignedSpectrogramThreshold, execParams);
                break;
            case PHASE_CHANGE:
                spectrum = new ReassignedSpectrumPhaseChange(samples, samplingRate, execParams);
                break;
            case FITZ_MATLAB:
                spectrum = new ReassignedFromMatlabFitz(samples, samplingRate, execParams.reassignedSpectrogramThreshold, execParams);
                break;
            case STANDARD_LIMIT_DISTANCE:
                spectrum = new ReassignedSpectrumLimitDistance(samples, samplingRate, execParams);
                break;
        }
        return spectrum;
    }

}

