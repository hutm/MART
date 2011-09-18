package simpleClasses;

import org.mart.crs.exec.operation.eval.chord.EvaluatorOld;
import org.mart.crs.management.label.chord.ChordStructure;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mart.crs.config.Settings.LABEL_EXT;

/**
 * @version 1.0 23.03.2009 17:24:56
 * @author: Maksim Khadkevich
 */
public class A {

    public A(int number) {
        printNumber(number);
    }

    protected void printNumber(int number) {
        System.out.println((new String("Cm")).replaceAll("m", ":min"));
    }

    public static void main(String[] args) {
//        test1();
        test2();
    }

    public static void test1() {
        (new ChordStructure("D:\\buildMIREX_final1\\work\\results\\results2\\02_-_Dig_a_Pony.lab")).getChordSegments();
        Map<String, List<float[]>> silence = new HashMap<String, List<float[]>>();
        List<float[]> silenceList = new ArrayList<float[]>();
        silenceList.add(new float[]{5.06f, 5.58f});
        silenceList.add(new float[]{5.7f, 7.08f});
        silenceList.add(new float[]{229.5f, 234.76245f});
        silenceList.add(new float[]{228.7f, 229.28f});
        silenceList.add(new float[]{227.64f, 228.64f});
        silence.put("02_-_Dig_a_Pony.lab", silenceList);
        ChordStructure.addNonChordSegments("D:/temp/output1", "D:/temp/output2", silence, true, LABEL_EXT);
    }

    public static void test2() {
        EvaluatorOld.makeEvaluation("D:\\buildMIREX_final_final_submission\\khadkevich_omologo\\output2", "D:/Beatles/labels", "D:\\buildMIREX_final_final_submission\\khadkevich_omologo\\output2" + File.separator + "!All" + ".txt");

    }
}
