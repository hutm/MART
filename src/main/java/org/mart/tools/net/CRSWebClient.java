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

package org.mart.tools.net;

import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.features.FeatureVector;
import org.mart.crs.management.features.FeaturesManager;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;
import org.mart.tools.tags.MP3TagManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 12-Jul-2010 14:57:09
 * @author: Hut
 */
public class CRSWebClient {

    protected static Logger logger = CRSLogger.getLogger(CRSWebClient.class);

    protected String audioFilePath;
    protected FeatureVector featureVector;


    public CRSWebClient(String audioFilePath) {
        this.audioFilePath = audioFilePath;
    }

    public List<ChordSegment> prepareFeaturesAndSendToServer() {

        try {
            this.featureVector = extractFeaturevectors(audioFilePath);
        } catch (Exception e) {
            logger.info(String.format("Could not process audio file %s", audioFilePath));
            return new ArrayList<ChordSegment>();
        }

        int hash = 0;
        try {
            hash = sendFeatureVectorsToServer(featureVector);
        } catch (Exception e) {
            logger.info(String.format("Could not send feature vectors to  server"));
            return new ArrayList<ChordSegment>();
        }
        List<ChordSegment> outList = null;
        try {
            outList = getLabelsFromServer(hash);
        } catch (Exception e) {
            logger.info(String.format("Could not retrieve chord labels from server"));
            return new ArrayList<ChordSegment>();
        }

        return outList;
    }


    protected FeatureVector extractFeaturevectors(String audioFilePath) {
        //First try to extract mp3 tag
        String[] data;
        data = MP3TagManager.getTitleAndArtist(HelperFile.getFile(audioFilePath));

        logger.info("Extracting features...");
        FeaturesManager featuresSet = new FeaturesManager(audioFilePath, "", false, ExecParams._initialExecParameters);
        featuresSet.initializeWithSong(audioFilePath);
        FeatureVector featureVector = featuresSet.extractFeatureVectorForTest(Settings.REFERENCE_FREQUENCY);

        featureVector.setAdditionalInfo(data[0] + "_-_" + data[1]);

        featuresSet = null;
        System.gc();
        return featureVector;
    }


    protected int sendFeatureVectorsToServer(FeatureVector featureVector) {
        boolean sent = false;
        int hash = 0;
        while (!sent) {
            try {
                hash = FeaturesVectorSender.sendFeatureVectors(featureVector);
                sent = true;
                logger.info("Successfully sent feature vectors to server");
            } catch (Exception e) {
                logger.error("Error when sending feature vectors to server");
                logger.error(Helper.getStackTrace(e));
                sent = false;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e1) {
                }
            }
        }
        return hash;
    }


    protected static List<ChordSegment> getLabelsFromServer(Integer hash) {
        logger.info("Waiting for the responce from server...");

        List<ChordSegment> chordLabels = null;

        do {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
            chordLabels = LabelsReceiver.receiveLabels(hash);
        } while (chordLabels == null || chordLabels.size() == 0);
        logger.info("Labels received!!!");
        logger.info("Prepairing players");
        return chordLabels;
    }


}
