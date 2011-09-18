package org.mart.tools.tuning;

import org.mart.crs.config.Settings;
import org.mart.crs.management.audio.ReferenceFreqManager;
import org.mart.crs.utils.helper.HelperFile;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @version 1.0 9/4/11 3:19 PM
 * @author: Hut
 */
public class TestComparedifferentTunings extends TestCase {

    public static final String OLDREFFREQ = "/home/hut/work/compareDirectories/refOld.txt";
    public static final String NEWREFFREQ = "/home/hut/work/compareDirectories/ref.txt";

    public static final String OLDRESULTS = "/home/hut/work/compareDirectories/hackedOld.txt";
    public static final String NEWRESULTS = "/home/hut/work/compareDirectories/hacked.txt";


    public void testFindDifferences() {
        ReferenceFreqManager managerOld = new ReferenceFreqManager(OLDREFFREQ);
        ReferenceFreqManager manager = new ReferenceFreqManager(NEWREFFREQ);

        List<SongResult> oldResults = getResultsMap(OLDRESULTS);
        List<SongResult> newResults = getResultsMap(NEWRESULTS);

        Settings.isToUseRefFreq = true;
        for (SongResult song : newResults) {
            song.setOldRefFreq(managerOld.getRefFreqForSong(song.getName() + Settings.WAV_EXT));
            song.setRefFreq(manager.getRefFreqForSong(song.getName() + Settings.WAV_EXT));
            song.setOldAccuracy(findSongResult(song.getName(), oldResults).getAccuracy());
        }

        Collections.sort(newResults);

        for(SongResult s:newResults){
            System.out.println(s);
        }


    }


    protected List<SongResult> getResultsMap(String resultsFilePath) {
        List<SongResult> outResults = new ArrayList<SongResult>();
        for (String line : HelperFile.readLinesFromTextFile(resultsFilePath)) {
            String[] comps = line.trim().split("\\s+");
            if (comps.length < 4) {
                break;
            }
            outResults.add(new SongResult(comps[0], Float.parseFloat(comps[1])));
        }
        return outResults;
    }


    protected SongResult findSongResult(String songName, List<SongResult> songList){
        for(SongResult s:songList){
            if(s.getName().equals(songName)){
                return s;
            }
        }
        return null;
    }


}


class SongResult implements Comparable<SongResult> {

    protected String name;

    protected float accuracy;
    protected float refFreq;

    protected float oldAccuracy;
    protected float oldRefFreq;



    SongResult(String name, float accuracy) {
        this.name = name;
        this.accuracy = accuracy;
    }


    SongResult(String name, float accuracy, float refFreq) {
        this.name = name;
        this.accuracy = accuracy;
        this.refFreq = refFreq;
    }


    public float getAccuracyDifferentceAbs(){
        return Math.abs(accuracy - oldAccuracy);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public float getRefFreq() {
        return refFreq;
    }

    public void setRefFreq(float refFreq) {
        this.refFreq = refFreq;
    }

    public float getOldAccuracy() {
        return oldAccuracy;
    }

    public void setOldAccuracy(float oldAccuracy) {
        this.oldAccuracy = oldAccuracy;
    }

    public float getOldRefFreq() {
        return oldRefFreq;
    }

    public void setOldRefFreq(float oldRefFreq) {
        this.oldRefFreq = oldRefFreq;
    }

    public int compareTo(SongResult o) {
        if (this.getAccuracyDifferentceAbs() < o.getAccuracyDifferentceAbs()) {
            return 1;
        }
        if (this.getAccuracyDifferentceAbs() > o.getAccuracyDifferentceAbs()) {
            return -1;
        }
        else{
            return 0;
        }

    }

    @Override
    public String toString() {
        return String.format("%s %5.5f %5.5f %5.5f %5.5f %5.5f", name, getAccuracyDifferentceAbs(), accuracy, oldAccuracy, refFreq, oldRefFreq);
    }
}
