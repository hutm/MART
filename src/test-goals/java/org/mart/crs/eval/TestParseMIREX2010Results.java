package org.mart.crs.eval;

import junit.framework.TestCase;
import org.mart.crs.exec.operation.eval.chord.ChordEvaluatorNema;
import org.mart.crs.exec.operation.eval.chord.ChordEvaluatorNemaFullDictionary;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @version 1.0 22-Aug-2010 23:46:13
 * @author: Hut
 */
public class TestParseMIREX2010Results extends TestCase {
    String folderDir = "/home/hut/PhD/experiments/mirex2011Chords/labelsRaw/";
    String outFolder = "/home/hut/PhD/experiments/mirex2011Chords/results/";
//    String[] outFolders = new String[]{"Ground-truth", "CWB1", "EW1", "EW2", "EW3", "EW4", "KO1", "MD1", "MK1", "MM1", "OFG1", "PP1", "PVM1", "RRHS1", "RRHS2", "UUOS1"};
    String[] outFolders = {"Ground-truth","BUURO1","BUURO2","BUURO3","BUURO4","BUURO5","CB1","CB2","CB3","KO1","KO2","NM1","NMSD1","NMSD2","NMSD3","PVM1","RHRC1","UUOS1","UUROS1"};
//    String[] outFolders = {"Ground-truth", "NMSD2","NMSD3","PVM1","RHRC1","UUOS1","UUROS1"};

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
                if (parsedChordSegment != null) {
                    chordSegmentList.add(parsedChordSegment);
                }
                lineIndex++;
                if (line.trim().endsWith("],") || line.trim().endsWith("]")) {
                    ChordStructure chordStructure = new ChordStructure(chordSegmentList, file.getName().replaceAll("\\.js", ""));
                    chordStructure.saveSegmentsInFile(paths[i], true);
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
        return new ChordSegment(startTime, endTime, chordName.replace("dim(9)", "dim").replace("S", "N").trim());
    }


    public void testReevaluate() {
        for (int i = 1; i < outFolders.length; i++) {
            ChordEvaluatorNema evaluatorNema = new ChordEvaluatorNema();
            evaluatorNema.initializeDirectories(outFolder + outFolders[i], outFolder + outFolders[0], "/home/hut/PhD/experiments/mirex2011Chords/html/" + outFolders[i] + ".txt");
            evaluatorNema.evaluate();
        }
    }

    public void testReevaluateFullDict() {
        for (int i = 1; i < outFolders.length; i++) {
            ChordEvaluatorNemaFullDictionary evaluatorNema = new ChordEvaluatorNemaFullDictionary();
            evaluatorNema.initializeDirectories(outFolder + outFolders[i], outFolder + outFolders[0], "/home/hut/PhD/experiments/mirex2011Chords/htmlFullDict/" + outFolders[i] + ".txt");
            evaluatorNema.evaluate();
        }
    }


}
