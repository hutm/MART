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

package org.mart.crs.management.xml;

import org.apache.log4j.Logger;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class XMLManager {

    protected static Logger logger = CRSLogger.getLogger(XMLManager.class);


    protected String xmlFilePath;
    protected Element rootElement;


    public XMLManager(String xmlFilePath) {
        this.xmlFilePath = xmlFilePath;
        File docFile = HelperFile.getFile(xmlFilePath);

        Document doc = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(docFile);
        } catch (java.io.IOException e) {
            logger.error("Can't find file " + xmlFilePath);
            logger.error(Helper.getStackTrace(e));
        } catch (Exception e) {
            logger.error("Problem parsing the file " + xmlFilePath);
            logger.error(Helper.getStackTrace(e));
        }

        rootElement = doc.getDocumentElement();
    }

    public XMLManager(Element rootElement) {
        this.rootElement = rootElement;
    }


    protected XMLManager(){
    }

    public static String getStringData(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        Element line = (Element) nodeList.item(0);

        try {
            Node child = line.getFirstChild();
            if (child instanceof CharacterData) {
                CharacterData cd = (CharacterData) child;
                return cd.getData().trim();
            }
        } catch (Exception e) {
            logger.warn(String.format("Problem while reading tag %s from XML file", tagName));
        }
        return "";
    }

    public static void addDOMElement(Document doc, Element rootElement, String tag, String value) {
        Element element = doc.createElement(tag);
        Text text = doc.createTextNode(value);
        element.appendChild(text);
        rootElement.appendChild(element);
    }


    public static void writeXMLToFile(Document document, String filePath) {
        try {
            FileWriter writer = new FileWriter(filePath);

            //set up a transformer
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");

            //create string from xml tree

            StreamResult result = new StreamResult(writer);
            DOMSource source = new DOMSource(document);
            trans.transform(source, result);
            writer.close();

            //print xml
            logger.debug("XML data exported to file " + filePath);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }


    public Element getRootElement() {
        return rootElement;
    }
}
