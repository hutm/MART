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

package org.mart.crs.exec.operation.eval;

import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @version 1.0 26-Apr-2010 22:53:52
 * @author: Hut
 */
public class FMeasure {

    public static final String totalResultsFileName = "total.txt";
    public static final String recallFileName = "recall.txt";
    public static final String recallWeightedFileName = "recallWeighted.txt";
    public static final String precisionFileName = "precision.txt";
    public static final String precisionWeightedFileName = "precisionWeighted.txt";
    public static final String fMeasureFileName = "fMMeasure.txt";
    public static final String fMeasureWeightedFileName = "fMeasureWeighted.txt";
    public static final String ampRelationFileName = "ampRelations.txt";
    public static final String ampRelationGTFileName = "ampRelationsGT.txt";


    private double maxDistance;
    private boolean isLogDistance;
    private boolean isUniqueGTLabel = false; // if set to true only one trascription result can be connected with one gt record

    private double recall;
    private double recallWeighted;

    private List<double[]> positivesNegativesFrameData;
    private List<double[]> weightsPositivesNegativesFrameData;

    private List<Double> recallData;
    private List<Double> recallDataWeighted;


    private double precision;
    private double precisionWeighted;

    private List<Double> precisionData;
    private List<Double> precisionDataWeighted;


    private double fMeasure;
    private double fMeasureWeighted;

    private List<Double> fMeasureData;
    private List<Double> fMeasureDataWeighted;

    private double ampRelation;
    private double ampRelationGT;

    private List<Double> ampRelationData;
    private List<Double> ampRelationGTData;


    public FMeasure(float maxDistance, boolean logDistance) {
        this.maxDistance = maxDistance;
        isLogDistance = logDistance;


        positivesNegativesFrameData = new ArrayList<double[]>();
        weightsPositivesNegativesFrameData = new ArrayList<double[]>();

        recallData = new ArrayList<Double>();
        recallDataWeighted = new ArrayList<Double>();
        precisionData = new ArrayList<Double>();
        precisionDataWeighted = new ArrayList<Double>();
        fMeasureData = new ArrayList<Double>();
        fMeasureDataWeighted = new ArrayList<Double>();

        ampRelationData = new ArrayList<Double>();
        ampRelationGTData = new ArrayList<Double>();

    }

    public void processFrame(double[] gt, double[] gtWeight, double[] transcription, double[] transcriptionWeight) {
        if (transcription.length == 0 || gt.length == 0) {
            return;
        }

        int[] coincidenceIndexes = new int[gt.length];
        for (int i = 0; i < coincidenceIndexes.length; i++) {
            coincidenceIndexes[i] = -1;
        }
        int numberOfTruePositives = 0;
        double weightOfTruePositives = 0;

        int numberOfFalsePositives = 0;
        double weightOfFalsePositives = 0;

        int numberOfFalseNegatives = 0;
        double weightOfFalseNegatives = 0;

        double gtSum = HelperArrays.sum(gtWeight);
        double transSum = HelperArrays.sum(transcriptionWeight);

        for (int i = 0; i < transcription.length; i++) {
            boolean isFound = false;
            for (int j = 0; j < gt.length; j++) {
                double distance = getDistance(transcription[i], gt[j]);
                if (distance < maxDistance) {
                    if (!isUniqueGTLabel || coincidenceIndexes[j] < 0) {
                        numberOfTruePositives += 1;
                    }
                    coincidenceIndexes[j] = i;
                    weightOfTruePositives += Math.min(transcriptionWeight[i] / gtWeight[j], gtWeight[j] / transcriptionWeight[i]);
                    isFound = true;
                    break;
                }
            }
            if (!isFound) {
                numberOfFalsePositives += 1;
                weightOfFalsePositives += Math.min(transcriptionWeight[i] / gtSum, gtSum / transcriptionWeight[i]);
            }
        }

        //Now find false negatives
        for (int j = 0; j < gt.length; j++) {
            if (coincidenceIndexes[j] < 0 && gt[j] > 0) {
                numberOfFalseNegatives += 1;
                weightOfFalseNegatives += Math.min(gtWeight[j] / gtSum, gtSum / gtWeight[j]);
            }
        }

        if (gt.length == 2 || (gt.length == 3 && gt[2] < 0)) {
            if (numberOfFalseNegatives > 0) {
                ampRelationData.add(0d);
            } else {
                ampRelationData.add(transcriptionWeight[coincidenceIndexes[0]] / transcriptionWeight[coincidenceIndexes[1]]);
            }
            ampRelationGTData.add(gtWeight[0] / gtWeight[1]);
        }


        double precision = numberOfTruePositives / (float) (numberOfTruePositives + numberOfFalsePositives);
        double recall = numberOfTruePositives / (float) (numberOfTruePositives + numberOfFalseNegatives);

        double precisionWeighted = weightOfTruePositives / (weightOfTruePositives + weightOfFalsePositives);
        double recallWeighted = weightOfTruePositives / (weightOfTruePositives + weightOfFalseNegatives);

        this.precisionData.add(precision);
        this.precisionDataWeighted.add(precisionWeighted);
        this.recallData.add(recall);
        this.recallDataWeighted.add(recallWeighted);
        this.fMeasureData.add(2 * precision * recall / (precision + recall));
        this.fMeasureDataWeighted.add(2 * precisionWeighted * recallWeighted / (precisionWeighted + recallWeighted));

        this.positivesNegativesFrameData.add(new double[]{numberOfTruePositives, numberOfFalsePositives, numberOfFalseNegatives});
        this.weightsPositivesNegativesFrameData.add(new double[]{weightOfTruePositives, weightOfFalsePositives, weightOfFalseNegatives});

    }


