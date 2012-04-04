package simpleClasses;

import junit.framework.TestCase;
import org.mart.crs.utils.helper.HelperArrays;

import java.util.Random;

/**
 * @version 1.0 Sep 12, 2009 8:16:56 PM
 * @author: Maksim Khadkevich
 */
public class TestCorrelation extends TestCase{

    public static final int n = 10;

    public void testCorrelation(){
        float[] x = new float[n];
        float[] y = new float[n];

        for(int i = 0; i < n; i ++){
            x[i] = 1;
            y[i] = 1+1;
        }

        System.out.print(HelperArrays.correlation(x, y));

    }

    public void testFloor(){
        Random rand = new Random();
        int sum1 = 0;
        int sum2 = 0;
        for (int i = 0; i < 10000; i++) {
            short outValue = (short) (3 / 2.0f + rand.nextFloat());
            if(outValue == 1){
                sum1++;
            }
            else {
                sum2++;
            }
        }
        System.out.println("Sum1 = " + sum1);
        System.out.println("Sum2 = " + sum2);

    }
}
