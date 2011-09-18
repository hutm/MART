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

import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.management.label.chord.Root;
import org.mart.crs.management.label.chord.ChordType;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;
import org.mart.crs.utils.helper.HelperFile;
import org.apache.log4j.Logger;
import org.mart.crs.config.ExecParams;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static org.mart.crs.config.Settings.NUMBER_OF_SEMITONES_IN_OCTAVE;

/**
 * User: hut
 * Date: Aug 20, 2008
 * Time: 6:20:35 PM
 */
public class HMMPopulator {

    protected static Logger logger = CRSLogger.getLogger(HMMPopulator.class);


    public static void main(String[] args) {
        populate(args[0]);
    }


    public static void populate(String shortHmmPath) {


        try {
            BufferedReader reader = new BufferedReader(new FileReader(HelperFile.getFile(shortHmmPath)));
            BufferedWriter[] writer = new BufferedWriter[ChordSegment.SEMITONE_NUMBER];
            for (int i = 0; i < ChordSegment.SEMITONE_NUMBER; i++) {
                writer[i] = new BufferedWriter(new FileWriter(HelperFile.getFile(shortHmmPath + i)));
            }

            String line, modality;

            while ((line = reader.readLine()) != null && line.length() > 1) {
                if (ExecParams._initialExecParameters.featureExtractors.length > 1 && line.startsWith("<SWEIGHTS>")) {
                    for (int i = 0; i < ChordSegment.SEMITONE_NUMBER; i++) {
                        writer[i].write(line + "\n");
                        for (int j = 0; j < ExecParams._initialExecParameters.featureExtractorsWeights.length; j++) {
                            writer[i].write(String.format("%5.2f ", ExecParams._initialExecParameters.featureExtractorsWeights[j]));
                        }
                        writer[i].write("\n");
                    }
                    reader.readLine();
                    continue;
                }

                if (line.startsWith("~h \"")) {
                    modality = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
                    if (modality.equals(ChordType.NOT_A_CHORD.getName())) {
                        for (int i = 0; i < ChordSegment.SEMITONE_NUMBER; i++) {
                            writer[i].write("~h \"" + modality + "\"\n");
                        }
                        continue;
                    }
                    for (int i = 0; i < ChordSegment.SEMITONE_NUMBER; i++) {
                        writer[i].write("~h \"" + Root.values()[i] + modality + "\"\n");
                    }
                    continue;
                }
                if (line.startsWith("<MEAN>") || line.startsWith("<VARIANCE>")) {
                    for (int i = 0; i < ChordSegment.SEMITONE_NUMBER; i++) {
                        writer[i].write(line + "\n");
                    }
                    line = reader.readLine();
                    for (int i = 0; i < ChordSegment.SEMITONE_NUMBER; i++) {
                        writer[i].write(" " + shiftLine(line, i).trim() + "\n");
                    }
                    continue;
                }
                for (int i = 0; i < ChordSegment.SEMITONE_NUMBER; i++) {
                    writer[i].write(line + "\n");
                }
            }
            for (int i = 0; i < ChordSegment.SEMITONE_NUMBER; i++) {
                writer[i].close();
            }

            reader.close();

        } catch (IOException e) {
            logger.fatal("Cannot read data from file " + shortHmmPath);
//            System.exit(1);
        }


    }

    private static String shiftLine(String line, int shift) {
        StringTokenizer tokenizer = new StringTokenizer(line);
        List<String> values = new ArrayList<String>();
        while (tokenizer.hasMoreTokens()) {
            values.add(tokenizer.nextToken());
        }
        StringBuffer shiftedVector = new StringBuffer();
        int index;
        for (int startNoteInOctave = 0; startNoteInOctave < values.size(); startNoteInOctave += NUMBER_OF_SEMITONES_IN_OCTAVE) {
            for (int j = startNoteInOctave + NUMBER_OF_SEMITONES_IN_OCTAVE - shift; j < startNoteInOctave + NUMBER_OF_SEMITONES_IN_OCTAVE; j++) {
                shiftedVector.append(values.get(j)).append(" ");
            }
            for (int j = startNoteInOctave; j < startNoteInOctave + NUMBER_OF_SEMITONES_IN_OCTAVE - shift; j++) {
                shiftedVector.append(values.get(j)).append(" ");
            }
        }
        return new String(shiftedVector);
    }


    public static void transformTransitionMatrixForNoBeat(String shortHmmPath) {
        try {
            String transformedFilePath = shortHmmPath + "_";
            BufferedReader reader = new BufferedReader(new FileReader(HelperFile.getFile(shortHmmPath)));
            BufferedWriter writer = new BufferedWriter(new FileWriter(HelperFile.getFile(transformedFilePath)));

            String line;
            boolean noBeatModelStarted = false;
            StringBuilder silenceModelString = new StringBuilder();
            while ((line = reader.readLine()) != null && line.length() > 1) {
                if (line.startsWith(String.format("~h \"%s\"", StageParameters.NO_BEAT))) {
                    noBeatModelStarted = true;
                    silenceModelString = new StringBuilder();
                    writer.write(line + "\n");
                    silenceModelString.append(line.replaceAll(StageParameters.NO_BEAT, StageParameters.NOTHING_SYMBOL) + "\n");
                    continue;
                }
                if (line.startsWith("<TRANSP>")) {
                    silenceModelString.append(line + "\n");
                    writer.write(line + "\n");
                    int numberOfStates = Helper.parseInt(line.substring(line.lastIndexOf(">") + 1));

                    float[][] transitionMatrix = new float[numberOfStates][numberOfStates];
                    for (int i = 0; i < numberOfStates - 1; i++) {
                        transitionMatrix[i][i + 1] = 1.0f;
                    }
                    writer.write(HelperArrays.arrayToString(transitionMatrix));

                    transitionMatrix = new float[numberOfStates][numberOfStates];
                    transitionMatrix[0][1] = 1.0f;
                    for (int i = 1; i < numberOfStates - 1; i++) {
                        transitionMatrix[i][i + 1] = 0.4f;
                        transitionMatrix[i][i] = 0.6f;
                    }
                    silenceModelString.append(HelperArrays.arrayToString(transitionMatrix));

                    while (!(line = reader.readLine()).startsWith("<ENDHMM>")) {
                        //skip lines
                    }
                    writer.write(line + "\n");
                    silenceModelString.append(line + "\n");

                    continue;
                }
                writer.write(line + "\n");
                if (noBeatModelStarted) {
                    silenceModelString.append(line + "\n");
                }
            }
            if (noBeatModelStarted) {
                writer.write(silenceModelString.toString());
            }
            writer.close();
            HelperFile.renameFile(HelperFile.getFile(transformedFilePath), HelperFile.getFile(shortHmmPath), true);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
