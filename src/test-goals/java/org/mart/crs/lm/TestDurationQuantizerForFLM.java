package org.mart.crs.lm;

import junit.framework.TestCase;
import org.mart.crs.utils.helper.Helper;

/**
 * @version 1.0 24.04.2009 20:39:28
 * @author: Maksim Khadkevich
 */
public class TestDurationQuantizerForFLM extends TestCase {

    public void  testDurationQuantizerForFLM(){
     for(int i = 0; i < 32; i++){
         System.out.println(i + " : " + Helper.quantizeDuration(i));
     }
    }
}
