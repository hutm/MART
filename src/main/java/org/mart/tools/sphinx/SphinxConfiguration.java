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

package org.mart.tools.sphinx;

import org.apache.log4j.Logger;
import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.features.FeaturesExtractionOperation;
import org.mart.crs.exec.operation.models.htk.HTKResultsParser;
import org.mart.crs.exec.operation.models.htk.parser.chord.ChordHTKParser;
import org.mart.crs.exec.operation.models.htk.parser.chord.ChordHTKParserFromLattice;
import org.mart.crs.exec.operation.models.lm.TextForLMCreator;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.beat.BeatsManager;
import org.mart.crs.management.features.FeaturesManagerSphinx;
import org.mart.crs.management.features.extractor.FeaturesExtractorHTK;
import org.mart.crs.management.label.LabelsParser;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.management.label.chord.ChordType;
import org.mart.crs.management.label.chord.Root;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;

import java.io.*;
import java.util.*;

import static org.mart.crs.config.Settings.IS_FACTORED_LM;
import static org.mart.crs.config.Settings.IS_FACTORED_LM_FOR_STANDARD_VERSION;
import static org.mart.crs.config.Settings.WAV_EXT;

/**
 * Configuration issues are addressed here
 *
 * @version 1.0 2/23/12 12:34 PM
 * @author: Hut
 */
public class SphinxConfiguration {

    protected static Logger logger = CRSLogger.getLogger(SphinxConfiguration.class);

    protected String outDirectory;

    protected String etcDirectory;
    protected String taskName;

    protected String dictFile;
    protected String phoneFile;
    protected String lmFile;
    protected String jsgfFile;
    protected String fillerFile;
    protected String trainListFile;
    protected String testListFile;
    protected String trainTranscriptionFile;
    protected String testTranscriptionFile;
    protected String featuresDir;
    protected String featuresDirTrain;
    protected String featuresDirTest;
    protected String transcriptCTMfile;

    protected String tempDir;

    public SphinxConfiguration(String etcDirectory, String taskName, String outDirectory) {
        this.outDirectory = outDirectory;
        this.etcDirectory = etcDirectory;
        this.taskName = taskName;

        HelperFile.createDir(etcDirectory);

        dictFile = String.format("%s/%s.dic", etcDirectory, taskName);
        phoneFile = String.format("%s/%s.phone", etcDirectory, taskName);
        lmFile = String.format("%s/%s.lm", etcDirectory, taskName);
        jsgfFile = String.format("%s/%s.jsgf", etcDirectory, taskName);

        fillerFile = String.format("%s/%s.filler", etcDirectory, taskName);
        trainListFile = String.format("%s/%s_train.fileids", etcDirectory, taskName);
        testListFile = String.format("%s/%s_test.fileids", etcDirectory, taskName);
        trainTranscriptionFile = String.format("%s/%s_train.transcription", etcDirectory, taskName);
        testTranscriptionFile = String.format("%s/%s_test.transcription", etcDirectory, taskName);
        featuresDir = String.format("%s/../feat", etcDirectory);
        featuresDirTest = String.format("%s/../feat/test", etcDirectory);
        featuresDirTrain = String.format("%s/../feat/train", etcDirectory);
        transcriptCTMfile = String.format("%s/../result/chordsSphinx-1-1.matchCTM", etcDirectory);
        tempDir = String.format("%s/temp", etcDirectory);
    }


    public void run() {
        createDictionaryFile();
        createWordLists();
//        extractFeatures();
        createTrainTranscription();
//        createLM();
        createJsgf();
    }

    public void createDictionaryFile() {

        try {
            FileWriter writer = new FileWriter(HelperFile.getFile(dictFile));
            for (ChordType modality : ChordType.chordDictionary) {
                if (!modality.equals(ChordType.NOT_A_CHORD)) {
                    for (Root root : Root.values()) {
                        writer.write(String.format("%s%s %s%s\n", root, modality, root, modality));
                    }
                }
            }
            if (Arrays.asList(ChordType.chordDictionary).contains(ChordType.NOT_A_CHORD)) {
                writer.write(String.format("%s %s\n", ChordType.NOT_A_CHORD, ChordType.NOT_A_CHORD));
                writer.write(String.format("%s(2) SIL\n", ChordType.NOT_A_CHORD));
            }
            writer.close();
        } catch (IOException e) {
            logger.error("Problems: ");
            logger.error(Helper.getStackTrace(e));
        }
    }


