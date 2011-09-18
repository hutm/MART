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

package org.mart.crs.exec.operation.models.htk;

import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.label.chord.Root;
import org.mart.crs.utils.helper.Helper;
import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;

import static org.mart.crs.config.Settings.NUMBER_OF_SEMITONES_IN_OCTAVE;
import static org.mart.crs.management.label.chord.ChordType.chordDictionary;
import static org.mart.crs.utils.helper.HelperFile.getFile;


/**
 * User: hut
 * Date: Aug 9, 2008
 * Time: 1:17:59 PM
 */
public class HMMDefCreator {

    /**
     * Logger
     */
    protected static Logger logger = CRSLogger.getLogger(HMMDefCreator.class);

    public static final int NUMBER_OF_STATES = 1;
    private static final double transProbability = 0.5;


    private static float[] chroma_no_harmonics_maj = {1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f};
    private static float[] chroma_no_harmonics_min = {1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f};

    private static float[] chroma_no_harmonics_maj_best = {0.82f, 0.3f, 0.3f, 0.3f, 0.82f, 0.3f, 0.3f, 0.82f, 0.3f, 0.3f, 0.3f, 0.3f};
    private static float[] chroma_no_harmonics_min_best = {0.82f, 0.3f, 0.3f, 0.82f, 0.3f, 0.3f, 0.3f, 0.82f, 0.3f, 0.3f, 0.3f, 0.3f};

    private static float[] chroma_4_harmonics_maj = {0.8f, 0.0f, 0.15f, 0.0f, 0.8f, 0.0f, 0.0f, 0.95f, 0.0f, 0.0f, 0.0f, 0.15f};
    private static float[] chroma_4_harmonics_min = {0.8f, 0.0f, 0.15f, 0.8f, 0.0f, 0.0f, 0.0f, 0.95f, 0.0f, 0.0f, 0.15f, 0.0f};

    private static float[] chroma_6_harmonics_maj = {0.75f, 0.0f, 0.15f, 0.0f, 0.8f, 0.0f, 0.0f, 0.9f, 0.05f, 0.0f, 0.0f, 0.25f};
    private static float[] chroma_6_harmonics_min = {0.75f, 0.0f, 0.15f, 0.75f, 0.05f, 0.0f, 0.0f, 0.95f, 0.0f, 0.0f, 0.15f, 0.05f};

    private static float[] variance = {0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f};


    public static void main(String[] args) {
        //TODO add parsing args
        createMacros(args[0]);
        createHMMDef(args[1]);
    }


    public static void createMacros(String macrosFilePath) {

        String chordName;
        float[][] chromaPrototype = {chroma_no_harmonics_maj_best, chroma_no_harmonics_min_best};

        try {
            FileWriter writer = new FileWriter(getFile(macrosFilePath));
            writer.write("~o\t" + Cons.VEC_SIZE_TAG + "\t" + NUMBER_OF_SEMITONES_IN_OCTAVE + "\t" + Cons.USER_TAG + "\n");
            for (int i = 0; i < chordDictionary.length; i++) {
                for (Root root :Root.values()) {
                    chordName = root + chordDictionary[i].getName();
                    writer.write("~s\t\"" + chordName + "\"\n");
                    writer.write("\t" + Cons.MEAN_TAG + "\t" + NUMBER_OF_SEMITONES_IN_OCTAVE + "\n");
                    writer.write("\t" + floatArrayToString(makeShift(chromaPrototype[i], root.ordinal())) + "\n");
                    writer.write("\t" + Cons.VARIANCE_TAG + "\t" + NUMBER_OF_SEMITONES_IN_OCTAVE + "\n");
                    writer.write("\t" + floatArrayToString(variance) + "\n");
                }
            }
            //Now write transition Matrix
            writer.write("~t \"transMatrix\"\n");
            writer.write("\t" + Cons.TRANSITION_MATRIX_TAG + "\t" + (NUMBER_OF_STATES + 2) + "\n\t\t");
            float[][] transMatrix = generateTransMatrix(NUMBER_OF_STATES);
            for (int i = 0; i < (NUMBER_OF_STATES + 2); i++) {
                for (int j = 0; j < (NUMBER_OF_STATES + 2); j++) {
                    writer.write(" " + transMatrix[i][j]);
                }
                writer.write("\n\t\t");
            }
            writer.close();

        } catch (IOException e) {
            logger.error("Unexpexted Error occured ");
            logger.error(Helper.getStackTrace(e));
        }


    }


    public static void createHMMDef(String HMMDefFilePath) {
        String chordName;

        try {
            FileWriter writer = new FileWriter(getFile(HMMDefFilePath));
            for (int i = 0; i < chordDictionary.length; i++) {
                for (Root root : Root.values()) {
                    chordName = root + chordDictionary[i].getName();
                    writer.write("~h\t\"" + chordName + "\"\n");
                    writer.write(Cons.BEGIN_HMM_TAG + "\n");
                    writer.write("\t" + Cons.NUM_STATES_TAG + "\t" + (NUMBER_OF_STATES + 2) + "\n");
                    for (int k = 1; k < (NUMBER_OF_STATES + 1); k++) {
                        writer.write("\t" + Cons.STATE_TAG + "\t" + (k + 1) + "\n");
                        writer.write("\t\t~s \"" + chordName + "\"\n");
                    }
                    writer.write("\t~t \"transMatrix\"\n");
                    writer.write(Cons.END_HMM_TAG + "\n");
                }
            }
            writer.close();


        } catch (IOException e) {
            logger.error("Unexpexted Error occured ");
            logger.error(Helper.getStackTrace(e));
        }
    }

    static float[] makeShift(float[] chroma, int jump) {
        float[] output = new float[chroma.length];
        int outIndex;
        for (int i = 0; i < NUMBER_OF_SEMITONES_IN_OCTAVE; i++) {
            outIndex = i + jump;
            while (outIndex < 0) {
                outIndex += NUMBER_OF_SEMITONES_IN_OCTAVE;
            }
            while (outIndex >= NUMBER_OF_SEMITONES_IN_OCTAVE) {
                outIndex -= NUMBER_OF_SEMITONES_IN_OCTAVE;
            }
            output[outIndex] = chroma[i];
        }
        return output;
    }

    static String floatArrayToString(float[] array) {
        String out = "";
        for (int i = 0; i < array.length; i++) {
            out = out + " " + array[i];
        }
        return out;
    }

    static float[][] generateTransMatrix(int nOfStates) {
        float[][] out = new float[nOfStates + 2][nOfStates + 2];
        out[0][1] = 1.0f;
        for (int i = 1; i < (nOfStates + 1); i++) {
            out[i][i] = (float) transProbability;
            out[i][i + 1] = (float) (1.0 - transProbability);
        }
        return out;
    }
}
