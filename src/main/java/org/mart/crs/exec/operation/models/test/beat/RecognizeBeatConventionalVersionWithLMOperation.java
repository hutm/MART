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

package org.mart.crs.exec.operation.models.test.beat;

import org.mart.crs.config.ConfigSettings;
import org.mart.crs.config.ExecParams;
import org.mart.crs.exec.operation.AbstractCRSOperation;
import org.mart.crs.exec.operation.models.htk.HTKResultsParserBeat;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.management.beat.BeatStructure;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.mart.crs.config.Settings.*;
import static org.mart.crs.exec.scenario.stage.StageParameters.LMModelStandardVersion;
import static org.mart.crs.exec.scenario.stage.StageParameters.binariesDir;
/**
 * @version 1.0 12/20/10 12:35 PM
 * @author: Hut
 */
public class RecognizeBeatConventionalVersionWithLMOperation extends RecognizeBeatOperation {

    public RecognizeBeatConventionalVersionWithLMOperation(StageParameters stageParameters, ExecParams execParams) {
        super(stageParameters, execParams);
    }


    @Override
    public void initialize() {
        super.initialize();
        isToOutputLattices = true;
    }


    /**
     * This method is used for applying lms to convevtional version without preliminary segmenting
     * according to the beat structure
     */
    public void operate() {
        super.operate();

        //Creating list of lattices
        String fileList = String.format("%s/latticeList_%d_%3.2f.txt", tempDirPath, gaussianNumber, penalty);
        HelperFile.createFileList(extractedFeaturesDir, fileList, new ExtensionFileFilter(LATTICE_EXT), false);
        List<String> latticeFilePathList = HelperFile.readLinesFromTextFile(fileList);


        //Assign Language Model Weights
        AbstractCRSOperation.logger.info("Assign Language Model Weights...");
        String outLattice;
        String command;
        File oldLattice, newLattice;
        for (String inLattice : latticeFilePathList) {
            outLattice = inLattice + "_";
            AbstractCRSOperation.logger.debug("processing lattice " + inLattice);

            command = binariesDir + "/lattice-tool" + EXECUTABLE_EXTENSION + " -debug 0 -in-lattice " + inLattice + " -out-lattice " + outLattice + " -read-htk -write-htk -lm " + lmDir + File.separator + LMModelStandardVersion + " -htk-logbase 2.71828 -no-nulls -no-htk-nulls -order " + execParams.latticeRescoringOrder;
            Helper.execCmd(command, null, false);

            oldLattice = HelperFile.getFile(inLattice);
            newLattice = HelperFile.getFile(outLattice);
            oldLattice.delete();
            newLattice.renameTo(oldLattice);
        }

        //Rescoring lattices, applying different lm, ac weights and wip
        AbstractCRSOperation.logger.info("Rescoring lattices...");
        float lmWeight;
        float acWeight;
        float wip;


        for (int i = 0; i < execParams._lmWeights.length; i++) {
            for (int j = 0; j < execParams._acWeights.length; j++) {
                for (int k = 0; k < execParams._wips.length; k++) {
                    lmWeight = execParams._lmWeights[i];
                    acWeight = execParams._acWeights[j];
                    wip = execParams._wips[k];

                    AbstractCRSOperation.logger.info("LM " + lmWeight + "; AC " + acWeight + "WIP " + wip);
                    AbstractCRSOperation.logger.info("--------------------------------------------------");

                    String resultsDirName = String.format("lmWeight_%3.2f%sacWeight_%3.2f%swip_%3.2f", lmWeight, FIELD_SEPARATOR, acWeight, FIELD_SEPARATOR, wip);
                    String recognizedFolder = resultsDir + File.separator + resultsDirName;
//                    LabelsManager.recognizedFolder_compare = recognizedFolder; //This is done in order to estimate the advantages of LM
                    String outRescoredFile = tempDirPath + File.separator + "out_" + resultsDirName;

                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(outRescoredFile));
                        command = binariesDir + "/lattice-tool" + EXECUTABLE_EXTENSION + " -debug 0 -in-lattice-list " + fileList + " -read-htk -htk-lmscale " + lmWeight + " -htk-wdpenalty " + wip + " -htk-acscale " + acWeight + " -nbest-viterbi -output-ctm -viterbi-decode"; //-posterior-decode\"
                        Helper.execCmd(command, writer, false);
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //Parsing and evaluating results
                    AbstractCRSOperation.logger.info("Parsing results and evaluating");

//                    BeatHTKParserLattice beatHTKParserLattice = new BeatHTKParserLattice(outRescoredFile, recognizedFolder);
//                    beatHTKParserLattice.run();  //TODO migrate to BeatHTKParserLattice and to BeatHTKParser from static classes
                    HTKResultsParserBeat.parseLM(outRescoredFile, recognizedFolder, ExecParams._initialExecParameters);
                    if(isMIREX || isQUAERO){
                        String inFilePath = HelperFile.getFile(recognizedFolder).listFiles()[0].getPath();
                        if(isQUAERO){
                            HelperFile.copyFile(inFilePath, ConfigSettings.outPathExternal);
                        } else{
                            BeatStructure beatStructure = BeatStructure.getBeatStructure(inFilePath);
                            beatStructure.serializeIntoTXT(ConfigSettings.outPathExternal, false);
                        }
                        HelperFile.getFile(inFilePath).delete();
                    }
//                    EvaluatorOld.makeEvaluation(recognizedFolder, labelsGroundTruthDir, resultsDir + File.separator + resultsDirName + ".txt");
                }
            }
        }

        for (String inLattice : latticeFilePathList) {
            HelperFile.getFile(inLattice).delete();
        }
    }


}
