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

import java.io.*;
import java.util.StringTokenizer;

/**
 * @version 1.0 24.04.2009 21:47:48
 * @author: Maksim Khadkevich
 */
public class FLMSpecification {

    //If to use Duration for zero level
    public static boolean isDO = false;

    public static void createFLMSpec(String outFile, int nW, int nD) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(HelperFile.getFile(outFile)));

            writer.write("1\n");
            int numberOfParents = nW + nD;
            if (isDO) {
                numberOfParents++;
            }
            writer.write("W : " + numberOfParents + " ");
            if (isDO) {
                writer.write("D(0) ");
            }
            for (int i = 1; i <= nW; i++) {
                writer.write("W(-" + i + ") ");
            }
            for (int i = 1; i <= nD; i++) {
                writer.write("D(-" + i + ") ");
            }

            numberOfParents++;
            writer.write("myflm_wd.count.gz myflm_wd.lm.gz " + numberOfParents + "\n");

            //Now write backOffStrategy
            int numberWordsLeft = nW;
            int numberDursLeft = nD;

            while (numberDursLeft > 0 || numberWordsLeft > 0) {
                if (isDO) {
                    writer.write("D0");
                }
                for (int i = 1; i <= numberWordsLeft; i++) {
                    writer.write("W" + i);
                }
                for (int i = 1; i <= numberDursLeft; i++) {
                    writer.write("D" + i);
                }

                if (numberWordsLeft > numberDursLeft) {
                    //Remove word
                    writer.write(" W" + numberWordsLeft + " ");
                    numberWordsLeft--;
                } else {
                    //Remove duration
                    writer.write(" D" + numberDursLeft + " ");
                    numberDursLeft--;
                }
                writer.write("kndiscount gtmin 1 interpolate \n");
            }

            if (isDO) {
                writer.write("D0 D0 kndiscount gtmin 1 interpolate \n");
            }

            writer.write("0 0 kndiscount gtmin 1 interpolate \n");

            writer.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     *  Missing perplexity computation.
     *  Parsing a bunch of files to find the lowest perplexity and store to txt file
     */
    @Deprecated
    public static void parseResults(String inFilePath, String outFilePath, int nW, int nD) throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter(outFilePath));
        BufferedReader reader = new BufferedReader(new FileReader(inFilePath));
        String[] data = new String[nW * (nD + 1)];
        int index = 0;
        String line, token;
        while ((line = reader.readLine()) != null) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            while (tokenizer.hasMoreTokens()) {
                if (tokenizer.nextToken().equals("ppl=")) {
                    data[index++] = tokenizer.nextToken().replace(".", ",");
                }
            }

        }

        for (int i = 1; i <= nW; i++) {
            for (int j = 0; j <= nD; j++) {
                writer.write(data[(i - 1) * (nD + 1) + j]);
                writer.write("\t");
            }
            writer.write("\n");
        }

        writer.close();
        reader.close();


    }


}




