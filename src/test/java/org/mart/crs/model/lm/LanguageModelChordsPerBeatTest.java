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

package org.mart.crs.model.lm;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.MartSuiteTest;

import java.io.FileNotFoundException;

/**
 * @version 1.0 3/24/12 1:51 PM
 * @author: Hut
 */
public class LanguageModelChordsPerBeatTest extends LanguageModelChordTest{

    @DataProvider
    public Object[][] testData() throws FileNotFoundException {
        return new Object[][]{
                {
                        MartSuiteTest.CHORD_LIST_SHORT,
                        MartSuiteTest.CHORD_LABELS_DIR,
                        MartSuiteTest.BEAT_LABELS_DIR,
                        String.format("%s/outTextPerBeat.txt", MartSuiteTest.getWorkingDirectoryFilePath()),
                        " N D#min D#min D#min D#min G#min G#min G#min G#min F#maj",
                        String.format("%s/outLMPerBeat.lm", MartSuiteTest.getWorkingDirectoryFilePath()),
                        12,
                        11000
                }
        };
    }




    @Test(dataProvider = "testData")
    public void processTest(String wavFileList, String chordLabelsSource, String beatLabelsSource, String outTextFilePath, String biginningOfTheFirstLine, String lmPath, int lmOrder, int minNumberOfLinesInOutLMFile) {
        languageModelChord = new LanguageModelChordPerBeat(chordLabelsSource, beatLabelsSource, wavFileList, outTextFilePath);
        testLanguageModel(outTextFilePath, biginningOfTheFirstLine, lmPath, lmOrder, minNumberOfLinesInOutLMFile);
    }



}
