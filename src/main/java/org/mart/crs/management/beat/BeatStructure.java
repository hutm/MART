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

package org.mart.crs.management.beat;

import org.apache.log4j.Logger;
import org.mart.crs.config.Extensions;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.beat.segment.BeatSegment;
import org.mart.crs.management.beat.segment.BeatSegmentState;
import org.mart.crs.management.beat.segment.MeasureSegment;
import org.mart.crs.management.xml.XMLManager;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @version 1.0 07/11/10 22:23
 * @author: Hut
 */
public class BeatStructure implements Comparable<BeatStructure> {


    protected static Logger logger = CRSLogger.getLogger(BeatStructure.class);

    protected String sourceFilePath;

    protected List<BeatSegment> beatSegments;
    protected List<MeasureSegment> measureSegments;
    protected String songName;

    protected BeatStructure(String sourceFilePath) {
        this.sourceFilePath = sourceFilePath;
        this.beatSegments = new ArrayList<BeatSegment>();
        this.songName = HelperFile.getNameWithoutExtension(sourceFilePath);
        parseFromSource();
        formMeasureStructure();
    }


    public BeatStructure(List<BeatSegment> beatSegmentList) {
        this.beatSegments = beatSegmentList;
        formMeasureStructure();
    }

    public BeatStructure(List<BeatSegment> beatSegmentList, String songName) {
        this(beatSegmentList);
        this.songName = songName;
    }

    protected BeatStructure() {
    }


    public static BeatStructure getBeatStructure(String sourceFilePath) {
        String extension = HelperFile.getExtension(sourceFilePath);
        if (extension.equals(Extensions.BEAT_EXT)) {
            return new BeatStructureXML(sourceFilePath);
        }
        if (extension.equals(Extensions.BEAT_TXT_EXT)) {
            return new BeatStructureText(sourceFilePath);
        }
        if (extension.equals(Extensions.BEAT_MAZURKA_EXT)) {
            return new BeatStructureMazurka(sourceFilePath);
        }
        if (extension.equals(Extensions.ONSET_EXT)) {
            return new BeatStructureText(sourceFilePath);
        }
        throw new IllegalArgumentException(String.format("Cannot extract beat structure from file %s with extension %s", sourceFilePath, extension));
    }


    public void parseFromSource() {
        throw new IllegalArgumentException("This method should be overriden");
    }


    protected void formMeasureStructure() {
        this.measureSegments = new ArrayList<MeasureSegment>();

        MeasureSegment lastSegment = null;
        Collections.sort(beatSegments);
        for (int i = 0; i < beatSegments.size(); i++) {
            BeatSegment beat = beatSegments.get(i);
            if (beat.isDownBeat()) {
                if (lastSegment != null) {
                    measureSegments.add(lastSegment);
                }
                lastSegment = new MeasureSegment(beat);
            } else {
                if (lastSegment != null) {
                    lastSegment.addBeatSegment(beat);
                }
            }

            //Now set beat offset time
            if (i < beatSegments.size() - 1) {
                beat.setNextBeatTimeInstant(beatSegments.get(i + 1).getTimeInstant());
            } else {
                beat.setNextBeatTimeInstant(beat.getTimeInstant());
            }
        }
        measureSegments.add(lastSegment);
    }


    public void serializeIntoXML(String XMLFilePath) {

        try {

            //We need a Document
            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            Document doc = docBuilder.newDocument();


            //create the root element and add it to the document
            Element root = doc.createElement("BeatStructure");
            doc.appendChild(root);

            root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            root.setAttribute("format", "20090701A");
            root.setAttribute("date", Helper.getCurrentDate());

            Node mediaElement = doc.createElement("media");
            mediaElement.appendChild(doc.createTextNode(songName));
            root.appendChild(mediaElement);


            Element descriptionElement = doc.createElement("descriptiondefinition");
            descriptionElement.setAttribute("id", "1");
            Element typeElement = doc.createElement("type");
            Element generatorElement = doc.createElement("generator");
            generatorElement.setAttribute("version", "0.1");
            descriptionElement.appendChild(typeElement);
            descriptionElement.appendChild(generatorElement);
            root.appendChild(descriptionElement);

            for (BeatSegment beatSegment : beatSegments) {


                Element segmentElement = doc.createElement("segment");


                segmentElement.setAttribute("length", "0");
                segmentElement.setAttribute("comment", "-");
                segmentElement.setAttribute("sourcetrack", "0");
                segmentElement.setAttribute("time", String.format("%5.4f", beatSegment.getTimeInstant()));


                Element beatElement = doc.createElement("beattype");
                beatElement.setAttribute("id", "1");
                beatElement.setAttribute("pattern", "0");

                if(beatSegment.getBeat() == 1){
                    beatSegment.setMeasure(1);
                }

                beatElement.setAttribute("measure", String.valueOf(beatSegment.getMeasure()));
                beatElement.setAttribute("beat", String.valueOf(beatSegment.getBeat()));
                beatElement.setAttribute("tatum", "1");

                for (BeatSegmentState beatSegmentState : beatSegment.getBeatSegmentStates()) {
                    Element beatStateElement = doc.createElement("beatSegmentState");
                    beatStateElement.setAttribute("id", String.valueOf(beatSegmentState.getStateNumber()));
                    beatStateElement.setAttribute("beginTime", String.valueOf(beatSegmentState.getStartTime()));
                    beatStateElement.setAttribute("offset", String.valueOf(beatSegmentState.getEndTime()));

                    beatElement.appendChild(beatStateElement);
                }

                segmentElement.appendChild(beatElement);

                root.appendChild(segmentElement);
            }

            XMLManager.writeXMLToFile(doc, XMLFilePath);

        } catch (Exception e) {
            logger.error(Helper.getStackTrace(e));
        }

    }

