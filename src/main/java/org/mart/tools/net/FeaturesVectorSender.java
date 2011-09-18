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

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @version 1.0 07-Jul-2010 22:59:40
 * @author: Hut
 */
public class FeaturesVectorSender {


    public static void main(String[] args) throws Exception {
        List<float[][]> vectors = new ArrayList<float[][]>();
        vectors.add(new float[][]{{2, 3, 4}, {5, 3, 6}});
        vectors.add(new float[][]{{1, 2, 3}, {4, 5, 7}});

        FeatureVector featureVector = new FeatureVector(vectors, 1001);
        sendFeatureVectors(NetHelper.urlFeaturesSend, featureVector);

        vectors.remove(0);
        featureVector = new FeatureVector(vectors, 1002);
        sendFeatureVectors(NetHelper.urlFeaturesSend, featureVector);


    }

    public static int sendFeatureVectors(FeatureVector featureVector) throws Exception {
        return sendFeatureVectors(NetHelper.urlFeaturesSend, featureVector);
    }


    public static int sendFeatureVectors(String urlString, FeatureVector featureVector) throws Exception {
        int hash = 0;
        URLConnection urlConnection = NetHelper.getConnection(urlString);

//----------------------------------------------------------------------
// send data to servlet
//----------------------------------------------------------------------
        OutputStream os = urlConnection.getOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(os));
        oos.writeObject(featureVector);
        oos.flush();
        oos.close();


//----------------------------------------------------------------------
// read any response data, and store in a ByteArrayOutputStream
//----------------------------------------------------------------------
        InputStream is;
        ObjectInputStream objectInputStream;
        if ((is = urlConnection.getInputStream()) != null) {
            objectInputStream = new ObjectInputStream(new GZIPInputStream(is));
            hash = (Integer) objectInputStream.readObject();
            objectInputStream.close();
        }


        return hash;
    }

}
