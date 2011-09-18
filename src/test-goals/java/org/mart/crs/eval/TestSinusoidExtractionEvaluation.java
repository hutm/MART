package org.mart.crs.eval;

import org.mart.crs.analysis.filterbank.FilterBankManager;
import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.core.AudioReader;
import org.mart.crs.exec.operation.eval.FMeasure;
import org.mart.crs.exec.operation.eval.sinusoid.SinusoidExtractorEvaluation;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.HelperFile;
import com.jamal.JamalException;
import com.jamal.client.MatlabClient;
import junit.framework.TestCase;

import java.io.File;

/**
 * @version 1.0 28-Apr-2010 14:50:52
 * @author: Hut
 */
public class TestSinusoidExtractionEvaluation extends TestCase {


    public void testMain() {
        String configDir = "d:/dev/CHORDS/CHORDS3.1/cfg/sineEstimation/";
        String outDir = "d:/dev/CHORDS/CHORDS3.1/data/sineEstimation/";

        //First generate sines
//        runDataGenerate(outDir, configDir);

        //Now process generated audio
        File[] dirsToProcess = HelperFile.getFile(outDir).listFiles();
        for (File dir : dirsToProcess) {
            String gtFile = String.format("%s/gt.txt", dir);
            String waveFile = String.format("%s/1.wav", dir);
            String transcriptionFile = String.format("%s/1.chan", dir);
            String evaluationFolder = String.format("%s/eval", dir);

            System.out.printf("Processing folder %s\r\n", dir.getName());

            //Run Transcription
            performTranscription(waveFile, transcriptionFile);

            //Now perform evaluation and store results in a file
            evaluate(gtFile, transcriptionFile, evaluationFolder);

            System.out.println("End");
        }


    }


    private void runDataGenerate(String outDir, String configDir) {
        File[] configFiles = HelperFile.getFile(configDir).listFiles(new ExtensionFileFilter(".cfg"));
        try {
            MatlabClient client = new MatlabClient();
            for (File configFile : configFiles) {
                System.out.printf("Creating data from config file %s\r\n", configFile.getName());
                client.executeMatlabFunction("generateWave", new Object[]{outDir, configFile.getPath()}, 0);
            }
        } catch (JamalException e) {
            e.printStackTrace();
        }
    }

    private void performTranscription(String waveFile, String transcriptionFile) {
        //Now perform transcription
        String configFilePath;
        configFilePath = Settings.FILTERBANK_CONFIG_PATH;
        FilterBankManager manager = new FilterBankManager(new AudioReader(waveFile), configFilePath, ExecParams._initialExecParameters._initialExecParameters.PQMFBasedSpectrumFrameLength);
        //TODO incompatible witht the curent version of Manager
//        manager.detectPeriodicities();
//        manager.exportDetectedPeriodicities(transcriptionFile);
    }

    private void evaluate(String gtFile, String transcriptionFile, String evaluationFolder) {
        SinusoidExtractorEvaluation sinusoidExtractorEvaluation = new SinusoidExtractorEvaluation(gtFile, transcriptionFile);
        FMeasure fMeasure = sinusoidExtractorEvaluation.evaluate();
        fMeasure.calculateGlobalValues();
        fMeasure.storeResults(evaluationFolder);
    }

    public void testSummarize() {
        FMeasure.summarize("d:/dev/CHORDS/CHORDS3.1/data/sineEstimation/", "d:/dev/CHORDS/CHORDS3.1/data/sineEstimation/summary.txt");
    }
}
