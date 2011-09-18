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

package org.mart.crs.exec.operation.models.htk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.StringTokenizer;

/**
 * @version 1.0 12/9/10 2:08 PM
 * @author: Hut
 */
public class HTKLatticeTransformerBeats extends HTKLatticeTransformer {


    public void transformLattice(String latticeFileName, String outLatticeFileName, boolean isOneChordPerBeatVersion) throws Exception {


        BufferedReader reader = new BufferedReader(new FileReader(latticeFileName));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outLatticeFileName));
        String line, lineOut, token, word;
        StringTokenizer tokenizer;
        int nodeindex, frameIndex, duration;
        int beginIndex = 0, endIndex = 0;


        while ((line = reader.readLine()) != null && line.length() > 0) {

            lineOut = line;

            if (line.startsWith("N=")) {
                int numberOfNodes = Integer.parseInt(line.substring(2, line.indexOf(" ")));
                startIndexes = new int[numberOfNodes];
            }

            if (line.startsWith("I=")) {
                tokenizer = new StringTokenizer(line);
                nodeindex = Integer.parseInt(tokenizer.nextToken().substring(2));
                frameIndex = Math.round(Float.parseFloat(tokenizer.nextToken().substring(2)) * 100);
                startIndexes[nodeindex] = frameIndex;
            }
            if (line.startsWith("J=")) {
                tokenizer = new StringTokenizer(line);
                while (tokenizer.hasMoreTokens()) {
                    token = tokenizer.nextToken();
                    if (token.startsWith("S=")) {
                        beginIndex = Integer.parseInt(token.substring(2));
                    }
                    if (token.startsWith("E=")) {
                        endIndex = Integer.parseInt(token.substring(2));
                    }
                    if (token.startsWith("W=")) {
                        word = token.substring(2);
                        duration = (startIndexes[endIndex] - startIndexes[beginIndex]); //TODO make it clear here. Now the time unit is 10ms
                        lineOut = lineOut.replaceAll(token, String.format("W=%s%d", word, duration));
                    }
                }
            }
            writer.write(lineOut + "\n");
        }

        writer.close();
        reader.close();


    }


}
