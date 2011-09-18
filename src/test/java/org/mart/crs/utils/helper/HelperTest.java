package org.mart.crs.utils.helper;

import org.junit.Assert;
import org.junit.Test;

/**
 * @version 1.0 7/3/11 4:35 PM
 * @author: Hut
 */
public class HelperTest {

    @Test
    public void testGetNoteForMidiIndex() throws Exception {
        Assert.assertEquals(Helper.getMidiNumberForNote("C4"), 60);
        Assert.assertEquals(Helper.getMidiNumberForNote("C#4"), 61);
        Assert.assertEquals(Helper.getMidiNumberForNote("A4"), 69);
        Assert.assertEquals(Helper.getMidiNumberForNote("A#-1"), 10);
    }
}
