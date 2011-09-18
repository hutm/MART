package org.mart.crs.eval.tempo;

import org.mart.crs.exec.operation.eval.tempo.TempoExtractionOperation;
import junit.framework.TestCase;

/**
 * TempoExtractionOperation Tester.
 *
 * @author Hut
 * @since <pre>03/10/2011</pre>
 * @version 1.0
 */
public class TempoExtractionOperationTest extends TestCase {



    public void testClass() throws Exception {
        TempoExtractionOperation extractionOperation = new TempoExtractionOperation("/home/hut/work/tempo/4", "/home/hut/work/tempo/4/lmWeight_10.00#acWeight_1.00#wip_3.00");
        extractionOperation.operate();

        extractionOperation = new TempoExtractionOperation("/home/hut/work/tempo/4", "/home/hut/work/tempo/4/-");
        extractionOperation.operate();
    }

}
