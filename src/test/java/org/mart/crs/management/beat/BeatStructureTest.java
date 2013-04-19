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
