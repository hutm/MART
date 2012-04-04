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
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.MartSuiteTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * @version 1.0 3/26/12 7:00 PM
 * @author: Hut
 */
public class LanguageModelChordTest {

    protected LanguageModelChord languageModelChord;

    @DataProvider
    public Object[][] testData() throws FileNotFoundException {
        return new Object[][]{
                {
                        MartSuiteTest.CHORD_LIST_SHORT,
                        MartSuiteTest.CHORD_LABELS_DIR,
                        MartSuiteTest.BEAT_LABELS_DIR,
                        String.format("%s/outText.txt", MartSuiteTest.workingDirectoryFilePath),
                        "<s> N D#min G#min F#maj Bmaj",
                        String.format("%s/outLM.lm", MartSuiteTest.workingDirectoryFilePath),
                        12,
                        5000
                }
        };
    }


    @Test(dataProvider = "testData")
    public void processTest(String wavFileList, String chordLabelsSource, String beatLabelsSource, String outTextFilePath, String biginningOfTheFirstLine, String lmPath, int lmOrder, int minNumberOfLinesInOutLMFile) {
        languageModelChord = new LanguageModelChord(chordLabelsSource, wavFileList, outTextFilePath);
        testLanguageModel(outTextFilePath, biginningOfTheFirstLine, lmPath, lmOrder, minNumberOfLinesInOutLMFile);

    }


    protected void testLanguageModel(String outTextFilePath, String biginningOfTheFirstLine, String lmPath, int lmOrder, int minNumberOfLinesInOutLMFile){
        languageModelChord.process();

        List<String> lines = HelperFile.readLinesFromTextFile(outTextFilePath);
        Assert.assertEquals(lines.size(), 12);
        Assert.assertTrue(lines.get(0).startsWith(biginningOfTheFirstLine));


        languageModelChord.createLanguageModel(lmOrder, lmPath);

        Assert.assertTrue(new File(lmPath).exists());
        List<String> linesRead = HelperFile.readLinesFromTextFile(lmPath);
        Assert.assertTrue(linesRead.size() > minNumberOfLinesInOutLMFile);

    }


    
}
