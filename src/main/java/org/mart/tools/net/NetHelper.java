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

import org.mart.crs.config.Settings;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * @version 1.0 08-Jul-2010 18:26:48
 * @author: Hut
 */
public class NetHelper {

    public static String urlRoot = Settings.urlRoot;
    public static String urlFeaturesSend = urlRoot + "/featuresSend";
    public static String urlFeaturesReceive = urlRoot + "/featuresReceive";

    public static String urlLabelsSend = urlRoot + "/labelsSend";
    public static String urlLabelsReceive = urlRoot + "/labelsReceive";
    

     public static URLConnection getConnection(String urlString) throws Exception {
        URL url = new URL(urlString);
        URLConnection urlConnection = url.openConnection();
        if (urlConnection instanceof HttpURLConnection) {
            ((HttpURLConnection) urlConnection).setRequestMethod("POST");
        } else {
            throw new Exception("this connection is NOT an HttpUrlConnection connection");
        }

        //------------------------------------------------------------------------
        // configure the connection to allow for the operations necessary.
        // Specifically:
        //
        // 1. Turn off all caching, so that each new request/response is made
        // from a fresh connection with the servlet.
        //
        // 2. Indicated that this client will attempt to SEND request
        // data to the servlet.
        //
        // 3. Indicated that this client will attempt to READ any response
        // data sent back from the servlet.
        //
        // 4. Set the "mimetype" to indicate that byte data will be sent.
        //
        //------------------------------------------------------------------------
        urlConnection.setUseCaches(false);
        urlConnection.setDefaultUseCaches(false);
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type", "application/octet-stream");

        //----------------------------------------------------------------------
        // connect to the servlet
        //----------------------------------------------------------------------
        urlConnection.connect();
        return urlConnection;
    }


}
