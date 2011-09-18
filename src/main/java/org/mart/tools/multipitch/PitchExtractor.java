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

import org.mart.crs.core.spectrum.SpectrumImpl;

/**
 * Interface for pitch extractor class
 *
 * @version 1.0 Oct 6, 2009 3:26:04 PM
 * @author: Maksim Khadkevich
 */
public abstract class PitchExtractor {

    public static int TIME_SHIFT_IN_SAMPLES = 1;

    public static float MIN_DISTANCE_BETWEEN_PEAKS = 0.95f; //In semitone scale. (When searching for peaks)
    public static float MAX_DISTANCE_IN_HARMONIC_LINES = 0.1f; //In semitone scale. (When grouping into Harmonic (horizontal) lines)

    public static float maxAbsPeakDistance = 0.3f; //in semitone scale. (When searching for higher harmonics). It is the Distance  <f0*n, f1>)
    public static int numberOfHarmonicsToConsider = 20; //Number of possible higher harmonics


    public static int numberOfCandidatesPerOctave = 7;   //Per octave
    public static int firstOctave = 2;
    public static int lastOctave = 9;


    public static int MIN_DURATION_IN_FRAMES = 2;

    public static float MIN_FUNDAMENTAL_FRAME_ENERGY_AMP_RELATION = 0.03f;
    public static float MIN_FUNDAMENTAL_HARMONICS_AMP_RELATION = 0.2f;
    public static float MAX_FUNDAMENTAL_AMP_RELATION = 0.8f;


    public static float MIN_CORRELATION_VALUE = 0.9f;


    //----------------------depricated - currently not used parameters for the methods that are not used for the moment
    public static int minNumberOfHarmonics = 2;  //Default is 4
    public static int minNumberOfOddHarmonics = 1;

    //Weight of F0 candidates parameters
    static int e1 = 20;
    static int e2 = 120;

    static float rate = 0.4f;  //Determines when to stop taking harmonics (for Klapuri method)
    //---------------------------------------------------

    public abstract float[][] getSpectrumOfFundamentals(SpectrumImpl spectrum);

}
