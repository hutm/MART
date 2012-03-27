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

package org.mart.crs.exec.operation.models.test.chord;

import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.eval.chord.EvaluatorOld;
import org.mart.crs.exec.operation.models.htk.parser.chord.ChordHTKParserFromLatticeBeatSynchronous;
import org.mart.crs.exec.operation.models.htk.parser.chord.ChordHTKParserHypotheses;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.exec.operation.models.htk.parser.chord.ChordHTKParserBeatSynchronous;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.exec.operation.models.htk.parser.chord.ChordHTKParser;
import org.mart.crs.management.label.lattice.Lattice;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.mart.crs.exec.scenario.stage.StageParameters.*;
import static org.mart.crs.config.Settings.*;

/**
 * @version 1.0 5/13/11 6:36 PM
 * @author: Hut
 */
public class RecognizeBeatSynchronousOperation extends RecognizeOperation {

    public RecognizeBeatSynchronousOperation(StageParameters stageParameters, ExecParams execParams) {
        super(stageParameters, execParams);
    }


    /**
     * Obtain n-best list for each chord segment.
     * At first the song is segmented, then viterbi decoder is applied for each segment
     */
    public void operate() {

        String outFilePath = decodedOutPath + String.format("%d_%2.1f", gaussianNumber, penalty);


        String command = String.format("%s/HVite%s -o N -C %s ", binariesDir, EXECUTABLE_EXTENSION, configPath);
        for (int i = 0; i < NUMBER_OF_SEMITONES_IN_OCTAVE; i++) {
            command = command + String.format(" -H %s/%s/%s ", trainedModelsDir, hmmFolder + "_" + gaussianNumber, hmmDefs + i);
        }
        command = command + String.format(" -H %s/%s/%s -T 1 -S %s -i %s -n %d %d -w %s -p %5.2f %s %s",
                trainedModelsDir, hmmFolder + "_" + gaussianNumber, macros, featureFileListTest, outFilePath, Settings.maxNumberOfHypos, Settings.maxNumberOfHypos,
                netFilePath, penalty, dictFilePath, wordListTestPath);
        Helper.execCmd(command);


        String recognizedFolder = tempDirPath + File.separator + "-firstPass-";
        ChordHTKParserBeatSynchronous parserTemp = new ChordHTKParserBeatSynchronous(outFilePath, recognizedFolder);
        parserTemp.run();

        //Evaluate intermediate results
        EvaluatorOld evaluator = new EvaluatorOld();
        evaluator.evaluate(recognizedFolder, Settings.chordLabelsGroundTruthDir, recognizedFolder + ".txt");


        ChordHTKParser parser = new ChordHTKParserHypotheses(outFilePath, recognizedFolder);
        parser.parseResults();
        List<ChordStructure> songList = parser.getResults();


        String latticeDir = String.format("%s/%s", tempDirPath, latticesDirName);
        HelperFile.getFile(latticeDir).mkdir();
        Lattice aLattice;
        for (ChordStructure song : songList) {
            aLattice = new Lattice(song);
            aLattice.storeInFile(String.format("%s/%s%s", latticeDir, song.getSongName(), Settings.LATTICE_EXT));
        }

        //Creating list of lattices
        String fileList = String.format("%s/latticeList_%d_%3.2f.txt", tempDirPath, gaussianNumber, penalty);
        HelperFile.createFileList(latticeDir, fileList, new ExtensionFileFilter(LATTICE_EXT), false);
        List<String> latticeFilePathList = HelperFile.readTokensFromTextFile(fileList, 1);


        //Assign Factored Language Model Weights
        logger.info("Assign Language Model Weights...");
        String outLattice;
        File oldLattice, newLattice;
        for (String inLattice : latticeFilePathList) {
            outLattice = inLattice + "_";
            logger.debug("processing lattice " + inLattice);
            if (IS_FACTORED_LM) {
                command = binariesDir + "/lattice-tool" + EXECUTABLE_EXTENSION + " -debug 0 -in-lattice " + inLattice + " -out-lattice " + outLattice + " -read-htk -write-htk -factored -lm " + lmDir + File.separator + LMSpecFactoredFileName + " -htk-logbase 2.71828 -no-nulls -no-htk-nulls -order " + execParams.latticeRescoringOrder;
            } else {
                command = binariesDir + "/lattice-tool" + EXECUTABLE_EXTENSION + " -debug 0 -in-lattice " + inLattice + " -out-lattice " + outLattice + " -read-htk -write-htk -lm " + lmDir + File.separator + LMModelStandardVersion + " -htk-logbase 2.71828 -no-nulls -no-htk-nulls -order " + execParams.latticeRescoringOrder;
            }
            Helper.execCmd(command, null, false);

            oldLattice = HelperFile.getFile(inLattice);
            newLattice = HelperFile.getFile(outLattice);
            oldLattice.delete();
            newLattice.renameTo(oldLattice);
        }

        //Rescoring lattices, applying different lm, ac weights and wip
        logger.info("Rescoring lattices...");
        float lmWeight;
        float acWeight;
        float wip;


        for (int i = 0; i < execParams._lmWeights.length; i++) {
            for (int j = 0; j < execParams._acWeights.length; j++) {
                for (int k = 0; k < execParams._wips.length; k++) {
                    lmWeight = execParams._lmWeights[i];
                    acWeight = execParams._acWeights[j];
                    wip = execParams._wips[k];

                    logger.info("LM " + lmWeight + "; AC " + acWeight + "WIP " + wip);
                    logger.info("--------------------------------------------------");

                    String resultsDirName = String.format("lmWeight_%3.2f%sacWeight_%3.2f%swip_%3.2f", lmWeight, FIELD_SEPARATOR, acWeight, FIELD_SEPARATOR, wip);
                    recognizedFolder = resultsDir + File.separator + resultsDirName;
//                    LabelsManager.recognizedFolder_compare = recognizedFolder; //This is done in order to estimate the advantages of LM
                    String outRescoredFile = tempDirPath + File.separator + "out_" + resultsDirName;

                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(outRescoredFile));
                        command = binariesDir + "/lattice-tool" + EXECUTABLE_EXTENSION + " -debug 0 -in-lattice-list " + fileList + " -read-htk -htk-lmscale " + lmWeight + " -htk-wdpenalty " + wip + " -htk-acscale " + acWeight + " -nbest-viterbi -viterbi-decode -output-ctm";
                        Helper.execCmd(command, writer, false);
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //Parsing and evaluating results
                    logger.info("Parsing results and evaluating");
//                    HTKResultsParser.parseLM(outRescoredFile, recognizedFolder, false);
                    ChordHTKParserFromLatticeBeatSynchronous parserLMBeatSynchronous = new ChordHTKParserFromLatticeBeatSynchronous(outRescoredFile, recognizedFolder);
                    parserLMBeatSynchronous.run();
                }
            }
        }

    }

    protected void createGrammarFile() {
        createGrammarFile(true);
    }


}
