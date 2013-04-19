/*
 * Copyright (c) 2008-2013 Maksim Khadkevich and Fondazione Bruno Kessler.
 *
 * This file is part of MART.
 * MART is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2, as published
 * by the Free Software Foundation.
 *
 * MART is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with MART; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.mart.crs.analysis.filterbank;


import org.mart.crs.core.AudioReader;
import org.mart.crs.logging.CRSException;
import org.mart.crs.management.xml.XMLManager;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import static org.mart.crs.management.xml.Tags.CHANNEL_TAG;


/**
 * @version 1.0 Feb 2, 2010 10:37:52 AM
 * @author: Maksim Khadkevich
 */
public class FilterBankManager extends XMLManager {

    public static String FILTERBANK_CONFIG_PATH = "cfg/filters/PQQMFconfig.xml";
    public static String FILTERBANK_CONFIG_QMF_PATH = "cfg/filters/QMFconfig.cfg";



    protected AudioReader audioReader;
    protected float frameSizeOfFeaturesInSecs;

    private List<Channel> channelList;

    protected float maxSamplingFreq = 0f;


    protected FilterBankManager() {
    }

    public FilterBankManager(AudioReader audioReader, String configFilePath, float frameSizeOfFeaturesInSecs) {
        super(configFilePath);

        this.audioReader = audioReader;
        this.frameSizeOfFeaturesInSecs = frameSizeOfFeaturesInSecs;


        parseFromXML();
    }

    protected void parseFromXML() {
        NodeList children = rootElement.getElementsByTagName(CHANNEL_TAG);
        Node node;
        Element element;
        for (int i = 0; i < children.getLength(); i++) {
            node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                element = (Element) node;
                try {
                    addChannelToList(element);
                } catch (CRSException e) {
                    e.printStackTrace();
                }
            }
        }
        findMaxSamplingFreq();
    }

    protected void addChannelToList(Element element) throws CRSException {
        if (channelList == null) {
            channelList = new ArrayList<Channel>();
        }
        Channel channel = new Channel(element);
        channelList.add(channel);
    }


    protected void findMaxSamplingFreq() {
        for (Channel channel : channelList) {
            if (channel.samplingFreq > maxSamplingFreq) {
                maxSamplingFreq = channel.samplingFreq;
            }
        }
    }


    public List<Channel> getChannelList() {
        return channelList;
    }
}
