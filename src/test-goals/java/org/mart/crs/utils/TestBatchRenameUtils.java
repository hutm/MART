package org.mart.crs.utils;

import junit.framework.TestCase;
import org.mart.crs.utils.helper.HelperFile;

import java.io.File;
import java.util.Arrays;

/**
 * @version 1.0 6/5/11 6:05 PM
 * @author: Hut
 */
public class TestBatchRenameUtils extends TestCase {



    public void testBatchRename(){
        String srcDir = "/home/hut/Beatles/data/tempLab";
        String destDir = "/home/hut/Beatles/data/flac2";
        File[] srcFiles = HelperFile.getFile(srcDir).listFiles();
        File[] destFiles = HelperFile.getFile(destDir).listFiles();
        Arrays.sort(srcFiles);
        Arrays.sort(destFiles);
        for(int i = 0; i < srcFiles.length; i++){
            String srcName =  srcFiles[i].getName();
            String destName = destFiles[i].getName();
            if (!srcName.equals(destName)) {
                System.out.println(String.format("%s > %s", srcName, destName));
                destFiles[i].renameTo(HelperFile.getFile(destFiles[i].getParent() + "/" + srcName));
            }
        }
    }



}
