package misc;

import org.mart.crs.utils.helper.HelperFile;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 Jan 24, 2010 10:59:08 AM
 * @author: Maksim Khadkevich
 */
public class A853CoverSongsManager extends TestCase {


    public void testParseFileList() {

        String listFilePath = "F:\\853_Cover_Songs\\list.txt";
        String outlistFilePath = "F:\\853_Cover_Songs\\list_emule.txt";
        String coverSongsDir = "F:\\853_Cover_Songs";

        List<String> lines = HelperFile.readLinesFromTextFile(listFilePath);

        List<Song> outList = new ArrayList<Song>();
        List<String> outputLines = new ArrayList<String>();

        String token;
        String artist, songName, origArtist;
        for (String aLine:lines) {
            try {
                artist = aLine.substring(0, aLine.indexOf('-')).trim();
                songName = aLine.substring( aLine.indexOf('-') + 1, aLine.lastIndexOf("(")).trim();
                origArtist = aLine.substring(aLine.lastIndexOf("(") + 1, aLine.indexOf("cover")).trim();

                List<String> filePaths = HelperFile.findFilesWithName(coverSongsDir, songName);
                outList.add(new Song(artist, songName, origArtist, filePaths));
                outputLines.add(origArtist + " " + songName);
            } catch (Exception e) {
                System.out.println("Problem with line: \n" + aLine);
            }
        }

        HelperFile.saveCollectionInFile(outputLines, outlistFilePath, false);
        System.out.println("Done");

    }


}

class Song{

    private String artist;
    private String songName;
    private String originalArtist;

    private List<String> origFileName;

    Song(String artist, String songName, String originalArtist, List<String> origFileName) {
        this.artist = artist;
        this.songName = songName;
        this.originalArtist = originalArtist;
        this.origFileName = origFileName;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getOriginalArtist() {
        return originalArtist;
    }

    public void setOriginalArtist(String originalArtist) {
        this.originalArtist = originalArtist;
    }

    public List<String> getOrigFileName() {
        return origFileName;
    }

    public void setOrigFileName(List<String> origFileName) {
        this.origFileName = origFileName;
    }
}

