package suite;/*
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

import org.mart.crs.utils.helper.HelperFile;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;

import static org.mart.crs.utils.helper.HelperFile.getResourceFilePath;

/**
 * @version 1.0 3/25/12 5:42 PM
 * @author: Hut
 */
public class MartSuiteTest {

    public static final String TESTNG_XML_PATH = "/testng.xml";

    public static String CHORD_LIST_SHORT;
    public static String CHORD_LABELS_DIR;
    public static String BEAT_LABELS_DIR;

    public static String workingDirectoryFilePath;

    static {
        setUpWorkingDirectory();
    }


    @BeforeSuite
    public static void setUpWorkingDirectory(){
        try {
            workingDirectoryFilePath  = String.format("%s/temp", HelperFile.getResourceFile(TESTNG_XML_PATH).getParentFile().getParent());
            HelperFile.deleteDirectory(workingDirectoryFilePath);
            HelperFile.createDir(workingDirectoryFilePath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(String.format("File %s not dound in resources path", TESTNG_XML_PATH));
        }


        try {
            CHORD_LIST_SHORT = getResourceFilePath("/list/perBeatChordList.txt");
            CHORD_LABELS_DIR = getResourceFilePath("/chordLabels");
            BEAT_LABELS_DIR =  getResourceFilePath("/beatLabels");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void  testWorkingDirectory(){
        Assert.assertNotNull(workingDirectoryFilePath);
    }



    
}
