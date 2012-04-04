package org.mart.crs.labelling;

import junit.framework.TestCase;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @version 1.0 27/10/10 02:39
 * @author: Hut
 */
public class TestCreateConfigListFromPrototype extends TestCase {

    protected static float startX = 0.9f;
    protected static float endX = 1.1f;
    protected static float stepX = 0.05f;


    protected static float startY = 0.6f;
    protected static float endY = 0.9f;
    protected static float stepY = 0.05f;


    public void testCreateListForNFold() throws IOException {

        String protoFile = "d:\\work\\resultsFirstDay\\current\\1000HzRetest8192_harmonic.cfg";

        String outDir = "d:\\work\\resultsFirstDay\\current\\out3";

        HelperFile.createDir(outDir);

        int numberOfBinsX = Math.round((endX - startX) / stepX) + 1;
        int numberOfBinsY = Math.round((endY - startY) / stepY) + 1;


        FileWriter[][] writerList = new FileWriter[numberOfBinsX][numberOfBinsY];
        for (float i = startX; i <= endX; i += stepX) {
            for (float j = startY; j <= endY; j += stepY) {
                FileWriter writer = new FileWriter(String.format("%s/%3.2f_%3.2f.cfg", outDir, i, j));
                writerList[(int) Math.round((i - startX) / stepX)][(int) Math.round((j - startY) / stepY)] = writer;
            }
        }

        FileReader reader = new FileReader(protoFile);
        List<String> lines = HelperFile.readLinesFromTextFile(protoFile);
        for (String line : lines) {
        for (float i = startX; i <= endX; i += stepX) {
            for (float j = startY; j <= endY; j += stepY) {
                    if (line.startsWith("featureExtractorsWeights=")) {
                        line = String.format("featureExtractorsWeights=%3.2f %3.2f", i, j);
                    }
                    writerList[(int) Math.round((i - startX) / stepX)][(int) Math.round((j - startY) / stepY)].write(line + "\r\n");
                }
            }
        }


        for (float i = startX; i <= endX; i += stepX) {
            for (float j = startY; j <= endY; j += stepY) {
                writerList[(int) Math.round((i - startX) / stepX)][(int) Math.round((j - startY) / stepY)].close();
            }
        }


    }


    public void testParseResultsInaSummaryFile() {
        String inDirPath = "/media/Data/work/resultsFirstDay/weightsTrain";
        File[] dirList = HelperFile.getFile(inDirPath).listFiles();

        int numberOfBinsX = Math.round((endX - startX) / stepX);
        int numberOfBinsY = Math.round((endY - startY) / stepY);


        float[][] results = new float[numberOfBinsX][numberOfBinsY];

        for (File dir : dirList) {
            if (dir.isFile()) {
                continue;
            }
            float weight1 = Helper.parseFloat(dir.getName().substring(0, dir.getName().indexOf("_")));
            float weight2 = Helper.parseFloat(dir.getName().substring(dir.getName().indexOf("_") + 1));
            float resultValue = getResultForDir(dir);
            results[(int) Math.round((weight1 - startX) / stepX)][(int) Math.round((weight2 - startY) / stepY)] = resultValue;
        }


        for (float i = startX; i <= endX; i += stepX) {
            for (float j = startY; j < endY; j += stepY) {
                System.out.print(results[(int) Math.round((i - startX) / stepX)][(int) Math.round((j - startY) / stepY)] + " ");
            }
            System.out.println("\r\n");
        }

        System.out.println("");

    }


    protected float getResultForDir(File dir) {
        String fileName = dir.getPath() + "/summary.txt";
        List<String> lines = HelperFile.readLinesFromTextFile(fileName);
        for (String line : lines) {
            if (line.startsWith("false\ttrue\t2\t1024\t0.9\t2048\t-35.0\t  9.0\t  1.0\t -3.0")) {
                return Helper.parseFloat(line.substring(line.lastIndexOf("\t")));
            }
        }
        return 0;
    }


}
