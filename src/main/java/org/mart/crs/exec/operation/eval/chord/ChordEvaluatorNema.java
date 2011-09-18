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

import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.helper.Helper;
import org.apache.log4j.Logger;
import org.imirsel.nema.analytics.evaluation.Evaluator;
import org.imirsel.nema.analytics.evaluation.EvaluatorFactory;
import org.imirsel.nema.analytics.evaluation.ResultRenderer;
import org.imirsel.nema.analytics.evaluation.ResultRendererFactory;
import org.imirsel.nema.model.*;
import org.imirsel.nema.model.fileTypes.ChordShortHandTextFile;
import org.imirsel.nema.model.fileTypes.SingleTrackEvalFileType;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.mart.crs.utils.helper.HelperFile.getFile;

/**
 * @version 1.0 3/31/11 11:03 AM
 * @author: Hut
 */
public class ChordEvaluatorNema extends ChordEvaluator {

    protected static Logger logger = CRSLogger.getLogger(ChordEvaluator.class);

    protected NemaTask task;
    protected NemaDataset dataset;
    protected static File workingDirectory;




    public void initializeDirectories(String recognizedDirPath, String groundTruthFolder, String outTxtFile) {
        super.initializeDirectories(recognizedDirPath, groundTruthFolder, outTxtFile);
        String tempLocation = System.getProperty("java.io.tmpdir");
        workingDirectory = new File(tempLocation);

        try {
            setUp();
        } catch (Exception e) {
            logger.error(Helper.getStackTrace(e));
        }
    }


    public void setUp() throws Exception {
        task = new NemaTask();
        task.setId(17);
        task.setName("Chord MIREX09");
        task.setDescription("Chord transcription task requiring participants to annotate and segment the chord events in the MIREX09chord transcription dataset.");
        task.setDatasetId(33);
        task.setSubjectTrackMetadataId(13);
        setUpMetaDataName();

        dataset = new NemaDataset();
        dataset.setId(task.getDatasetId());
        dataset.setName("MIREX09 Chord");
        dataset.setDescription("MIREX 2009 Chord transcription dataset composed of Christopher Harte's Beatles dataset (C4DM, Queen Mary's University of London) and Matthias Mauch's Queen and Zweieck dataset (C4DM, Queen Mary's University of London)");

    }

    protected void setUpMetaDataName(){
        task.setSubjectTrackMetadataName(NemaDataConstants.CHORD_LABEL_SEQUENCE);
    }

    public void evaluate() {
        try {
            List<NemaTrackList> testSets;
            File groundTruthDirectory = getFile(this.groundTruthFolder);
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


            File resultsDirectory = getFile(this.recognizedDirPath);
            String systemName = "Mega system!";
            Evaluator evaluator;
            ResultRenderer renderer;

            evaluator = EvaluatorFactory.getEvaluator(task.getSubjectTrackMetadataName(), task, dataset, null, testSets);
            renderer = ResultRendererFactory.getRenderer(task.getSubjectTrackMetadataName(), outputDirectory, workingDirectory, false, null);

            evaluator.setGroundTruth(groundTruth);

            List<NemaData> resultsForAllTracks = reader.readDirectory(resultsDirectory, null);
            evaluator.addResults(systemName, systemName, testSets.get(0), resultsForAllTracks);


            //test evaluation
            NemaEvaluationResultSet results = evaluator.evaluate();
            Map<NemaTrackList,List<NemaData>> perTrackResults =  results.getPerTrackEvaluationAndResults(systemName);
            List<NemaData> nemaDataList = perTrackResults.get(testSets.get(0));
            for(NemaData nemaData:nemaDataList){
                String song = nemaData.getId();
                double recognitionRate = nemaData.getDoubleMetadata(NemaDataConstants.CHORD_OVERLAP_RATIO);
                chordEvalResults.add(new ChordEvalResult(song, 1, 1, 1, recognitionRate, 1, 1));
            }

            Collections.sort(chordEvalResults);
            saveResults();

            //test rendering
            renderer.renderResults(results);
        } catch (IOException e) {
            logger.error(Helper.getStackTrace(e));
        } catch (InstantiationException e) {
            logger.error(Helper.getStackTrace(e));
        } catch (IllegalAccessException e) {
            logger.error(Helper.getStackTrace(e));
        }
    }



    public String getResultsHeadersCommaSeparated() {
        return String.format("%s,", CHORD_RECOGNITION_RATE_NAME);
    }

    public String getResultsValuesCommaSeparated() {
        return String.format("%5.3f,", chordRecognitionRateGlobal);
    }


}
