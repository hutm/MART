package org.mart.crs.filter;


import org.mart.crs.core.pcp.spectral.PCP;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.helper.HelperFile;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;


/**
 * @version 1.0 Dec 9, 2009 2:09:58 PM
 * @author: Maksim Khadkevich
 */
public class TestFilterBank extends TestCase {

    protected static Logger logger = CRSLogger.getLogger(TestFilterBank.class);


    public static final float frameSize = 0.3f; //Frame size in msecs

    int MIDINumner_start = 24;
    int MIDINumner_end = 83;


    public PCP testCreateFilterBank() {
        String audioFileName = "data/1_.wav";
        String filterConfigFilePath = "data/filters/filters.txt";
        Map<String, String> filtersMap = HelperFile.readMapFromTextFile(filterConfigFilePath);

        Map<Integer, float[]> noteProbabilities = new HashMap<Integer, float[]>();

        PCP pcp = null;
        for (String midiNumber : filtersMap.keySet()) {
            String samplingFreqString = filtersMap.get(midiNumber);
            float samplingFreq = Float.parseFloat(samplingFreqString);

            String filterFilePath = String.format("data/filters/%s_%s.%s", midiNumber, samplingFreqString, "flt");

            logger.info("passing signal through filter " + filterFilePath);

            //TODO rewrite this part of code
//            Filter filter = null;
//            filter = FilterManager.getFilter(filterFilePath);
//
//            AudioReaderImpl audioReader = new AudioReaderImpl(audioFileName, samplingFreq);
//
//            float[] out = filter.process(audioReader.getSamples());
//            audioReader.setSamples(out);
//
//            windowLength = Math.round(frameSize * samplingFreq);
//            SpectrumCombTime spectrum = new SpectrumCombTime(audioReader, Helper.getFreqForMIDINote(parseInt(midiNumber)));
//            spectrum.calculateHarmonicProfileVectors();
//
//            if (pcpList == null) {
//                pcpList = new PCP(spectrum, EXTRACTION_ALGORITHM);
//            }

//            noteProbabilities.put(parseInt(midiNumber), spectrum.getHarmonicPVScore());
        }

        //Now construct PCP vector from this


        float[][] outPCPData = new float[noteProbabilities.get(MIDINumner_start).length][MIDINumner_end - MIDINumner_start + 1];

        for (int i = 0; i < outPCPData.length; i++) {
            float[] data = noteProbabilities.get(i + MIDINumner_start);
            if (data != null) {
                for (int j = 0; j < outPCPData[0].length; j++) {
                    outPCPData[j][i] = data[j];
                }
            }
        }

        pcp.setPcpUnwrapped(outPCPData);


        return pcp;


    }


}
