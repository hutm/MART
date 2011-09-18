package org.mart.crs.lm;

import org.mart.crs.exec.operation.models.lm.TextForLMCreator;
import junit.framework.TestCase;

/**
 * @version 1.0 May 11, 2009 9:37:42 PM
 * @author: Maksim Khadkevich
 */
public class TestExtractTextForHTKLM extends TestCase {


    public void testCreateText(){
        for(int i = 0; i < 5; i++){
            String labelsFilePaths = "D:\\Beatles\\wav\\" + i + "_train_labels";
            String outputText =  "D:\\Beatles\\wav\\outText\\" + i + "_text";
            TextForLMCreator.process(labelsFilePaths, outputText, false);
        }
    }
}
