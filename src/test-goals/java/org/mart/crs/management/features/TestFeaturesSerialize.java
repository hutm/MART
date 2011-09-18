package org.mart.crs.management.features;

import junit.framework.TestCase;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 08-Jul-2010 14:09:07
 * @author: Hut
 */
public class TestFeaturesSerialize extends TestCase {

    public void testSerialize() throws IOException, ClassNotFoundException {
        List<float[][]> vectors = new ArrayList<float[][]>();
        vectors.add(new float[][]{{2, 3, 4}, {5, 3, 6}});
        vectors.add(new float[][]{{1, 2, 3}, {4, 5, 7}});

        FeatureVector featureVector = new FeatureVector(vectors, 1001);

        FileOutputStream fos = new FileOutputStream("temp/temp.out");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(featureVector);
        oos.writeObject(featureVector);
        oos.flush();
        oos.close();

        FileInputStream fis = new FileInputStream("temp/temp.out");
        ObjectInputStream oin = new ObjectInputStream(fis);
        FeatureVector ts = (FeatureVector) oin.readObject();
        FeatureVector ts1 = (FeatureVector) oin.readObject();
        oin.close();


        System.out.println("version=");



    }


}
