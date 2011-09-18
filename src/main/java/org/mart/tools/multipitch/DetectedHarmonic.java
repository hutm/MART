/*
 * This file is part of MART.
 * MART is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2, as published
 * by the Free Software Foundation.
 *
 * MART is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MART; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.mart.tools.multipitch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetectedHarmonic implements Comparable {

    private int frameIndex; //Frame number starting from the beginning

    private float freq; //Freq value
    private float amp; //Aplitude value
    private float spectralAmplitude; //Sum of all frequencies amplitudes in the frame with this DetectedHarmonic
    private float weight; //Propotional to the probability to be a fundamental

    private Map<DetectedHarmonic, Integer> probableHigherHarmonicsForThisFundamental;
    private Map<DetectedHarmonic, Integer> probableFundamentalsForThisHarmonic;
    private List<DetectedHarmonic> harmonicsToIgnore;

    private boolean amIHarmonic = false;  //By deault all candidates are concidered to be fundamentals

    private boolean amIFundamental = true; //By deault all candidates are concidered to be fundamentals

    public DetectedHarmonic(float freq, float amp, int frameIndex) {
        this.freq = freq;
        this.amp = amp;
        this.frameIndex = frameIndex;

        probableHigherHarmonicsForThisFundamental = new HashMap<DetectedHarmonic, Integer>();
        probableFundamentalsForThisHarmonic = new HashMap<DetectedHarmonic, Integer>();
        harmonicsToIgnore = new ArrayList<DetectedHarmonic>();
    }

    public DetectedHarmonic(float freq, float amp, int frameIndex, float spectralEnergy) {
        this(freq, amp, frameIndex);
        this.spectralAmplitude = spectralEnergy;
    }


    public int compareTo(Object o) {
        if (((DetectedHarmonic) o).getWeight() > this.getWeight()) {
            return 1;
        } else {
            return -1;
        }
    }


    public float getFreq() {
        return freq;
    }

    public void setFreq(float freq) {
        this.freq = freq;
    }

    public float getAmp() {
        return amp;
    }

    public void setAmp(float amp) {
        this.amp = amp;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getAmp2FrameAmpRatio() {
        return amp / spectralAmplitude;
    }


    public void addHarmonic(DetectedHarmonic harmonic, int number) {
        if (!probableHigherHarmonicsForThisFundamental.containsKey(harmonic)) {
            probableHigherHarmonicsForThisFundamental.put(harmonic, number);
        }
    }

    public void addFundamentalForThisHamonic(DetectedHarmonic harmonic, int number) {
        if (!probableFundamentalsForThisHarmonic.containsKey(harmonic)) {
            probableFundamentalsForThisHarmonic.put(harmonic, number);
        }
    }

    public void removeHarmonic(DetectedHarmonic harmonic) {
        if (probableHigherHarmonicsForThisFundamental.containsValue(harmonic)) {
            probableHigherHarmonicsForThisFundamental.remove(harmonic);
        }
    }

    public void removeFundamentalForThisHamonic(DetectedHarmonic harmonic) {
        if (probableFundamentalsForThisHarmonic.containsKey(harmonic)) {
            probableFundamentalsForThisHarmonic.remove(harmonic);
        }
    }

    public int getNumberOfHigherHarmonics() {
        return this.probableHigherHarmonicsForThisFundamental.size();
    }


    public Map<DetectedHarmonic, Integer> getProbableHigherHarmonicsForThisFundamental() {
        return probableHigherHarmonicsForThisFundamental;
    }

    public Map<DetectedHarmonic, Integer> getProbableFundamentalsForThisHarmonic() {
        return probableFundamentalsForThisHarmonic;
    }

    public int getNumberForHarmonic(DetectedHarmonic harmonic) {
        if (probableHigherHarmonicsForThisFundamental.containsKey(harmonic)) {
            return probableHigherHarmonicsForThisFundamental.get(harmonic);
        }
        return 0; //otherwise
    }

    public int getNumberForFundamental(DetectedHarmonic harmonic) {
        if (probableFundamentalsForThisHarmonic.containsKey(harmonic)) {
            return probableFundamentalsForThisHarmonic.get(harmonic);
        }
        return 0; //otherwise
    }


    public DetectedHarmonic getHarmonicWithNumber(int harmonicNumber) {
        if (probableHigherHarmonicsForThisFundamental.containsValue(harmonicNumber)) {
            for (DetectedHarmonic aHarmonic : probableHigherHarmonicsForThisFundamental.keySet()) {
                if (probableHigherHarmonicsForThisFundamental.get(aHarmonic) == harmonicNumber) {
                    return aHarmonic;
                }
            }
        }

        return null; //otherwise
    }

    public DetectedHarmonic getFundamentalWithNumber(int harmonicNumber) {
        if (probableFundamentalsForThisHarmonic.containsValue(harmonicNumber)) {
            for (DetectedHarmonic aHarmonic : probableFundamentalsForThisHarmonic.keySet()) {
                if (probableFundamentalsForThisHarmonic.get(aHarmonic) == harmonicNumber) {
                    return aHarmonic;
                }
            }
        }

        return null; //otherwise
    }


    public void addHarmonicToIgnoreSet(DetectedHarmonic aHarmonic) {
        if (!harmonicsToIgnore.contains(aHarmonic)) {
            harmonicsToIgnore.add(aHarmonic);
        }
    }

    public boolean isInIgnoreList(DetectedHarmonic harmonic) {
        return harmonicsToIgnore.contains(harmonic);
    }

    public List<DetectedHarmonic> getHarmonicsToIgnore() {
        return harmonicsToIgnore;
    }

    public boolean isAmIHarmonic() {
        return amIHarmonic;
    }

    public void setAmIHarmonic(boolean amIHarmonic) {
        this.amIHarmonic = amIHarmonic;
    }

    public int getFrameIndex() {
        return frameIndex;
    }

    public boolean isAmIFundamental() {
        return amIFundamental;
    }

    public void setAmIFundamental(boolean amIFundamental) {
        this.amIFundamental = amIFundamental;
    }

    public void setSpectralAmplitude(float spectralAmplitude) {
        this.spectralAmplitude = spectralAmplitude;
    }

    public float getSpectralAmplitude() {
        return spectralAmplitude;
    }


}
