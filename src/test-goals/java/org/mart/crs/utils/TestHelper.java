package org.mart.crs.utils;

import junit.framework.TestCase;
import org.mart.crs.utils.helper.HelperFile;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @version 1.0 Sep 21, 2009 2:13:40 PM
 * @author: Maksim Khadkevich
 */
public class TestHelper extends TestCase {


    public void testGetClasses() {
        try {
            String[] entriyNames = HelperFile.getResourceListing(Class.forName("javazoom.jl.converter.jlc"), "javazoom/jl/converter/");
            assert entriyNames.length == 13;
            entriyNames = HelperFile.getResourceListing(Class.forName("org.mart.gui.panel.dynamic.panel.DynamicPanel"), "com/mart/gui/panel/dynamic/");

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }
}
