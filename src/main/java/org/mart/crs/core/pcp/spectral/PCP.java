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

package org.mart.crs.core.pcp.spectral;

import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.core.pcp.PCPBin;
import org.mart.crs.core.pcp.PCPBuilder;
import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrum;
import org.mart.crs.logging.CRSException;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.management.label.chord.Root;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperArrays;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import static org.mart.crs.config.Settings.NUMBER_OF_SEMITONES_IN_OCTAVE;
import static org.mart.crs.config.Settings.START_NOTE_FOR_PCP_UNWRAPPED;

/**
 * User: Hut
 * Date: 17.06.2008
 * Time: 22:56:27
 * Performs operations with extracting PCP from spectrum
 */
public abstract class PCP {


    protected static Logger logger = CRSLogger.getLogger(PCP.class);

    protected ExecParams execParams;

    protected float[][] pcp;
    protected float[][] pcpUnwrapped;

    protected float sampleRatePCP;
    protected double frameSizePCP;

    protected double refFreq;
    protected int averagingFactor;
    protected int numberOfBinsPerSemitone;
    protected boolean isToNormalize;

    protected int startNoteForPCPUnwrapped;
    protected int endNoteForPCPUnwrapped;

    protected int startNoteForPCPWrapped;
    protected int endNoteForPCPWrapped;

    protected float chromaSpectrumRate;


    public PCP(PCPBuilder builder) {
        this.execParams = builder.getExecParams();
        this.refFreq = builder.getRefFreq();
        this.averagingFactor = builder.getAveraginFactor();
        this.numberOfBinsPerSemitone = builder.getNumberOfBinsPerSemitone();
        this.isToNormalize = builder.isToNormalize();
        this.startNoteForPCPUnwrapped = builder.getStartNoteForPCPUnwrapped();
        this.startNoteForPCPWrapped = builder.getStartNoteForPCPWrapped();
        this.endNoteForPCPUnwrapped = builder.getEndNoteForPCPUnwrapped();
        this.endNoteForPCPWrapped = builder.getEndNoteForPCPWrapped();
        this.chromaSpectrumRate = builder.getSpectrumMagnitudeRateForChromaCalculation();
        setFrameSizePCP(builder.getFrameSizePCP());

        try {
            init(builder);
        } catch (CRSException e) {
            logger.error(Helper.getStackTrace(e));
        }

        if (averagingFactor > 1 && !Settings.saveNoResolutionRepresentation) {
            performPCPDownsampling();
        }
        calculatePCPWrapped();
    }

    protected void init(PCPBuilder builder) throws CRSException {
        if (builder.getSpectrum() != null) {
            if (!Settings.saveNoResolutionRepresentation) {
                init(builder.getSpectrum());
            } else{
                initReasVersionNoResolution((ReassignedSpectrum) builder.getSpectrum());
            }
            return;
        }
        if (builder.getPcpData() != null) {
            init(builder.getPcpData(), builder.isUnwrappedPCPData());
            return;
        }
        throw new CRSException("Could not instantiate PCP from builder: not enough data");
    }


    protected void init(SpectrumImpl spectrum) {
        int numberOfFrames = spectrum.getMagSpec().length;
        setSampleRatePCP(spectrum.getSampleRateSpectrum());
        pcpUnwrapped = new float[numberOfFrames][getNumberOfBinsForUnwrappedChroma()];
        for (int i = 0; i < numberOfFrames; i++) {
            pcpUnwrapped[i] = calculateChromagram(spectrum, i, refFreq);
        }
    }


    protected void initReasVersionNoResolution(ReassignedSpectrum spectrum) {
        spectrum.getMagSpec();
        float[][] energyValues = spectrum.getEnergyReasValues();
        float[][] timeInstants = spectrum.getTimeReasValues();
        float[][] freqValues = spectrum.getFrequencyReasValues();
        int numberOfFrames = energyValues.length / averagingFactor + 1;
        setSampleRatePCP(spectrum.getSampleRateSpectrum() / averagingFactor);
        pcpUnwrapped = new float[numberOfFrames][getNumberOfBinsForUnwrappedChroma()];
        for (int i = 0; i < timeInstants.length; i++) {
            for (int j = 0; j < energyValues[i].length; j++) {
                int pcpBinTime = Math.round(sampleRatePCP * timeInstants[i][j] / execParams.samplingRate);

                float midiNoteIndexFloat = Helper.getMidiNoteForFreq(freqValues[i][j], refFreq);
                float chromaNoteIndexFloat = midiNoteIndexFloat - START_NOTE_FOR_PCP_UNWRAPPED;
                int chromaBinIndex = calcualteChromaBinIndex(chromaNoteIndexFloat);

                //Prevent from IndexOutOfBoundException
                if (chromaBinIndex >= 0 && chromaBinIndex < pcpUnwrapped[0].length && pcpBinTime >=0 && pcpBinTime < pcpUnwrapped.length) {
                    pcpUnwrapped[pcpBinTime][chromaBinIndex] += energyValues[i][j];
                }
            }
        }
    }


