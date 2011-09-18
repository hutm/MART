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

package org.mart.crs.analysis.filter;

import org.mart.crs.config.ConfigSettings;
import org.mart.crs.logging.CRSException;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.xml.XMLManager;
import org.mart.crs.utils.helper.Helper;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import java.lang.reflect.Constructor;

import static org.mart.crs.management.xml.Tags.CLASS_TAG;
import static org.mart.crs.management.xml.Tags.FILTER_FILE_PATH_TAG;


/**
 * @version 1.0 Nov 25, 2009 11:14:47 AM
 * @author: Maksim Khadkevich
 */
public class FilterManager extends XMLManager {

    protected static Logger logger = CRSLogger.getLogger(FilterManager.class);

    private FilterManager(String xmlFilePath) {
        super(xmlFilePath);
        logger.info("Instantiating filter from configFilePath: " + xmlFilePath);
    }

    public FilterManager(Element rootElement) {
        super(rootElement);
    }

    private Filter getFilter() {
        Filter filter = null;
        String filterFilePath = getStringData(rootElement, FILTER_FILE_PATH_TAG);
        if(!filterFilePath.equals("")){
            filter = FilterManager.getFilter(ConfigSettings.CONFIG_FILTERS_ROOT_FILE_PATH + filterFilePath);
            return filter;
        }
        String className = getStringData(rootElement, CLASS_TAG);
        try {
            Class filterClass = Class.forName(className);
            if (Filter.class.isAssignableFrom(filterClass)) {
                Constructor constructor = filterClass.getConstructor(new Class[]{Element.class});
                filter = (Filter) constructor.newInstance(rootElement);
            } else{
                throw new CRSException("Filter cannot be instantiated");
            }
        } catch (Exception e) {
            logger.error(Helper.getStackTrace(e));
        }
        return filter;
    }

    public static Filter getFilter(String xmlFilePath) {
        FilterManager filterManager = new FilterManager(xmlFilePath);
        return filterManager.getFilter();
    }

    public static Filter getFilter(Element rootElement) {
        FilterManager filterManager = new FilterManager(rootElement);
        return filterManager.getFilter();
    }


}
