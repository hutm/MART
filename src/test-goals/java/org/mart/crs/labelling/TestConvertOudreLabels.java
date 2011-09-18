package org.mart.crs.labelling;

import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.models.htk.HTKResultsParser;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.util.*;

import static org.mart.crs.utils.helper.HelperFile.getFile;

/**
 * @version 1.0 Jan 20, 2010 6:40:29 PM
 * @author: Maksim Khadkevich
 */
public class TestConvertOudreLabels {


    public static void main(String[] args) {
        Map<String, List<ChordSegment>> outMap = new HashMap<String, List<ChordSegment>>();

        String inDir = "d:\\downloads\\MIREX_2010_parsed\\OFG1";
        String outDir = "d:\\downloads\\MIREX_2010_parsed\\OFG1_";

        getFile(outDir).mkdirs();

        for(File lablelFile:getFile(inDir).listFiles(new ExtensionFileFilter(new String[]{Settings.LABEL_EXT}))){
            System.out.println("Processing file " + lablelFile.getName());
            List<String> lines = HelperFile.readLinesFromTextFile(lablelFile.getPath());
            List<ChordSegment> outList = new ArrayList<ChordSegment>();
            String currentChord = "";
            float startTime = -1;

            for(String aLine:lines){
                StringTokenizer tokenizer = new StringTokenizer(aLine);
                float time = Float.parseFloat(tokenizer.nextToken());
                String chordName = null;
                //TODO refactor notesFromRandomChord String chordName = LabelsParser.getChordForNumber(tokenizer.nextToken());
                if(currentChord != chordName ){
                    if(currentChord != ""){
                        outList.add(new ChordSegment(startTime, time, currentChord));
                    }
                    currentChord = chordName;
                    startTime = time;
                }
            }
            outMap.put(lablelFile.getName(), outList);

        }

        HTKResultsParser.storeResults(outMap, outDir);

    }
}
