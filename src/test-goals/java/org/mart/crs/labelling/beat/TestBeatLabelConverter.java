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

/**
 * @version 1.0 9/24/11 10:38 AM
 * @author: Hut
 */
public class TestBeatLabelConverter extends TestCase {

    String indir = "/home/hut/temp/peetersBin/results";
    String outdir = "/home/hut/temp/peetersBin/resultsXML";

    String labelsDir = "/home/hut/mirdata/beatenst/labels2011";
    String gtDir = "/home/hut/temp/peetersBin/gtLabelsTXT";


    public void testConvertLabels() {


        BeatLabelsConverter.transformLabels(indir, outdir);
        BeatLabelsConverter.transformLabels(labelsDir, gtDir);


        BeatEvaluator evaluator = new BeatEvaluator();
        evaluator.initializeDirectories(outdir, gtDir, outdir + "evaluated");
        evaluator.evaluate();



    }

    public void testEvaluateNema() throws Exception {
        BeatEvaluatorNema evaluatorNema = new BeatEvaluatorNema();
        evaluatorNema.initializeDirectories("/home/hut/temp/compareWithDavies/output", "/home/hut/temp/compareWithDavies/gt", "/home/hut/temp/compareWithDavies/gt_results");
        evaluatorNema.evaluate();

        BeatEvaluatorNema.isOnlyDownBeatEvaluation = true;
        evaluatorNema = new BeatEvaluatorNema();
        evaluatorNema.initializeDirectories("/home/hut/temp/compareWithDavies/output", "/home/hut/temp/compareWithDavies/gt", "/home/hut/temp/compareWithDavies/gt_results");
        evaluatorNema.evaluate();
    }


    public void testCreateListsForMatlabTest(){
        BeatLabelsConverter.createListForMatlabBeatEvaluations(labelsDir, outdir, outdir + "listMatlab.txt");
    }

}
