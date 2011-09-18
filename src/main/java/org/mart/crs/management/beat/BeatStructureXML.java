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

import org.mart.crs.management.beat.segment.BeatSegment;
import org.mart.crs.management.beat.segment.BeatSegmentState;
import org.mart.crs.management.xml.Tags;
import org.mart.crs.management.xml.XMLManager;
import org.mart.crs.utils.helper.Helper;
import com.sun.org.apache.xerces.internal.dom.DeferredElementImpl;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 1.0 7/3/11 6:56 PM
 * @author: Hut
 */
public class BeatStructureXML extends BeatStructure{

    public BeatStructureXML(String sourceFilePath) {
        super(sourceFilePath);
    }

    public void parseFromSource() {
        XMLManager manager = new XMLManager(sourceFilePath);

        NodeList children = manager.getRootElement().getElementsByTagName(Tags.SEGMENT_TAG);
        Node segmentNode;
        BeatSegment lastBeatSegment = null;
        for (int i = 0; i < children.getLength(); i++) {
            segmentNode = children.item(i);

            NamedNodeMap segmentChildrenNodes = segmentNode.getAttributes();
            float time = Helper.parseFloat(segmentChildrenNodes.getNamedItem(Tags.TIME_TAG).getNodeValue());

            Node beattypeNode = ((DeferredElementImpl) segmentNode).getElementsByTagName(Tags.BEATTYPE_TAG).item(0);
            if (beattypeNode == null) {
                continue;
            }
            NamedNodeMap beattypeChildrenNodes = beattypeNode.getAttributes();
            int beat = Helper.parseInt(beattypeChildrenNodes.getNamedItem(Tags.BEAT_TAG).getNodeValue());
            int measure = Helper.parseInt(beattypeChildrenNodes.getNamedItem(Tags.MEASURE_TAG).getNodeValue());
            int tatum = Helper.parseInt(beattypeChildrenNodes.getNamedItem(Tags.TATUM_TAG).getNodeValue());

            if (lastBeatSegment != null) {
                lastBeatSegment.setNextBeatTimeInstant(time);
                beatSegments.add(lastBeatSegment);
            }
            lastBeatSegment = new BeatSegment(time, beat, measure, tatum);

            //Now parse beatSegmentStates
            NodeList beatSegmentStateNodeList = ((DeferredElementImpl) segmentNode).getElementsByTagName(Tags.BEAT_SEGMENT_STATE_TAG);
            for (int state = 0; state < beatSegmentStateNodeList.getLength(); state++) {
                Node beatSegmentStateNode = beatSegmentStateNodeList.item(state);
                NamedNodeMap beatSegmentStateNodeAttributes = beatSegmentStateNode.getAttributes();

                int stateId = Helper.parseInt(beatSegmentStateNodeAttributes.getNamedItem("id").getNodeValue());
                float stateStartTime = Helper.parseFloat(beatSegmentStateNodeAttributes.getNamedItem("beginTime").getNodeValue());
                float stateEndTime = Helper.parseFloat(beatSegmentStateNodeAttributes.getNamedItem("offset").getNodeValue());
                lastBeatSegment.addBeatSegmentState(new BeatSegmentState(stateStartTime, stateEndTime, stateId));
            }
        }
        if (lastBeatSegment != null) {
            lastBeatSegment.setNextBeatTimeInstant(lastBeatSegment.getTimeInstant());
            beatSegments.add(lastBeatSegment);
        }

        formMeasureStructure();
    }


}
