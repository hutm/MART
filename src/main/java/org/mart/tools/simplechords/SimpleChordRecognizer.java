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

import org.apache.log4j.Logger;
import org.mart.crs.config.ExecParams;
import org.mart.crs.core.AudioReader;
import org.mart.crs.core.pcp.PCPBuilder;
import org.mart.crs.core.pcp.spectral.PCP;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrumHarmonicPart;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.HelperFile;
import org.mart.crs.utils.windowing.WindowType;

import java.io.File;
import java.util.List;

/**
 * @version 1.0 1/5/12 8:40 PM
 * @author: Hut
 */
public class SimpleChordRecognizer {

    protected static Logger logger = CRSLogger.getLogger(AudioReader.class);


    protected String fileNameIn;
    protected String fileNameOut;

    public static final float SAMPLING_RATE = 2000f;
    public static final int FFT_SIZE = 256;
    public static final float OVERLAPPING = 0.5f;


    public SimpleChordRecognizer(String fileNameIn, String fileNameOut) {
        this.fileNameIn = fileNameIn;
        this.fileNameOut = fileNameOut;
    }


    public void initialize(){
        logger.info(String.format("Extracting chords from file %s", fileNameIn));
        
        File outDir = new File(fileNameOut);
        if(!outDir.exists() || !outDir.isDirectory() ) {
            outDir.mkdirs();
        }
        
        AudioReader reader = new AudioReader(fileNameIn, SAMPLING_RATE);
//        reader.storeAPieceOfMusicAsWav(reader.getSamples(), reader.getAudioFormat(), fileNameOut);
        ReassignedSpectrumHarmonicPart reassignedSpectrumHarmonicPart = new ReassignedSpectrumHarmonicPart(reader, FFT_SIZE, WindowType.HANNING_WINDOW.ordinal(), OVERLAPPING, 0.4f, ExecParams._initialExecParameters);
        PCP bass = new PCPBuilder(PCPBuilder.BASIC_ALG).setExecParams(ExecParams._initialExecParameters).setSpectrum(reassignedSpectrumHarmonicPart).setStartNoteForPCPWrapped(24).setEndNoteForPCPWrapped(54).build();
        PCP treble = new PCPBuilder(PCPBuilder.BASIC_ALG).setExecParams(ExecParams._initialExecParameters).setSpectrum(reassignedSpectrumHarmonicPart).setStartNoteForPCPWrapped(44).setEndNoteForPCPWrapped(82).build();

        PatternMatcher matcher = new PatternMatcherBass(bass.getPCP(), treble.getPCP());
        matcher.process();
        float[][] chordgram = matcher.getChordgram();
        
        float[] init = new float[chordgram[0].length];
        init[init.length - 1] = 1;

        float[] delta = new float[chordgram.length * chordgram[0].length];
        
        float selftransprob = 0.7f;
        float[][] transMatrix = new float[chordgram[0].length][chordgram[0].length];
        float lowProb = (1 - selftransprob) / (chordgram[0].length - 1);
        for(int i = 0; i < transMatrix.length; i++){
            for(int j = 0; j < transMatrix.length; j++){
                transMatrix[i][j] = lowProb;
            }
            transMatrix[i][i] = selftransprob;
            
        }
        
        for(int i = 0 ;i < transMatrix.length; i++){
            transMatrix[24][i] = 0.0f;
        }

        
        int[] path = ViterbiPath.decode(init, transMatrix, chordgram, delta);

        float frameSize = FFT_SIZE * (1 - OVERLAPPING) / SAMPLING_RATE;

        List<ChordSegment> chordSegments = ChordStructureParser.parseChords(path, frameSize);

        ChordStructure chordStructure = new ChordStructure(chordSegments, HelperFile.getShortFileNameWithoutExt(fileNameIn));
        chordStructure.saveSegmentsInFile(fileNameOut);
        
        System.out.println("done");
    }



//    public static void main(String[] args) {
//        String fileNameIn = "/home/hut/Downloads/NeNado.mp3";
////        String fileNameIn = "/home/hut/music/downloaded/игорь растеряева-комбайнеры .mp3";
////        String fileNameIn = "/home/hut/mirdata/chords/audio/03_-_I'm_Only_Sleeping.wav";
//        String fileNameOut = "/home/hut/temp/testShortNewGaussian";
//        SimpleChordRecognizer recognizer = new SimpleChordRecognizer(fileNameIn, fileNameOut);
//        recognizer.initialize();
//    }
//
    public static void main(String[] args){
        String fileNameIn = args[0];
        String fileNameOut = args[1];
        File[] inFiles = new File(fileNameIn).listFiles(new ExtensionFileFilter(new String[]{".wav", ".mp3"}));
        for(File inWavFile: inFiles){
            SimpleChordRecognizer recognizer = new SimpleChordRecognizer(inWavFile.getPath(), fileNameOut);
            recognizer.initialize();
        }
    }
    
    
    
}
