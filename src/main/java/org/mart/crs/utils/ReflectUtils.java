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

package org.mart.crs.utils;

/**
 * @version 1.0 1/27/11 4:05 PM
 * @author: Hut
 */

import org.mart.crs.exec.scenario.BatchParameter;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.helper.Helper;
import org.apache.log4j.Logger;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

public class ReflectUtils {

    protected static Logger logger = CRSLogger.getLogger(ReflectUtils.class);


    public static Object getVariableValue(Object classOrInstance, String fieldName) {
        Field field;
        Class classType = null;
        String className;

        try {
            field = getFieldFromClassOrInstance(classOrInstance, fieldName);

            classType = field.getType();
            className = classType.getName();

            if (classType.toString().equals("int"))
                return field.getInt(classOrInstance);
            else if (className.equals("short"))
                return field.getShort(classOrInstance);
            else if (className.equals("long"))
                return field.getLong(classOrInstance);
            else if (className.equals("float"))
                return field.getFloat(classOrInstance);
            else if (className.equals("double"))
                return field.getDouble(classOrInstance);
            else if (className.equals("boolean"))
                return field.getBoolean(classOrInstance);

            return field.get(classOrInstance);
        } catch (NoSuchFieldException e) {
            logger.error(Helper.getStackTrace(e));
        } catch (IllegalAccessException e) {
            logger.error(Helper.getStackTrace(e));
        }
        throw new IllegalArgumentException(String.format("Could not get value for variable %s from class %s", fieldName, classType.toString()));

    }

    public static String getVariableValueInStringFormat(Object classOrInstance, String fieldName) {
        StringBuilder stringBuilder = new StringBuilder();
        Object inArrayObject = getVariableValue(classOrInstance, fieldName);
        Field field = null;
        try {
            field = getFieldFromClassOrInstance(classOrInstance, fieldName);
        } catch (NoSuchFieldException e) {
            logger.error(Helper.getStackTrace(e));
        }
        Class classType = field.getType();
        String className = classType.getName();
        if (className.contains("[")) {
            for (int i = 0; i < Array.getLength(inArrayObject); i++) {
                stringBuilder.append(Array.get(inArrayObject, i)).append(" ");
            }
        } else{
            return String.valueOf(inArrayObject);
        }
        return stringBuilder.toString();
    }


    public static void setVariableValue(Object classOrInstance, String fieldName, String unparsedValue) {
        Field field;
        Class classType = null;
        String className;
        try {

            field = getFieldFromClassOrInstance(classOrInstance, fieldName);
            classType = field.getType();
            className = classType.getName();
            Object array = null;
            if (className.contains("[")) {
                if (className.equals("[I"))
                    array = Helper.getStringValuesAsInts(unparsedValue);
                else if (className.equals("[F"))
                    array = Helper.getStringValuesAsFloats(unparsedValue);
                else if (className.equals("[D"))
                    array = Helper.getStringValuesAsDoubles(unparsedValue);
                else if (className.equals("[Z"))
                    array = Helper.getStringValuesAsBooleans(unparsedValue);
                else if (className.equals("[S"))
                    array = Helper.getStringValuesAsShorts(unparsedValue);
                else if (className.equals("[B"))
                    array = Helper.getStringValuesAsBytes(unparsedValue);
                else if (className.equals("[J"))
                    array = Helper.getStringValuesAsLongs(unparsedValue);
                else if (className.equals("[Ljava.lang.String;"))
                    array = Helper.getStringValuesAsStrings(unparsedValue);

                field.set(classOrInstance, array);
            } else if (className.equals("int"))
                field.setInt(classOrInstance, Helper.parseInt(unparsedValue));
            else if (className.equals("short"))
                field.setShort(classOrInstance, Helper.parseShort(unparsedValue));
            else if (className.equals("long"))
                field.setLong(classOrInstance, Helper.parseLong(unparsedValue));
            else if (className.equals("float"))
                field.setFloat(classOrInstance, Helper.parseFloat(unparsedValue));
            else if (className.equals("double"))
                field.setDouble(classOrInstance, Helper.parseDouble(unparsedValue));
            else if (className.equals("boolean"))
                field.setBoolean(classOrInstance, Helper.parseBoolean(unparsedValue));
            else if (className.equals("byte"))
                field.set(classOrInstance, Helper.parseByte(unparsedValue));
            else if (className.equals("java.lang.String"))
                field.set(classOrInstance, unparsedValue.trim());

        } catch (NoSuchFieldException e) {
            logger.error(Helper.getStackTrace(e));
        } catch (IllegalAccessException e) {
            logger.error(Helper.getStackTrace(e));
        }
    }


    public static List<BatchParameter> getSettingsVariables(Object classOrInstance, String prefix) {
        List<BatchParameter> outList = new ArrayList<BatchParameter>();
        Class clazz = getClassFromClassOrInstance(classOrInstance);
        for (Field f : clazz.getDeclaredFields()) {
            f.setAccessible(true);
            if (f.getName().startsWith(prefix)) {
                outList.add(new BatchParameter(classOrInstance, f, prefix));
            }
        }
        return outList;
    }

    public static List<String> getDeclaredFields(Object classOrInstance) {
        List<String> outList = new ArrayList<String>();
        Class clazz = getClassFromClassOrInstance(classOrInstance);
        for (Field f : clazz.getDeclaredFields()) {
            f.setAccessible(true);
            if (!f.getName().startsWith("_")) {
                outList.add(f.getName());
            }
        }
        return outList;
    }


    public static void fillInVariables(Object classOrInstance, Properties properties) {
        Class clazz = getClassFromClassOrInstance(classOrInstance);
        Field[] fieldArray = clazz.getDeclaredFields();
        Map<String, Field> fieldNames = new HashMap<String, Field>();
        for (Field field : fieldArray) {
            fieldNames.put(field.getName(), field);
        }

        Enumeration propertyNames = properties.propertyNames();
        String key;
        while (propertyNames.hasMoreElements()) {
            key = (String) propertyNames.nextElement();
            if (fieldNames.containsKey(key)) {
                ReflectUtils.setVariableValue(classOrInstance, key, properties.getProperty(key));
            }
        }
    }


    public static Field getFieldFromClassOrInstance(Object classOrInstance, String fieldName) throws NoSuchFieldException {
        if (classOrInstance instanceof Class) {
            return ((Class) classOrInstance).getField(fieldName);
        } else {
            return classOrInstance.getClass().getField(fieldName);
        }
    }


    public static Class getClassFromClassOrInstance(Object classOrInstance) {
        Class clazz;
        if (classOrInstance instanceof Class) {
            clazz = (Class) classOrInstance;
        } else {
            clazz = classOrInstance.getClass();
        }
        return clazz;
    }


}


