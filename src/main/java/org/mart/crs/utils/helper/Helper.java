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

package org.mart.crs.utils.helper;

import org.apache.log4j.Logger;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.management.label.chord.Root;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mart.crs.management.config.Configuration.*;

/**
 * User: Hut
 * Date: 18.06.2008
 * Time: 15:48:31
 * Methods of this class are used for different helping purposes
 */
public class Helper {

    protected static Logger logger = CRSLogger.getLogger(Helper.class);

    protected static Random random;

    public static int getClosestLogDistanceIndex(float value, float[] data) {

        float minDistance = (float) Math.abs(Math.log(value) - Math.log(data[0]));
        int index = 0;
        for (int i = 1; i < data.length; i++) {
            float distance = (float) Math.abs(Math.log(value) - Math.log(data[i]));
            if (distance < minDistance) {
                minDistance = distance;
                index = i;
            }
        }
        return index;
    }

    /**
     * calculates distance between frequencies in semitone scale
     *
     * @param freq1 frequency 1
     * @param freq2 frequency 2
     * @return semitone-scale Distance
     */
    public static double getSemitoneDistance(double freq1, double freq2) {
        float value = (float) (12 * Math.log(freq2 / freq1) / Math.log(2));
        return value;
    }

    /**
     * calculates Absolute distance value between frequencies in semitone scale
     *
     * @param freq1 frequency 1
     * @param freq2 frequency 2
     * @return semitone-scale Distance
     */
    public static double getSemitoneDistanceAbs(double freq1, double freq2) {
        return Math.abs(getSemitoneDistance(freq1, freq2));
    }


    public static float getFreqForMIDINote(float midiNote) {
        return getFreqForMIDINote(midiNote, REFERENCE_FREQUENCY);
    }

    public static float getMidiNoteForFreq(float freq) {
        return getMidiNoteForFreq(freq, REFERENCE_FREQUENCY);
    }

    public static float getFreqForMIDINote(float midiNote, float refFreq) {
        return (float) (refFreq * Math.pow(2, (midiNote - REFERENCE_FREQUENCY_MIDI_NOTE) / 12));
    }

    public static float getMidiNoteForFreq(double freq, double refFreq) {
        return (float) (12 * Math.log(freq / refFreq) / Math.log(2) + REFERENCE_FREQUENCY_MIDI_NOTE);
    }

    public static void execCmd(final String command, final BufferedWriter writer) {
        execCmd(command, writer, true);
    }


