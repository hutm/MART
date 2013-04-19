/*
 * Copyright (c) 2008-2013 Maksim Khadkevich and Fondazione Bruno Kessler.
 *
 * This file is part of MART.
 * MART is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2, as published
 * by the Free Software Foundation.
 *
 * MART is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with MART; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

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
