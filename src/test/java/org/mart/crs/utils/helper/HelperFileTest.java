/*
 * This file is part of MART.
 * MART is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2, as published
 * by the Free Software Foundation.
 *
 * MART is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MART; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.mart.crs.utils.helper;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;

/**
 * @version 1.0 3/24/12 2:54 PM
 * @author: Hut
 */
public class HelperFileTest {

    @DataProvider
    public Object[][] testDataFound() {
        return new Object[][]{
                {"testng.xml"}, {"/testng.xml"}, {"/audio/1.mp3"}
        };
    }

    @DataProvider
    public Object[][] testDataNotFound() {
        return new Object[][]{
                {"/oeiutoieuoiuerutovetoeurovecsaasdfdyigdfg"}, {"/ForSureIAmNotInResourceFilePath.extension"}, {"oeiutoieuoiuerutovetoeurovecsaasdfdyigdfg"}, {"/audio/oeiutoieuoiuerutovetoeurovecsaasdfdyigdfg"}
        };
    }



    @Test(dataProvider = "testDataFound")
    public void testGetResourceFile(String resorcePath) throws Exception {
        Assert.assertNotNull(HelperFile.getResourceFile(resorcePath));
    }


    @Test(dataProvider = "testDataNotFound", expectedExceptions = FileNotFoundException.class)
    public void testGetResourceFile1(String resorcePath) throws Exception {
        HelperFile.getResourceFile(resorcePath);
    }




}
