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

package org.mart.crs.exec.operation.features;

import org.mart.crs.config.ExecParams;
import org.mart.crs.exec.scenario.stage.StageParameters;

/**
 * @version 1.0 11-Jun-2010 17:53:20
 * @author: Hut
 */
public class FeaturesExtractionLMOperation extends FeaturesExtractionOperation {

    public FeaturesExtractionLMOperation(StageParameters stageParameters, ExecParams execParams, String fileListPath, boolean isForTraining, int numberOfParralelThreads) {
        super(stageParameters, execParams);
    }


  //TODO  
//package org.mart.crs.management.audio;
//
//import Settings;
//
//import org.mart.crs.core.pcpList.spectral.PCP;
//import SpectrumImpl;
//import HTKResultsParser;
//import FeatureVector;
//import FeaturesManager;
//import org.mart.crs.hmm.HTK.execute.CRSExecSettings;
//import ChordSegment;
//import KeyManager;
//import LabelsParser;
//import TextForLMCreator;
//import Helper;
//
//import java.io.File;
//import java.util.List;
//
//import static Settings.*;
//import static org.mart.crs.hmm.HTK.execute.CRSExecSettings.BEATS_DIR;
//import static HelperFile.getFile;
//import static HelperFile.getPathForFileWithTheSameName;
//
///**
// * Created by IntelliJ IDEA.
// * User: hut
// * Date: Apr 2, 2009
// * Time: 7:48:48 PM
// * To change this template use File | Settings | File Templates.
// */
//public class AudioCollectionManagerLM extends AudioCollectionManager {
//    /**
//     * Constructor
//     *
//     * @param fileListPath  wavDirPath
//     * @param outDirPath    outDirPath
//     * @param isForTraining isForTraining
//     */
//    public AudioCollectionManagerLM(String fileListPath, String outDirPath, boolean isForTraining) {
//        super(fileListPath, outDirPath, isForTraining);
//    }
//
//
//    public void extractFeaturesForSong(String songFilePath) {
//        String wavFilePath = songFilePath;
//        String shortName = (getFile(songFilePath)).getName();
//        logger.info("Processing file " + wavFilePath + " ...");
//        AudioReaderImpl audio = new AudioReaderImpl(wavFilePath, Settings.samplingRate, 1);
//
//
//        if (isToUseRefFreq) {
//            REFERENCE_FREQUENCY = ReferenceFreqManager.getRefFreqForSong((getFile(wavFilePath).getName()));
//        }
//
//        float[] samples;
//        float[][] pcpChord, pcpForStore;
//        SpectrumImpl spektr;
//        PCP pcpList;
//        String filename, fileNameToStore;
//        int chrSamplingRate = (int) HTKResultsParser.FEATURE_SAMPLE_RATE; //This parameter does not influence anything
//
//        String dirName = outDir + File.separator + (shortName.subSequence(0, (shortName.lastIndexOf("."))));
//
//
//        Float[] beats = BeatsManager.getBeatsForWavFile(wavFilePath);
//        pcpForStore = new float[beats.length - 1][];
//        float onset, offset;
//        for (int i = 0; i < beats.length - 1; i++) {
//            onset = beats[i];
//            offset = beats[i + 1];
//                                            //getSamplesForSpectrumCalculation
//            spektr = new SpectrumImpl(audio.getSamples(onset, offset), audio.getSampleRate());
//            pcpList = new PCP(spektr, EXTRACTION_ALGORITHM);
//            pcpForStore[i] = pcpList.calculateGlobalValue();
//        }
//
//
//        if (isForTraining) {
//            String lblFilePath = fileListWithLabels.get(songFilePath);
//            List<ChordSegment> segments = LabelsParser.getSegments(lblFilePath, true);
//
//            String beatFilePath = getPathForFileWithTheSameName((getFile(lblFilePath)).getName(), BEATS_DIR, BEAT_EXT);
//
//            if (beatFilePath != null) {
//                List<String> beatList = Helper.readTokensFromTextFile(beatFilePath, 1);
//                String[] beatsArray = new String[beatList.size()];
//                beatList.toArray(beatsArray);
//                List<ChordSegment> chordDurations = TextForLMCreator.getChordSegmentsList(beatsArray, segments);
//
//                int duration, currentIndexInPCP = 0;
//                for (ChordSegment chordSegment : chordDurations) {
//                    duration = Math.round(chordSegment.getOffset());
//                    pcpChord = new float[duration][];
//
//                    for (int i = currentIndexInPCP; i < currentIndexInPCP + duration; i++) {
//                        pcpChord[i - currentIndexInPCP] = pcpForStore[i];
//                    }
//
//                    //Now store chroma
//                    if (!(chordSegment.getChordName()).equals(LabelsParser.NOT_A_CHORD) && !(chordSegment.getChordName()).equals(LabelsParser.UNKNOWN_CHORD)) {
//                        chordSegment.setOnset(chordSegment.getOnset() + currentIndexInPCP);
//                        chordSegment.setOffset(chordSegment.getOffset() + currentIndexInPCP);
//                        filename = new StringBuilder().append((String.valueOf(chordSegment.getOnset())).substring(0, Math.min((String.valueOf(chordSegment.getOnset())).length(), 6))).append("_").append((String.valueOf(chordSegment.getOffset())).substring(0, Math.min((String.valueOf(chordSegment.getOffset())).length(), 6))).append("_").append("C").append(LabelsParser.getChordType(chordSegment.getChordName())).append(CHROMA_EXT).toString();
//                        // Now rotate pcpList to C or Cm
//                        pcpChord = PCP.rotatePCP(pcpChord, chordSegment.getChordName(), LabelsParser.TONE_NAMES[0]);
//                        fileNameToStore = new StringBuilder().append(dirName).append(File.separator).append(filename).toString();
//                        FeaturesManager.storeDataInHTKFormat(fileNameToStore, new FeatureVector(pcpChord, chrSamplingRate));
//                    }
//                    currentIndexInPCP += duration;
//
//
//                }
//            }
//
//        } else {
//
//            filename = new StringBuilder().append("0_").append((String.valueOf(audio.getDuration())).substring(0, Math.min((String.valueOf(audio.getDuration())).length(), 6))).append("_").append(CHROMA_EXT).toString();
//            fileNameToStore = new StringBuilder().append(dirName).append(File.separator).append(filename).toString();
//
//
//            //The following lines make transform from the given key to the base (C or Am)
//            if (USE_BASE_KEY_TRANSFORM) {
//                String keyFrom = KeyManager.getKeyForSong(shortName, false);
//                String keyTo = LabelsParser.getKeyForStandartTransform(keyFrom);
//                pcpForStore = PCP.rotatePCP(pcpForStore, keyFrom, keyTo);
//            }
//
//
//            FeaturesManager.storeDataInHTKFormat(fileNameToStore, new FeatureVector(pcpForStore, chrSamplingRate));
//        }
//
//
//    }
//}



}
