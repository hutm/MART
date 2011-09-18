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

package org.mart.crs.exec.operation.models.training.chord;

import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.AbstractCRSOperation;
import org.mart.crs.exec.operation.OperationType;
import org.mart.crs.exec.operation.models.htk.Cons;
import org.mart.crs.exec.operation.models.htk.HMMPopulator;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.exec.scenario.stage.TrainFeaturesStage;
import org.mart.crs.exec.scenario.stage.TrainModelsStage;
import org.mart.crs.management.features.FeaturesManager;
import org.mart.crs.utils.filefilter.TailPlusExtensionFileFilter;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;
import org.mart.crs.utils.helper.HelperFile;

import java.io.*;

import static org.mart.crs.config.Settings.CHROMA_EXT;
import static org.mart.crs.config.Settings.EXECUTABLE_EXTENSION;
import static org.mart.crs.exec.scenario.stage.StageParameters.*;
import static org.mart.crs.utils.helper.HelperFile.*;

/**
 * @version 1.0 11-Jun-2010 15:44:52
 * @author: Hut
 */
public class TrainingAcousticModelsOperation extends AbstractCRSOperation {

    public static final int numberOfIterationsHERest = 6;

    protected String trainedModelsDir;
    protected String extractedFeaturesDir;


    protected String prototypePath;
    protected String hmmDirPath;
    protected String hedFilePath;
    protected String featureFileListTrain;


    /**
     * fileNameTail is used to train just models based on the training examples that has   fileNameTail ending in their file names.
     */
    protected String fileNameTail;
    protected int fileNameTailIndex;


    public TrainingAcousticModelsOperation(StageParameters stageParameters, ExecParams execParams) {
        super(stageParameters, execParams);
        fileNameTail = "";

        TrainModelsStage trainModelsStage = (TrainModelsStage) stageParameters.getStage(TrainModelsStage.class);
        TrainFeaturesStage trainFeaturesStage = ((TrainFeaturesStage) stageParameters.getStage(TrainFeaturesStage.class));

        this.trainedModelsDir = trainModelsStage.getHMMsDirPath();
        this.extractedFeaturesDir = trainFeaturesStage.getFeaturesDirPath();
    }

    public TrainingAcousticModelsOperation(StageParameters stageParameters, ExecParams execParams, String fileNameTail, int fileNameTailIndex) {
        this(stageParameters, execParams);
        this.fileNameTail = fileNameTail;
        this.fileNameTailIndex = fileNameTailIndex;
    }

    @Override
    public void initialize() {
        super.initialize();

        this.wordListTrainPath = String.format("%s%s", wordListTrainPath, fileNameTail);
        this.prototypePath = String.format("%s/%s", tempDirPath, PROTOTYPE);
        this.hmmDirPath = String.format("%s/%s", tempDirPath, HMM_DIR);
        this.hedFilePath = String.format("%s/%s", tempDirPath, HED_FILE);
        this.featureFileListTrain = String.format("%s/%s%s", tempDirPath, FEATURE_FILELIST_TRAIN, fileNameTail);

        createFileList(extractedFeaturesDir, featureFileListTrain, new TailPlusExtensionFileFilter(CHROMA_EXT, fileNameTail), true);

        //create directories for hmms
        File file = getFile(hmmDirPath);
        file.mkdirs();
        for (int i = 0; i <= numberOfIterationsHERest; i++) {
            file = getFile(hmmDirPath + File.separator + hmmFolder + i);
            file.mkdirs();
        }
        (getFile(hedFilePath)).mkdirs();

        if (FeaturesManager.featureSizes == null) {
            FeaturesManager.initializeFeatureSize();
        }
    }


    protected void initializeHMMModels(int statesNumber, String[] modelsArray, boolean append) {
        String command;
        //create prototype
        if (execParams.featureExtractors.length > 1) {
            createPrototype(prototypePath, statesNumber, HelperArrays.sum(FeaturesManager.featureSizes), execParams.isDiagonal, FeaturesManager.featureSizes.length, FeaturesManager.featureSizes);
        } else {
            createPrototype(prototypePath, statesNumber, HelperArrays.sum(FeaturesManager.featureSizes), execParams.isDiagonal);
        }

        logger.info("Initializing models...");
        command = String.format("%s/HCompV%s -C %s -f 0.01 -m -S %s  -M %s0 %s",
                binariesDir, EXECUTABLE_EXTENSION, configPath,
                featureFileListTrain, hmmDirPath + File.separator + hmmFolder, prototypePath);
        Helper.execCmd(command);

        createHmmDefs(modelsArray, append);
    }