    public void createWordLists() {
        try {
            FileWriter fileWriter = new FileWriter(HelperFile.getFile(phoneFile));
            for (ChordType modality : ChordType.chordDictionary) {
                if (!modality.equals(ChordType.NOT_A_CHORD)) {
                    for (Root root : Root.values()) {
                        fileWriter.write(String.format("%s%s\r\n", root, modality));
                    }
                }
            }
            if (Arrays.asList(ChordType.chordDictionary).contains(ChordType.NOT_A_CHORD)) {
                fileWriter.write(String.format("%s\r\n", ChordType.NOT_A_CHORD));
                fileWriter.write("SIL\r\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            logger.error("Problems: ");
            logger.error(Helper.getStackTrace(e));
        }


        try {
            FileWriter fileWriter = new FileWriter(HelperFile.getFile(fillerFile));
            fileWriter.write(String.format("%s %s\r\n", LabelsParser.START_SENTENCE, ChordType.NOT_A_CHORD));
            fileWriter.write(String.format("%s %s\r\n", LabelsParser.END_SENTENCE, ChordType.NOT_A_CHORD));

            fileWriter.close();
        } catch (IOException e) {
            logger.error("Problems: ");
            logger.error(Helper.getStackTrace(e));
        }


    }


    public void createJsgf(){

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(jsgfFile));

            writer.write("#JSGF 1.0 ISO8559-1;\r\ngrammar chordSequence;\r\n");
            writer.write("public <chordSequence> = <chord>+;\r\n");
            writer.write("<chord> = ");
            for (ChordType modality : ChordType.chordDictionary) {
                if (!modality.equals(ChordType.NOT_A_CHORD)) {
                    for (Root root : Root.values()) {
                        writer.write(String.format("%s%s | ", root, modality));
                    }
                }
            }

            writer.write(ChordType.NOT_A_CHORD.toString() + ";\r\n");

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createLM() {
//        HelperFile.copyFile("data/sphinx/language_model_stnd.lm", lmFile); //TODO remove hardcode

        HelperFile.createDir(tempDir);

        //First create text for standard language model
        String textFilePath = String.format("%s/text_lan_model_standard", tempDir);
        (new TextForLMCreator()).process(ExecParams._initialExecParameters._waveFilesTrainFileList, textFilePath, false);

        String command = String.format("%s/ngram-count -text %s -order %d -wbdiscount -lm %s", StageParameters.binariesDir, textFilePath, ExecParams._initialExecParameters.standardLmOrder, lmFile);
        Helper.execCmd(command);
    }


    public void parseOutput() {
        ChordHTKParserFromLattice paser = new ChordHTKParserFromLattice(transcriptCTMfile, outDirectory);
        paser.run();
    }


    public void extractFeatures() {

        FeaturesExtractionOperation operation = new FeaturesExtractionOperation(ExecParams._initialExecParameters._waveFilesTrainFileList, true, featuresDirTrain, Settings.tuningsGroudTruthFilePath);
        operation.initializeSphinx();
        operation.operate();

        FeaturesExtractionOperation operation1 = new FeaturesExtractionOperation(ExecParams._initialExecParameters._waveFilesTestFileList, false, featuresDirTest, Settings.tuningsGroudTruthFilePath);
        operation1.initializeSphinx();
        operation1.operate();
    }

    public void createTrainTranscription() {
        List<String> filesToUseForTraining = new ArrayList<String>();
        List<String> filesToUseForTest = new ArrayList<String>();

        List<String> lines = new ArrayList<String>();
        File[] songDirs = HelperFile.getFile(featuresDirTrain).listFiles();
        for (File song : songDirs) {
            File[] featureFiles = song.listFiles();
            for (File featureFile : featureFiles) {
                String currentChord;
                String name = HelperFile.getNameWithoutExtension(featureFile.getName());
                String ending = name.substring(name.lastIndexOf("_") + 1);
                if (ending.endsWith(LabelsParser.NOT_A_CHORD)) {
                    currentChord = LabelsParser.NOT_A_CHORD;
                } else {
                    currentChord = ending;
                }
                lines.add(String.format("<s> %s </s> (%s)", currentChord, HelperFile.getNameWithoutExtension(featureFile.getName())));
                filesToUseForTraining.add(String.format("train/%s/%s", song.getName(), HelperFile.getNameWithoutExtension(featureFile.getName())));
            }
        }
        HelperFile.saveCollectionInFile(lines, trainTranscriptionFile, false);
        HelperFile.saveCollectionInFile(filesToUseForTraining, trainListFile, false);

        File[] songDirsTest = HelperFile.getFile(featuresDirTest).listFiles();
        for (File song : songDirsTest) {
            File[] featureFiles = song.listFiles();
            for (File featureFile : featureFiles) {
                filesToUseForTest.add(String.format("test/%s/%s", song.getName(), HelperFile.getNameWithoutExtension(featureFile.getName())));
            }
        }
        HelperFile.saveCollectionInFile(filesToUseForTest, testListFile, false);

    }


    public static void main(String[] args) {
        String dir = "/home/hut/work/testSphinx4/";
        SphinxConfiguration configuration = new SphinxConfiguration(dir + "etc", "chordsSphinx", dir + "out/");
//        configuration.run();
        configuration.parseOutput();



//        configuration.createLM();
    }


}