    protected void init(float[][] pcp, boolean isUnwrapped) {
        if (isUnwrapped) {
            setPcpUnwrapped(pcp);
        } else {
            this.pcp = pcp;
        }
    }

    public void setPcpUnwrapped(float[][] pcpUnwrapped) {
        this.pcpUnwrapped = new float[pcpUnwrapped.length][pcpUnwrapped[0].length];
        for (int i = 0; i < pcpUnwrapped.length; i++) {
            this.pcpUnwrapped[i] = pcpUnwrapped[i];
        }
        calculatePCPWrapped();
    }


    protected void performPCPDownsampling() {
        //Now downsample pcpList by averaging
        int numberOfFrames = pcpUnwrapped.length / averagingFactor;
        setSampleRatePCP(sampleRatePCP / averagingFactor);

        float[][] pcpUnwrappedDownsampled = new float[numberOfFrames][getNumberOfBinsForUnwrappedChroma()];
        for (int i = 0; i < numberOfFrames; i++) {
            int startIndex = i * averagingFactor;
            int endIndex = (i + 1) * averagingFactor;
            pcpUnwrappedDownsampled[i] = HelperArrays.average(pcpUnwrapped, startIndex, endIndex);
        }
        this.pcpUnwrapped = pcpUnwrappedDownsampled;
    }


    /**
     * calculates chromagram
     *
     * @param spectrum   Input spectrum
     * @param frameIndex Frame index in the spectrum
     * @param refFreq    reference frequency
     * @return spectrum
     */
    protected float[] calculateChromagram(SpectrumImpl spectrum, int frameIndex, double refFreq) {
        float[] magSpectrFrame = spectrum.getMagSpec()[frameIndex];

        float spectralResolution = spectrum.getFrequencyResolution();

        //First transform magnitudes to energy if necessary
        float[] energySpectrFrame = Arrays.copyOf(magSpectrFrame, magSpectrFrame.length);
        if (chromaSpectrumRate != 1) {
            energySpectrFrame = HelperArrays.pow(energySpectrFrame, chromaSpectrumRate);
        }

        //If it is modified version of PCP, make preprocessing
        energySpectrFrame = preProcessEnergySpectrumFrame(energySpectrFrame, spectralResolution);

        //Now prepare output chroma
        float[] chromaUnwrapped = new float[getNumberOfBinsForUnwrappedChroma()];

        float midiNoteIndexFloat, chromaNoteIndexFloat;
        int chromaBinIndex;
        for (int i = 1; i < energySpectrFrame.length; i++) {
            midiNoteIndexFloat = Helper.getMidiNoteForFreq(spectrum.index2freq(i), refFreq);
            chromaNoteIndexFloat = midiNoteIndexFloat - START_NOTE_FOR_PCP_UNWRAPPED;
            chromaBinIndex = calcualteChromaBinIndex(chromaNoteIndexFloat);

            //Prevent from IndexOutOfBoundException
            if (chromaBinIndex >= 0 && chromaBinIndex < chromaUnwrapped.length) {
                chromaUnwrapped[chromaBinIndex] += energySpectrFrame[i];
            }
        }

        return chromaUnwrapped;
    }

    /**
     * Maps chromaNote to ChromaBin
     *
     * @param chromaNoteIndex
     * @return
     */
    protected int calcualteChromaBinIndex(float chromaNoteIndex) {
        return Math.round(chromaNoteIndex * numberOfBinsPerSemitone);
    }


    protected int getNumberOfBinsForUnwrappedChroma() {
        return (endNoteForPCPUnwrapped - startNoteForPCPUnwrapped + 1) * numberOfBinsPerSemitone;
    }

    protected int getNumberOfBinsForWrappedChroma() {
        return NUMBER_OF_SEMITONES_IN_OCTAVE * numberOfBinsPerSemitone;
    }


    /**
     * Preprocesses spectrum. Used in Harmonic PCP and Enhenced PCP.
     *
     * @param spectrum
     * @param spectalResolution
     * @return
     */
    protected abstract float[] preProcessEnergySpectrumFrame(float[] spectrum, float spectalResolution);


    protected void calculatePCPWrapped() {
        if (pcpUnwrapped == null) {
            return;
        }
        pcp = new float[pcpUnwrapped.length][getNumberOfBinsForWrappedChroma()];
        for (int i = 0; i < pcpUnwrapped.length; i++) {
            pcp[i] = calculatePCPWrapped(pcpUnwrapped[i]);
        }
    }


