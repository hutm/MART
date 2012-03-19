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

package org.mart.crs.management.features;

import org.apache.log4j.Logger;
import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.core.AudioReader;
import org.mart.crs.exec.operation.models.htk.parser.chord.ChordHTKParser;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.audio.ReferenceFreqManager;
import org.mart.crs.management.beat.BeatStructure;
import org.mart.crs.management.beat.segment.BeatSegment;
import org.mart.crs.management.features.extractor.FeaturesExtractorHTK;
import org.mart.crs.management.features.extractor.chroma.SpectrumBased;
import org.mart.crs.management.features.extractor.unused.EllisBassReas;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.management.label.chord.ChordType;
import org.mart.crs.management.label.chord.Root;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;
import org.mart.crs.utils.helper.HelperFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mart.crs.config.Settings.*;
import static org.mart.crs.utils.helper.HelperData.*;
import static org.mart.crs.utils.helper.HelperFile.*;

/**
 * @version 1.0 Nov 10, 2009 11:26:44 AM
 * @author: Maksim Khadkevich
 */
public class FeaturesManager {

    protected static Logger logger = CRSLogger.getLogger(FeaturesManager.class);

    public static final String packageExtractors = "org.mart.crs.management.features.extractor.";

    protected ExecParams execParams;

    protected List<FeaturesExtractorHTK> featureExtractorList;
    public static int[] featureSizes;
    public int chrSamplingPeriod;

    protected float songDuration;


    public FeaturesManager(String songFilePath, String outDirPath, boolean isForTraining, ExecParams execParams) {
        this.execParams = execParams;
        this.outDirPath = outDirPath;
        this.isForTraining = isForTraining;
        this.songFilePath = songFilePath;
        reinitializeFeaturesManager();
        initializeWithSong(songFilePath);
    }


    protected String outDirPath;
    protected boolean isForTraining;

    /**
     * The song filePath to be processed
     */
    protected String songFilePath;


    /**
     * This is a copy of the static list to process
     */
    protected List<FeaturesExtractorHTK> featureExtractorToWorkWith;


    public void reinitializeFeaturesManager() {
        chrSamplingPeriod = getChrSamplingPeriod(execParams);

        featureExtractorList = new ArrayList<FeaturesExtractorHTK>();
        featureSizes = new int[execParams.featureExtractors.length];

        int counter = 0;
        for (String extractor : execParams.featureExtractors) {
            try {
                Class featureExtractorClass = Class.forName(packageExtractors + extractor);
                FeaturesExtractorHTK newExtractor = (FeaturesExtractorHTK) featureExtractorClass.newInstance();
                featureExtractorList.add(newExtractor);
                featureSizes[counter] = newExtractor.getVectorSize();
                if (execParams.extractDeltaCoefficients) {
                    featureSizes[counter] *= 2;
                }
                counter++;
            } catch (Exception e) {
                logger.error(String.format("Cannon instantiate class %s", extractor));
                logger.error(Helper.getStackTrace(e));
            }
        }
    }


    public static void initializeFeatureSize() {
        int counter = 0;
        featureSizes = new int[ExecParams._initialExecParameters.featureExtractors.length];
        for (String extractor : ExecParams._initialExecParameters.featureExtractors) {
            try {
                Class featureExtractorClass = Class.forName(packageExtractors + extractor);
                FeaturesExtractorHTK newExtractor = (FeaturesExtractorHTK) featureExtractorClass.newInstance();
                featureSizes[counter] = newExtractor.getVectorSize();
                if (ExecParams._initialExecParameters.extractDeltaCoefficients) {
                    featureSizes[counter] *= 2;
                }
                counter++;
            } catch (Exception e) {
                logger.error(String.format("Cannon instantiate class %s", extractor));
                logger.error(Helper.getStackTrace(e));
            }
        }
    }


    public static int getChrSamplingPeriod(ExecParams execParams) {
        return (int) Math.floor(execParams.windowLength / execParams.samplingRate * ChordHTKParser.FEATURE_SAMPLE_RATE * (1 - execParams.overlapping) * execParams.pcpAveragingFactor);
    }