    public static void execCmd(final String command, final BufferedWriter writer, final boolean isToLogOutput) {
        try {
            Runtime rt = Runtime.getRuntime();
            logger.debug(command);

//            final Process pr = rt.exec(command, new String[]{String.format("PATH=%s:./bin/", System.getenv("PATH"))});
            final Process pr = rt.exec(command);

            Thread shutDownHook = new Thread() {
                public void run() {
                    logger.fatal("Killing process launched by the command:\n" + command);
                    pr.destroy();
                }
            };

            Runtime.getRuntime().addShutdownHook(shutDownHook);

            final BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            BufferedReader input1 = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

            Thread thread1 = new Thread() {
                public void run() {
                    try {
                        String line;
                        while ((line = input.readLine()) != null) {
                            if (writer != null) {
                                writer.write(line + "\n");
                            } else {
                                if (isToLogOutput) {
                                    logger.debug(line);
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread1.start();
            String line;
            while ((line = input1.readLine()) != null) {
                if (isToLogOutput) {
                    logger.error(String.format("!!!From exec!!!: %s", line));
                }
            }


            int exitVal = pr.waitFor();
//            System.out.println("Exited with error code " + exitVal);
            Runtime.getRuntime().removeShutdownHook(shutDownHook);

        } catch (Exception e) {
            logger.error("Exception while executing thread");
            logger.error(getStackTrace(e));
        }
    }

    public static void execCmd(String command) {
        execCmd(command, null);
    }


    public static String getCurrentDate() {
        String DATE_FORMAT = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Calendar c1 = Calendar.getInstance(); // today
        return sdf.format(c1.getTime());
    }


    public static String[] parseStringValues(String inString) {
        StringTokenizer tokenizer = new StringTokenizer(inString);
        String[] out = new String[tokenizer.countTokens()];
        int index = 0;
        while (tokenizer.hasMoreTokens()) {
            out[index] = tokenizer.nextToken();
            index++;
        }
        return out;
    }


    /**
     * duration quantizer for duration factor in FLM
     *
     * @param duration
     * @return
     */
    public static int quantizeDuration(int duration) {
        int[] quants = new int[]{1, 2, 3, 4, 6, 8, 12, 16, 24, 32, 48, 64, 96, 128, 256, 512};
        for (int i = 0; i < quants.length; i++) {
            if (duration >= quants[i] && duration <= quants[i + 1]) {
                if (duration < (quants[i] + quants[i + 1]) / 2.0f) {
                    return quants[i];
                } else {
                    return quants[i + 1];
                }
            }
        }
        return 0;

    }

    public static void replaceText(String inFilePath, String outFilePath, Map<String, String> replaceMap) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(inFilePath));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFilePath));
        String line = "";
        while ((line = reader.readLine()) != null) {
            for (String from : replaceMap.keySet()) {
                line = line.replace(from, replaceMap.get(from));
            }
            writer.write(line + "\n");
        }
        reader.close();
        writer.close();

    }


    public static List<float[]> detectSilenceIntervals(float[] data, float threshold, int minDurationOfSilence, float sampleRate, float duration) {
        int nInAveraging = 5;

        List<float[]> outList = new ArrayList<float[]>();
        float absThreshold = threshold * HelperArrays.calculateMean(data);
        int startIndex = -1;
        boolean started = false;
        for (int i = 0; i < data.length - nInAveraging; i++) {
            if (HelperArrays.calculateMean(data, i, i + nInAveraging) < absThreshold) {
                if (!started) {
                    started = true;
                    startIndex = i;
                }
            } else {
                if (started && (i - startIndex) >= minDurationOfSilence) {
                    outList.add(new float[]{startIndex * sampleRate, (i - 1) * sampleRate});
                }
                started = false;
            }
        }
        if (started) {
            outList.add(new float[]{startIndex * sampleRate, duration});
        }
        return outList;
    }


    /**
     * returns true if 2 sections intersect
     *
     * @param start1 start1
     * @param end1   end1
     * @param start2 start2
     * @param end2   end2
     * @return if they intersect
     */
    public static boolean intersect(int start1, int end1, int start2, int end2) {
        return (end1 > start2) && (end2 > start1);
    }

    public static boolean intersect(int[] interval1, int[] interval2) {
        return intersect(interval1[0], interval1[1], interval2[0], interval2[1]);
    }

    public static int[] getIntersection(int start1, int end1, int start2, int end2) {
        int start = Math.max(start1, start2);
        int end = Math.min(end1, end2);
        return new int[]{start, end};
    }

    public static int[] getIntersection(int[] interval1, int[] interval2) {
        return getIntersection(interval1[0], interval1[1], interval2[0], interval2[1]);
    }


    /**
     * Returns interval(s) of non-intersection area for interval1
     *
     * @param interval
     * @param intervalToRemove
     * @return
     */
    public static List<int[]> getNonIntersection(int[] interval, int[] intervalToRemove) {
        List<int[]> outList = new ArrayList<int[]>();

        int[] intersection = getIntersection(interval, intervalToRemove);

        if (intersection[0] == interval[0]) {
            if (intersection[1] == interval[1]) {
                //There is no intervals for output
            } else {
                outList.add(new int[]{intersection[1] + 1, interval[1]});
            }
            return outList;
        }

        if (intersection[1] == interval[1]) {
            outList.add(new int[]{interval[0], intersection[0] - 1});
        } else {
            //The intersection is inside the interval
            outList.add(new int[]{interval[0], intersection[0] - 1});
            outList.add(new int[]{intersection[1] + 1, interval[1]});
        }
        return outList;
    }


    public static void removeIntersection(List<int[]> intervals, List<int[]> intervalsToRemove) {
        List<int[]> newIntervals;
        boolean isChanged = true;

        while (isChanged) {
            isChanged = false;
            for (int[] intervalToRemove : intervalsToRemove) {
                for (int[] interval : intervals) {
                    if (intersect(intervalToRemove, interval)) {
                        newIntervals = getNonIntersection(interval, getIntersection(interval, intervalToRemove));
                        intervals.remove(interval);
                        for (int[] newInterval : newIntervals) {
                            intervals.add(newInterval);
                        }
                        isChanged = true;
                        break;
                    }
                }
            }
        }
    }

    /**
     * Returns stackTrace as a String
     *
     * @param aThrowable throwable
     * @return stackTrace
     */
    public static String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }


    //parsing java variables from properties methods

    public static boolean parseBoolean(String s) {
        boolean out = false;
        try {
            out = Boolean.parseBoolean(s.trim());
        } catch (Exception e) {
            logger.debug(String.format("Could not parse string '%s' into boolean", s));
        }
        return out;
    }

    public static float parseFloat(String s) {
        float out = Float.MIN_VALUE;
        try {
            out = Float.parseFloat(s.trim());
        } catch (Exception e) {
            logger.debug(String.format("Could not parse string '%s' into float", s));
        }
        return out;
    }

    public static double parseDouble(String s) {
        double out = Double.MIN_VALUE;
        try {
            out = Double.parseDouble(s.trim());
        } catch (Exception e) {
            logger.debug(String.format("Could not parse string '%s' into double", s));
        }
        return out;
    }

    public static long parseLong(String s) {
        long out = Long.MIN_VALUE;
        try {
            out = Long.parseLong(s.trim());
        } catch (Exception e) {
            logger.debug(String.format("Could not parse string '%s' into int", s));
        }
        return out;
    }

    public static int parseInt(String s) {
        int out = Integer.MIN_VALUE;
        try {
            out = Integer.parseInt(s.trim());
        } catch (Exception e) {
            logger.debug(String.format("Could not parse string '%s' into int", s));
        }
        return out;
    }

    public static short parseShort(String s) {
        short out = Short.MIN_VALUE;
        try {
            out = Short.parseShort(s.trim());
        } catch (Exception e) {
            logger.debug(String.format("Could not parse string '%s' into short", s));
        }
        return out;
    }


    public static byte parseByte(String s) {
        byte out = Byte.MIN_VALUE;
        try {
            out = Byte.parseByte(s.trim());
        } catch (Exception e) {
            logger.debug(String.format("Could not parse string '%s' into byte", s));
        }
        return out;
    }

    public static boolean isBoolean(String s) {
        try {
            Boolean.parseBoolean(s.trim());
            return true;
        } catch (Exception e) {
            logger.debug(String.format("Could not parse string '%s' into boolean", s));
        }
        return false;
    }

    public static boolean isFloat(String s) {
        try {
            Float.parseFloat(s.trim());
            return true;
        } catch (Exception e) {
            logger.debug(String.format("Could not parse string '%s' into float", s));
        }
        return false;
    }

    public static boolean isInt(String s) {
        try {
            Integer.parseInt(s.trim());
            return true;
        } catch (Exception e) {
            logger.debug(String.format("Could not parse string '%s' into int", s));
        }
        return false;
    }


    //parsing java variables from properties WITH LOGGING methods

    public static boolean getBoolean(Properties properties, String key) {
        String s = null;
        boolean out = false;
        try {
            s = properties.getProperty(key).trim();
            out = Boolean.parseBoolean(s.trim());
            logger.debug(String.format("%s=%s", key, String.valueOf(out)));
        } catch (Exception e) {
            logger.warn(String.format("Property '%s' was not initialized correctly", key));
            logger.warn(String.format("Could not parse string '%s' into boolean", s));
        }
        return out;
    }

    public static float getFloat(Properties properties, String key) {
        String s = null;
        float out = 0f;
        try {
            s = properties.getProperty(key);
            out = Float.parseFloat(s.trim());
            logger.debug(String.format("%s=%5.2f", key, out));
        } catch (Exception e) {
            logger.warn(String.format("Property '%s' was not initialized correctly", key));
            logger.warn(String.format("Could not parse string '%s' into float", s));
        }
        return out;
    }

    public static int getInt(Properties properties, String key) {
        String s = null;
        int out = Integer.MIN_VALUE;
        try {
            s = properties.getProperty(key);
            out = Integer.parseInt(s.trim());
            logger.debug(String.format("%s=%d", key, out));
        } catch (Exception e) {
            logger.warn(String.format("Property '%s' was not initialized correctly", key));
            logger.warn(String.format("Could not parse string '%s' into int", s));
        }
        return out;
    }

    public static String getString(Properties properties, String key) {
        String s = properties.getProperty(key);
        if (s != null) {
            logger.debug(String.format("%s=%s", key, s));
            return s.trim();
        } else {
            logger.warn(String.format("Property '%s' was not initialized correctly", key));
            return "";
        }
    }


    public static float[] getStringValuesAsFloats(Properties properties, String key) {
        String inString = properties.getProperty(key);
        logger.debug(String.format("%s=%s", key, inString));
        float[] out = new float[0];
        try {
            out = getStringValuesAsFloats(inString);
        } catch (Exception e) {
            logger.warn(String.format("Property '%s' was not initialized correctly", key));
        }
        return out;
    }

    public static float[] getStringValuesAsFloats(String inString){
        float[] out;
        try {
            String[] stringValues = parseStringValues(inString);
            out = new float[stringValues.length];
            for (int i = 0; i < stringValues.length; i++) {
                out[i] = Float.parseFloat(stringValues[i]);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Cannot parse floats from string '%s'", inString), e);
        }
        return out;
    }

    public static double[] getStringValuesAsDoubles(String inString){
        double[] out;
        try {
            String[] stringValues = parseStringValues(inString);
            out = new double[stringValues.length];
            for (int i = 0; i < stringValues.length; i++) {
                out[i] = Double.parseDouble(stringValues[i]);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Cannot parse doubles from string '%s'", inString), e);
        }
        return out;
    }

    public static int[] getStringValuesAsInts(Properties properties, String key) {
        String inString = properties.getProperty(key);
        logger.debug(String.format("%s=%s", key, inString));
        int[] out = new int[0];
        try {
            out = getStringValuesAsInts(inString);
        } catch (Exception e) {
            logger.warn(String.format("Property '%s' was not initialized correctly", key));
        }
        return out;
    }

    public static int[] getStringValuesAsInts(String inString) {
        int[] out;
        try {
            String[] stringValues = parseStringValues(inString);
            out = new int[stringValues.length];
            for (int i = 0; i < stringValues.length; i++) {
                out[i] = Integer.parseInt(stringValues[i]);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Cannot parse ints from string '%s'", inString), e);
        }
        return out;
    }

    public static short[] getStringValuesAsShorts(String inString) {
        short[] out;
        try {
            String[] stringValues = parseStringValues(inString);
            out = new short[stringValues.length];
            for (int i = 0; i < stringValues.length; i++) {
                out[i] = Short.parseShort(stringValues[i]);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Cannot parse shorts from string '%s'", inString), e);
        }
        return out;
    }

     public static long[] getStringValuesAsLongs(String inString) {
        long[] out;
        try {
            String[] stringValues = parseStringValues(inString);
            out = new long[stringValues.length];
            for (int i = 0; i < stringValues.length; i++) {
                out[i] = Long.parseLong(stringValues[i]);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Cannot parse longs from string '%s'", inString), e);
        }
        return out;
    }

    public static byte[] getStringValuesAsBytes(String inString) {
        byte[] out;
        try {
            String[] stringValues = parseStringValues(inString);
            out = new byte[stringValues.length];
            for (int i = 0; i < stringValues.length; i++) {
                out[i] = Byte.parseByte(stringValues[i]);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Cannot parse bytes from string '%s'", inString), e);
        }
        return out;
    }

    public static boolean[] getStringValuesAsBooleans(Properties properties, String key) {
        String inString = properties.getProperty(key);
        logger.debug(String.format("%s=%s", key, inString));
        boolean[] out = new boolean[0];
        try {
            out = getStringValuesAsBooleans(inString);
        } catch (Exception e) {
            logger.warn(String.format("Property %s was not initialized correctly", key));
        }
        return out;
    }

    public static boolean[] getStringValuesAsBooleans(String inString){
        boolean[] out;
        try {
            String[] stringValues = parseStringValues(inString);
            out = new boolean[stringValues.length];
            for (int i = 0; i < stringValues.length; i++) {
                out[i] = Boolean.parseBoolean(stringValues[i]);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Cannot parse booleans from string %s", inString), e);
        }
        return out;
    }


    public static String[] getStringValuesAsStrings(Properties properties, String key) {
        String inString = properties.getProperty(key);
        logger.debug(String.format("%s=%s", key, inString));
        String[] out = new String[0];
        out = parseStringValues(inString);
        return out;
    }

    public static String[] getStringValuesAsStrings(String inString) {
        String[] out = parseStringValues(inString);
        return out;
    }


    public static String getDoubleArrayAsString(double[] array){
        StringBuilder builder = new StringBuilder();
        for(double number : array){
            builder.append(number).append(" ");
        }
        return builder.toString();
    }

    public static String getStringValueForObject(Object o) {
        if (o instanceof String) {
            return (String) o;
        }
        if (o instanceof Float) {
            return String.valueOf(((Float) o).floatValue());
        }
        if (o instanceof Double) {
            return String.valueOf(((Double) o).doubleValue());
        }
        if (o instanceof Short) {
            return String.valueOf(((Short) o).shortValue());
        }
        if (o instanceof Integer) {
            return String.valueOf(((Integer) o).intValue());
        }
        if (o instanceof Boolean) {
            return String.valueOf(((Boolean) o).booleanValue());
        }
        return o.toString();
    }

    /**
     * adds number of spaces at the end so, the String has a desired length
     *
     * @return
     */
    public static String getStringPadded(String inString, int desiredLegth) {
        StringBuffer builder = new StringBuffer(inString);
        for (int i = inString.length(); i <= desiredLegth; i++) {
            builder.append(" ");
        }
        return builder.toString();
    }


    public static String getRandomStringForDate() {
        if (random == null) {
            random = new Random();
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SS");
        String dateStr = formatter.format(new Date());
        return String.format("%s_%d", dateStr, (int)(random.nextFloat() * 1000));
    }


    public static int getMidiNumberForNote(String note) {
        String noteName;
        int octave;
        if (note.indexOf("#") == 1) {
            noteName = note.substring(0, 2);
            try {
                octave = Integer.parseInt(note.substring(2));
            } catch (Exception e) {
                logger.error("Bad String originalFormat for MIDI note " + note);
                logger.error(getStackTrace(e));
                return 0;
            }
        } else {
            noteName = note.substring(0, 1);
            try {
                octave = Integer.parseInt(note.substring(1));
            } catch (Exception e) {
                logger.error("Bad String originalFormat for MIDI note " + note);
                logger.error(getStackTrace(e));
                return 0;
            }
        }

        int noteIndex = new ChordSegment(0, 0, noteName, 0).getRoot().ordinal();

        return 12 + octave * NUMBER_OF_SEMITONES_IN_OCTAVE + noteIndex;
    }

    /**
     * Produces tone name by it's midi number
     *
     * @param midiIndex index in midi scale
     * @return string note representation
     */
    public static String getNoteForMidiIndex(int midiIndex) {
        int octave = (midiIndex - START_NOTE_FOR_PCP_UNWRAPPED) / NUMBER_OF_SEMITONES_IN_OCTAVE + 1;
        int toneIndex = midiIndex % NUMBER_OF_SEMITONES_IN_OCTAVE;
        return Root.values()[toneIndex].getName() + octave;
    }
}
