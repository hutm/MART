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

package org.mart.crs.core.spectrum.reassigned;

import org.mart.crs.config.ExecParams;
import org.mart.crs.core.AudioReader;

/**
 * @version 1.0 6/18/11 10:19 PM
 * @author: Hut
 */
public class ReassignedSpectrumLimitDistance extends ReassignedSpectrum {

    public ReassignedSpectrumLimitDistance(AudioReader audioReader, int startSampleIndex, int endSampleIndex, int windowLength, int windowType, float overlapping, ExecParams execParams) {
        super(audioReader, startSampleIndex, endSampleIndex, windowLength, windowType, overlapping, execParams);
    }

    public ReassignedSpectrumLimitDistance(AudioReader audioReader, int windowLength, int windowType, float overlapping, ExecParams execParams) {
        super(audioReader, windowLength, windowType, overlapping, execParams);
    }

    public ReassignedSpectrumLimitDistance(float[] samples, int startSampleIndex, int endSampleIndex, float sampleRate, ExecParams execParams) {
        super(samples, startSampleIndex, endSampleIndex, sampleRate, execParams);
    }

    public ReassignedSpectrumLimitDistance(float[] samples, float sampleRate, ExecParams execParams) {
        super(samples, sampleRate, execParams);
    }

    public ReassignedSpectrumLimitDistance(AudioReader audioReader, int startSampleIndex, int endSampleIndex, ExecParams execParams) {
        super(audioReader, startSampleIndex, endSampleIndex, execParams);
    }

    public ReassignedSpectrumLimitDistance(AudioReader audioReader, ExecParams execParams) {
        super(audioReader, execParams);
    }



    protected boolean isDeltaFrequencyOK(float deltaFreq) {
        return Math.abs(deltaFreq) < execParams.maxAllowedDeltaFreq;
    }


    protected boolean isDeltaTimeOK(float deltaTime) {
        return Math.abs(deltaTime) < execParams.maxAllowedDeltaTime;
    }



}
