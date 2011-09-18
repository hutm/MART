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
import org.mart.crs.exec.scenario.stage.StageParameters;

/**
 * @version 1.0 11-Jun-2010 17:36:22
 * @author: Hut
 */
public class RecognizeBeatSyncLMOperation extends RecognizeOperation {


    public RecognizeBeatSyncLMOperation(StageParameters stageParameters, ExecParams execParams) {
        super(stageParameters, execParams);
    }



    /**
     * @deprecated has a lot of bugs
     */
    public void operate() {

        /*createDir(tempDirPath);

        HTKExecutionHelper.copyConfigFile();


        createFileList(extractedFeaturesDir, fileListTest, new ExtensionFileFilter(new String[]{CHROMA_EXT}), true);

        HTKExecutionHelper.createGrammarFile(false);

        HTKExecutionHelper.createWordLists();

        HTKExecutionHelper.createDictionaryFile();

        //Make CONFIG file
        HTKExecutionHelper.createConfig();

        String command = binariesDir + "HParse" + EXECUTABLE_EXTENSION + " " + gramFilePath + " " + netFilePath;
        Helper.execCmd(command);

        //Now create templates for HMM - instead of trained models
//        String macrosFilePath = trainedModelsDir + File.separator + hmmFolder + File.separator + macros;
//        String hmmdefsFilePath = trainedModelsDir + File.separator + hmmFolder + File.separator + hmmDefs;
//        File modelsFolder = getFile(trainedModelsDir + File.separator + hmmFolder);
//        modelsFolder.mkdirs();
//        HMMDefCreator.main(new String[]{macrosFilePath, hmmdefsFilePath});
//        //Generate lattices
//        command = binariesDir + "HVite" + EXECUTABLE_EXTENSION + " " + PRUNING + " -C " + configPath +
//                " -H " + macrosFilePath + " ";
//        command = command + " -H " + hmmdefsFilePath + " " +
//                "-S " + fileListTest + " -i " + decodedOutPath + " -z lattice -n " + NBestCalculationLatticeOrder + " -q Atvaldmn" + " -w " + netFilePath + " " + PENALTY_BEATS_ARTIFICIAL + " " + dictFilePath + " " + wordListTestPath;
//        Helper.execCmd(command);

        //Recognizes from trained hmms
        command = binariesDir + "HVite" + EXECUTABLE_EXTENSION + " " + PRUNING + " -C " + configPath +
                " -H " + trainedModelsDir + File.separator + hmmFolder + File.separator + macros + " " +
                " -H " + trainedModelsDir + File.separator + hmmFolder + File.separator + hmmDefs + " ";
        for (int i = 1; i < NUMBER_OF_SEMITONES_IN_OCTAVE; i++) {
            command = command + " -H " + trainedModelsDir + File.separator + hmmFolder + File.separator + hmmDefs + i + " ";
        }
        command = command +
                "-S " + fileListTest + " -i " + decodedOutPath + " -z lattice -n " + NBestCalculationLatticeOrder + " -q Atvaldmn" + " -w " + netFilePath + " -p " + penalty + " " + dictFilePath + " " + wordListTestPath;
        Helper.execCmd(command);

        //different results are saved in different folders for future comparison
        String resultsDirName = "results";

        //Make evaluation without language modelling
        String recognizedFolder = resultsDir + File.separator + resultsDirName;
        HTKResultsParser.parse_OneVectorPerFrameVersion(decodedOutPath, recognizedFolder);

        EvaluatorOld.makeEvaluation(recognizedFolder, groundTruthFolder, resultsDir + File.separator + resultsDirName + ".txt");


        //Creating list of lattices
        String fileList = tempDirPath + File.separator + "latticeList";
        createFileList(extractedFeaturesDir, fileList, new ExtensionFileFilter(new String[]{LATTICE_EXT}), false);
        List<String> latticeFilePathList = HelperFile.readTokensFromTextFile(fileList, 1);

        //Transform all lattices into FLM originalFormat
//        if (IS_FACTORED_LM) {
        logger.info("Transforming lattices into FLM originalFormat");
        HTKLatticeTransformer.transformFolder(latticeFilePathList, true);
//        }

        //Assign Factored Language Model Weights
        logger.info("Assign Language Model Weights");
        String outLattice;
        File oldLattice, newLattice;
        for (String inLattice : latticeFilePathList) {
            outLattice = inLattice + "_";
            logger.info("processing lattice " + inLattice);
            if (IS_FACTORED_LM) {
                command = binariesDir + "/lattice-tool" + EXECUTABLE_EXTENSION + " -debug 0 -in-lattice \"" + inLattice + "\" -out-lattice \"" + outLattice + "\" -read-htk -write-htk -factored -lm " + lmDir + File.separator + LMSpecFactoredFileName + " -htk-logbase 2.71828 -no-nulls -no-htk-nulls -order " + latticeRescoringOrder;
            } else {
                command = binariesDir + "/lattice-tool" + EXECUTABLE_EXTENSION + " -debug 0 -in-lattice \"" + inLattice + "\" -out-lattice \"" + outLattice + "\" -read-htk -write-htk -factored -lm " + lmDir + File.separator + LMSpecFactoredFileName + " -htk-logbase 2.71828 -no-nulls -no-htk-nulls -order " + latticeRescoringOrder;
//                command = binariesDir + "/lattice-tool" + EXECUTABLE_EXTENSION + " -debug 0 -in-lattice " + inLattice + " -out-lattice " + outLattice + " -read-htk -write-htk -lm " + LMFilePath_conventional + " -htk-logbase 2.71828 -no-nulls -no-htk-nulls -order " + latticeRescoringOrder;
            }
            Helper.execCmd(command);

            oldLattice = getFile(inLattice);
            newLattice = getFile(outLattice);
            oldLattice.delete();
            newLattice.renameTo(oldLattice);
        }


        //Rescoring lattices, applying different lm, ac weights and wip
        logger.info("Rescoring lattices...");
        float lmWeight;
        float acWeight;
        float wip;


        for (int i = 0; i < _lmWeights.length; i++) {
            for (int j = 0; j < _acWeights.length; j++) {
                for (int k = 0; k < _wips.length; k++) {
                    lmWeight = _lmWeights[i];
                    acWeight = _acWeights[j];
                    wip = _wips[k];

                    logger.info("LM " + lmWeight + "; AC " + acWeight + "WIP " + wip);
                    logger.info("--------------------------------------------------");

                    resultsDirName = "results_" + "l_" + lmWeight + "_a_" + acWeight + "_w_" + wip + "_factored_" + IS_FACTORED_LM;
                    recognizedFolder = resultsDir + File.separator + resultsDirName;
                    String outRescoredFile = tempDirPath + File.separator + "out_" + resultsDirName;

                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(outRescoredFile));
                        command = binariesDir + "/lattice-tool" + EXECUTABLE_EXTENSION + " -debug 0 -in-lattice-list " + fileList + " -read-htk -htk-lmscale " + lmWeight + " -htk-wdpenalty " + wip + " -htk-acscale " + acWeight + " -nbest-viterbi -viterbi-decode -output-ctm";
                        Helper.execCmd(command, writer);
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //Parsing and evaluating results
                    logger.info("Parsing results and evaluating");
                    HTKResultsParser.parseLM(outRescoredFile, recognizedFolder, true);
                    EvaluatorOld.makeEvaluation(recognizedFolder, groundTruthFolder, resultsDir + File.separator + resultsDirName + ".txt");
                }
            }
        }*/
    }
}
