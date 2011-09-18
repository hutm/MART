package org.mart.crs.labelling;

import org.mart.crs.config.Settings;
import org.mart.crs.management.beat.BeatStructure;
import org.mart.crs.utils.helper.HelperArrays;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @version 1.0 7/19/11 2:12 PM
 * @author: Hut
 */
public class FindMazurkaStableRythmData {


    public static void main(String[] args) {
        String mazurkaLabelsDir = "/home/hut/Beatles/mazurka/labels/";
        String mazurkaWavDir = "/home/hut/Beatles/mazurka/audio/";
        String mazurkaLabelsDirOut = "/home/hut/Beatles/mazurka/labelsNew/";
        String mazurkaWavDirOut = "/home/hut/Beatles/mazurka/audioNew/";

        File[] files = HelperFile.getFile(mazurkaLabelsDir).listFiles();
        List<SongStat> songStats = new ArrayList<SongStat>();
        for(File labelFile:files){
            BeatStructure beatStructure = BeatStructure.getBeatStructure(labelFile.getPath());

            double[] beatSegments = beatStructure.getBeatDurations();
            float[] stat = HelperArrays.calculateMeanAndStandardDeviation(HelperArrays.getDoubleAsFloat(beatSegments));
            songStats.add(new SongStat(labelFile.getPath(), stat[0], stat[1]));
            System.out.println(String.format("%s %5.3f %5.3f", labelFile.getName(), stat[0], stat[1]));

        }
        Collections.sort(songStats);

        for(int i = 0; i < 100; i++){
            String filePath = songStats.get(i).getFilePath();
            String fileName = HelperFile.getShortFileName(filePath);
            String outLabelFilePath = mazurkaLabelsDirOut + fileName;
            String inWavFilePath = HelperFile.getPathForFileWithTheSameName(fileName, mazurkaWavDir, Settings.WAV_EXT);
            String outWavFilePath =  mazurkaWavDirOut + HelperFile.getNameWithoutExtension(inWavFilePath) + Settings.WAV_EXT;

            HelperFile.copyFile(filePath, outLabelFilePath);
            HelperFile.copyFile(inWavFilePath, outWavFilePath);

        }
    }

}



class SongStat implements Comparable<SongStat>{

    protected String filePath;

    protected float mean;

    protected float std;

    SongStat(String filePath, float mean, float std) {
        this.filePath = filePath;
        this.mean = mean;
        this.std = std;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public float getMean() {
        return mean;
    }

    public void setMean(float mean) {
        this.mean = mean;
    }

    public float getStd() {
        return std;
    }

    public void setStd(float std) {
        this.std = std;
    }

    public int compareTo(SongStat o) {
        int result = 0;
        if(std > o.getStd()){
            result = 1;
        }
        if(std < o.getStd()){
            result = -1;
        }
        return result;
    }
}
