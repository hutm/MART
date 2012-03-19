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

package org.mart.tools.simplechords;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 1/11/12 4:19 PM
 * @author: Hut
 */
public class ViterbiPath {


    public static int[] decode(float[] init, float[][] trans, float[][] obs, float[] delta) {

        int nState = init.length;
        int nFrame = obs.length;

        // check for consistency
        if (trans[0].length != nState || trans.length != nState || obs[0].length != nState) {
            System.out.println("ERROR: matrix sizes inconsistent.");
        }

        // vector<vector<double> > delta; // "matrix" of conditional probabilities
        int[][] psi = new int[nFrame][nState]; //  "matrix" of remembered indices of the best transitions
        int[] path = new int[nFrame];     // the final output path (current assignment arbitrary, makes sense only for Chordino, where nChord-1 is the "no chord" label)
        for (int i = 0; i < nFrame; i++) {
            path[i] = nState - 1;
        }

        List<Float> scale = new ArrayList<Float>(nFrame); // remembers by how much the vectors in delta are scaled.

        double deltasum = 0;

        /* initialise first frame */
        // delta.push_back(init);
        for (int iState = 0; iState < nState; ++iState) {
            delta[iState] = init[iState] * obs[0][iState];
            deltasum += delta[iState];
        }
        for (int iState = 0; iState < nState; ++iState) delta[iState] /= deltasum; // normalise (scale)
        scale.add(new Float(1.0f / deltasum));
//        psi.push_back(vector <int>(nState, 0));

        /* rest of the forward step */
        for (int iFrame = 1; iFrame < nFrame; ++iFrame) {
            // delta.push_back(vector<double>(nState,0));
            deltasum = 0;
//            psi.push_back(vector <int>(nState, 0));
            /* every state wants to know which previous state suits him best */
            for (int jState = 0; jState < nState; ++jState) {
                int bestState = nState - 1;
                float bestValue = 0;
                if (obs[iFrame][jState] > 0) {
                    for (int iState = 0; iState < nState; ++iState) {
                        float currentValue = delta[(iFrame - 1) * nState + iState] * trans[iState][jState];
                        if (currentValue > bestValue) {
                            bestValue = currentValue;
                            bestState = iState;
                        }
                    }
                }
                // cerr << bestState <<" ::: " << bestValue << endl ;
                delta[iFrame * nState + jState] = bestValue * obs[iFrame][jState];
                deltasum += delta[iFrame * nState + jState];
                psi[iFrame][jState] = bestState;
            }
            if (deltasum > 0) {
                for (int iState = 0; iState < nState; ++iState) {
                    delta[iFrame * nState + iState] /= deltasum; // normalise (scale)
                }
                scale.add(new Float(1.0 / deltasum));
            } else {
                for (int iState = 0; iState < nState; ++iState) {
                    delta[iFrame * nState + iState] = 1.0f / nState;
                }
                scale.add(1.0f);
            }

        }

        /* initialise backward step */
        float bestValue = 0;
        for (int iState = 0; iState < nState; ++iState) {
            float currentValue = delta[(nFrame - 1) * nState + iState];
            if (currentValue > path[nFrame - 1]) {
                bestValue = currentValue;
                path[nFrame - 1] = iState;
            }
        }

        /* rest of backward step */
        for (int iFrame = nFrame - 2; iFrame > -1; --iFrame) {
            path[iFrame] = psi[iFrame + 1][path[iFrame + 1]];
            // cerr << path[iFrame] << endl;
        }

        return path;

    }


}
