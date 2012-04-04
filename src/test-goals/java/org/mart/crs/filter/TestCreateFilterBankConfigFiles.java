package org.mart.crs.filter;

import junit.framework.TestCase;
import org.mart.crs.config.ConfigSettings;
import org.mart.crs.management.xml.Tags;
import org.mart.crs.management.xml.XMLManager;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;

import static org.mart.crs.management.xml.Tags.*;

/**
 * @version 1.0 30-Apr-2010 14:59:02
 * @author: Hut
 */
public class TestCreateFilterBankConfigFiles extends TestCase {

    public static final String dirPath = ConfigSettings.CONFIG_FILTERS_ROOT_FILE_PATH + "PQMF/";


    public void testCreateFiltersForPQMFConfig() {
        List<String[]> dataMap = HelperFile.readTokensFromFileStrings("cfg/filters/PQMFConfig.cfg", 2);

        //We need a Document
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = dbfac.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document doc = docBuilder.newDocument();


        //create the root element and add it to the document
        Element root = doc.createElement("PQMFFilterBank");
        doc.appendChild(root);

        for (String[] data : dataMap) {
            exportXMLPQMFFilters(data[1], data[0], dirPath);
            int channelNumber = Integer.valueOf(data[1]);
            float samplingRate = Float.valueOf(data[0]);

            Element channel = doc.createElement(Tags.CHANNEL_TAG);
            root.appendChild(channel);
            XMLManager.addDOMElement(doc, channel, Tags.CLASS_TAG, "PQMF");
            XMLManager.addDOMElement(doc, channel, Tags.SAMPLING_FREQ_TAG, data[0]);
            XMLManager.addDOMElement(doc, channel, Tags.CHANNEL_NUMBER_TAG, data[1]);
            XMLManager.addDOMElement(doc, channel, Tags.START_FREQ_TAG, String.valueOf((samplingRate / 2) / 32 * channelNumber));
            XMLManager.addDOMElement(doc, channel, Tags.END_FREQ_TAG, String.valueOf((samplingRate / 2) / 32 * (channelNumber + 1)));

            Element filter_element = doc.createElement(FILTER_TAG);
            channel.appendChild(filter_element);
            XMLManager.addDOMElement(doc, filter_element, FILTER_FILE_PATH_TAG, String.format("PQMF/%s_%s.xml", data[0], channelNumber));
        }
        XMLManager.writeXMLToFile(doc, ConfigSettings.CONFIG_FILTERS_ROOT_FILE_PATH + "PQMF.xml");
    }


    private static void exportXMLPQMFFilters(String channel, String samplingFreq, String dirPath) {
        try {

            //We need a Document
            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            Document doc = docBuilder.newDocument();


            //create the root element and add it to the document
            Element root = doc.createElement("Filter");
            doc.appendChild(root);

            XMLManager.addDOMElement(doc, root, Tags.CLASS_TAG, "PQMF");
            XMLManager.addDOMElement(doc, root, Tags.CHANNEL_NUMBER_TAG, channel);

            XMLManager.writeXMLToFile(doc, String.format("%s%s_%s.xml", dirPath, samplingFreq, channel));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void testCreateXMLConfigForEllipticSemitoneFB() {

        List<String[]> dataMap = HelperFile.readTokensFromFileStrings("cfg/filters/ellipticConfig.cfg", 2);

        try {
            //We need a Document
            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            Document doc = docBuilder.newDocument();


            //create the root element and add it to the document
            Element root = doc.createElement("EllipticFilterBank");
            doc.appendChild(root);

            for (String[] filter : dataMap) {
                Element channel = doc.createElement(Tags.CHANNEL_TAG);
                root.appendChild(channel);

                XMLManager.addDOMElement(doc, channel, SAMPLING_FREQ_TAG, filter[0]);
                XMLManager.addDOMElement(doc, channel, CHANNEL_NUMBER_TAG, filter[1]);
                XMLManager.addDOMElement(doc, channel, START_FREQ_TAG, String.valueOf(Helper.getFreqForMIDINote(Integer.valueOf(filter[1]) - 0.5f)));
                XMLManager.addDOMElement(doc, channel, END_FREQ_TAG, String.valueOf(Helper.getFreqForMIDINote(Integer.valueOf(filter[1]) + 0.5f)));

                Element filter_element = doc.createElement(FILTER_TAG);
                channel.appendChild(filter_element);
                XMLManager.addDOMElement(doc, filter_element, FILTER_FILE_PATH_TAG, String.format("ellipticSemitone/%s.xml", filter[1]));
            }

            XMLManager.writeXMLToFile(doc, ConfigSettings.CONFIG_FILTERS_ROOT_FILE_PATH + "ellipticSemitone.xml");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
