package org.mart.crs.filter;

import org.mart.crs.analysis.filter.PQMF.PQMFHarmonicExtraction;
import junit.framework.TestCase;

/**
 * @version 1.0 Jul 14, 2009 11:47:08 PM
 * @author: Maksim Khadkevich
 */
public class TestPQMFPitchExtractor extends TestCase {


    public void test1(){
        PQMFHarmonicExtraction pqmfPitchExtraction= new PQMFHarmonicExtraction("D:\\downloads\\1.wav", "D:\\dev\\CHORDS\\CRS_MODULE\\cfg\\PQMFConfig.cfg", 0.01f);
    }
}
