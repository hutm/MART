package org.mart.crs.core.similarity;

import junit.framework.TestCase;
import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.core.AudioReader;
import org.mart.crs.core.pcp.PCP;
import org.mart.crs.core.spectrum.SpectrumImpl;

/**
 * SimilarityMatrix Tester.
 *
 * @author Hut
 * @version 1.0
 * @since <pre>03/15/2011</pre>
 */
public class SimilarityMatrixTest extends TestCase {

    public void testComputeDetectionFunction() throws Exception {
        Settings.initializationByParts = false;
        AudioReader reader = new AudioReader(this.getClass().getResource("/0400_-_George_Michael_-_Careless_Whisper_.wav").getPath());
        SpectrumImpl spectrumImpl = new SpectrumImpl(reader, ExecParams._initialExecParameters);
        PCP pcp = new PCPBuilder(PCPBuilder.BASIC_ALG).setExecParams(ExecParams._initialExecParameters).setSpectrum(spectrumImpl).build();
        SimilarityMatrix similarityMatrix = new SimilarityMatrix(pcp.getPCP());
        similarityMatrix.setContextLength(15);
        float[] onsetdetectionFunction1 = similarityMatrix.getDetectionFunction();

        Settings.initializationByParts = true;
        SimilarityMatrix similarityMatrix2 = new SimilarityMatrix(pcp.getPCP());
        similarityMatrix2.setContextLength(15);
        float[] onsetdetectionFunction2 = similarityMatrix2.getDetectionFunction();
        for(int i = 0; i < onsetdetectionFunction1.length; i++){
            assertEquals(onsetdetectionFunction1[i], onsetdetectionFunction2[i]);
        }
        System.out.println("Done");
    }


}
