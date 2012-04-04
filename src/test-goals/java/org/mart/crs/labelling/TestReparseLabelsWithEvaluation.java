package org.mart.crs.labelling;

import junit.framework.TestCase;
import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.eval.chord.EvaluatorOld;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;

/**
 * @version 1.0 Jun 16, 2009 10:39:21 PM
 * @author: Maksim Khadkevich
 */
public class TestReparseLabelsWithEvaluation extends TestCase {


    public void testReparseLabels() {
        String rootDirFilePath = "D:\\Beatles\\lmTransformFLM";
        File rootDir = HelperFile.getFile(rootDirFilePath);
        parseFolder(rootDir);

    }

    public void parseFolder(File folder) {
        File[] children = folder.listFiles();
        for (File child : children) {
            if (child.isDirectory()) {
                for (File child2 : child.listFiles()) {
                    if (child2.isDirectory()) {
                        for (File child4 : child2.listFiles()) {
//                            for (File child4 : child3.listFiles()) {
                                if (child4.isDirectory() && child4.getName().startsWith("result")) {
                                    System.out.println("processing " + child4.getAbsolutePath());
                                    EvaluatorOld.makeEvaluation(child4.getAbsolutePath(), Settings.labelsGroundTruthDir, child4.getAbsolutePath() + "_new.txt");
//                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        String outputLablesDir = "D:\\Beatles\\outOudre_";
        EvaluatorOld.makeEvaluation(outputLablesDir, Settings.labelsGroundTruthDir, outputLablesDir + "_new.txt");
    }
}
