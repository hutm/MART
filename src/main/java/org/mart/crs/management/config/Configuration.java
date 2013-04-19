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

package org.mart.crs.management.config;

import org.mart.crs.utils.windowing.WindowType;

/**
 * @version 1.0 3/27/12 11:29 PM
 * @author: Hut
 */
public class Configuration {

    public static String CONFIG_FILTERS_ROOT_FILE_PATH = "cfg/filters/";

//    protected PCP(double refFreq, int averagingFactor, int numberOfBinsPerSemitone, boolean toNormalize, int startNoteForPCPUnwrapped,
//                  int endNoteForPCPUnwrapped, int startNoteForPCPWrapped, int endNoteForPCPWrapped, float chromaSpectrumRate) {

    //Used in spectrum not to store large data in memory
    public static boolean initializationByParts;

    public static float samplingRateDefault = 11025f;

    public static int windowLengthDefault = 2048;
    public static int windowTypeDefault = WindowType.HANNING_WINDOW.ordinal();
    public static float overlappingDefault = 0.5f;

    public static float reassignedthresholdDefault = 0.4f;

    //----------Some constants---------------------------------------------------------------------------------
    public static final float REFERENCE_FREQUENCY = 440.0f;//261.6256f;
    public static final int REFERENCE_FREQUENCY_MIDI_NOTE = 69; //with respect to the REFERENCE_FREQUENCY
    public static final int NUMBER_OF_SEMITONES_IN_OCTAVE = 12;
    public static final int START_NOTE_FOR_PCP_UNWRAPPED = 24; //C1 midi note
    public static final int END_NOTE_FOR_PCP_UNWRAPPED = 107; //B7 midi note


    //PCP default settings
    public static final int averagingFactorDefault = 1;
    public static final int numberOfBinsPerSemitoneDefault = 1;
    public static final boolean toNormalizeDefault = true;
    public static final int startNoteForPcpWrappedDefault = 44;
    public static final int endNoteForPcpWrappedDefault = 84;
    public static final float chromaSpectrumRateDefault = 1.0f;

    //Chord extraction settings
    public static String[] chordDictionary = new String[]{"N", "min", "maj"};

}
