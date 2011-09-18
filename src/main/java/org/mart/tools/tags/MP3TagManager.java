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

package org.mart.tools.tags;

import org.mart.crs.utils.helper.HelperFile;
import org.blinkenlights.jid3.ID3Exception;
import org.blinkenlights.jid3.ID3Tag;
import org.blinkenlights.jid3.MP3File;
import org.blinkenlights.jid3.MediaFile;
import org.blinkenlights.jid3.v1.ID3V1_0Tag;
import org.blinkenlights.jid3.v1.ID3V1_1Tag;
import org.blinkenlights.jid3.v2.ID3V2_3_0Tag;

import java.io.*;
import java.net.URLDecoder;
import java.util.*;


/**
 * @version 1.0 Jan 24, 2010 5:13:18 PM
 * @author: Maksim Khadkevich
 */
public class MP3TagManager {

    private static Map<String, List<String>> titleArtistMap = new HashMap<String, List<String>>();

    public void testFindCoverSongs() {

       /* String coverDirPath = "F:\\853_Cover_Songs\\mp3";
        String origDirPath = "F:\\853_Cover_Songs\\orig";
        String outDirPath = "F:\\853_Cover_Songs\\final";
        String outFile = "F:\\853_Cover_Songs\\final.txt";*/

    	// a folder with new covers
    	String origDirPath = "D:\\temp\\popcover";
    	// a folder with the other covers
        String coverDirPath = "D:\\temp\\MorePopCoversIn";
        // output dir
        String outDirPath = "D:\\temp\\final";
        //log file
        String outFile = "D:\\temp\\final.txt";

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
        // start at 400
        int counter = 400;
        for (String song : songs.keySet()) {
            List<File> fileList = songs.get(song);
            for (File file : fileList) {
                String[] tags = getTitleAndArtist(file);
                String title = tags[1].trim().toLowerCase().replaceAll(" ", "_").replaceAll("'", "").replaceAll("!", "").replaceAll("\\?", "");
                String artist = tags[0].trim().toLowerCase().replaceAll(" ", "_").replaceAll("'", "").replaceAll("!", "").replaceAll("\\?", "");
                ;

                String newFileName = String.format("%05d_%s_-_%s", counter++, title, artist);

                try {
                	// only copy if new addition
                    if(contains(listCoverFiles, file))copyFile(file, getFile(outDirPath + File.separator + newFileName + ".mp3"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                outList.add(newFileName);
            }
        }

        storeCollectionInFile(outList, outFile, false);
    }

    private boolean contains(File[] haystack, File needle){
    	for(File f : haystack) if(f.equals(needle)) return true;
    	return false;
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


    public static String[] getTitleAndArtist(File file) {
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


    public static void copyFile(File sourceFile, File destFile) {
        try {
            InputStream in = new FileInputStream(sourceFile);

            //For Append the file.
//      OutputStream out = new FileOutputStream(destFile,true);

            //For Overwrite the file.
            OutputStream out = new FileOutputStream(destFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            System.out.println(String.format("File %s copied to file %s", sourceFile.getPath(), destFile.getPath()));
        }
        catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage() + " in the specified directory.");
//            System.exit(0);
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Stores collection in a File
     *
     * @param collection      collection
     * @param filePathToStore filePathToStore
     * @param isToUseQuotes   isToUseQuotes
     */
    public static void storeCollectionInFile(Collection<String> collection, String filePathToStore, boolean isToUseQuotes) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePathToStore));
            for (String s : collection) {
                if (isToUseQuotes) {
                    writer.write("\"" + s + "\"\r\n");
                } else {
                    writer.write(s + "\r\n");
                }
            }
            writer.close();

        } catch (IOException e) {
            System.out.println("IO Error occured");
        }
    }

    public static File getFile(String filePath) {
        File outFile = null;
        if (filePath == null) {
            return null;
        }
        if (filePath.startsWith("\"")) {
            filePath = filePath.replaceAll("\"", "");
        }
//        URL url = null;
        try {
//            url = new URL("file:/" + (new File(filePath)).getAbsolutePath());
            String filePath_out = URLDecoder.decode(filePath, System.getProperty("file.encoding"));
            outFile = new File(filePath_out);
            if (!outFile.exists()) {
                System.out.println(String.format("Probable problem with file %s (does not exist)", outFile.getPath()));
            }
        } catch (Exception e) {
//            logger.error(String.originalFormat("Error with file %s", url));
        }
        return outFile;
    }


    public static void main(String[] args){
//    	new MP3TagManager().testFindCoverSongs();
        String[] data = MP3TagManager.getTitleAndArtist(HelperFile.getFile("d:\\music\\!Masterpieces\\02-?????????.mp3"));
        System.out.println(data[0]);
        System.out.println(data[1]);
    }
}
