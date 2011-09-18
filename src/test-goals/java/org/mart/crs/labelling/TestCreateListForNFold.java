package org.mart.crs.labelling;

import org.mart.crs.utils.helper.HelperFile;
import junit.framework.TestCase;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @version 1.0 May 8, 2009 11:58:03 PM
 * @author: Maksim Khadkevich
 */
public class TestCreateListForNFold extends TestCase {

        public static int NFolds = 3;
        public static String wavListAll = "d:\\dev\\CHORDS4\\temp\\bestEver\\all.txt";
        public static String outDir = "d:\\dev\\CHORDS4\\temp\\bestEver\\all\\";


        public void testCreateListForNFold() throws IOException {

                List<String> songFilePath = HelperFile.readTokensFromTextFile(wavListAll, 1);

                BufferedWriter[] labelFileTrain = new BufferedWriter[NFolds];
                BufferedWriter[] labelFileTest = new BufferedWriter[NFolds];
                for (int i = 0; i < NFolds; i++) {
                        labelFileTrain[i] = new BufferedWriter(new FileWriter(String.format("%strain_%d.txt", outDir, i)));
                        labelFileTest[i] = new BufferedWriter(new FileWriter(String.format("%stest_%d.txt", outDir, i)));
                }

                for (int j = 0; j < songFilePath.size(); j++) {
                        for (int i = 0; i < NFolds; i++) {
                                if (j % NFolds == i) {
                                        labelFileTest[i].write(songFilePath.get(j) + "\n");
                                } else {
                                        labelFileTrain[i].write(songFilePath.get(j) + "\n");
                                }
                        }
                }

                for (int i = 0; i < NFolds; i++) {
                        labelFileTrain[i].close();
                        ;
                        labelFileTest[i].close();
                }




        }
}
