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

package org.mart.crs.management.label;

import org.apache.log4j.Logger;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.management.label.chord.ChordStructure;
import org.mart.crs.management.xml.XMLManager;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


/**
 * User: Hut
 * Date: 10.05.2008
 * Time: 10:26:48
 * Performs all parsing operations
 */
public class LabelsParser {

    protected static Logger logger = CRSLogger.getLogger(LabelsParser.class);


    public static final String START_SENTENCE = "<s>";
    public static final String END_SENTENCE = "</s>";


    public static void transformXMLLabelsFormat(String filePathList, String outFolder) {
        List<String> fileList = HelperFile.readLinesFromTextFile(filePathList);
        HelperFile.createDir(outFolder);
        for (String aFile : fileList) {
            XMLManager manager = new XMLManager(aFile);
            Element rootElement = manager.getRootElement();
            NodeList list = rootElement.getElementsByTagName("segment");
            List<ChordSegment> segmentList = new ArrayList<ChordSegment>();
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                NamedNodeMap beattypeChildrenNodes = node.getAttributes();
                float startTime = Helper.parseFloat(beattypeChildrenNodes.getNamedItem("time").getNodeValue());
                float duration = Helper.parseFloat(beattypeChildrenNodes.getNamedItem("length").getNodeValue());
                Node chord = ((Element) node).getElementsByTagName("chordtype").item(0);
                NamedNodeMap chordChildrenNodes = chord.getAttributes();
                String chordName = chordChildrenNodes.getNamedItem("value").getNodeValue();
                segmentList.add(new ChordSegment(startTime, startTime + duration, chordName));

            }
            ChordStructure chordStructure = new ChordStructure(segmentList, HelperFile.getNameWithoutExtension(aFile));
            chordStructure.saveSegmentsInFile(outFolder);
        }
    }


    public static void main(String[] args) {
        LabelsParser.transformXMLLabelsFormat("/media/Data/dev/Matlab/beat_data/6.2.4_Chord/withbeats/list.txt", "/media/Data/dev/Matlab/beat_data/6.2.4_Chord/withbeats1");
    }










    public static final String NOT_A_CHORD = "N";
    public static final String UNKNOWN_CHORD = "UNK";

    protected static Map<String, String> mapping = null;


    static{
        mapping = HelperFile.readMapFromReader(new InputStreamReader(LabelsParser.class.getResourceAsStream("/mapping.txt")));
    }


    /**
     * Returns List of classes of type ChordSegment
     *
     * @param labelFile                   labelFile
     * @param isChordNameConversionNeeded boolean flag isChordNameConversionNeeded
     * @return ChordSegment List
     */
    public static List<ChordSegment> getSegments(String labelFile, boolean isChordNameConversionNeeded) {
        File file;


        List<ChordSegment> data = new ArrayList<ChordSegment>();

        try {
            if (labelFile == null) {
                return null;
            }
            file = HelperFile.getFile(labelFile);

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line, chord, chordName, chordRoot, chordType;
            float startTime, endTime;

            while ((line = reader.readLine()) != null && line.length() > 1) {
                StringTokenizer tokenizer = new StringTokenizer(line);
                startTime = Float.parseFloat(tokenizer.nextToken());
                endTime = Float.parseFloat(tokenizer.nextToken());
                chordName = tokenizer.nextToken();
                chord = chordName;
                //Do chord convertion only in case it was not done in previous steps
                if (line.indexOf(":") > 0) {

                }
                if (isChordNameConversionNeeded) {
                    chordRoot = mapRoot(chordName);
                    chordType = readChordType(chordName);
                    if (chordRoot.equals(NOT_A_CHORD) || chordType.equals(NOT_A_CHORD)) {
                        chord = NOT_A_CHORD;
                    } else if (chordType.equals(UNKNOWN_CHORD)) {
                        chord = UNKNOWN_CHORD;
                    } else {
                        chord = chordRoot + chordType;
                    }
                }

                //Now store data
                // If endtime <0 do not store this chord
                if (endTime > 0) {
                    if (startTime < 0) {
                        startTime = 0;
                    }
                    data.add(new ChordSegment(startTime, endTime, chord));
                }

            }
            reader.close();
        } catch (FileNotFoundException e) {
            logger.info("Could not find label file " + labelFile);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected Error occured ", e);
            return null;
        }
        return data;
    }

    /**
     * find allowable rootName (from a list) for a given chord name
     *
     * @param chordName chordName
     * @return rootName
     */
    private static String mapRoot(String chordName) {
        if (!chordName.equals(NOT_A_CHORD)) {
            String root;
            if (chordName.indexOf(":") > 0) {
                root = chordName.substring(0, chordName.indexOf(":"));
            } else if (chordName.indexOf("/") > 0) {
                root = chordName.substring(0, chordName.indexOf("/"));
            } else if (chordName.indexOf("m") > 0) {
                root = chordName.substring(0, chordName.indexOf("m"));
            } else root = chordName;
            if (!mapping.containsKey(root)) {
                if (!root.startsWith("<")) {
                    logger.warn("Could not find mapping for root " + root);
                }
                return NOT_A_CHORD;
            }
            return mapping.get(root);
        }
        return NOT_A_CHORD;
    }

    /**
     * Reads chord type from chord name
     *
     * @param chordName chordName
     * @return chordType
     */
    private static String readChordType(String chordName) {

        if (chordName.contains(":maj") || chordName.contains(":7") || chordName.contains(":9")) {
            return ":maj";
        }

        if (chordName.contains(":min")) {
            return ":min";
        }
        if (chordName.contains(":dim") || chordName.contains(":hdim")) {
            return UNKNOWN_CHORD;
        }
        if (chordName.contains(":aug")) {
            return UNKNOWN_CHORD;
        }
        if (chordName.contains(":sus")) {
            return UNKNOWN_CHORD;
        }
        if (chordName.contains(":(")) {
            return UNKNOWN_CHORD;
        }
        if (chordName.equals(NOT_A_CHORD)) {
            return NOT_A_CHORD;
        }

        if (chordName.equals(mapRoot(chordName))) {
            return ":maj";
        }
        if (!chordName.contains(":") && !chordName.equals(NOT_A_CHORD)) {
            return ":maj";
        }
        logger.warn("Could not find mapping for chordType " + chordName);
        return NOT_A_CHORD;
    }


}
