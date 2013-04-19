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

import org.mart.crs.utils.helper.Helper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @version 1.0 3/26/12 5:36 PM
 * @author: Hut
 */
public class LanguageModelChordFactored extends LanguageModelChordPerBeat {

    
    public static final String  factoredLMTemplateFilePath = "cfg/LM/spec_wd_template.flm";
    

    public LanguageModelChordFactored(String chordLabelsSoursePath, String beatLabelsSourcePath, String wavFileList, String outTextFilePath) {
        super(chordLabelsSoursePath, beatLabelsSourcePath, wavFileList, outTextFilePath);
    }


    @Override
    protected String additionalTransform(String shiftedLine) {
        return compactLine(shiftedLine);
    }

    /**
     * Transforms chord sequence into FLM representation (W=C:D=4), where W is ChordName, D - duration in beats
     *
     * @param line         Input line
     * @param isToQuantize If set to true, duratio quantization is performed
     * @return Compact line in FLM format
     */
    public String compactLine(String line, boolean isToQuantize) {
        String currentChord, token;
        int currentChordCount = 1;
        StringBuffer stringBuffer = new StringBuffer();
        StringTokenizer tokenizer = new StringTokenizer(line);
        currentChord = tokenizer.nextToken();
        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            if (token.equals(currentChord)) {
                currentChordCount++;
            } else {
                if (isToQuantize) {
                    currentChordCount = Helper.quantizeDuration(currentChordCount);
                }
                stringBuffer.append("W-" + currentChord + ":D-" + currentChordCount + " ");
                currentChord = token;
                currentChordCount = 1;
            }
        }

        return stringBuffer.toString();
    }

    /**
     * Performs line transform into FLM format without quantizing
     *
     * @param line Input line
     * @return Compact line in FLM format
     */
    public String compactLine(String line) {
        return compactLine(line, false);
    }

    @Override
    public void createLanguageModel(int lmOrder, String lmFilePath) {  //TODO: parameter lmOrder is not used. Actual order is taken from FLM specidfication file
        Map<String, String> replacementMap = new HashMap<String, String>();
        replacementMap.put("FLMfilePathCount FLMfilePathLM", String.format("%s.counts %s", lmFilePath, lmFilePath));

        String specFilePath = String.format("%s.spec", lmFilePath);
        try {
            Helper.replaceText(factoredLMTemplateFilePath,
                    specFilePath,
                    replacementMap
            );
        } catch (IOException e) {
            logger.error(String.format("Could not replace text from template %s into file %s", factoredLMTemplateFilePath, specFilePath));
        }

        String command = String.format("fngram-count -debug 2 -factor-file %s -text %s -write-counts -lm %s -nonull -no-virtual-begin-sentence", specFilePath, outTextFilePath, lmFilePath);
        Helper.execCmd(command);
    }


}
