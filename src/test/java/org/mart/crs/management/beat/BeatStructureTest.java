package org.mart.crs.management.beat;


import org.testng.annotations.Test;

/**
 * @version 1.0 7/3/11 4:11 PM
 * @author: Hut
 */
public class BeatStructureTest {

    @Test
    public void testParseFromXML() throws Exception {
        String annotationsFilePath = this.getClass().getResource("/label/1beats.xml").getPath();
        BeatStructure beatStructure = BeatStructure.getBeatStructure(annotationsFilePath);
        assert beatStructure.getBeats().length == 19;
        assert beatStructure.getDownBeats().length == 5;
    }

    @Test
    public void testParseFromText() throws Exception {
        String annotationsFileName = "/label/pid1263-13-01.tap";
        String annotationsFilePath = this.getClass().getResource(annotationsFileName).getPath();
        BeatStructure beatStructure = new BeatStructureMazurka(annotationsFilePath);
        assert beatStructure.getBeats().length == 397;
    }


}
