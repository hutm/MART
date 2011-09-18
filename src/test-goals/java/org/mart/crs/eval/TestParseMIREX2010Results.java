package org.mart.crs.eval;

import org.mart.crs.exec.operation.eval.chord.EvaluatorOld;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.utils.helper.HelperFile;
import junit.framework.TestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @version 1.0 22-Aug-2010 23:46:13
 * @author: Hut
 */
public class TestParseMIREX2010Results extends TestCase {
    String folderDir = "d:\\temp\\mirex\\results\\";
    String outFolder = "d:\\temp\\mirex\\results\\";
    String[] outFolders = new String[]{"Ground-truth", "CWB1", "EW1", "EW2", "EW3", "EW4", "KO1", "MD1", "MK1", "MM1", "OFG1", "PP1", "PVM1", "RRHS1", "RRHS2", "UUOS1"};


    public void testParseResults() {


        File folder = new File(folderDir);
        File[] files = folder.listFiles();


        String[] submissionPaths = new String[outFolders.length];


        int counter = 0;
        for (String folderName : outFolders) {
            File dir = new File(outFolder + folderName);
            dir.mkdirs();
            submissionPaths[counter++] = outFolder + folderName;
        }

        for (File file : files) {
            processFile(file, submissionPaths);
        }


    }


    public void processFile(File file, String[] paths) {
        int lineIndex = 1;


        List<String> lines = HelperFile.readLinesFromTextFile(file.getPath());

        for (int i = 0; i < paths.length; i++) {
            List<ChordSegment> chordSegmentList = new ArrayList<ChordSegment>();
            boolean started = false;
            while (lineIndex < lines.size()) {
                String line = lines.get(lineIndex);
                if (!line.startsWith("[") && !started) {
                    System.out.println("ERROR: with line index " + lineIndex);
                    System.out.println("ERROR: line " + line);
                } else {
                    started = true;
                }
                ChordSegment parsedChordSegment = null;
                try {
                    parsedChordSegment = parseLine(line);
                } catch (Exception e) {
                    System.out.println("File " + file.getName());
                    e.printStackTrace();
                }
                chordSegmentList.add(parsedChordSegment);
                lineIndex++;
                if (line.trim().endsWith("],") || line.trim().endsWith("]")) {
                    //TODO LabelsParser.saveSegmentsInFile(chordSegmentList, paths[i] + "/" + file.getName().replaceAll("\\.js", ".lab"));
                    break;
                }
            }

        }


    }

    private ChordSegment parseLine(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line, ":,");
        tokenizer.nextToken();
        float startTime = Float.parseFloat(tokenizer.nextToken());
        tokenizer.nextToken();
        float endTime = Float.parseFloat(tokenizer.nextToken());
        tokenizer.nextToken();

        StringTokenizer tokenizer2 = new StringTokenizer(line, "\"");
        tokenizer2.nextToken();
        String chordName = tokenizer2.nextToken();
        return new ChordSegment(startTime, endTime, chordName.trim());
    }


    public void testReevaluate() {
        for (int i = 1; i < outFolders.length; i++) {
            EvaluatorOld.makeEvaluation(outFolder + outFolders[i], "d:\\temp\\mirex\\Ground-truth", outFolder + outFolders[i] + ".txt");
        }
    }


}
