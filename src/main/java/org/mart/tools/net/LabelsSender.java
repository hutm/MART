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

package org.mart.tools.net;

import org.mart.crs.management.label.chord.ChordSegment;

import java.io.*;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @version 1.0 08-Jul-2010 15:17:50
 * @author: Hut
 */
public class LabelsSender {

    public static void main(String[] args) throws Exception {

        Map<Integer, List<ChordSegment>> labelsMap = new HashMap<Integer, List<ChordSegment>>();
        labelsMap.put(0, new ArrayList<ChordSegment>());
        labelsMap.put(10, new ArrayList<ChordSegment>());

        sendLabels(NetHelper.urlLabelsSend, labelsMap);

    }

    public static void sendLabels(Map<Integer, List<ChordSegment>> labelsMap) throws Exception {
        sendLabels(NetHelper.urlLabelsSend, labelsMap);
    }


    public static void sendLabels(String urlString, Map<Integer, List<ChordSegment>> labelsMap) throws Exception {
        URLConnection urlConnection = NetHelper.getConnection(urlString);

        OutputStream os = urlConnection.getOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(os));
        oos.writeObject(labelsMap);
        oos.flush();
        oos.close();

        try {
            InputStream is;
            ObjectInputStream objectInputStream;
            if ((is = urlConnection.getInputStream()) != null) {
                objectInputStream = new ObjectInputStream(new GZIPInputStream(is));
                objectInputStream.close();
            }
        } catch (IOException e) {
            //Nothing should be here, but this block is necessary
        }

    }

}
