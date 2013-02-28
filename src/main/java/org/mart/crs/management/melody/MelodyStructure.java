package org.mart.crs.management.melody;

import org.apache.log4j.Logger;
import org.mart.crs.config.Extensions;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.beat.BeatStructureCSV;
import org.mart.crs.management.label.chord.ChordSegment;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 2/21/13 11:25 AM
 * @author: Hut
 */
public class MelodyStructure {

    protected static Logger logger = CRSLogger.getLogger(MelodyStructure.class);


    protected List<MelodySegment> melodySegments;

    private MelodyStructure(String csvPath) {
        melodySegments = new ArrayList<MelodySegment>();
        File file;
        try {

            file = HelperFile.getFile(csvPath);

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            float prevTime = 0;
            float prevPitch = 0;
            float time = 0;
            float pitch = 0;
            boolean started = false;
            while ((line = reader.readLine()) != null && line.length() > 1) {
                String[] comps = line.trim().replaceAll("\"", "").split(",");
                time = Float.parseFloat(comps[0]);
                pitch = Float.parseFloat(comps[1]);
                if (started) {                                                                                                        //TODO chords from nnls-chroma may have the following format: C:maj/A which is not parsed correctly
                    if (pitch != prevPitch) {
                        melodySegments.add(new MelodySegment(prevTime, time, prevPitch));
                        prevPitch = pitch;
                        prevTime = time;
                    }
                } else {
                    prevPitch = pitch;
                    prevTime = time;
                    started = true;
                }
            }
            melodySegments.add(new MelodySegment(prevTime, time, prevPitch));
            reader.close();
        } catch (FileNotFoundException e) {
            logger.info("Could not find label file " + csvPath);
        } catch (Exception e) {
            logger.error("Unexpected Error occured ");
            logger.error(Helper.getStackTrace(e));
        }

    }


    public static MelodyStructure getMelodyStructure(String filePath) {
        String extension = HelperFile.getExtension(filePath);
        if (extension.equals(Extensions.CSV_EXT)) {
            return new MelodyStructure(filePath);
        }
        throw new IllegalArgumentException(String.format("Cannot extract beat structure from file %s with extension %s", filePath, extension));
    }


    public List<MelodySegment> getMelodySegments() {
        return melodySegments;
    }
}