    private float[] calculatePCPWrapped(float[] chromaUnwrapped) {
        float[] outChroma = new float[getNumberOfBinsForWrappedChroma()];
        int index;
        int startIndexForChromaWrapped = (startNoteForPCPWrapped - startNoteForPCPUnwrapped) * numberOfBinsPerSemitone;
        int endIndexForChromaWrapped = (endNoteForPCPWrapped - startNoteForPCPUnwrapped) * numberOfBinsPerSemitone;
        for (int i = startIndexForChromaWrapped; i < endIndexForChromaWrapped; i++) {
            index = getIndexForBinInUnwrappedChroma(i);
            outChroma[index] += chromaUnwrapped[i];
        }
        return outChroma;
    }


    /**
     * Returns index of Tone in wrapped chroma representation
     *
     * @param index Index in unwraped chroma representation
     * @return index
     */
    public int getIndexForBinInUnwrappedChroma(int index) {
        return HelperArrays.transformIntValueToBaseRange(index + startNoteForPCPUnwrapped * numberOfBinsPerSemitone, getNumberOfBinsForWrappedChroma());
    }


    /**
     * Returns Tone name for index in unwrapped chroma
     *
     * @param index
     * @return
     */
    public PCPBin getStringForBinInUnwrappedChroma(int index) {
        int noteIndex = (index + startNoteForPCPUnwrapped * numberOfBinsPerSemitone) / numberOfBinsPerSemitone % Settings.NUMBER_OF_SEMITONES_IN_OCTAVE;
        int octave = (index + startNoteForPCPUnwrapped * numberOfBinsPerSemitone) / numberOfBinsPerSemitone / Settings.NUMBER_OF_SEMITONES_IN_OCTAVE - 1;
        String note = Root.values()[noteIndex].getName();
        int noteSubIndex;
        if (numberOfBinsPerSemitone == 1) {
            noteSubIndex = -1;
        } else {
            noteSubIndex = index % numberOfBinsPerSemitone;
        }
        return new PCPBin(note, noteSubIndex, octave);
    }


    /**
     * Returns Tone name for index in unwrapped chroma
     *
     * @param index
     * @return
     */
    public PCPBin getStringForBinInWrappedChroma(int index) {
        return new PCPBin(Root.values()[getIndexForBinInUnwrappedChroma(index)].getName(), -1, 0);
    }


    public Map<PCPBin, Float> getDataForDisplaying(float timeInstant, boolean isUnwrapped) {
        Map<PCPBin, Float> outMap = new TreeMap<PCPBin, Float>();
        float[] data = getPCPVectorValue(timeInstant, isUnwrapped);
        PCPBin pcpBin;
        if (isUnwrapped) {
            for (int i = 0; i < endNoteForPCPUnwrapped - startNoteForPCPUnwrapped; i++) {
                pcpBin = getStringForBinInUnwrappedChroma(i);
                outMap.put(pcpBin, data[i]);
            }
        } else {
            for (int i = 0; i < data.length; i++) {
                pcpBin = getStringForBinInWrappedChroma(i);
                outMap.put(pcpBin, data[i]);
            }
        }
        return outMap;
    }


    public float[][] rotatePCP(Root chordNameFrom, Root chordNameTo) {
        return rotatePCP(this.pcp, numberOfBinsPerSemitone, chordNameFrom, chordNameTo);
    }


    public static float[][] rotatePCP(float[][] inData, int numberOfBinsPerSemitone, Root chordNameFrom, Root chordNameTo, boolean isToPutFromEndToBeginning) {
        int shift;
        int rootFromIndex = chordNameFrom.ordinal();
        int rootToIndex = chordNameTo.ordinal();
        shift = (rootToIndex - rootFromIndex) * numberOfBinsPerSemitone;
        shift = HelperArrays.transformIntValueToBaseRange(shift, numberOfBinsPerSemitone * Settings.NUMBER_OF_SEMITONES_IN_OCTAVE);
        float[][] shiftedPCP = shiftPCP(inData, shift, isToPutFromEndToBeginning);
        return shiftedPCP;
    }

    public static float[][] rotatePCP(float[][] inData, int numberOfBinsPerSemitone, Root chordNameFrom, Root chordNameTo) {
        return rotatePCP(inData, numberOfBinsPerSemitone, chordNameFrom, chordNameTo, true);
    }