    public void extractFeaturesForSong(ReferenceFreqManager referenceFreqManager) {
        float refFrequency = referenceFreqManager.getRefFreqForSong(HelperFile.getShortFileName(songFilePath));

        logger.info(String.format("Processing file %s with reference freq %5.3f", HelperFile.getShortFileName(songFilePath), refFrequency));
        String dirName = outDirPath + File.separator + getNameWithoutExtension(songFilePath);

        processSong(refFrequency, dirName);
    }


    protected void processSong(float refFrequency, String dirName) {
        if (isForTraining) {
            exctractForTraining(refFrequency, dirName);
        } else {
            if (Settings.isBeatSynchronousDecoding) {
                exctractForTestBeatSynchronous(refFrequency, dirName);
            } else {
                extractForTest(refFrequency, dirName);
            }
        }
    }


    /**
     * calculate index in the feature vector array that corresponds to the given time instant
     *
     * @param timeInstant timeInstant
     * @return index
     */
    public static int getIndexForTimeInstant(double timeInstant, ExecParams execParams) {
        return (int) Math.floor(timeInstant / (getChrSamplingPeriod(execParams) / ChordHTKParser.FEATURE_SAMPLE_RATE));
    }

    public static double getTimePeriodForFramesNumber(int frames, ExecParams execParams) {
        return frames * (getChrSamplingPeriod(execParams) / ChordHTKParser.FEATURE_SAMPLE_RATE);
    }


