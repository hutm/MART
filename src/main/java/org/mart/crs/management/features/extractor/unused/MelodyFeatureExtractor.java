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

package org.mart.crs.management.features.extractor.unused;

import org.mart.crs.config.Settings;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.features.extractor.FeaturesExtractorHTK;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.StringTokenizer;

/**
 * @version 1.0 15-Mar-2010 20:28:09
 * @author: Maksim Khadkevich
 */
public class MelodyFeatureExtractor extends FeaturesExtractorHTK {

    protected static Logger logger = CRSLogger.getLogger(MelodyFeatureExtractor.class);

    private int[] pitches;
    public static final float MELODY_FRAME_PERIOD = 0.01f;

    private static WindowFunction windowFunction = WindowFunction.rectangular;

    private enum WindowFunction {
        rectangular,
        triangular
    }

    @Override
    protected void extractGlobalFeatures(double refFrequency) {
        //TODO
    }

    @Override
    public int getVectorSize() {
        return Settings.NUMBER_OF_SEMITONES_IN_OCTAVE;
    }


    public void initialize(String songFilePath) {
        super.initialize(songFilePath);
        String melodyFilePath = HelperFile.getPathForFileWithTheSameName(songFilePath, Settings.melodyGroundTruthFolder, Settings.LABEL_EXT);
        List<String> lines = HelperFile.readLinesFromTextFile(melodyFilePath);
        pitches = new int[lines.size()];
        int i = 0;
        for (String line : lines) {
            StringTokenizer stringTokenizer = new StringTokenizer(line);
            if (stringTokenizer.hasMoreTokens()) {
                stringTokenizer.nextToken();
                pitches[i++] = Helper.parseInt(stringTokenizer.nextToken());
            }
        }
    }


    //TODO
//    public void extractFeatures(float onset, float offset, float refFrequency, String chordFrom) {
//        float frameLength = Settings.windowLength / Settings.samplingRate * (1 - Settings.overlapping);
//        int numberOfFrames = (int) Math.floor((offset - onset) / frameLength) + 1;
//
//        float[][] out = new float[numberOfFrames][Settings.NUMBER_OF_SEMITONES_IN_OCTAVE];
//
//        for (int i = 0; i < out.length; i++) {
//            float time = onset + i * frameLength;
//            out[i] = calculateFeatureVector(time);
//        }
//        return rotateFeatures(out, chordFrom);
//    }
//
//
//    public float[][] extractFeatures(float refFrequency, String chordFrom) {
//        float onset = 0;
//        float offset = pitches.length * MELODY_FRAME_PERIOD;
//        return extractFeatures(onset, offset, refFrequency, chordFrom);
//    }


    private float[] calculateFeatureVector(float time) {
        float[] out = new float[Settings.NUMBER_OF_SEMITONES_IN_OCTAVE];

        float startTime = Math.max(0, Math.round((time - Settings.melodyWindowSize / 2) / MELODY_FRAME_PERIOD) * MELODY_FRAME_PERIOD);
        float endTime = Math.round((time + Settings.melodyWindowSize / 2) / MELODY_FRAME_PERIOD) * MELODY_FRAME_PERIOD;
        for (float currentTime = startTime; currentTime < endTime; currentTime += MELODY_FRAME_PERIOD) {
            int index = (int) Math.floor(currentTime / MELODY_FRAME_PERIOD);
            if (index < this.pitches.length) {
                int MIDIPitch = this.pitches[index];
                if (MIDIPitch > 0) {
                    float coeff = getWindowingCoeff(currentTime - time);
                    int chromaBin = MIDIPitch % Settings.NUMBER_OF_SEMITONES_IN_OCTAVE;
                    out[chromaBin] += coeff;
                }
            }
        }
        return out;
    }


    private static float getWindowingCoeff(float time) {
        if (time > (Settings.melodyWindowSize / 2) || time < (-1) * (Settings.melodyWindowSize / 2)) {
            return 0;
        }
        switch (windowFunction) {
            case rectangular:
                return 1;
            case triangular:
                return 1 - Math.abs(1.0f / (Settings.melodyWindowSize / 2) * time);
            default:
                return 0;
        }
    }

}
