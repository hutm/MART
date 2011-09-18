package simpleClasses;

import org.mart.crs.core.AudioReader;
import org.mart.crs.utils.windowing.Hanning;
import org.mart.crs.utils.windowing.WindowFunction;

/**
 * @version 1.0 17-Aug-2010 15:43:02
 * @author: Hut
 */
public class GeneralPurposeClass {

    public static void main(String[] args) {
        WindowFunction window = new Hanning();
//
        int winLength =  512;
        float[] frequencyweighted = new float[winLength];
        float[] timeWeighted = new float[winLength];
        float[] timeFreqWeighted = new float[winLength];
        for (int i = 0; i < winLength; i++) {
            frequencyweighted[i] = window.getFuctionFrequencyWeighted(i, 0, winLength, 11025);
            timeWeighted[i] = window.getFunctionTimeWeighted(i, 0, winLength, 11025);
            timeFreqWeighted[i] = window.getFunctionTimeFrequencyWeighted(i, 0, winLength, 11025);
        }

        System.out.println("");

        AudioReader reader = new AudioReader("d:\\work\\notes\\chords\\A#m_A#3_BassFlute.wav_C#4_TenorTrombone.wav_F2_Horn.wav_.wav");
        reader.getSamples();
        System.out.println("");

    }
}
