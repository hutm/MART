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

package org.mart.crs.exec.scenario;

import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.ReflectUtils;
import org.mart.crs.utils.helper.Helper;
import org.apache.log4j.Logger;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * @version 1.0 1/27/11 7:26 PM
 * @author: Hut
 */
public class BatchParameter {

    protected static Logger logger = CRSLogger.getLogger(BatchParameter.class);


    /**
     * Class that contains this field
     */
    protected Object classOrObject;

    /**
     * Field that contain array of values for batch processing
     */
    protected Field field;

    /**
     * Array of values for batch processing
     */
    protected Object array;

    /**
     * Batch prefix
     */
    protected String prefix;

    /**
     * Short name for the field
     */
    protected String shortName;


    public BatchParameter(Object classOrObject, Field field, String prefix) {
        this.classOrObject = classOrObject;
        this.field = field;
        this.prefix = prefix;
        this.shortName = field.getName().substring(prefix.length());
        array = ReflectUtils.getVariableValue(classOrObject, field.getName());
    }

    public void setParameterWithIndex(int index){
        setParameterWithIndex(index, classOrObject);
    }

    public void setParameterWithIndex(int index, Object targetClassOrInstance){
        try {
            ReflectUtils.getFieldFromClassOrInstance(targetClassOrInstance, shortName).set(targetClassOrInstance, Array.get(array, index));
        } catch (IllegalAccessException e) {
            logger.error(Helper.getStackTrace(e));
        } catch (NoSuchFieldException e) {
            logger.error(Helper.getStackTrace(e));
        }
    }

    public Field getField() {
        return field;
    }

    public Object getArray() {
        return array;
    }

    public String getPrefix() {
        return prefix;
    }


    /**
     * String representation of the batch parameter set to a  valueIndex value
     * @param valueIndex
     * @return
     */
    public String getStringWithValue(int valueIndex){
        return  String.format("%s_%s", shortName, String.valueOf(Array.get(array, valueIndex)));
    }



}
