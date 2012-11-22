package org.mart.tools.hdf5;

import ncsa.hdf.hdf5lib.exceptions.HDF5AtomException;
import ncsa.hdf.object.h5.H5File;
import org.mart.crs.management.beat.BeatStructure;
import org.mart.crs.management.beat.segment.BeatSegment;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.HelperFile;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 11/20/12 11:00 AM
 * @author: Hut
 */
public class hdf5_gettersTest {

    @Test
    public void testHDF() throws Exception {
        String resourceHDF = getClass().getResource("/data/TRAAABD128F429CF47.h5").getPath();
        H5File h5 = hdf5_getters.hdf5_open_readonly(resourceHDF);

        double[] beats = hdf5_getters.get_beats_start(h5);
        double[] segmentsStart = hdf5_getters.get_segments_start(h5);
        double[] chroma = hdf5_getters.get_segments_pitches(h5);
        Assert.assertTrue(chroma.length > 0);
        Assert.assertTrue(chroma.length == segmentsStart.length * 12);


    }


    @Test(groups = {"static"})
    public void testExtractBeatsAndSegments() throws Exception {

        String dirIn = "/home/hut/mirdata/chords/h5";
        String dirBeatsOut = "/home/hut/mirdata/chords/h5beats";
        String dirSegmentsOut = "/home/hut/mirdata/chords/h5segments";

        File[] inFiles = new File(dirIn).listFiles(new ExtensionFileFilter(new String[]{".h5"}));
        for(File inWavFile: inFiles){
            try {
                H5File h5 = hdf5_getters.hdf5_open_readonly(inWavFile.getCanonicalPath());
                double[] beats = hdf5_getters.get_beats_start(h5);
                double[] segmentsStart = hdf5_getters.get_segments_start(h5);

                String songFileName = HelperFile.getNameWithoutExtension(inWavFile.getName());

                BeatStructure beatStructure = new BeatStructure(createBeatSegments(beats), songFileName);
                beatStructure.serializeIntoTXT(dirBeatsOut + "/" + songFileName + ".txt", false);

                BeatStructure beatStructure2 = new BeatStructure(createBeatSegments(segmentsStart), songFileName);
                beatStructure2.serializeIntoTXT(dirSegmentsOut + "/" + songFileName + ".txt", false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



    }


    protected List<BeatSegment> createBeatSegments(double[] onsets){
        List<BeatSegment> outList = new ArrayList<BeatSegment>();
        for(Double onset:onsets){
            outList.add(new BeatSegment(onset, 1));
        }
        return outList;
    }


}
