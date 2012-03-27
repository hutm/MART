package org.mart.crs.utils.helper;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @version 1.0 7/3/11 4:35 PM
 * @author: Hut
 */
public class HelperTest {

    @DataProvider
    public Object[][] testData(){
        return new Object[][]{
                {"C4", 60}, {"C#4", 61}, {"A4", 69}, {"A#-1", 10}
        };
    }
    
    
    @Test(dataProvider = "testData")
    public void testGetNoteForMidiIndex(String note, int number) throws Exception {
        Assert.assertEquals(Helper.getMidiNumberForNote(note), number);
    }
}
