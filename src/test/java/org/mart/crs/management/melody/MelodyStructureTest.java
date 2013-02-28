package org.mart.crs.management.melody;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @version 1.0 2/21/13 1:25 PM
 * @author: Hut
 */
public class MelodyStructureTest {
    @Test
    public void testGetMelodyStructure() throws Exception {
        String baseFilePath = this.getClass().getResource("/label/1_vamp_mtg-melodia_melodia_melody.csv").getPath();
        MelodyStructure melodyStructure = MelodyStructure.getMelodyStructure(baseFilePath);
        Assert.assertEquals(melodyStructure.getMelodySegments().size(), 22955);

    }
}
