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

package org.mart.crs.management.xml;

/**
 * @version 1.0 30-Apr-2010 13:48:42
 * @author: Hut
 */
public interface Tags {

    public static final String TYPE_TAG = "type";


    //Filtering tags

    public static final String CLASS_TAG = "class";
    public static final String MATLAB_FUNCTION_TAG = "matlabFunc";
    public static final String MIDI_NOTE_TAG = "midiNote";

    public static final String A_COEFF_TAG = "aCoeff";
    public static final String B_COEFF_TAG = "bCoeff";
    public static final String DELAY_TAG = "Delay";

    public static final String SAMPLING_FREQ_TAG = "SamplingFreq";
    public static final String CHANNEL_NUMBER_TAG = "ChannelNumber";
    public static final String FILTER_TAG = "Filter";

    public static final String CHANNEL_TAG = "Channel";
    public static final String END_FREQ_TAG = "EndFreq";
    public static final String START_FREQ_TAG = "StartFreq";




    //VisualComponents

    public static final String VISUAL_COMPONENT_TAG = "VisualComponent";

    public static final String NAME_TAG = "name";
    public static final String SOURCE_TAG = "source";
    public static final String IS_ENABLED_TAG = "enabled";

    public static final String DATA_FILE_NAME_TAG = "DataFileName";
    public static final String SAMPLING_RATE_TAG = "SamplingRate";
    public static final String MIN_VALUE_TAG = "MinValue";
    public static final String MAX_VALUE_TAG = "MaxValue";


    public static final String TIME_SHIFT_IN_SAMPLES_TAG = "TIME_SHIFT_IN_SAMPLES";
    public static final String MIN_DISTANCE_BETWEEN_PEAKS_TAG = "MIN_DISTANCE_BETWEEN_PEAKS";
    public static final String MAX_DISTANCE_IN_HARMONIC_LINES_TAG = "MAX_DISTANCE_IN_HARMONIC_LINES";
    public static final String maxAbsPeakDistance_TAG = "maxAbsPeakDistance";
    public static final String numberOfHarmonicsToConsider_TAG = "numberOfHarmonicsToConsider";
    public static final String numberOfCandidatesPerOctave_TAG = "numberOfCandidatesPerOctave";
    public static final String firstOctave_TAG = "firstOctave";
    public static final String lastOctave_TAG = "lastOctave";
    public static final String MIN_DURATION_IN_FRAMES_TAG = "MIN_DURATION_IN_FRAMES";
    public static final String MIN_FUNDAMENTAL_FRAME_ENERGY_AMP_RELATION_TAG = "MIN_FUNDAMENTAL_FRAME_ENERGY_AMP_RELATION";
    public static final String MIN_FUNDAMENTAL_HARMONICS_AMP_RELATION_TAG = "MIN_FUNDAMENTAL_HARMONICS_AMP_RELATION";
    public static final String MAX_FUNDAMENTAL_AMP_RELATION_TAG = "MAX_FUNDAMENTAL_AMP_RELATION";
    public static final String MIN_CORRELATION_VALUE_TAG = "MIN_CORRELATION_VALUE";

    public static final String LATTICE_SOURCE_TAG = "LatticeSource";


    public static final String FRAME_SIZE_TAG = "FrameSize";
    public static final String PQMF_CONFIG_FILEPATH_TAG = "PqmfConfig";

    public static final String FROM_FILE_PATH_TAG = "FromFilePath";

    public static final String IS_WRAPPED_TAG = "isWrapped";
    public static final String EXTRACTION_ALG_TAG = "ExtractionAlg";
    public static final String BASS_TREBLE_TAG = "bassTreble";
    public static final String WINDOW_SIZE_TAG = "WindowSize";
    public static final String AVERAGE_FACTOR_TAG = "AverageFactor";
    public static final String FILTER_FILE_PATH_TAG = "FilterFilePath";


    public static final String IS_SPECTRUM_FILTERING_TAG = "SpectrumFiltering";


    public static final String QMF_CONFIGURATION_FILE_TAG = "QMFConfigurationFile";

    public static final String OVERLAPPING_TAG = "Overlapping";
    public static final String WINDOW_TYPE_TAG = "WindowType";
     public static final String MIN_FREQ_IN_SPECTROGRAM_TAG = "MinFreqInSpectrogram"; 
    public static final String MAX_FREQ_IN_SPECTROGRAM_TAG = "MaxFreqInSpectrogram";

    public static final String AUDIO_FILE_NAME_TAG = "AudioFileName";
    public static final String FILTERING_TAG = "Filtering";

    public static final String REASSIGNED_TYPE_TAG = "ReassignedSpectrumTag";
    public static final String REASSIGNED_THRESHOLD_TAG = "ReassignedThreshold";



    public static final String SEGMENT_TAG = "segment";
    public static final String TIME_TAG = "time";
    public static final String BEATTYPE_TAG = "beattype";
    public static final String BEAT_SEGMENT_STATE_TAG = "beatSegmentState";

    public static final String BEAT_TAG = "beat";
    public static final String MEASURE_TAG = "measure";
    public static final String TATUM_TAG = "tatum";


    public static final String SVF_TAG = "SVF";
    public static final String ID_TAG = "ID";
    public static final String SVF_ORDER_TAG = "SVFOrder";
    public static final String COLOR_ID_TAG = "ColorID";
    public static final String EXTRACTOR_ID_TAG = "ExtractorID";


    public static final String CHORD_NUMBER_TAG = "ChordNumber";
    public static final String REGRESSION_WINDOW_TAG = "RegressionWindowSize";






}