    public void calculateGlobalValues() {

        precision = getMean(precisionData);
        precisionWeighted = getMean(precisionDataWeighted);

        recall = getMean(recallData);
        recallWeighted = getMean(recallDataWeighted);

        fMeasure = 2 * precision * recall / (precision + recall);
        fMeasureWeighted = 2 * precisionWeighted * recallWeighted / (precisionWeighted + recallWeighted);

        ampRelation = getMean(ampRelationData);
        ampRelationGT = getMean(ampRelationGTData);

    }


    private double getDistance(double a, double b) {
        if (isLogDistance) {
            return Helper.getSemitoneDistanceAbs(a, b);
        } else {
            return Math.abs(a - b);
        }
    }

    private float getMean(List<Float> list) {
        float sum = 0;
        for (Float aList : list) {
            sum += aList;
        }
        return sum / list.size();
    }

    private double getMean(List<Double> list) {
        double sum = 0;
        for (Double aList : list) {
            sum += aList;
        }
        return sum / list.size();
    }


    public void storeResults(String outDir) {
        File outDirectory = HelperFile.getFile(outDir);
        outDirectory.mkdirs();

        Map<String, String> totalResults = new LinkedHashMap<String, String>();
        totalResults.put("recall", String.valueOf(recall));
        totalResults.put("precision", String.valueOf(precision));
        totalResults.put("fmeasure", String.valueOf(fMeasure));

        totalResults.put("recallWeighted", String.valueOf(recallWeighted));
        totalResults.put("precisionWeighted", String.valueOf(precisionWeighted));
        totalResults.put("fMeasureWeighted", String.valueOf(fMeasureWeighted));

        totalResults.put("ampRelation", String.valueOf(ampRelation));
        totalResults.put("ampRelationGT", String.valueOf(ampRelationGT));


        HelperFile.saveMapInTextFile(totalResults, String.format("%s/%s", outDir, totalResultsFileName));

        HelperFile.saveDoubleDataInTextFile(recallData, String.format("%s/%s", outDir, recallFileName));
        HelperFile.saveDoubleDataInTextFile(recallDataWeighted, String.format("%s/%s", outDir, recallWeightedFileName));
        HelperFile.saveDoubleDataInTextFile(precisionData, String.format("%s/%s", outDir, precisionFileName));
        HelperFile.saveDoubleDataInTextFile(precisionDataWeighted, String.format("%s/%s", outDir, precisionWeightedFileName));
        HelperFile.saveDoubleDataInTextFile(fMeasureData, String.format("%s/%s", outDir, fMeasureFileName));
        HelperFile.saveDoubleDataInTextFile(fMeasureDataWeighted, String.format("%s/%s", outDir, fMeasureWeightedFileName));
        HelperFile.saveDoubleDataInTextFile(ampRelationData, String.format("%s/%s", outDir, ampRelationFileName));
        HelperFile.saveDoubleDataInTextFile(ampRelationGTData, String.format("%s/%s", outDir, ampRelationGTFileName));

    }


    public double getRecall() {
        return recall;
    }

    public double getRecallWeighted() {
        return recallWeighted;
    }

    public double getPrecision() {
        return precision;
    }

    public double getPrecisionWeighted() {
        return precisionWeighted;
    }

    public double getfMeasure() {
        return fMeasure;
    }

    public double getfMeasureWeighted() {
        return fMeasureWeighted;
    }


    public static void summarize(String rootDir, String outFile){
        Map<String, String> map = null;

        File rootDirectory = HelperFile.getFile(rootDir);
        File[] listFiles = rootDirectory.listFiles();
        for(File innerDir:listFiles){
            if(innerDir.isDirectory()){
                String resultFilePath = innerDir.getPath() + "/eval/total.txt";
                Map<String, String> map_new = HelperFile.readMapFromTextFile(resultFilePath);
                if(map == null){
                    map_new.put("conf", innerDir.getName());
                    map = map_new;
                    continue;
                } else{
                    Map<String, String> updatedMap = new LinkedHashMap<String, String>();
                    for(String key:map.keySet()){
                        String valueOld = map.get(key);
                        String valueNew = map_new.get(key);
                        updatedMap.put(key, String.format("%s %s", valueOld, valueNew));
                    }
                    updatedMap.put("conf", map.get("conf") + " " + innerDir.getName());
                    map = updatedMap;
                }
            }
        }
        HelperFile.saveMapInTextFile(map, outFile);

    }


    @Override
    public String toString() {
        return String.format("recall=%5.3f precision=%5.3f recallWeighted=%5.3f precisionWeighted=%5.3f",
                recall, precision, recallWeighted, precisionWeighted);
    }

}
