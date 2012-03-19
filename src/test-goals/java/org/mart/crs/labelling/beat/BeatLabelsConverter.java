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

package org.mart.crs.labelling.beat;

import junit.framework.TestCase;
import org.mart.crs.exec.operation.eval.beat.BeatEvaluator;
import org.mart.crs.exec.operation.eval.beat.BeatEvaluatorNema;
import org.mart.crs.management.beat.BeatStructure;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @version 1.0 9/21/11 7:26 PM
 * @author: Hut
 */
public class BeatLabelsConverter extends TestCase {

    String inDir1 = "/home/hut/PhD/papers/mypapers/11_ICASSP2012/results/hansworth/output/results_3dim";
    String inDir2 = "/home/hut/PhD/papers/mypapers/11_ICASSP2012/results/hansworth/output/results_2dim";
    String inDir3 = "/home/hut/PhD/papers/mypapers/11_ICASSP2012/results/hansworth/output/labels";

    String outDir1 = "/home/hut/PhD/papers/mypapers/11_ICASSP2012/results/hansworth/xml/results_3dim";
    String outDir2 = "/home/hut/PhD/papers/mypapers/11_ICASSP2012/results/hansworth/xml/results_2dim";
    String outDir3 = "/home/hut/PhD/papers/mypapers/11_ICASSP2012/results/hansworth/xml/labels";

    String list1 = "/home/hut/PhD/papers/mypapers/11_ICASSP2012/results/hansworth/xml/list3dims.txt";
    String list2 = "/home/hut/PhD/papers/mypapers/11_ICASSP2012/results/hansworth/xml/list2dims.txt";

    public void testTrainsformLabels() {
        transformLabels(inDir1, outDir1);
        transformLabels(inDir2, outDir2);
        transformLabels(inDir3, outDir3);

        createListForMatlabBeatEvaluations(outDir3, "/home/hut/PhD/experiments/beatForICASSP/hains1/output/2dims4", "/home/hut/PhD/experiments/beatForICASSP/hains1/matlabList");
        createListForMatlabBeatEvaluations(outDir3, "/home/hut/PhD/experiments/beatForICASSP/hains1/output/DAVIES", "/home/hut/PhD/experiments/beatForICASSP/hains1/matlabListDAVIES");

    }


    public void testTrainsformLabelsFromMazurka() {
        transformLabels("/home/hut/mirdata/mazurka/labels", "/home/hut/mirdata/mazurka/labelsXML");
    }


    public void testEvaluate() throws Exception {

//        BeatEvaluator evaluator = new BeatEvaluator();
//        evaluator.initializeDirectories(outDir1, outDir3, outDir3 + "results3dims");
//        evaluator.evaluate();
//
//        evaluator = new BeatEvaluator();
//        evaluator.initializeDirectories(outDir2, outDir3, outDir3 + "results2dims");
//        evaluator.evaluate();

        BeatEvaluatorNema evaluatorNema = new BeatEvaluatorNema();
        evaluatorNema.initializeDirectories("/home/hut/PhD/experiments/beatForICASSP/hainsworth/output", "/home/hut/PhD/experiments/beatForICASSP/hainsworth/gt", "/home/hut/PhD/experiments/beatForICASSP/hainsworth/NEMARESULTS");
        evaluatorNema.evaluate();


    }


    public static void createListForMatlabBeatEvaluations(String gtFilePath, String outFilePath, String outFolder) {

        File outDirInitial = new File(outFilePath);
        String outDirTransformed = String.format("%s/%s", outFolder, outDirInitial.getName());
        String listFilePath = String.format("%s/%s.txt", outFolder, outDirInitial.getName());


        String gtDirTransformed = String.format("%s/gt", outFolder);

        transformLabels(gtFilePath, gtDirTransformed);
        transformLabels(outFilePath, outDirTransformed);


        List<String> outStrings1 = new ArrayList<String>();

        File[] files = HelperFile.getFile(gtDirTransformed).listFiles();
        for (File file : files) {
            outStrings1.add(String.format("%s\t%s/%s", file.getPath(), outDirTransformed, file.getName()));
        }

        Collections.sort(outStrings1);
        HelperFile.saveCollectionInFile(outStrings1, listFilePath, false);

    }





    public static void transformLabels(String inDir, String outDir) {
        HelperFile.createDir(outDir);
        File[] files = HelperFile.getFile(inDir).listFiles();
        for (File file : files) {
            BeatStructure beatStructure = BeatStructure.getBeatStructure(file.getPath());
            beatStructure.serializeIntoXML(String.format("%s/%s.xml", outDir, HelperFile.getNameWithoutExtension(file.getName())));
        }
    }



}
