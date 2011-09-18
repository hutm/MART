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

import org.apache.log4j.Logger;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.beat.BeatsManager;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;

import java.io.*;
import java.util.List;
import java.util.StringTokenizer;

import static org.mart.crs.config.Settings.*;
/**
 * Makes nesessary transform for FLM
 *
 * @version 1.0 03.04.2009 15:37:12
 * @author: Maksim Khadkevich
 */
public class HTKLatticeTransformer {

    protected static Logger logger = CRSLogger.getLogger(HTKLatticeTransformer.class);

    protected int[] startIndexes;

    public void transformLattice(String latticeFileName, String outLatticeFileName, boolean isOneChordPerBeatVersion) throws Exception {

        double[] beats = null;
        if (!isOneChordPerBeatVersion) {
            //First get beats for corresponding wave file
            File lattice = HelperFile.getFile(latticeFileName);
            String songName = (HelperFile.getFile(lattice.getParent())).getName();

            beats = (new BeatsManager()).getBeatsForWavFile(songName + WAV_EXT);
        }


        BufferedReader reader = new BufferedReader(new FileReader(latticeFileName));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outLatticeFileName));
        String line, lineOut, token, chord;
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
                        chord = token.substring(2);
                        if (!isOneChordPerBeatVersion) {
                            duration = calculateDuration(beats, startIndexes[beginIndex] / 10.0f, startIndexes[endIndex] / 10.0f);
                        } else {
                            duration = (startIndexes[endIndex] - startIndexes[beginIndex]) / 10;
                        }
                        if (IS_QUANTIZED_DURATION) {
                            duration = Helper.quantizeDuration(duration);
                        }
                        lineOut = lineOut.replaceAll(token, "W=W-" + chord + ":D-" + duration);
                    }
                }
            }
            writer.write(lineOut + "\n");
        }

        writer.close();
        reader.close();


    }

    protected  int calculateDuration(double[] beats, float startTime, float endTime) throws Exception {
        int startNearestBeatIndex = -1;
        int endNearestBeatIndex = -1;

        if (startTime <= beats[0]) {
            startNearestBeatIndex = 0;
        }
        if (endTime <= beats[0]) {
            endNearestBeatIndex = 0;
        }
        if (startTime >= beats[beats.length - 1]) {
            startNearestBeatIndex = beats.length;
        }
        if (endTime >= beats[beats.length - 1]) {
            endNearestBeatIndex = beats.length;
        }

        for (int i = 0; i < beats.length - 1; i++) {
            if (startTime > beats[i] && startTime <= beats[i + 1]) {
                if (startTime - beats[i] < beats[i + 1] - startTime) {
                    startNearestBeatIndex = i;
                } else {
                    startNearestBeatIndex = i + 1;
                }
            }

            if (endTime > beats[i] && endTime <= beats[i + 1]) {
                if (endTime - beats[i] < beats[i + 1] - endTime) {
                    endNearestBeatIndex = i;
                } else {
                    endNearestBeatIndex = i + 1;
                }
            }
        }

        if (startNearestBeatIndex < 0 || endNearestBeatIndex < 0) {
            throw new Exception("otstoj");
        }
        if (endNearestBeatIndex - startNearestBeatIndex == 0) {
            return 0;    //Before it was 1;
        } else {
            return endNearestBeatIndex - startNearestBeatIndex;
        }
    }


    public void transformFolder(String dirPath, boolean isOneVectorPerBeatVersion) {
        String fileList = System.getProperty("java.io.tmpdir") + File.separator + "latticeList";
        HelperFile.createFileList(dirPath, fileList, new ExtensionFileFilter(new String[]{LATTICE_EXT}), false);
        List<String> latticeFilePathList = HelperFile.readLinesFromTextFile(fileList);

        transformFolder(latticeFilePathList, isOneVectorPerBeatVersion);
    }


    public void transformFolder(List<String> latticeFilePathList, boolean isOneVectorPerBeatVersion) {
        String outLatticeFilePath;
        File oldLattice, newLattice;
        for (String latticeFilePath : latticeFilePathList) {
            outLatticeFilePath = latticeFilePath.replaceAll(LATTICE_EXT, "_1" + LATTICE_EXT);
            try {
                logger.debug("Transforming " + latticeFilePath + " into " + outLatticeFilePath);
                transformLattice(latticeFilePath, outLatticeFilePath, isOneVectorPerBeatVersion);
                oldLattice = HelperFile.getFile(latticeFilePath);
                newLattice = HelperFile.getFile(outLatticeFilePath);
                oldLattice.delete();
                newLattice.renameTo(oldLattice);
            } catch (Exception e) {
                logger.error("Error while transforming file " + latticeFilePath + " into " + outLatticeFilePath);
                logger.error(Helper.getStackTrace(e));
            }
        }
    }


    public static void main(String[] args) {
        try {
//            new HTKLatticeTransformer().transformLattice("D:\\Beatles\\20090401\\results\\features\\01_-_Come_Together\\0_260.59_.lattice", "D:\\Beatles\\20090401\\results\\features\\01_-_Come_Together\\1.lattice");
            new HTKLatticeTransformer().transformFolder("D:\\Beatles\\lmTransform\\All\\results\\features\\01_-_A_Hard_Day's_Night", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
