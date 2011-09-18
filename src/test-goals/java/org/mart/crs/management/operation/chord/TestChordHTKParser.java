package org.mart.crs.management.operation.chord;

import org.mart.crs.exec.operation.models.htk.parser.chord.ChordHTKParser;
import org.mart.crs.exec.operation.models.htk.parser.chord.ChordHTKParserFromLattice;
import junit.framework.TestCase;

/**
 * @version 1.0 5/14/11 3:37 PM
 * @author: Hut
 */
public class TestChordHTKParser extends TestCase {


    public void test1(){
        String htkOut = "/home/hut/work/test_chords/_chords17/fold0/6-testRec/1352129371/temp/out32_-18.0";
        String outFolder = "/home/hut/work/test_chords/_chords17/fold0/6-testRec/1352129371/temp/out32_-18.0_dir";
        ChordHTKParser parser = new ChordHTKParser(htkOut, outFolder);
    }

    public void test2(){
        String htkOut = "/home/hut/work/test_chords/_chords17/fold0/6-testRec/1352129371/temp/out_lmWeight_6.00#acWeight_1.00#wip_-3.00";
        String outFolder = "/home/hut/work/test_chords/_chords17/fold0/6-testRec/1352129371/temp/out_lmWeight_6.00#acWeight_1.00#wip_-3.00_dir";
        ChordHTKParser parser = new ChordHTKParserFromLattice(htkOut, outFolder);
    }


}
