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

package org.mart.crs.exec.operation.models.htk.parser.chord;

/**
 * @version 1.0 5/20/11 3:19 PM
 * @author: Hut
 */
public class ChordHTKParserFromLatticeBeatSynchronous extends ChordHTKParserFromLattice {

    public ChordHTKParserFromLatticeBeatSynchronous(String htkOutFilePath, String parsedLabelsDir) {
        super(htkOutFilePath, parsedLabelsDir);
    }

    public void parseResults() {
        super.parseResults();
        assignBeatTimings();
    }

    @Override
    protected void setupMultiplicationCoeff() {
        this.multiplicationCoeff = 1.0f;
    }
}