    public void operate() {
        String command;

        if (!Settings.operationType.equals(OperationType.BEAT_OPERATION)) {
            initializeHMMModels(execParams.states, wordArrayForTrain, false);
        } else {
            initializeHMMModels(execParams.states, new String[]{NO_BEAT}, false);
            initializeHMMModels(execParams.statesBeat, new String[]{BEAT_END, BEAT_START, DOWNBEAT_END, DOWNBEAT_START}, true);
        }

        //Now training models increasing number of Gaussians
        for (int g = 1; g <= execParams.gaussianNumber; g *= 2) {
            logger.info("Training models with " + g + " mixures");
            String hedFile = String.format("%s/mix%d.hed", hedFilePath, g);

            int startHmmNumber = 0;
            //If necessary, make hed file
            if (g > 1) {
                startHmmNumber = 1;
                createHEDFile(g, execParams.states, wordArrayForTrain, hedFile);
                command = String.format("%s/HHEd%s -H %s -H %s -M %s %s %s", binariesDir, EXECUTABLE_EXTENSION,
                        hmmDirPath + File.separator + hmmFolder + 0 + File.separator + macros,
                        hmmDirPath + File.separator + hmmFolder + 0 + File.separator + hmmDefs,
                        hmmDirPath + File.separator + hmmFolder + 1 + File.separator,
                        hedFile, wordListTrainPath);
                Helper.execCmd(command);
            }

            //Now training models with splitted gaussians
            for (int i = startHmmNumber; i < numberOfIterationsHERest; i++) {
                command = String.format("%s/HERest%s -t 250.0 -C %s -I %s -S %s -H %s -H %s -M %s %s", binariesDir, EXECUTABLE_EXTENSION,
                        configPath, mlfFilePath, featureFileListTrain,
                        hmmDirPath + File.separator + hmmFolder + i + File.separator + macros,
                        hmmDirPath + File.separator + hmmFolder + i + File.separator + hmmDefs,
                        hmmDirPath + File.separator + hmmFolder + (i + 1), wordListTrainPath);
                Helper.execCmd(command);
            }

            //Now copy models to hmm0
            copyDirectory(getFile(hmmDirPath + File.separator + hmmFolder + numberOfIterationsHERest), getFile(hmmDirPath + File.separator + hmmFolder + 0));

            //Just to save HMMS with current number of Gaussians, copy them to separate folder
            copyDirectory(getFile(hmmDirPath + File.separator + hmmFolder + numberOfIterationsHERest), getFile(trainedModelsDir + File.separator + hmmFolder + "_" + g));
            String hmmFilePath = trainedModelsDir + File.separator + hmmFolder + "_" + g + File.separator + hmmDefs;

            if (fileNameTail.length() > 0) {
                HelperFile.renameFile(getFile(hmmFilePath), getFile(String.format("%s%d", hmmFilePath, fileNameTailIndex)), true);
            }


            //Now perform circular rotation to obtain all the models from the trained one
            if (Settings.operationType.equals(OperationType.CHORD_OPERATION) || Settings.operationType.equals(OperationType.KEY_OPERATION)) {
                HMMPopulator.populate(hmmFilePath);
            }
            if (Settings.operationType.equals(OperationType.BEAT_OPERATION) || Settings.operationType.equals(OperationType.BEAT_ONLY_OPERATION)) {
                HMMPopulator.transformTransitionMatrixForNoBeat(hmmFilePath);
            }
        }

    }


    protected void createPrototype(String outFile, int numberOfStates, int numberOfFeatures, boolean isDiagonalCovariance) {
        createPrototype(outFile, numberOfStates, numberOfFeatures, isDiagonalCovariance, 1, new int[]{numberOfFeatures});
    }


