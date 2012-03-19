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

import org.mart.crs.utils.filefilter.ExtensionFileFilter;

import java.io.File;

/**
 * @version 1.0 1/13/12 3:59 PM
 * @author: Hut
 */
public class TestSimpleChordRecognizer {
    
    
    public void testExtractBeatlesLabels(){
        String fileNameIn = "";
        String fileNameOut = "";
        
        File[] inFiles = new File(fileNameIn).listFiles(new ExtensionFileFilter(new String[]{".wav", ".mp3"}));
        for(File inWavFile: inFiles){
            SimpleChordRecognizer recognizer = new SimpleChordRecognizer(inWavFile.getPath(), fileNameOut);
            recognizer.initialize();
        }
    }
    
    
}
