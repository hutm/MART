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
import org.mart.crs.exec.operation.models.htk.HTKResultsParser;
import org.mart.crs.exec.operation.models.htk.parser.chord.ChordHTKParser;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.management.features.FeaturesManager;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.util.List;

import static org.mart.crs.config.Settings.*;
import static org.mart.crs.exec.scenario.stage.StageParameters.*;
/**
 * @version 1.0 11-Jun-2010 17:46:16
 * @author: Hut
 */
public class RecognizeSplitModeOperation extends RecognizeOperation {


    public RecognizeSplitModeOperation(StageParameters stageParameters, ExecParams execParams) {
        super(stageParameters, execParams);
    }

    /**
     * Obtain n-best list for each chord segment.
     * At first the song is segmented, then viterbi decoder is applied for each segment
     */
    public void operate() {

        String outFilePath = decodedOutPath + String.format("%d_%2.1f", gaussianNumber, penalty);
        hvite(trainedModelsDir, hmmFolder + "_" + gaussianNumber, penalty, outFilePath);
        String recognizedFolder = tempDirPath + File.separator + "-firstPass-";

        ChordHTKParser parser = new ChordHTKParser(outFilePath, recognizedFolder);
        parser.run();
//        HTKResultsParser.parse(outFilePath, recognizedFolder);


        String featuresFolderFilePath = extractedFeaturesDir;
        FeaturesManager.splitAllData(featuresFolderFilePath, recognizedFolder);

        //Now generate N-Best lists for all splited segments
        HelperFile.createFileList(featuresFolderFilePath, featureFileListTest, new ExtensionFileFilter(new String[]{CHROMA_SEC_PASS_EXT}), true);

        createGrammarFile(true);
        hParse();

        String command = String.format("%s/HVite%s -o N -C %s ", binariesDir, EXECUTABLE_EXTENSION, configPath);
        for (int i = 0; i < NUMBER_OF_SEMITONES_IN_OCTAVE; i++) {
            command = command + String.format(" -H %s/%s/%s ", trainedModelsDir, hmmFolder + "_" + gaussianNumber, hmmDefs + i);
        }
        if (isToOutputLattices) {
            command = command + String.format(" -H %s/%s/%s -T 1 -S %s -i %s -z lattice -n %d -q Atvaldmn -w %s -p %5.2f %s %s",
                    trainedModelsDir, hmmFolder + "_" + gaussianNumber, macros, featureFileListTest, decodedOutPath,
                    execParams.NBestCalculationLatticeOrder, netFilePath, penalty, dictFilePath, wordListTestPath);
        } else {
            command = command + String.format(" -H %s/%s/%s -T 1 -S %s -i %s -n 25 25 -w %s -p %5.2f %s %s",
                    trainedModelsDir, hmmFolder + "_" + gaussianNumber, macros, featureFileListTest, decodedOutPath,
                    netFilePath, penalty, dictFilePath, wordListTestPath);
        }
        Helper.execCmd(command);


        //Now parse the hypotheses and compose a lattice
        List<ChordStructure> songList = HTKResultsParser.parseChordHypotheses(decodedOutPath, recognizedFolder);

        String outFeaturesFolder = String.format("%s/%s", dataDirPath, FEATURES_DIR_NAME);
        HelperFile.createDir(outFeaturesFolder);
        for (ChordStructure chordStructure : songList) {
            String outFile = String.format("%s/%s%s", outFeaturesFolder, chordStructure.getSongName(), Settings.CHROMA_EXT);
            chordStructure.saveHypothesesInFile(outFile);
        }


        /*
        Lattice aLattice;
        for (ChordStructure song : songList) {
            aLattice = new Lattice(song);
            aLattice.storeInFile();
        }


        //And now perform final lattice rescoring

        //TODO createGrammarFile(false);

        operationDomain.createDictionaryFile();

        command = binariesDir + "/HParse" + EXECUTABLE_EXTENSION + " " + gramFilePath + " " + netFilePath;
        Helper.execCmd(command);

        //Creating list of lattices
        String fileList = tempDirPath + File.separator + "latticeList2ndPass";
        createFileList(extractedFeaturesDir, fileList, new ExtensionFileFilter(new String[]{LATTICE_SEC_PASS_EXT}), false);
        List<String> latticeFilePathList = HelperFile.readTokensFromTextFile(fileList, 1);

        //Assign Factored Language Model Weights
        logger.info("Assign Language Model Weights for generated lattices");
        String outLattice;
        File oldLattice, newLattice;
        for (String inLattice : latticeFilePathList) {
            outLattice = inLattice + "_";
            logger.debug("processing lattice " + inLattice);
            if (IS_FACTORED_LM) {
                command = binariesDir + "/lattice-tool" + EXECUTABLE_EXTENSION + " -debug 0 -in-lattice " + inLattice + " -out-lattice " + outLattice + " -read-htk -write-htk -factored -lm " + lmDir + File.separator + LMSpecFactoredFileName + " -htk-logbase 2.71828 -no-nulls -no-htk-nulls -order " + execParams.latticeRescoringOrder;
            } else {
                if (IS_FACTORED_LM_FOR_STANDARD_VERSION) {
                    command = binariesDir + "/lattice-tool" + EXECUTABLE_EXTENSION + " -debug 0 -in-lattice " + inLattice + " -out-lattice " + outLattice + " -read-htk -write-htk -factored -lm " + lmDir + File.separator + LMSpecFileName + " -htk-logbase 2.71828 -no-nulls -no-htk-nulls -order " + execParams.latticeRescoringOrder;
                } else {
                    command = binariesDir + "/lattice-tool" + EXECUTABLE_EXTENSION + " -debug 0 -in-lattice " + inLattice + " -out-lattice " + outLattice + " -read-htk -write-htk -lm " + lmDir + File.separator + LMModelStandardVersion + " -htk-logbase 2.71828 -no-nulls -no-htk-nulls -order " + execParams.latticeRescoringOrder;
                }
            }
            Helper.execCmd(command);

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

                    //TODO refactor
                    String resultsDirName = "results2ndPass_" + "l_" + lmWeight + "_a_" + acWeight + "_w_" + wip + "_factored_" + IS_FACTORED_LM;
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
                    HTKResultsParser.parseLM(outRescoredFile, recognizedFolder, false);
                    EvaluatorOld.makeEvaluation(recognizedFolder, Settings.labelsGroundTruthDir, resultsDir + File.separator + resultsDirName + ".txt");
                }
            }
        }
        */
    }


}