    protected void createPrototype(String outFile, int numberOfStates, int numberOfFeatures, boolean isDiagonalCovariance, int numberOfStreams, int[] streamVectorSizes) {
        try {
            FileWriter fileWriter = new FileWriter(getFile(outFile));
            fileWriter.write(Cons.BEGIN_HMM_TAG + "\n" +
                    Cons.NUM_STATES_TAG + numberOfStates + "\n" +
                    Cons.VEC_SIZE_TAG + numberOfFeatures + "\n");
            if (isDiagonalCovariance) {
                fileWriter.write(Cons.USER_TAG + "<nullD> <diagC>\n");
            } else {
                fileWriter.write(Cons.USER_TAG + "<nullD> <FullC>\n");
            }
            if (numberOfStreams == 1) {
                fileWriter.write(Cons.STREAM_INFO_TAG + " 1 " + numberOfFeatures + "\n");
            } else {
                fileWriter.write(String.format("%s %d", Cons.STREAM_INFO_TAG, numberOfStreams));
                for (int i = 0; i < streamVectorSizes.length; i++) {
                    fileWriter.write(String.format(" %d", streamVectorSizes[i]));
                }
                fileWriter.write("\n");
            }

            for (int i = 2; i < numberOfStates; i++) {
                fileWriter.write(Cons.STATE_TAG + i + "\n"
//                        + Cons.NUM_MIXES_TAG + "1\n" +
//                        Cons.MIXTURE_TAG + "1 1.0\n"
                );//Actualy these 2 additional tags are not necessary to put here. Moreover, when dealing with multi-stream vectors, they cause a fail in HCompVite

                for (int stream = 0; stream < streamVectorSizes.length; stream++) {
                    numberOfFeatures = streamVectorSizes[stream];
                    fileWriter.write(Cons.STREAM_TAG + " " + (stream + 1) + "\n" +
                            Cons.MEAN_TAG + numberOfFeatures + "\n");
                    for (int j = 0; j < numberOfFeatures; j++) {
                        fileWriter.write("0.0 ");
                    }
                    fileWriter.write("\n");
                    //print covariance
                    if (!isDiagonalCovariance) {
                        fileWriter.write(Cons.INV_COVAR_TAG + numberOfFeatures + "\n");
                        for (int j = 0; j < numberOfFeatures; j++) {
                            for (int k = 0; k < numberOfFeatures; k++) {
                                if (j == k) {
                                    fileWriter.write("1.0 ");
                                } else {
                                    if (k > j) {
                                        fileWriter.write("0.5 ");
                                    } else {
                                        fileWriter.write("    ");
                                    }
                                }
                            }
                            fileWriter.write("\n");
                        }
                    } else {
                        //Matrix is diagonal
                        fileWriter.write(Cons.VARIANCE_TAG + numberOfFeatures + "\n");
                        for (int j = 0; j < numberOfFeatures; j++) {
                            fileWriter.write("1.0 ");
                        }
                        fileWriter.write("\n");
                    }
                }
            }

            //Now write transition probabilities
            fileWriter.write(Cons.TRANSITION_MATRIX_TAG + numberOfStates + "\n");
            for (int i = 0; i < numberOfStates; i++) {
                for (int j = 0; j < numberOfStates; j++) {
                    if (j == 1 && i == 0) {
                        fileWriter.write("1.000e+0   ");
                    } else if (j == i && i != (numberOfStates - 1) && i != 0) {
                        fileWriter.write("9.900e-1   ");
//                        fileWriter.write("0.000e+0   ");
                    } else if (j == i + 1) {
                        fileWriter.write("0.100e-1   ");
//                        fileWriter.write("1.000e+0   ");
                    } else if (i == numberOfStates - 1) {
                        fileWriter.write("0.000e+0   ");
                    } else {
                        fileWriter.write("0.000e+0   ");
                    }
                }
                fileWriter.write("\n");
            }

            fileWriter.write(Cons.END_HMM_TAG + "\n");
            fileWriter.close();
        } catch (IOException e) {
            logger.error("Problems: ");
            logger.error(Helper.getStackTrace(e));
        }

    }


    protected void createHmmDefs(String[] chordArrayForTrain, boolean append) {
        try {
            BufferedReader readerProto = new BufferedReader(new FileReader(hmmDirPath + File.separator + hmmFolder + 0 + File.separator + PROTOTYPE));
            BufferedReader readerVFloors = new BufferedReader(new FileReader(hmmDirPath + File.separator + hmmFolder + 0 + File.separator + "vFloors"));
            FileWriter writerMacros = new FileWriter(hmmDirPath + File.separator + hmmFolder + 0 + File.separator + macros);
            FileWriter writerHmmdefs = new FileWriter(hmmDirPath + File.separator + hmmFolder + 0 + File.separator + hmmDefs, append);
            String lineProto, lineVFloors;
            while ((lineProto = readerProto.readLine()) != null && lineProto.length() > 1) {
                if (!lineProto.startsWith("~h")) {
                    writerMacros.write(lineProto + "\n");
                    if (!append) {
                        writerHmmdefs.write(lineProto + "\n");
                    }
                } else {
                    break;
                }
            }
            while ((lineVFloors = readerVFloors.readLine()) != null && lineVFloors.length() > 1) {
                writerMacros.write(lineVFloors + "\n");
            }
            readerVFloors.close();
            writerMacros.close();

            StringBuilder hmmRest = new StringBuilder();
            while ((lineProto = readerProto.readLine()) != null && lineProto.length() > 1) {
                hmmRest.append(lineProto).append("\n");
            }
            readerProto.close();

            for (String chord : chordArrayForTrain) {
                writerHmmdefs.write("~h \"" + chord + "\"\n");
                writerHmmdefs.write(hmmRest.toString());
            }
            writerHmmdefs.close();

        } catch (IOException e) {
            logger.error("There is a problem with IO");
            logger.error(Helper.getStackTrace(e));
        }
    }

    protected void createHEDFile(int numberOfGaussians, int numberOfStates, String[] chordArray, String outFilePath) {
        try {
            FileWriter writer = new FileWriter(getFile(outFilePath));
            String streamsInfo = "";
            if (execParams.featureExtractors.length > 1) {
                streamsInfo = String.format(".stream[1-%d]", execParams.featureExtractors.length);
            }
            for (String chord : chordArray) {
                if (numberOfStates == 3) {
                    writer.write(String.format("MU %d {%s.state[2]%s.mix}\n", numberOfGaussians, chord, streamsInfo));
                } else {
                    writer.write(String.format("MU %d {%s.state[2-%d]%s.mix}\n", numberOfGaussians, chord, (numberOfStates - 1), streamsInfo));
                }
            }
            writer.close();
        } catch (IOException e) {
            logger.error(Helper.getStackTrace(e));
        }
    }


}
