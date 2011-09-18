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

package org.mart.crs.exec;

import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.features.FeaturesSaveOperation;
import org.mart.crs.exec.operation.models.test.chord.RecognizeOperation;
import org.mart.crs.exec.scenario.CRSScenario;
import org.mart.crs.exec.scenario.stage.StageParameters;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.features.FeatureVector;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.utils.helper.Helper;
import org.mart.tools.net.FeatureVectorsReceiver;
import org.mart.tools.net.LabelsSender;
import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version 1.0 08-Jul-2010 21:04:25
 * @author: Hut
 */
public class ClassificatorService {

    protected static Logger logger = CRSLogger.getLogger(ClassificatorService.class);


    public static void main(String[] args) {
        try {
            while(true){
                Map<Integer, FeatureVector> inMap = null;
                try {
                    inMap = FeatureVectorsReceiver.receiveFeatureVectors();
                } catch (Exception e) {
                    logger.info("Could not connect to server");
                }
                if(inMap != null && inMap.entrySet().size() > 0){
                    logger.info(String.format("Processing %d files...", inMap.entrySet().size()));
                    saveTags(inMap);
                    Map<Integer, List<ChordSegment>> labelsMap = processFeatures(inMap);
                    boolean sent = false;
                    while(!sent){
                        try {
                            LabelsSender.sendLabels(labelsMap);
                            sent = true;
                            logger.info("Successfully sent labels to server");
                        } catch (Exception e) {
                            logger.error("Error when sending labels to server");
                            logger.error(Helper.getStackTrace(e));
                            sent = false;
                            Thread.sleep(2000);
                        }
                    }
                } else{
                    logger.info("Nothing to process");
                }
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            logger.error(Helper.getStackTrace(e));
            main(args);
        }
    }


    public static Map<Integer, List<ChordSegment>> processFeatures(Map<Integer, FeatureVector> featuresMap) {
        CRSScenario scenario = new CRSScenario(false);
        StageParameters stageParameters = new StageParameters();
        stageParameters.setWorkingDir(ExecParams._initialExecParameters._workingDir);
        scenario.addOperation(new FeaturesSaveOperation(stageParameters, ExecParams._initialExecParameters, featuresMap));
        scenario.addOperation(new RecognizeOperation(stageParameters, ExecParams._initialExecParameters));
        scenario.run();

        Map<Integer, List<ChordSegment>> outMap = new HashMap<Integer, List<ChordSegment>>();
        String outLabelsDir = String.format("%s/results/results_%d_%5.1f/", ExecParams._initialExecParameters._workingDir, ExecParams._initialExecParameters.gaussianNumber, ExecParams._initialExecParameters.penalty);
        for (Integer hash : featuresMap.keySet()) {
            List<ChordSegment> labels = (new ChordStructure(outLabelsDir + hash + Settings.LABEL_EXT)).getChordSegments();
            outMap.put(hash, labels);
        }
        return outMap;
    }



    protected static void saveTags(Map<Integer,FeatureVector> inMap){
        try {
            FileWriter writer = new FileWriter("statistics.txt", true);

            for(Integer hash:inMap.keySet()){
                String info = inMap.get(hash).getAdditionalInfo();
                writer.write(info + "\n");
            }

            writer.close();

        } catch (Exception e) {
            logger.error(Helper.getStackTrace(e));
        }

    }


}
