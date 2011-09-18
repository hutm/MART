package jni;

import org.mart.crs.config.Settings;

import java.io.File;

/**
 * @version 1.0 26.03.2009 19:13:54
 * @author: Maksim Khadkevich
 */
public class JniSimpleTest {


    private native float[] ncc(float[] vec1, float[] vec2);

    static {
        File dllFile = new File("bin" + File.separator + "NCC_DLL" + Settings.DYNAMIC_LIBRARY_EXTENSION);
        System.load(dllFile.getAbsolutePath());
    }

    public static void main(String[] args) {
        JniSimpleTest app = new JniSimpleTest();
        float[] vec1 = {0,0,0,0,0};
        float[] vec2 = {2,3,4,5,6};
        float[] result = app.ncc(vec1, vec2);
        for (int i = 0; i < result.length; i++) {
            System.out.println(result[i]);
        }
    }
}