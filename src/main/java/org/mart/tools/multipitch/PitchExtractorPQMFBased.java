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

package org.mart.tools.multipitch;

import org.mart.crs.analysis.filter.PQMF.PQMFHarmonicExtraction;
import org.mart.crs.config.Settings;
import org.mart.crs.core.spectrum.SpectrumImpl;

import static org.mart.crs.config.ExecParams._initialExecParameters;


/**
 * @version 1.0 Oct 6, 2009 4:35:40 PM
 * @author: Maksim Khadkevich
 */
public class PitchExtractorPQMFBased extends PitchExtractor {


    public float[][] getSpectrumOfFundamentals(SpectrumImpl spectrum) {
        PQMFHarmonicExtraction pqmfPitchExtraction = new PQMFHarmonicExtraction(spectrum.getAudioReader().getFilePath(), Settings.FILTERBANK_CONFIG_PQMF_PATH, ((1 - _initialExecParameters.overlapping) * _initialExecParameters.windowLength) / _initialExecParameters.samplingRate);
        return pqmfPitchExtraction.extractHarmonics();
    }
}
