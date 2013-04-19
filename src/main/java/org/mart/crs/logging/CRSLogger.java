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

package org.mart.crs.logging;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.net.URL;

/**
 * User: Hut
 * Date: 15.06.2008
 * Time: 22:38:16
 * This class exists primarily to assure that the log4j system has been
 * initialized
 */

public class CRSLogger {

    /**
     * Getter of logger for class c
     *
     * @param c class to get logger for
     * @return logger
     */
    public static Logger getLogger(Class c) {
        return Logger.getLogger(c);
    }

    //public static void configure(){

    static {
        try {
            URL url = CRSLogger.class.getResource("/crslog4j.properties");

            if (url != null) {
                PropertyConfigurator.configure(url);
                getLogger(CRSLogger.class).info("CRSLogger configuration successfully loaded");
            } else {
                System.err.println("WARNING: Unable to load crslog4j.properties");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
