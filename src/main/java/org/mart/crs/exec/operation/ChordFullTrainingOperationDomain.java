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

package org.mart.crs.exec.operation;

import org.mart.crs.config.ExecParams;
import org.mart.crs.management.features.FeaturesManager;
import org.mart.crs.management.features.FeaturesManagerChordFullTraining;
import org.mart.crs.management.label.chord.ChordType;
import org.mart.crs.management.label.chord.Root;
import org.mart.crs.utils.helper.Helper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mart.crs.management.label.LabelsParser.END_SENTENCE;
import static org.mart.crs.management.label.LabelsParser.START_SENTENCE;
import static org.mart.crs.management.label.chord.ChordType.NOT_A_CHORD;
import static org.mart.crs.management.label.chord.ChordType.chordDictionary;
import static org.mart.crs.utils.helper.HelperFile.getFile;

/**
 * @version 1.0 7/4/11 6:44 PM
 * @author: Hut
 */
public class ChordFullTrainingOperationDomain extends ChordOperationDomain {


    public void createWordLists() {
        try {

            String[] trainListTails = getFileTails();


            FileWriter[] fileWritersTrain = new FileWriter[trainListTails.length];

            for (int i = 0; i < trainListTails.length; i++) {
                fileWritersTrain[i] = new FileWriter(getFile(crsOperation.wordListTrainPath + trainListTails[i]));
            }
            FileWriter fileWriter = new FileWriter(getFile(crsOperation.wordListTestPath));
            FileWriter fileWriterLM = new FileWriter(getFile(crsOperation.wordListLMPath));


            List<String> wordArrayForTrainList = new ArrayList<String>();
            for (ChordType modality : chordDictionary) {
                if (!modality.equals(NOT_A_CHORD)) {
                    for (Root root : Root.values()) {
                        fileWritersTrain[root.ordinal()].write(String.format("%s%s\n", root, modality));
                        fileWriter.write(String.format("%s%s\n", root, modality));
                        fileWriterLM.write(String.format("%s%s\n", root, modality));
                        wordArrayForTrainList.add(String.format("%s%s", root, modality));
                    }
                } else {
                    if (Arrays.asList(chordDictionary).contains(NOT_A_CHORD)) {
                        fileWritersTrain[fileWritersTrain.length - 1].write(NOT_A_CHORD.getName() + "\n");
                        fileWriter.write(NOT_A_CHORD.getName() + "\n");
                        fileWriterLM.write(NOT_A_CHORD.getName() + "\n");
                        wordArrayForTrainList.add(String.format(NOT_A_CHORD.getName()));
                    }
                }
            }

            crsOperation.wordArrayForTrain = new String[wordArrayForTrainList.size()];
            wordArrayForTrainList.toArray(crsOperation.wordArrayForTrain);

            fileWriterLM.write(START_SENTENCE + "\n");
            fileWriterLM.write(END_SENTENCE + "\n");

            for (int i = 0; i < fileWritersTrain.length; i++) {
                fileWritersTrain[i].close();
            }
            fileWriter.close();
            fileWriterLM.close();
        } catch (IOException e) {
            logger.error("Problems: ");
            logger.error(Helper.getStackTrace(e));
        }
    }


    public static String[] getFileTails() {
        int numberOfTrainLists = Root.values().length;
        if (Arrays.asList(chordDictionary).contains(NOT_A_CHORD)) {
            numberOfTrainLists++;
        }

        String[] out = new String[numberOfTrainLists];
        for(int i = 0; i < Root.values().length; i++){
            out[i] = Root.values()[i].getName();
        }
        if (Arrays.asList(chordDictionary).contains(NOT_A_CHORD)) {
            out[numberOfTrainLists - 1] = NOT_A_CHORD.getName();
        }
        return out;
    }


    public static int getNumberOfTrainedHmms(){
        return getFileTails().length;
    }


    public void createMLF() {
        try {
            FileWriter fileWriter = new FileWriter(getFile(crsOperation.mlfFilePath));
            fileWriter.write("#!MLF!#\n");


            for (ChordType modality : chordDictionary) {
                if (!modality.equals(NOT_A_CHORD)) {
                    for (Root root : Root.values()) {
                        fileWriter.write(String.format("\"*%s%s.lab\"\n%s%s\n.\n", modality.getName(), root, root, modality.getName()));
                    }
                } else {
                    if (Arrays.asList(chordDictionary).contains(NOT_A_CHORD)) {
                        fileWriter.write(String.format("\"*%s.lab\"\n%s\n.\n", modality.getName(), modality.getName()));
                    }
                }
            }

            fileWriter.close();
        } catch (IOException e) {
            logger.error("Problems: ");
            logger.error(Helper.getStackTrace(e));
        }
    }


    @Override
    public FeaturesManager getFeaturesManager(String songFilePath, String outDirPath, boolean isForTraining, ExecParams execParams) {
        return new FeaturesManagerChordFullTraining(songFilePath, outDirPath, isForTraining, execParams);
    }


}
