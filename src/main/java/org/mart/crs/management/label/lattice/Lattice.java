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

package org.mart.crs.management.label.lattice;

import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static org.mart.crs.config.Settings.CHROMA_EXT;
import static org.mart.crs.utils.helper.HelperFile.getFile;

/**
 * @version 1.0 Sep 28, 2009 4:34:48 PM
 * @author: Maksim Khadkevich
 */
public class Lattice {

    protected static Logger logger = CRSLogger.getLogger(Lattice.class);

    //Defines a lattice originalFormat: false if a lattice was generated from FLM
    protected boolean isOnlyChordLabelWithoutDuration = true;

    protected String sourceFileName;

    //Song for which this lattice was generated
    protected ChordStructure song;


    //Lattice data
    private int[] nodes;
    List<Arc> arcs;


    /**
     * Reading lattice structure from a file
     *
     * @param fileName fileName
     */
    public Lattice(String fileName) {
        this.sourceFileName = fileName;
        initialize();
    }

    /**
     * Constructing lattice from a hypothesis list
     *
     * @param aSong song
     */
    public Lattice(ChordStructure aSong) {
        this.song = aSong;
        initializeFromSong();
    }


    /**
     * Read a lattice from file
     */
    public void initialize() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(sourceFileName));
            String line, token, chord;
            StringTokenizer tokenizer;
            int nodeindex, frameIndex;
            int beginIndex = 0, endIndex = 0;
            float acScore, lmScore;


            while ((line = reader.readLine()) != null && line.length() > 0) {

                Arc arc;

                if (line.startsWith("N=") || line.startsWith("NODES=")) {
                    int numberOfNodes = Integer.parseInt(line.substring(line.indexOf("=") + 1, line.indexOf(" ")));
                    nodes = new int[numberOfNodes];

//                    int numberOfArcs = Integer.parseInt(line.substring(line.indexOf("L=") + 2).trim());
                    arcs = new ArrayList<Arc>();
                }

                if (line.startsWith("I=")) {
                    tokenizer = new StringTokenizer(line);
                    token = tokenizer.nextToken();
                    nodeindex = Integer.parseInt(token.substring(token.indexOf("I=") + 2));
                    token = tokenizer.nextToken();
                    frameIndex = Math.round(Float.parseFloat(token.substring(token.indexOf("t=") + 2)) * 100);
                    nodes[nodeindex] = frameIndex;
                }

                if (line.startsWith("J=")) {
                    tokenizer = new StringTokenizer(line);
                    arc = new Arc();
                    while (tokenizer.hasMoreTokens()) {
                        token = tokenizer.nextToken();
                        if (token.startsWith("S=")) {
                            beginIndex = Integer.parseInt(token.substring(2));
                            arc.setOnset(nodes[beginIndex] / 10.0f);
                        }
                        if (token.startsWith("E=")) {
                            endIndex = Integer.parseInt(token.substring(2));
                            arc.setOffset(nodes[endIndex] / 10.0f);
                        }
                        if (token.startsWith("W=")) {
                            chord = token.substring(2);
                            if (isOnlyChordLabelWithoutDuration && chord.startsWith("W-")) {
                                chord = chord.substring(2, chord.indexOf(":"));
                            }
//                            arc.setChordName(chord); //TODO refactor
                        }
                        if (token.startsWith("a=")) {
                            acScore = Float.parseFloat(token.substring(2));
                            arc.setAcWeigth(acScore);
                        }
                        if (token.startsWith("l=")) {
                            lmScore = Float.parseFloat(token.substring(2));
                            arc.setLmWeigth(lmScore);
                        }
                    }
                    arcs.add(arc);
                }
            }
            reader.close();

        } catch (IOException e) {
            logger.error("Error reading lattice: ");
            logger.error(Helper.getStackTrace(e));
        }
    }


    /**
     * Read a lattice from Song object
     */
    public void initializeFromSong() {

        Arc arc;
        arcs = new ArrayList<Arc>();

        List<Integer> nodesList = new ArrayList<Integer>();
        for (ChordSegment segment : song.getChordSegments()) {
            int startTime = (int)Math.round(segment.getOnset());
            int endTime = (int)Math.round(segment.getOffset());
            if (!nodesList.contains(startTime)) {
                nodesList.add(startTime);
            }
            if (!nodesList.contains(endTime)) {
                nodesList.add(endTime);
            }

            List<ChordSegment> hypotheses = segment.getHypotheses();
            for (ChordSegment hypo : hypotheses) {
                arc = new Arc(segment.getOnset(), segment.getOffset(), hypo.getChordName(), hypo.getLogLikelihood(), 0.0f);
                arcs.add(arc);
            }

        }
        nodes = new int[nodesList.size()];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = nodesList.get(i);
        }
    }


    /**
     * Stores latice data structure in a file
     *
     * @param filePathToStore
     */
    public void storeInFile(String filePathToStore) {
        Collections.sort(arcs);
        Arrays.sort(nodes);

        try {
            FileWriter writer = new FileWriter(filePathToStore);
            String songName = HelperFile.getNameWithoutExtension(filePathToStore);
            //TODO Writing header
            String songFolder = getFile(filePathToStore).getParentFile().getCanonicalPath();
            songFolder = songFolder.replaceAll("\\\\", "/");

            String header = "VERSION=1.0\n" +
                    "UTTERANCE=" + songName + "/secPass" + CHROMA_EXT + "\n" +
                    "lmname=work\\temp\\net\n" +
                    "lmscale=1.00   wdpenalty=-11.00\n" +
                    "acscale=1.00  \n" +
                    "vocab=work/temp/dict\n" +
                    "NODES=" + nodes.length + " LINKS=" + arcs.size() + "\n";
            writer.write(header);


            //Writing nodes
            for (int i = 0; i < nodes.length; i++) {
                writer.write("I=" + i + "\tt=" + (nodes[i]) + "\n");
            }
            //Now write arcs
            int startIndex, endIndex;
            int counter = 0;
            for (Arc arc : arcs) {
                startIndex = -1;
                for (int i = 0; i < nodes.length; i++) {
                    float startTime_ = Math.round(arc.getOnset());
                    if (startTime_ == nodes[i]) {
                        startIndex = i;
                        break;
                    }
                }
                endIndex = -1;
                for (int i = 0; i < nodes.length; i++) {
                    float endTime_ = Math.round(arc.getOffset());
                    if (endTime_ == nodes[i]) {
                        endIndex = i;
                        break;
                    }
                }

                if (startIndex < 0 || endIndex < 0) {
                    logger.error("Error: Could not find nodes for ark " + arc);
                } else {
                    writer.write("J=" + counter++ + "\tS=" + startIndex + "\tE=" + endIndex + "\tW=" + arc.getChordName() + "\tv=1" + "\ta=" + arc.getAcWeigth() + "\tl=" + arc.getLmWeigth() + "\n");
                }

            }

            writer.close();
        } catch (IOException e) {
            logger.error(Helper.getStackTrace(e));
        }

    }



    public List<Arc> getArcs(float startTime, float endTime) {
        List<Arc> outList = new ArrayList<Arc>();
        Arrays.sort(nodes);
        for (Arc a : arcs) {
            if ((a.getOnset() > startTime && a.getOnset() < endTime) || (a.getOffset() > startTime && a.getOffset() < endTime)) {
                outList.add(a);
            }
        }
        return outList;
    }


    public static List<Float> getNodes(List<Arc> arcList, float startTime, float endTime) {
        List<Float> outList = new ArrayList<Float>();
        for (Arc a : arcList) {
            if (a.getOnset() > startTime) {
                if (!outList.contains(a.getOnset())) {
                    outList.add((float)a.getOnset());
                }
            }
            if (a.getOffset() < endTime) {
                if (!outList.contains(a.getOffset())) {
                    outList.add((float)a.getOffset());
                }
            }
        }

        return outList;
    }
}

