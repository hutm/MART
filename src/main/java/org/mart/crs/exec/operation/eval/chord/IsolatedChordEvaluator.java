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

package org.mart.crs.exec.operation.eval.chord;

import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.Operation;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @version 1.0 21-Sep-2010 13:26:13
 * @author: Hut
 */
public class IsolatedChordEvaluator extends Operation {

    public IsolatedChordEvaluator(String workingDir) {
        super(workingDir);
    }


    @Override
    public void initialize() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void operate() {
        try {
            FileWriter writer = new FileWriter(workingDir + ".txt");
            int allChords = 0;
            int correctChords = 0;

            File[] fileList = (new File(workingDir)).listFiles(new ExtensionFileFilter(Settings.LABEL_EXT));
            for(File file:fileList){
                List<String[]> list = HelperFile.readTokensFromFileStrings(file.getPath(), 3);
                String correctChord = file.getName().substring(0, file.getName().indexOf("_"));
                String recognizedChoed = list.get(0)[2];
                writer.write(String.format("%s\t\t%s", file.getName(), recognizedChoed));

                if(correctChord.equalsIgnoreCase(recognizedChoed)){
                    correctChords++;
                    writer.write("\n");
                } else{
                    writer.write("\tERROR!!!!!!!!!!!!!!!!!\n");
                }
                allChords++;

            }

            writer.write("--------------------------------------\n");
            writer.write(String.format("Total: %d\n", allChords));
            writer.write(String.format("Correct: %d\n", correctChords));


            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public static void main(String[] args) {
        IsolatedChordEvaluator isolatedChordEvaluator = new IsolatedChordEvaluator("d:\\dev\\CHORDS4\\chords\\reas_false_true_5_2048_0.50\\results\\results_1_-24.0");
        isolatedChordEvaluator.initialize();
        isolatedChordEvaluator.operate();
    }
}
