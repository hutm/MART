package org.mart.crs.eval;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import static org.mart.crs.utils.helper.Helper.parseInt;


/**
 * @version 1.0 Feb 14, 2010 11:24:23 PM
 * @author: Maksim Khadkevich
 */
public class TestSummarizeResultsToExcel extends TestCase {

    protected static Logger logger = CRSLogger.getLogger(TestSummarizeResultsToExcel.class);


    public static String dirPath = "D:\\PhD\\results\\PQMFNCC_ISMIR2010\\work_full_first";
    public static String outFile = "D:\\PhD\\results\\PQMFNCC_ISMIR2010\\work_full_first.txt";
    public static String ext = ".txt";

    public static String KEY = "Average timeunit:";


    public void testSummarizeResults() {

        List<Result> resultsList = new ArrayList<Result>();

        File rootDir = HelperFile.getFile(dirPath);
        parseTree(rootDir, resultsList);

        Collections.sort(resultsList);
        List<String> stringData = new ArrayList<String>();
        for (Result result : resultsList) {
            stringData.add(result.toString());
        }
        HelperFile.saveCollectionInFile(stringData, outFile, false);

    }

    private void parseTree(File file, List<Result> resultsList) {

        File[] files = file.listFiles(new ExtensionFileFilter(ext));
        for (File child : files) {
            if (child.isDirectory()) {
                parseTree(child, resultsList);
            } else {
                try {
                    resultsList.add(getDataFromFile(child));
                } catch (Exception e) {
                    logger.error(String.format("Problems with parsing file %s", child.getPath()));
                    logger.error(Helper.getStackTrace(e));
                }
            }

        }


    }

    private Result getDataFromFile(File file) {
        float result = 0;
        List<String> lines = HelperFile.readLinesFromTextFile(file.getPath());
        for (String line : lines) {
            if (line.startsWith(KEY)) {
                String floatStr = line.substring(KEY.length() + 1);
                result = Helper.parseFloat(floatStr);
            }

        }

        int fold;
        int frameSize;
        int gaussians;
        int penalty;

        StringTokenizer tokenizer = new StringTokenizer(file.getName(), "_.");
        tokenizer.nextToken();
        gaussians = parseInt(tokenizer.nextToken());
        penalty = parseInt(tokenizer.nextToken());

        tokenizer = new StringTokenizer(file.getParentFile().getParentFile().getName(), "_.");
        fold = parseInt(tokenizer.nextToken());
        frameSize = parseInt(tokenizer.nextToken());

        return new Result(fold, frameSize, gaussians, penalty, result);

    }


}


class Result implements Comparable {
    private int fold;
    private int frameSize;
    private int gaussians;
    private int penalty;
    private float result;


    Result(int fold, int frameSize, int gaussians, int penalty, float result) {
        this.fold = fold;
        this.frameSize = frameSize;
        this.gaussians = gaussians;
        this.penalty = penalty;
        this.result = result;
    }

    @Override
    public String toString() {
        return String.format("%d\t%d\t%d\t%d\t%5.2f", fold, frameSize, gaussians, penalty, result * 100);
    }

    public int compareTo(Object o) {
        Result compared = (Result) o;
        if (this.fold != compared.getFold()) {
            return this.fold - compared.getFold();
        } else if (this.frameSize != (compared.getFrameSize())) {
            return this.frameSize - (compared.getFrameSize());
        } else if (this.gaussians !=(compared.getGaussians())) {
            return this.gaussians - (compared.getGaussians());
        } else
            return -1 * (this.penalty - (compared.getPenalty()));
    }


    public int getFold() {
        return fold;
    }

    public int getFrameSize() {
        return frameSize;
    }

    public int getGaussians() {
        return gaussians;
    }

    public int getPenalty() {
        return penalty;
    }

    public float getResult() {
        return result;
    }
}