    public static float[][] shiftPCP(float[][] pcp, int shift, boolean isToPutFromEndToBeginning) {
        if (pcp.length == 0 || pcp[0].length == 0) {
            //Skip rotation
            logger.debug(String.format("Could not rotate features... because vector length is zero"));
            return pcp;
        }
        float[][] shiftedPCP = new float[pcp.length][pcp[0].length];
        int index;
        for (int i = 0; i < pcp.length; i++) {
            for (int j = 0; j < pcp[0].length; j++) {
                index = j + shift;
                if (!isToPutFromEndToBeginning && (index < 0 || index >= shiftedPCP[i].length)) {
                    //In this case just eliminate this value
                    continue;
                }
                index = HelperArrays.transformIntValueToBaseRange(index, pcp[0].length);
                shiftedPCP[i][index] = pcp[i][j];
            }
        }
        return shiftedPCP;  //To change body of created methods use File | Settings | File Templates.
    }


    //TODO substitute routines from the method
    public static float[] shiftPCP(float[] pcp, int shift, boolean isToPutFromEndToBeginning){
        float[] shiftedPCP = new float[pcp.length];
        int index;
        for (int j = 0; j < pcp.length; j++) {
            index = j + shift;
            if (!isToPutFromEndToBeginning && (index < 0 || index >= shiftedPCP.length)) {
                //In this case just eliminate this value
                continue;
            }
            index = HelperArrays.transformIntValueToBaseRange(index, pcp.length);
            shiftedPCP[index] = pcp[j];
        }
        return shiftedPCP;
    }

    public static float[][] shiftPCP(float[][] pcp, int shift) {
        return shiftPCP(pcp, shift, true);
    }


    public float[] getPCPVectorValue(float timeMoment, boolean isUnWrapped) {
        int index = getIndexForTimeMoment(timeMoment);
        float[] result;
        if (isUnWrapped) {
            result = Arrays.copyOf(pcpUnwrapped[index], pcpUnwrapped[index].length);
        } else {
            result = Arrays.copyOf(pcp[index], pcp[index].length);
        }
        return result;
    }


    public float[][] getPcpSegment(float startTime, float endTime, boolean isWrapped) {
        int startIndex = getIndexForTimeMoment(startTime);
        int endIndex = getIndexForTimeMoment(endTime);
        float[][] out = new float[endIndex - startIndex + 1][];
        for (int i = startIndex; i <= endIndex; i++) {
            if (isWrapped) {
                out[i - startIndex] = pcp[i];
            } else {
                out[i - startIndex] = pcpUnwrapped[i];
            }
        }
        return out;
    }


    protected int getIndexForTimeMoment(float timeMoment) {
        int index = (int) Math.floor(timeMoment * sampleRatePCP);
        if (index >= pcp.length) {
            index = pcp.length - 1;
        }
        return index;
    }


    /**
     * This method extracts only those bins that contain notes corresponding to the given chord
     *
     * @param chordNumber
     * @return
     */
    public float[][] getPCPForChord(int chordNumber) {
        float[][] out = new float[pcp.length][3];
        int[] notes = new int[3];
        notes[0] = chordNumber % 12;
        if (chordNumber < 12) {
            notes[1] = (chordNumber + 4) % 12;
        } else {
            notes[1] = (chordNumber + 3) % 12;
        }
        notes[2] = (chordNumber + 7) % 12;
        for (int i = 0; i < out.length; i++) {
            for (int j = 0; j < 3; j++) {
                out[i][j] = pcp[i][notes[j]];
            }
        }
        return out;
    }


    public float[][] getPCPUnwrapped() {
        return pcpUnwrapped;
    }

    public float[][] getPCP() {
        return pcp;
    }

    public void setSampleRatePCP(float sampleRatePCP) {
        this.sampleRatePCP = sampleRatePCP;
        this.frameSizePCP = 1 / sampleRatePCP;
    }

    public void setFrameSizePCP(double frameSizePCP) {
        this.frameSizePCP = frameSizePCP;
        this.sampleRatePCP = (float) (1 / frameSizePCP);
    }

    public float getSampleRatePCP() {
        return sampleRatePCP;
    }

    public double getFrameSizePCP() {
        return frameSizePCP;
    }

    public int getStartNoteForPCPUnwrapped() {
        return startNoteForPCPUnwrapped;
    }

    public int getEndNoteForPCPUnwrapped() {
        return endNoteForPCPUnwrapped;
    }

    public int getStartNoteForPCPWrapped() {
        return startNoteForPCPWrapped;
    }

    public int getEndNoteForPCPWrapped() {
        return endNoteForPCPWrapped;
    }

    public int getNumberOfBinsPerSemitone() {
        return numberOfBinsPerSemitone;
    }

    public int getNumerOfBinsInWrappedChromagram() {
        return numberOfBinsPerSemitone * Settings.NUMBER_OF_SEMITONES_IN_OCTAVE;
    }
}
