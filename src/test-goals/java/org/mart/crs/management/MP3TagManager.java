package org.mart.crs.management;

import org.mart.crs.config.Settings;
import org.mart.crs.utils.helper.HelperFile;
import org.blinkenlights.jid3.ID3Exception;
import org.blinkenlights.jid3.ID3Tag;
import org.blinkenlights.jid3.MP3File;
import org.blinkenlights.jid3.MediaFile;
import org.blinkenlights.jid3.v1.ID3V1_0Tag;
import org.blinkenlights.jid3.v1.ID3V1_1Tag;
import org.blinkenlights.jid3.v2.ID3V2_3_0Tag;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @version 1.0 Jan 24, 2010 5:13:18 PM
 * @author: Maksim Khadkevich
 */
public class MP3TagManager {


    private static Map<String, List<String>> titleArtistMap = new HashMap<String, List<String>>();

    @Test
    public void testFindCoverSongs() {

        String coverDirPath = "F:\\853_Cover_Songs\\mp3";
        String origDirPath = "F:\\853_Cover_Songs\\orig";
        String outDirPath = "F:\\853_Cover_Songs\\final";
        String outFile = "F:\\853_Cover_Songs\\final.txt";

        Map<String, List<File>> songs = new HashMap<String, List<File>>();


        File coverDir = new File(coverDirPath);
        File origDir = new File(origDirPath);

        File[] listCoverFiles = coverDir.listFiles();
        File[] listOrigFiles = origDir.listFiles();

        for (File file : listCoverFiles) {
            findAudioFiles(file, listCoverFiles, songs);
            findAudioFiles(file, listOrigFiles, songs);
        }

        List<String> outList = new ArrayList<String>();
        int counter = 0;
        for (String song : songs.keySet()) {
            List<File> fileList = songs.get(song);
            for (File file : fileList) {
                String[] tags = getTitleAndArtist(file);
                String title = tags[1].trim().toLowerCase().replaceAll(" ", "_").replaceAll("'", "").replaceAll("!", "").replaceAll("\\?", "");
                String artist = tags[0].trim().toLowerCase().replaceAll(" ", "_").replaceAll("'", "").replaceAll("!", "").replaceAll("\\?", "");;

                String newFileName = String.format("%05d_%s_-_%s", counter++, title, artist);

                try {
                    HelperFile.copyFile(file, HelperFile.getFile(outDirPath + File.separator + newFileName + Settings.MP3_EXT));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                outList.add(newFileName);
            }
        }

        HelperFile.saveCollectionInFile(outList, outFile, false);


    }

    private void findAudioFiles(File file, File[] fileList, Map<String, List<File>> songs) {
        String[] tags = getTitleAndArtist(file);

        if (tags[0] == null || tags[1] == null) {
            return;
        }
        String title = tags[1].trim().toLowerCase();
        String artist = tags[0].trim().toLowerCase();

        for (File secondFile : fileList) {
            if (file.equals(secondFile)) {
                continue;
            }
            String[] tagsSecondFile = getTitleAndArtist(secondFile);
            if (tagsSecondFile[0] == null || tagsSecondFile[1] == null) {
                continue;
            }
            String titleSecondFile = tagsSecondFile[1].trim().toLowerCase();
            String artistSecondFile = tagsSecondFile[0].trim().toLowerCase();

            if (title.equalsIgnoreCase(tagsSecondFile[1].trim())) {
                System.out.println("Found " + title);
                if (!songs.containsKey(title)) {
                    songs.put(title, new ArrayList<File>());
                    songs.get(title).add(file);
                    titleArtistMap.put(title, new ArrayList<String>());
                    titleArtistMap.get(title).add(artist);
                }
                if (!songs.get(title).contains(secondFile) && !titleArtistMap.get(title).contains(artistSecondFile)) {
                    songs.get(title).add(secondFile);
                    titleArtistMap.get(title).add(artistSecondFile);
                }
            }
        }
    }


    private String[] getTitleAndArtist(File file) {
        MediaFile oMediaFile = new MP3File(file);
        String[] out = new String[2];
        // any tags read from the file are returned, in an array, in an order which you should not assume
        try {
            ID3Tag[] aoID3Tag = oMediaFile.getTags();

            for (int i = 0; i < aoID3Tag.length; i++) {
                // check to see if we read a v1.0 tag, or a v2.3.0 tag (just for example..)
                if (aoID3Tag[i] instanceof ID3V1_0Tag) {
                    ID3V1_0Tag oID3V1_0Tag = (ID3V1_0Tag) aoID3Tag[i];
                    // does this tag happen to contain a title?
                    if (oID3V1_0Tag.getArtist() != null) {
                        out[0] = oID3V1_0Tag.getArtist();
                    }
                    if (oID3V1_0Tag.getTitle() != null) {
                        out[1] = oID3V1_0Tag.getTitle();
                    }
                } else if (aoID3Tag[i] instanceof ID3V1_1Tag) {
                    ID3V1_1Tag oID3V1_1Tag = (ID3V1_1Tag) aoID3Tag[i];
                    // does this tag happen to contain a title?
                    if (oID3V1_1Tag.getArtist() != null) {
                        out[0] = oID3V1_1Tag.getArtist();
                    }
                    if (oID3V1_1Tag.getTitle() != null) {
                        out[1] = oID3V1_1Tag.getTitle();
                    }
                } else if (aoID3Tag[i] instanceof ID3V2_3_0Tag) {
                    ID3V2_3_0Tag oID3V2_3_0Tag = (ID3V2_3_0Tag) aoID3Tag[i];
                    // check if this v2.3.0 frame contains a title, using the actual frame name
                    if (oID3V2_3_0Tag.getArtist() != null) {
                        out[0] = oID3V2_3_0Tag.getArtist();
                    }
                    if (oID3V2_3_0Tag.getTIT2TextInformationFrame() != null) {
                        out[1] = oID3V2_3_0Tag.getTitle();
                    }
                }
            }

        } catch (ID3Exception e) {
            e.printStackTrace();
        }
        return out;
    }


}
