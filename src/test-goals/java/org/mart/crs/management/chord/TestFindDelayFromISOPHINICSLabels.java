package org.mart.crs.management.chord;

import junit.framework.TestCase;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version 1.0 5/4/11 2:24 PM
 * @author: Hut
 */
public class TestFindDelayFromISOPHINICSLabels extends TestCase {

    protected static String myLabelsFilePath = "/home/hut/Beatles/labels";
    protected static String isoLabelsFilePath = "/home/hut/Beatles/labelsMauch";
    protected static String delaysTxtFilePath = "/home/hut/Beatles/labelsDelay.txt";
    protected static String keyLabelsDir = "/home/hut/Beatles/labelsKey";
    protected static String keyLabelsDirOut = "/home/hut/Beatles/labelsKeyCorrected";



    public void testFindDelay() throws IOException {
        String myLabelsFilePath = "/home/hut/Beatles/labels";
        String isoLabelsFilePath = "/home/hut/Beatles/labelsMauch";
        String delaysTxtFilePath = "/home/hut/Beatles/labelsDelay.txt";

        Map<String, String> delays = new HashMap<String, String>();

        File[] isoLabels = HelperFile.getFile(isoLabelsFilePath).listFiles();
        for (File isoLabel : isoLabels) {
            List<String> myFilePath = HelperFile.findFilesWithName(myLabelsFilePath, isoLabel.getName());
            ChordStructure isoStructure = new ChordStructure(isoLabel.getCanonicalPath());
            ChordStructure myStructure = new ChordStructure(myFilePath.get(0));

            double delay = -10000000d;
            for (ChordSegment isoCS : isoStructure.getChordSegments()) {
                for (ChordSegment myCS : myStructure.getChordSegments()) {
                    if (isoCS.getOnset() > 0 && myCS.getOnset() > 0 && Math.abs((isoCS.getOnset() - myCS.getOnset()) - (isoCS.getOffset() - myCS.getOffset())) <= 0.01 && isoCS.getChordNameOriginal().equals(myCS.getChordNameOriginal())) {
                        delay = myCS.getOnset() - isoCS.getOnset();
                        break;
                    }
                }
                if (delay > -10000000d) {
                    break;
                }
            }

            delays.put(isoLabel.getName(), String.format("%5.3f", delay));
        }

        HelperFile.saveMapInTextFile(delays, delaysTxtFilePath);
    }


    public void testFixKeyTranscriptions() throws IOException {
        Map <String, String> delaysMap = HelperFile.readMapFromTextFile(delaysTxtFilePath);
        for(String fileName:delaysMap.keySet()){
            List<String> keyLabelsFilePath = HelperFile.findFilesWithName(keyLabelsDir, fileName);
            if(keyLabelsFilePath.size() == 0){
                continue;
            }
            double delay = Double.valueOf(delaysMap.get(fileName));
            List<String> lines = HelperFile.readLinesFromTextFile(keyLabelsFilePath.get(0));
            List<String> linesOut = new ArrayList<String>();
            for(String line:lines){
                String[] comps = line.split("\\s+");
                double startTime = Double.valueOf(comps[0]);
                double endTime = Double.valueOf(comps[1]);

                double startTimeNew;
                if(startTime == 0.0d){
                    startTimeNew = 0.0d;
                } else{
                    startTimeNew = startTime + delay;
                }

                StringBuffer rest = new StringBuffer();
                for(int i = 2; i < comps.length; i++){
                    rest.append(comps[i]).append("\t");
                }
                linesOut.add(String.format("%5.3f\t%5.3f\t%s", startTimeNew, Math.max(0, endTime + delay), rest.toString()));

            }
            HelperFile.saveCollectionInFile(linesOut, String.format("%s/%s", keyLabelsDirOut, fileName), false);
        }
    }

    public void testTransformKeyTranscriptionsToChordEquivalents() throws IOException {
        Map <String, String> delaysMap = HelperFile.readMapFromTextFile(delaysTxtFilePath);
        for(String fileName:delaysMap.keySet()){
            List<String> keyLabelsFilePath = HelperFile.findFilesWithName(keyLabelsDir, fileName);
            if(keyLabelsFilePath.size() == 0){
                continue;
            }
            double delay = Double.valueOf(delaysMap.get(fileName));
            List<String> lines = HelperFile.readLinesFromTextFile(keyLabelsFilePath.get(0));
            List<String> linesOut = new ArrayList<String>();
            for(String line:lines){
                String[] comps = line.split("\\s+");
                double startTime = Double.valueOf(comps[0]);
                double endTime = Double.valueOf(comps[1]);

                double startTimeNew;
                if(startTime == 0.0d){
                    startTimeNew = 0.0d;
                } else{
                    startTimeNew = startTime + delay;
                }

                StringBuffer rest = new StringBuffer();
                for(int i = 2; i < comps.length; i++){
                    if (comps[i].equalsIgnoreCase("silence")) {
                        rest.append("N").append("\t");
                        break;
                    }
                    if (comps[i].equalsIgnoreCase("key")) {
                        String keyLabel = comps[i+1];
                        if(keyLabel.contains(":minor")){
                            rest.append(keyLabel.replace(":minor", ":min"));
                        } else {
                            int index = keyLabel.indexOf(":");
                            if (index < 0) {
                                rest.append(keyLabel).append(":maj");
                            }else{
                                rest.append(String.format("%s:maj", keyLabel.substring(0, index)));
                            }
                        }
                        break;
                    }
                }
                linesOut.add(String.format("%5.3f\t%5.3f\t%s", startTimeNew, Math.max(0, endTime + delay), rest.toString()));

            }
            HelperFile.saveCollectionInFile(linesOut, String.format("%s/%s", keyLabelsDirOut, fileName), false);
        }
    }


}
