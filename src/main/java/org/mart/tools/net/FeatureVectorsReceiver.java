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

import org.mart.crs.management.features.FeatureVector;

import java.io.*;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @version 1.0 07-Jul-2010 23:17:09
 * @author: Hut
 */
public class FeatureVectorsReceiver {


    public static void main(String[] args) {
        Map<Integer, FeatureVector> featureVectors = receiveFeatureVectors(NetHelper.urlFeaturesReceive);
    }


    public static Map<Integer, FeatureVector> receiveFeatureVectors() {
        return receiveFeatureVectors(NetHelper.urlFeaturesReceive);
    }


    public static Map<Integer, FeatureVector> receiveFeatureVectors(String urlString) {
        try {
            URLConnection urlConnection = NetHelper.getConnection(urlString);

            OutputStream os = urlConnection.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(os));
            oos.writeObject("nothing");
            oos.flush();
            oos.close();




            Map<Integer, FeatureVector> outMap = new HashMap<Integer, FeatureVector>();

            ObjectInputStream ois;
            InputStream is;
            if ((is = urlConnection.getInputStream()) != null) {
                ois = new ObjectInputStream(new GZIPInputStream(is));
                try {
                    List<Integer> hashes = (List<Integer>) ois.readObject();
                    for(Integer hash:hashes){
                        FeatureVector featureVector = (FeatureVector) ois.readObject();
                        outMap.put(hash, featureVector);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    ois.close();
                }
            }

            return outMap;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
