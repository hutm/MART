package simpleClasses;

import org.mart.crs.utils.helper.HelperFile;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 11-Oct-2010 19:20:49
 * @author: Hut
 */
public class createSSRCBatch {

    public static void main(String[] args) {
        List<String > strings  = HelperFile.readLinesFromTextFile("d:\\Beatles\\data\\wav\\start.bat");
        List<String>  outList = new ArrayList<String>();
        for(String s:strings){
            outList.add(String.format("ssrc.exe --rate 11025 %s out\\%s", s, s ));
        }
        HelperFile.saveCollectionInFile(outList, "d:\\Beatles\\data\\wav\\start_.bat", false);
    }
}
