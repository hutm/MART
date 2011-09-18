package org.mart.crs.management.chord;

import org.mart.crs.management.beat.BeatStructure;
import org.mart.crs.management.beat.BeatStructureText;
import org.mart.crs.utils.helper.HelperFile;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @version 1.0 5/12/11 6:38 PM
 * @author: Hut
 */
public class TextFindDelayFromISOPHONICSLabelsBeats extends TestCase {


    protected static String delaysTxtFilePath = "/home/hut/Beatles/labelsDelay.txt";

    protected static String beatLabelsDir = "/home/hut/Beatles/labelsBeat";
    protected static String beatLabelsDirOut = "/home/hut/Beatles/labelsBeatCorrected";



    public void testFixBeatTranscriptions() throws IOException {
        HelperFile.createDir(beatLabelsDirOut);

        Map <String, String> delaysMap = HelperFile.readMapFromTextFile(delaysTxtFilePath);
        for(String fileName:delaysMap.keySet()){
            List<String> beatLabelsFilePath = HelperFile.findFilesWithName(beatLabelsDir, fileName);
            if(beatLabelsFilePath.size() == 0){
                continue;
            }
            double delay = Double.valueOf(delaysMap.get(fileName));
            List<String> lines = HelperFile.readLinesFromTextFile(beatLabelsFilePath.get(0));
            List<String> linesOut = new ArrayList<String>();
            for(String line:lines){
                String[] comps = line.split("\\s+");
                double startTime = Double.valueOf(comps[0]);

                double startTimeNew;
                startTimeNew = startTime + delay;
                if(startTimeNew < 0){
                    System.out.println(String.format("%s %s %5.3f", beatLabelsFilePath.get(0), line, startTimeNew));
                    continue;
                }


                StringBuffer rest = new StringBuffer();
                for(int i = 1; i < comps.length; i++){
                    rest.append(comps[i]).append("\t");
                }
                linesOut.add(String.format("%5.3f\t%s", startTimeNew, rest.toString()));

            }
            HelperFile.saveCollectionInFile(linesOut, String.format("%s/%s", beatLabelsDirOut, fileName), false);
        }
    }


    public void testTranformToXML(){
        File[] files = HelperFile.getFile("/home/hut/work/test_beat/DAVIESLABELS/raw").listFiles();
        for(File file:files){
            BeatStructure beatStructure = new BeatStructureText(file.getPath());
            beatStructure.serializeIntoXML(file.getPath() + ".xml");
        }
    }



}
