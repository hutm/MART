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

package org.mart.crs.core.pcp;

import org.apache.log4j.Logger;
import org.mart.crs.core.spectrum.HarmonicFrofileVector;
import org.mart.crs.logging.CRSLogger;

import java.util.HashMap;
import java.util.Map;


/**
 * @version 1.0 Dec 16, 2009 5:55:55 PM
 * @author: Maksim Khadkevich
 */
public abstract class PCPComb extends PCPBasic {



    protected static Logger logger = CRSLogger.getLogger(PCPComb.class);

    protected float[][][] scores;

    protected Map<Integer, float[]> noteProbabilities;
    protected Map<Integer, HarmonicFrofileVector[]> scoresVectorMap;

    protected PCPComb() {
    }

    protected PCPComb(double refFreq, int averagingFactor, int numberOfBinsPerSemitone, boolean toNormalize, int startNoteForPCPWrapped, int endNoteForPCPWrapped, float chromaSpectrumRate) {
        super(refFreq, averagingFactor, numberOfBinsPerSemitone, toNormalize, startNoteForPCPWrapped, endNoteForPCPWrapped, chromaSpectrumRate);
    }

    protected void init(){
        noteProbabilities = new HashMap<Integer, float[]>();
        scoresVectorMap = new HashMap<Integer, HarmonicFrofileVector[]>();
    }

    protected void constructPCP() {
        float[][] outPCPData = new float[noteProbabilities.get(this.startNoteForPCPUnwrapped).length][getNumberOfBinsForUnwrappedChroma()];
        scores = new float[outPCPData.length][outPCPData[0].length][3];

        for (int j = 0; j < outPCPData[0].length; j++) {
            float[] data = noteProbabilities.get(j + this.startNoteForPCPUnwrapped);
            HarmonicFrofileVector[] proVectors = scoresVectorMap.get(j + this.startNoteForPCPUnwrapped);
            if (data != null) {
                for (int i = 0; i < outPCPData.length; i++) {
                    if (data.length > i) {
                        outPCPData[i][j] = data[i];
                        scores[i][j] = proVectors[i].getScores();
                    }
                }
            }
        }
        setPcpUnwrapped(outPCPData);
    }


    public float[] getHarmonicVectorScores(float timeMoment, int midiNumber) {
        int index = getIndexForTimeMoment(timeMoment);
        return scores[index][midiNumber - this.startNoteForPCPUnwrapped];
    }

}
