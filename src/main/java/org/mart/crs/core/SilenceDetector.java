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

package org.mart.crs.core;

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

    //Silence detection
    public static final float SILENCE_DETECTION_STEP = 0.02f; //In sec //TODO read this parameter from the config
    public static final float SILENCE_DETECTION_THRESHOLD = 0.04f; //TODO read this parameter from the config
    public static final float SILENCE_SEGMENT_MIN_DURATION = 0.4f; //in sec //TODO read this parameter from the config


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
        int length = (int) Math.floor(audioReader.getDuration() / (SILENCE_DETECTION_STEP)) + 1;
        float[] RMSData = new float[length];
        int counter = 0, startSample, endSample;
        while ((counter + 1) * SILENCE_DETECTION_STEP * audioReader.getSampleRate() < samples.length) {
            startSample = (int) Math.floor(counter * SILENCE_DETECTION_STEP * audioReader.getSampleRate());
            endSample = (int) Math.floor((counter + 1) * SILENCE_DETECTION_STEP * audioReader.getSampleRate());
            RMSData[counter++] = AudioHelper.getRMSEnergy(samples, startSample, endSample);
        }
        this.silenceIntervals = Helper.detectSilenceIntervals(RMSData, SILENCE_DETECTION_THRESHOLD, (int) (SILENCE_SEGMENT_MIN_DURATION / SILENCE_DETECTION_STEP), SILENCE_DETECTION_STEP, audioReader.getDuration());
    }
}