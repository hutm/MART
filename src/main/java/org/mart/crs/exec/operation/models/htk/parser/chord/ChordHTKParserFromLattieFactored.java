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

import java.util.StringTokenizer;

/**
 * @version 1.0 5/14/11 2:57 PM
 * @author: Hut
 */
public class ChordHTKParserFromLattieFactored extends ChordHTKParserFromLattice {

    public ChordHTKParserFromLattieFactored(String htkOutFilePath, String parsedLabelsDir) {
        super(htkOutFilePath, parsedLabelsDir);
    }


    @Override
    protected String parseChordName(String chordNameUnparsed) {
        StringTokenizer tokenizerChordName = new StringTokenizer(chordNameUnparsed, ":");
        return  tokenizerChordName.nextToken().substring(2);
    }
}
