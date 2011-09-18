package org.mart.crs.labelling;

import org.mart.crs.utils.helper.HelperFile;
import junit.framework.TestCase;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @version 1.0 29-Jun-2010 23:16:27
 * @author: Hut
 */
public class TestRankSongsByResult extends TestCase {

    public void testRank() throws IOException {
        String inFile = "temp/allresults.txt";
        String outFile = "temp/sortedallResults.txt";
        String outDir = "temp/trainLists";

        String prefix = "/hardmnt/hammer0/ssi/khadkevich/data/wav/";

        Map<String, String> inMap = HelperFile.readMapFromTextFile(inFile);

        Map<String, Float> outMap = new HashMap<String, Float>();
        for (String key : inMap.keySet()) {
            outMap.put(key, Float.parseFloat(inMap.get(key)));
        }

        SortedMap sortedData = new TreeMap(new ValueComparer(outMap));
        sortedData.putAll(outMap);

        ArrayList<String> sortedSongs = new ArrayList<String>();
        FileWriter writer = new FileWriter(outFile);
        for (Iterator iter = sortedData.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            writer.write(String.format("%s\t%5.4f\r\n", key, sortedData.get(key)));
            sortedSongs.add(key);
        }
        writer.close();

        HelperFile.createDir(outDir);

        int step = 5;
        int nextWriter = 220;
        List<FileWriter> openedFileWriters = new ArrayList<FileWriter>();
        for (int i = 0; i < 220; i++) {
            if (220 - nextWriter <= i) {
                writer = new FileWriter(outDir + "/train_" + nextWriter + ".txt");
                openedFileWriters.add(writer);
                nextWriter = nextWriter - step;
            }

            for (FileWriter writer1 : openedFileWriters) {
                writer1.write(prefix + sortedSongs.get(i).replaceAll("\\.lab", ".wav") + "\r\n");
            }
        }

        for (FileWriter writer1 : openedFileWriters) {
            writer1.close();
        }


    }

    /**
     * inner class to do soring of the map *
     */
    private static class ValueComparer implements Comparator {
        private Map<String, Float> _data = null;

        public ValueComparer(Map<String, Float> data) {
            super();
            _data = data;
        }

        public int compare(Object o1, Object o2) {
            Float e1 = _data.get(o1);
            Float e2 = _data.get(o2);
            return e1.compareTo(e2);
        }
    }

}