    public void initializeWithSong(String songFilePath) {
        AudioReader audioReader = new AudioReader(songFilePath, execParams.samplingRate);
        this.songDuration = audioReader.getDuration();
        featureExtractorToWorkWith = new ArrayList<FeaturesExtractorHTK>();
        for (FeaturesExtractorHTK featuresExtractor : featureExtractorList) {
            FeaturesExtractorHTK anExtractorToWork = null;
            try {
                anExtractorToWork = featuresExtractor.getClass().newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            anExtractorToWork.setExecParams(execParams);
            featureExtractorToWorkWith.add(anExtractorToWork);

            if (anExtractorToWork instanceof SpectrumBased || anExtractorToWork instanceof EllisBassReas) {
                anExtractorToWork.initialize(audioReader);
            } else {
                anExtractorToWork.initialize(songFilePath);
            }
        }
    }


    public void exctractForTraining(float refFrequency, String dirName) {
        String lblFilePath;
        if (!Settings.isMIREX) {
            lblFilePath = HelperFile.getPathForFileWithTheSameName(songFilePath, Settings.labelsGroundTruthDir, Settings.LABEL_EXT);
        } else {
            lblFilePath = songFilePath + ".txt";
        }

        if (lblFilePath == null) {
            logger.warn(String.format("Lablels for file %s were not found in directory %s", songFilePath, Settings.labelsGroundTruthDir));
            return;
        }

        List<ChordSegment> segments = (new ChordStructure(lblFilePath)).getChordSegments();

        for (int i = 0; i < segments.size(); i++) {

            ChordSegment curSegment = segments.get(i);
            if (!(curSegment.getChordType()).equals(ChordType.UNKNOWN_CHORD)) {


                //Skip unnecessary chord segments
                if (isToUseChordWrappersToTrainChordChildren) {
                    //In the current configuration all "wrapper"  chords are used to trained their reduced versions
                    if (!Arrays.asList(Settings.chordDictionary).contains(curSegment.getChordType().getName())) {
                        continue;
                    }
                } else {
                    //In this case only the chords themselves are used to train models, without wrappers
                    if (!Arrays.asList(ChordType.chordDictionary).contains(curSegment.getChordType())) {
                        continue;
                    }
                }


                String filename = String.format("%5.3f_%5.3f_%s%s", curSegment.getOnset(), curSegment.getOffset(), curSegment.getChordType().getName(), Settings.CHROMA_EXT);
                double startTime = segments.get(i).getOnset();
                double endTime = segments.get(i).getOffset();


                List<float[][]> features;
                String fileNameToStore;
                if (!(curSegment.getChordType() == ChordType.NOT_A_CHORD || isToSaveRotatedFeatures())) {
                    features = new ArrayList<float[][]>();
                    for (FeaturesExtractorHTK featuresExtractor : featureExtractorToWorkWith) {
                        float[][] feature = featuresExtractor.extractFeatures(startTime, endTime, refFrequency, curSegment.getRoot());
                        features.add(feature);
                    }

                    fileNameToStore = String.format("%s/%s", dirName, filename);
                    storeDataInHTKFormat(fileNameToStore, new FeatureVector(features, chrSamplingPeriod));
                }


                //For NOT_A_CHORD add all possible rotations of chroma
                if (curSegment.getChordType() == ChordType.NOT_A_CHORD || isToSaveRotatedFeatures()) {
                    String chordTypeName = curSegment.getChordType().getName();
                    for (int rotation = 0; rotation < ChordSegment.SEMITONE_NUMBER; rotation++) {
                        features = new ArrayList<float[][]>();
                        int newRootIndex = 0;
                        if (curSegment.getChordType() == ChordType.NOT_A_CHORD) {
                            newRootIndex = rotation;
                        } else {
                            newRootIndex = HelperArrays.transformIntValueToBaseRange(curSegment.getRoot().ordinal() + rotation, Root.values().length);
                        }
                        int newRootLabelIndex = HelperArrays.transformIntValueToBaseRange(-1 * rotation, Root.values().length);
                        for (FeaturesExtractorHTK featuresExtractor : featureExtractorToWorkWith) {
                            float[][] feature = featuresExtractor.extractFeatures(startTime, endTime, refFrequency, Root.values()[newRootIndex]);
                            features.add(feature);
                        }

                        String filePath;
                        if (Settings.isSphinx) {
                            if (curSegment.getChordType() == ChordType.NOT_A_CHORD) {
                                filePath = filename.replaceAll(chordTypeName, String.format("%s%s", Root.values()[newRootLabelIndex].getName(), chordTypeName));
                            } else {
                                filePath = filename.replaceAll(chordTypeName, String.format("%s%s", Root.values()[newRootLabelIndex].getName(), chordTypeName));
                            }
                        } else {
                            if (curSegment.getChordType() == ChordType.NOT_A_CHORD) {
                                filePath = filename.replaceAll(chordTypeName, String.format("%s%s", Root.values()[newRootLabelIndex].getName(), chordTypeName));
                            } else {
                                filePath = filename.replaceAll(chordTypeName, String.format("%s%s", chordTypeName, Root.values()[newRootLabelIndex].getName()));
                            }
                        }

                        fileNameToStore = String.format("%s/%s", dirName, filePath);
                        storeDataInHTKFormat(fileNameToStore, new FeatureVector(features, chrSamplingPeriod));
                    }
                }
            }
        }
    }


    /**
     * If necessary, perform circular roration and save
     */
    protected boolean isToSaveRotatedFeatures() {
        return false;
    }


    public void extractForTest(float refFrequency, String dirName) {
        FeatureVector featureVector = extractFeatureVectorForTest(refFrequency);

        String filenameToSave = new StringBuilder().append(dirName).append(File.separator).append(featureVector.getFileNameToStoreTestVersion()).toString();
        storeDataInHTKFormat(filenameToSave, featureVector);
    }


    public FeatureVector extractFeatureVectorForTest(float refFrequency) {
        List<float[][]> features = new ArrayList<float[][]>();
        for (FeaturesExtractorHTK featuresExtractor : featureExtractorToWorkWith) {
            float[][] feature = featuresExtractor.extractFeatures(refFrequency, Root.C);
            features.add(feature);
        }

        FeatureVector outFeatureVector = new FeatureVector(features, chrSamplingPeriod);
        outFeatureVector.setDuration(this.songDuration);
        return outFeatureVector;
    }


    public void exctractForTestBeatSynchronous(float refFrequency, String dirName) {
        String beatLabelFilePath;
        beatLabelFilePath = HelperFile.getPathForFileWithTheSameName(songFilePath, Settings.beatLabelsGroundTruthDir, Settings.BEAT_EXT);

        if (beatLabelFilePath == null) {
            logger.warn(String.format("Lablels for file %s were not found in directory %s", songFilePath, Settings.labelsGroundTruthDir));
            return;
        }

        BeatStructure beatStructure = BeatStructure.getBeatStructure(beatLabelFilePath);
        List<BeatSegment> segments;
        if (Settings.downbeatGranulation) {
            segments = beatStructure.getDownBeatPositions(true);
        } else {
            segments = beatStructure.getBeatSegments();
        }

        double startTime, endTime;
        int onsetBeatNumber, offsetBeatNumber;

        for (int i = 0; i < segments.size(); i++) {
            onsetBeatNumber = i;
            startTime = segments.get(i).getTimeInstant();
            for (int length = 1; length <= Settings.maxSegmentInBeats; length++) {

                offsetBeatNumber = onsetBeatNumber + length;
                if (offsetBeatNumber >= segments.size()) {
                    //Not possible to extract longer segments
                    continue;
                }

                endTime = segments.get(offsetBeatNumber).getTimeInstant();

                String filename = String.format("%d_%d_%s", onsetBeatNumber, offsetBeatNumber, Settings.CHROMA_EXT);


                List<float[][]> features = new ArrayList<float[][]>();
                for (FeaturesExtractorHTK featuresExtractor : featureExtractorToWorkWith) {
                    float[][] feature = featuresExtractor.extractFeatures(startTime, endTime, refFrequency, Root.C);
                    features.add(feature);
                }

                String fileNameToStore = String.format("%s/%s", dirName, filename);
                storeDataInHTKFormat(fileNameToStore, new FeatureVector(features, chrSamplingPeriod));
            }
        }
    }


    protected void storeDataInHTKFormat(String fileNameToStore, FeatureVector featureVector) {
        storeDataInHTKFormatStatic(fileNameToStore, featureVector);
    }

    /**
     * Stores Data in HTK originalFormat
     *
     * @param fileNameToStore filename
     * @param featureVector   FeatureVector data structure to store
     */
    public static void storeDataInHTKFormatStatic(String fileNameToStore, FeatureVector featureVector) {
        List<float[][]> vectors;

        vectors = featureVector.getVectors();

        if (vectors.size() == 0 || vectors.get(0).length == 0 || vectors.get(0)[0].length == 0) {
            return;
        }

        try {
            FileOutputStream outStream_ = new FileOutputStream(fileNameToStore);
            BufferedOutputStream outStream = new BufferedOutputStream(outStream_);

            int vectorSize = 0;
            for (float[][] vector : vectors) {
                vectorSize += vector[0].length;
            }

            //First write HTK Header
            //
            //int nSamples;
            //int sampPeriod;
            //short sampSize;
            //short parmKind;
            writeInt(vectors.get(0).length, outStream);
            writeInt(featureVector.getSamplePeriod(), outStream);
            writeShort((short) (vectorSize * Float.SIZE / 8), outStream);
            writeShort((short) 9, outStream);

            //Write Pcp data
            for (int i = 0; i < vectors.get(0).length; i++) {
                for (float[][] vector : vectors) {
                    for (int j = 0; j < vector[0].length; j++) {
                        writeFloat(vector[i][j], outStream);
                    }
                }
            }

            outStream.close();
        } catch (FileNotFoundException e) {
            logger.error("Cannot open stream to write data to file " + fileNameToStore);
            logger.error(Helper.getStackTrace(e));
        } catch (IOException e) {
            logger.error("Some strange error");
            logger.error(Helper.getStackTrace(e));
        }
    }


    /**
     * Reads chroma vectors from file
     *
     * @param fileName
     * @return
     */
    public static FeatureVector readFeatureVector(String fileName) {
        float[][] pcp;
        int nSamples, samplingPeriod;
        short sampSize, paramKind;
        try {
            FileInputStream inputStream = new FileInputStream(fileName);
            BufferedInputStream in = new BufferedInputStream(inputStream);


            nSamples = readInt(in);
            samplingPeriod = readInt(in);
            sampSize = readShort(in);
            paramKind = readShort(in);
            pcp = new float[nSamples][(sampSize * 8 / Float.SIZE)];
            for (int i = 0; i < nSamples; i++) {
                for (int j = 0; j < (sampSize * 8 / Float.SIZE); j++) {
                    pcp[i][j] = readFloat(in);
                }
            }

            FeatureVector outVector = new FeatureVector(pcp, samplingPeriod);
            return outVector;

        } catch (Exception e) {
            logger.error(Helper.getStackTrace(e));
            return null;
        }


    }


    /**
     * This functions splits the feature vector stream into parts that contain only one chord segment. This is done
     * after first-pass ViterbiPath decoding
     *
     * @param featureVectorFilePath File path to the feature vector data
     * @param chordSegments         parsed data about chord segments' boundaries
     * @param outFolderPath         folder to save splited segments
     * @return number of parts
     */
    public static int splitFeatureVectorsInSegments(String featureVectorFilePath, List<ChordSegment> chordSegments, String outFolderPath, String songName) {
        FeatureVector featureVector = FeaturesManager.readFeatureVector(featureVectorFilePath);
        int samplingPeriod = featureVector.getSamplePeriod();
        float[][] pcp = featureVector.getVectors().get(0);

        File outFolder = getFile(outFolderPath);
        outFolder.mkdirs();

        //now segment and store
        String outFilePath;
        float[][] segmentData;

        int counter = 0; //segment counter
        for (ChordSegment chordSegment : chordSegments) {
            int startIndex = (int) Math.round(chordSegment.getOnset() / samplingPeriod * ChordHTKParser.FEATURE_SAMPLE_RATE);
            int endIndex = (int) Math.round(chordSegment.getOffset() / samplingPeriod * ChordHTKParser.FEATURE_SAMPLE_RATE);
            if (endIndex >= pcp.length) {
                endIndex = pcp.length - 1;
            }


            segmentData = new float[endIndex - startIndex][];
            for (int i = startIndex; i < endIndex; i++) {
                segmentData[i - startIndex] = pcp[i];
            }

            outFilePath = outFolder.getAbsolutePath() + File.separator + counter++ + songName + CHROMA_SEC_PASS_EXT;
            storeDataInHTKFormatStatic(outFilePath, new FeatureVector(segmentData, samplingPeriod));
        }

        return counter;
    }


    public static int splitFeatureVectorsInSegments(String featureVectorFilePath, List<ChordSegment> chordSegments, String outFolderPath) {
        return splitFeatureVectorsInSegments(featureVectorFilePath, chordSegments, outFolderPath, "");
    }


    /**
     * Splits all data in the given folder
     *
     * @param featuresFolderPath   featuresFolderPath
     * @param recognizedLabelsPath recognizedLabelsPath
     */
    public static void splitAllData(String featuresFolderPath, String recognizedLabelsPath) {
        File featuresFolderDir = getFile(featuresFolderPath);
        File featureVectorFile;
        List<ChordSegment> chordLabels;
        String outDir;
        for (File songNameDir : featuresFolderDir.listFiles()) {
            if (songNameDir.isDirectory()) {
                File[] chromaFiles = songNameDir.listFiles(new ExtensionFileFilter(new String[]{CHROMA_EXT}, false));
                if (chromaFiles.length > 1) {
                    logger.error("More than 1 feature vector data in directory " + songNameDir.getPath());
                } else {
                    if (chromaFiles.length == 0) {
                        continue; //There is no chroma output
                    }
                    logger.info("Spliting feature vectors for song " + songNameDir.getName());
                    featureVectorFile = chromaFiles[0];
                    chordLabels = (new ChordStructure(getPathForFileWithTheSameName(songNameDir.getName() + WAV_EXT, recognizedLabelsPath, LABEL_EXT))).getChordSegments();
                    outDir = songNameDir + File.separator + "out";
                    splitFeatureVectorsInSegments(featureVectorFile.getPath(), chordLabels, outDir);
                }
            }
        }
    }


    public static void main(String[] args) {
        readFeatureVector("/home/hut/work/test_keys/_key3/fold0/2-trainFeat/1050233778/data/01_-_A_Hard_Day's_Night/1.900_148.250_maj.chr");
    }

}

