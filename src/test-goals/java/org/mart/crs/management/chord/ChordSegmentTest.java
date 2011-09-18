package org.mart.crs.management.chord;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * ChordSegment Tester.
 *
 * @author Hut
 * @since <pre>03/31/2011</pre>
 * @version 1.0
 */
public class ChordSegmentTest extends TestCase {
    public ChordSegmentTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     *
     * Method: convertChordName(String chordName)
     *
     */
    public void testConvertChordName() throws Exception {
        //TODO: Test goes here...
    }

    /**
     *
     * Method: getChordName()
     *
     */
    public void testGetChordName() throws Exception {
        //TODO: Test goes here...
    }

    /**
     *
     * Method: setChordName(String chordName)
     *
     */
    public void testSetChordName() throws Exception {
        //TODO: Test goes here...
    }

    /**
     *
     * Method: addHypothesis(String label, Float score)
     *
     */
    public void testAddHypothesis() throws Exception {
        //TODO: Test goes here...
    }

    /**
     *
     * Method: getHypotheses()
     *
     */
    public void testGetHypotheses() throws Exception {
        //TODO: Test goes here...
    }

    /**
     *
     * Method: getDurationInBeats()
     *
     */
    public void testGetDurationInBeats() throws Exception {
        //TODO: Test goes here...
    }

    /**
     *
     * Method: setDurationInBeats(int durationInBeats)
     *
     */
    public void testSetDurationInBeats() throws Exception {
        //TODO: Test goes here...
    }

    /**
     *
     * Method: getLogLikelihood()
     *
     */
    public void testGetLogLikelihood() throws Exception {
        //TODO: Test goes here...
    }

    /**
     *
     * Method: setLogLikelihood(float logLikelihood)
     *
     */
    public void testSetLogLikelihood() throws Exception {
        //TODO: Test goes here...
    }

    /**
     *
     * Method: isValidChord()
     *
     */
    public void testIsValidChord() throws Exception {
        //TODO: Test goes here...
    }

    /**
     *
     * Method: toString()
     *
     */
    public void testToString() throws Exception {
        //TODO: Test goes here...
    }

    /**
     *
     * Method: equals(Object obj)
     *
     */
    public void testEquals() throws Exception {
        //TODO: Test goes here...
    }

    /**
     *
     * Method: intersects(ChordSegment cs1)
     *
     */
    public void testIntersects() throws Exception {
        //TODO: Test goes here...
    }

    /**
     *
     * Method: getIntersection(ChordSegment cs2)
     *
     */
    public void testGetIntersection() throws Exception {
        //TODO: Test goes here...
    }

    /**
     *
     * Method: getNotesWithoutDuplicates()
     *
     */
    public void testGetNotesWithoutDuplicates() throws Exception {
        int[] test = new int[]{1 ,2, 3, 2 ,1};
//        int[] out = ChordSegment.getNotesWithoutDuplicates(test);
    }



    public static Test suite() {
        return new TestSuite(ChordSegmentTest.class);
    }
}
