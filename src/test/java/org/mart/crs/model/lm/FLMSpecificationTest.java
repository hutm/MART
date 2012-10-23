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

package org.mart.crs.model.lm;

import org.mart.crs.utils.helper.HelperFile;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.MartSuiteTest;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * @version 1.0 3/27/12 4:02 PM
 * @author: Hut
 */
public class FLMSpecificationTest {

    @DataProvider
    public Object[][] testData(){
        return new Object[][]{
            {
                    String.format("%s/flmspecs",MartSuiteTest.getWorkingDirectoryFilePath()),
                    2,
                    2
            }
        };
    }
    

    @Test(dataProvider = "testData")
    public void testCreateFLMSpec(String directory, int maxNumberOfWords, int maxNumberOfDurations) throws Exception {
        HelperFile.createDir(directory);
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(String.format("%s/summary.txt", directory)));
        String filePath;
        for (int i = 1; i <= maxNumberOfWords; i++) {
            for (int j = 0; j <= maxNumberOfDurations; j++) {
                filePath = String.format("%s/%d_%d.flm", directory, i, j);
                FLMSpecification.createFLMSpec(filePath, i, j);
//                Helper.execCmd("D:\\temp\\script\\FLMTrain\\traintest.bat " + filePath, writer);
                writer.write("It was FLM : W = " + i + " , D = " + j + "\n");
                writer.write("------------------------------------------\n");
            }
        }

        writer.close();



    }
}
