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
