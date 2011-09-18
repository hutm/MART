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

package org.mart.crs.management.features.extractor.unused;

import org.mart.crs.config.Settings;
import org.mart.crs.core.pcp.PCPBuilder;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.features.extractor.FeaturesExtractorHTK;
import org.mart.crs.management.matlab.MatlabManager;
import org.mart.crs.utils.helper.HelperFile;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * @version 1.0 Mar 3, 2010 3:21:47 PM
 * @author: Maksim Khadkevich
 */
public class PitchFilteringBased extends FeaturesExtractorHTK {

    protected static Logger logger = CRSLogger.getLogger(PitchFilteringBased.class);


    protected float[][] pcp;
    protected float featuresSamplingRate;


    public PitchFilteringBased() {
        super();
        HelperFile.getFile(Settings.SAVED_FEATURES_ALTERNATIVE_FOLDER).mkdirs();
    }

    @Override
    public int getVectorSize() {
        return Settings.NUMBER_OF_SEMITONES_IN_OCTAVE;
    }


    public void initialize(String songFilePath) {
        super.initialize(songFilePath);
        String extractedFeaturesFilePath = Settings.SAVED_FEATURES_ALTERNATIVE_FOLDER + File.separator + HelperFile.getShortFileNameWithoutExt(songFilePath) + "_chroma" + Settings.MAT_EXT;
        if (HelperFile.getFile(extractedFeaturesFilePath).exists()) {
            logger.info(String.format("PCP has already been extracted for file %s", songFilePath));
            MatlabManager manager = new MatlabManager(extractedFeaturesFilePath);
            pcp = manager.getStoredPCP();
            featuresSamplingRate = manager.getSamplingRate();
//TODO             this.chrSamplingPeriod = (int) (HTKResultsParser.FEATURE_SAMPLE_RATE / featuresSamplingRate);
        } else {
            logger.warn(String.format("Could not find stored PCP for file %s", songFilePath));
        }

    }


    @Override
    public void  extractGlobalFeatures(double refFrequency) {
        float[][] outUnrotated = new PCPBuilder(PCPBuilder.BASIC_ALG).setPcpData(pcp).setUnwrappedPCPData(false).build().getPCP();
        globalVectors.add(outUnrotated);
    }
}
