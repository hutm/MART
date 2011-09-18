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

package org.mart.crs.core;

import org.mart.crs.config.Settings;
import org.mart.crs.utils.AudioHelper;
import org.mart.crs.utils.helper.Helper;

import java.util.ArrayList;
import java.util.List;

/**
 * A rms-energy based silence detector
 * @version 1.0 28-Jul-2010 16:34:02
 * @author: Hut
 */
public class SilenceDetector {


    protected List<float[]> silenceIntervals = new ArrayList<float[]>();
    protected AudioReader audioReader;

    public SilenceDetector(AudioReader audioReader) {
        this.audioReader = audioReader;
        detectSilence();
    }

    public List<float[]> getSilenceIntervals() {
        return silenceIntervals;
    }

    public void detectSilence() {
        float[] samples = audioReader.getSamples();
        //First calculate array of RMS Data
        int length = (int) Math.floor(audioReader.getDuration() / (Settings.SILENCE_DETECTION_STEP)) + 1;
        float[] RMSData = new float[length];
        int counter = 0, startSample, endSample;
        while ((counter + 1) * Settings.SILENCE_DETECTION_STEP * audioReader.getSampleRate() < samples.length) {
            startSample = (int) Math.floor(counter * Settings.SILENCE_DETECTION_STEP * audioReader.getSampleRate());
            endSample = (int) Math.floor((counter + 1) * Settings.SILENCE_DETECTION_STEP * audioReader.getSampleRate());
            RMSData[counter++] = AudioHelper.getRMSEnergy(samples, startSample, endSample);
        }
        this.silenceIntervals = Helper.detectSilenceIntervals(RMSData, Settings.SILENCE_DETECTION_THRESHOLD, (int) (Settings.SILENCE_SEGMENT_MIN_DURATION / Settings.SILENCE_DETECTION_STEP), Settings.SILENCE_DETECTION_STEP, audioReader.getDuration());
    }
}