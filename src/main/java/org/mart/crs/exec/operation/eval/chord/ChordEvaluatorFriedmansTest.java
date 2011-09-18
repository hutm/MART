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

import org.imirsel.nema.analytics.evaluation.Evaluator;
import org.imirsel.nema.analytics.evaluation.EvaluatorFactory;
import org.imirsel.nema.analytics.evaluation.ResultRenderer;
import org.imirsel.nema.analytics.evaluation.ResultRendererFactory;
import org.imirsel.nema.model.NemaData;
import org.imirsel.nema.model.NemaEvaluationResultSet;
import org.imirsel.nema.model.NemaTrack;
import org.imirsel.nema.model.NemaTrackList;
import org.imirsel.nema.model.fileTypes.ChordShortHandTextFile;
import org.imirsel.nema.model.fileTypes.SingleTrackEvalFileType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @version 1.0 6/23/11 5:16 PM
 * @author: Hut
 */
public class ChordEvaluatorFriedmansTest extends ChordEvaluatorNema {




	public void evaluateManyShortHandBasedSystems() throws FileNotFoundException, IOException, IllegalArgumentException, IOException, InstantiationException, IllegalAccessException{
		List<NemaTrackList> testSets;
		File groundTruthDirectory = new File(groundTruthFolder);
        SingleTrackEvalFileType reader = new ChordShortHandTextFile();
        List<NemaData> groundTruth = reader.readDirectory(groundTruthDirectory, null);

        ArrayList<NemaTrack> trackList = new ArrayList<NemaTrack>(groundTruth.size());
        for (Iterator<NemaData> iterator = groundTruth.iterator(); iterator.hasNext();) {
        	trackList.add(new NemaTrack(iterator.next().getId()));
		}

        testSets = new ArrayList<NemaTrackList>(1);
        int id = 0;
        testSets.add(new NemaTrackList(id, task.getDatasetId(), 3, "test", id, trackList));
        id++;


		File resultsDirectory = new File(recognizedDirPath);

		List<File> systemDirs = new ArrayList<File>();
		List<String> systemNames = new ArrayList<String>();
		File [] files = resultsDirectory.listFiles();
        for (int i = 0; i < files.length; i++) {
			if(files[i].isDirectory() && !(files[i].getName().equals(".svn"))){
				String systemName = files[i].getName();

				System.out.println("got system: " + systemName);
				systemDirs.add(files[i]);
				systemNames.add(systemName);
			}
		}


		Evaluator evaluator;
		ResultRenderer renderer;

		//test reader and setup for evaluation
		evaluator = EvaluatorFactory.getEvaluator(task.getSubjectTrackMetadataName(), task, dataset, null, testSets);
		renderer = ResultRendererFactory.getRenderer(task.getSubjectTrackMetadataName(), outputDirectory, workingDirectory, true, new File("/opt/matlab2010/bin/matlab"));

		evaluator.setGroundTruth(groundTruth);

		//read system results
		for (int i = 0; i < systemNames.size(); i++) {
			List<NemaData> resultsForAllTracks = reader.readDirectory(systemDirs.get(i), null);
			evaluator.addResults(systemNames.get(i), systemNames.get(i), testSets.get(0), resultsForAllTracks);
		}

		//test evaluation
		//test evaluation
		NemaEvaluationResultSet results = evaluator.evaluate();

		//test rendering
		renderer.renderResults(results);
	}



    public static void main(String[] args) throws Exception {
        ChordEvaluatorFriedmansTest chordEvaluatorFriedmansTest = new ChordEvaluatorFriedmansTest();
        chordEvaluatorFriedmansTest.initializeDirectories("/home/hut/PhD/experiments/ReasresolutionConfigs/limitReasDistanceResults/data_less", "/home/hut/PhD/experiments/ReasresolutionConfigs/limitReasDistanceResults/labels", "/home/hut/PhD/experiments/ReasresolutionConfigs/limitReasDistanceResults/results.txt");
        chordEvaluatorFriedmansTest.setUp();
        chordEvaluatorFriedmansTest.evaluateManyShortHandBasedSystems();
    }


}