    public void serializeIntoTXT(String outFilePath, boolean isOnlyDownbeats) {
        List<String> dataToSave = new ArrayList<String>();
        int counter = 0;
        for (BeatSegment beatSegment : beatSegments) {
            if (!isOnlyDownbeats || beatSegment.getBeat() == 1) {
                dataToSave.add(String.format("%7.4f", beatSegment.getTimeInstant()));
            }
        }
        HelperFile.saveCollectionInFile(dataToSave, outFilePath, false);
    }


    public double[] getBeats() {
        double[] out = new double[beatSegments.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = beatSegments.get(i).getTimeInstant();
        }
        return out;
    }

     public double[] getBeatDurations() {
        double[] out = new double[beatSegments.size() - 1];
        for (int i = 0; i < out.length - 1; i++) {
            out[i] = beatSegments.get(i+1).getTimeInstant() - beatSegments.get(i).getTimeInstant();
        }
        return out;
    }


    public double[] getDownBeats() {
        List<Double> outList = new ArrayList<Double>();
        for (int i = 0; i < beatSegments.size(); i++) {
            if (beatSegments.get(i).isDownBeat()) {
                outList.add(beatSegments.get(i).getTimeInstant());
            }
        }
        double[] out = new double[outList.size()];

        int counter = 0;
        for (double downbeat : outList) {
            out[counter++] = downbeat;
        }
        return out;
    }


    public List<BeatSegment> getBeatSegments() {
        return beatSegments;
    }

    public void addBeatSegment(BeatSegment segment) {
        beatSegments.add(segment);
    }

    public List<MeasureSegment> getMeasureSegments() {
        return measureSegments;
    }

    public List<BeatSegment> getDownBeatPositions(boolean isDownbeat) {
        List<BeatSegment> outList = new ArrayList<BeatSegment>();
        for (BeatSegment segment : beatSegments) {
            if (isDownbeat && segment.getBeat() == 1) {
                outList.add(segment);
            }
            if (!isDownbeat && segment.getBeat() != 1) {
                outList.add(segment);
            }
        }

        return outList;
    }


    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongName() {
        return songName;
    }


    public static void transfromLabelsToMIREXFormat(String resultsDir, String outDir, boolean isGroundTruth, boolean isOnlyDownBeats) {
        File[] files = HelperFile.getFile(resultsDir).listFiles(new ExtensionFileFilter(Extensions.BEAT_EXTENSIONS));
        HelperFile.getFile(outDir).mkdirs();
        String extension = Extensions.TXT_EXT;
        if (!isGroundTruth) {
            extension = ".out.txt";
        }
        for (File file : files) {
            BeatStructure beatStructure = BeatStructure.getBeatStructure(file.getPath());
            String outPath = String.format("%s/%s%s", outDir, beatStructure.getSongName().replaceAll("'", ""), extension);
            beatStructure.serializeIntoTXT(outPath, isOnlyDownBeats);
        }
    }


    public void addTrailingBeats(double songDuration){
        if (beatSegments.get(0).getTimeInstant() > 0.01) {
            this.beatSegments.add(new BeatSegment(0, 0));
        }
        this.beatSegments.add(new BeatSegment(songDuration, 0));
        formMeasureStructure();
    }


    public int compareTo(BeatStructure o) {
        return songName.compareTo(o.getSongName());
    }


}
