package org.mart.crs.utils;

import org.mart.crs.config.ExecParams;
import org.mart.crs.exec.scenario.BatchParameter;
import org.junit.Assert;

import java.util.List;

/**
 * ReflectUtils Tester.
 *
 * @author Hut
 * @version 1.0
 * @since <pre>02/15/2011</pre>
 */
public class ReflectUtilsTest {



    /**
     * Method: setVariableValue(Class classOrObject, String fieldName, String unparsedValue)
     */
    @org.junit.Test
    public void testSetVariableValue() throws Exception {
        ExecParams execParams = new ExecParams();
        List<BatchParameter> fields = ReflectUtils.getSettingsVariables(execParams, "_TRAIN_FEATURES_");
        String fieldName = fields.get(0).getField().getName();
        execParams.samplingRate = 11026;
        Assert.assertEquals(ReflectUtils.getVariableValue(execParams, "samplingRate"), 11026.0f);
    }


}
