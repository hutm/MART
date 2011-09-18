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

package org.mart.crs.core.pcp;

import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.core.pcp.comb.PCPCombSpectral;
import org.mart.crs.core.pcp.comb.PCPCombTemporal;
import org.mart.crs.core.pcp.spectral.PCP;
import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.utils.helper.HelperArrays;
import org.mart.crs.core.pcp.spectral.PCPBasic;
import org.mart.crs.core.pcp.spectral.PCPEnhenced;
import org.mart.crs.core.pcp.spectral.PCPHarmonic;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 24-May-2010 19:06:37
 * @author: Hut
 */
public class PCPBuilder {


    public static final int BASIC_ALG = 0;
    public static final int ENHENCED_ALG = 1;
    public static final int HARMONIC_ALG = 2;
    public static final int COMB_TEMPOTAL_ALG = 3;
    public static final int COMB_SPECTRAL_ALG = 4;


    protected int PCPtype;

    protected ExecParams execParams;

    protected double refFreq = Settings.REFERENCE_FREQUENCY;

    protected int numberOfBinsPerSemitone;
    protected boolean isToNormalize;
    protected double frameSizePCP;


    protected int startNoteForPCPUnwrapped;
    protected int endNoteForPCPUnwrapped;

    protected int startNoteForPCPWrapped;
    protected int endNoteForPCPWrapped;
    protected int averaginFactor;

    protected float spectrumMagnitudeRateForChromaCalculation;


    protected SpectrumImpl spectrum;
    protected float[][] pcpData;
    protected boolean isUnwrappedPCPData;

    protected String filterBankConfigFilePath;
    protected String audioFilePath;
    protected float samplingFreqOfAudio;

    protected boolean initializationByParts;


    public PCPBuilder(int PCPType) {
        this.PCPtype = PCPType;
        refFreq = Settings.REFERENCE_FREQUENCY;
        numberOfBinsPerSemitone = 1;
        isToNormalize = true;
        startNoteForPCPUnwrapped = Settings.START_NOTE_FOR_PCP_UNWRAPPED;
        endNoteForPCPUnwrapped = Settings.END_NOTE_FOR_PCP_UNWRAPPED;
        initializationByParts = Settings.initializationByParts;
    }

    public PCPBuilder setExecParams(ExecParams execParams) {
        this.execParams = execParams;
        startNoteForPCPWrapped = execParams.startMidiNote;
        endNoteForPCPWrapped = execParams.endMidiNote;
        averaginFactor = execParams.pcpAveragingFactor;
        spectrumMagnitudeRateForChromaCalculation = execParams.spectrumMagnitudeRateForChromaCalculation;
        return this;
    }

    public PCPBuilder setRefFreq(double refFreq) {
        this.refFreq = refFreq;
        return this;
    }

    public PCPBuilder setAveraginFactor(int averaginFactor) {
        this.averaginFactor = averaginFactor;
        return this;
    }

    public PCPBuilder setNumberOfBinsPerSemitone(int numberOfBinsPerSemitone) {
        this.numberOfBinsPerSemitone = numberOfBinsPerSemitone;
        return this;
    }

    public PCPBuilder setToNormalize(boolean toNormalize) {
        isToNormalize = toNormalize;
        return this;
    }

    public PCPBuilder setStartNoteForPCPUnwrapped(int startNoteForPCPUnwrapped) {
        this.startNoteForPCPUnwrapped = startNoteForPCPUnwrapped;
        return this;
    }

    public PCPBuilder setEndNoteForPCPUnwrapped(int endNoteForPCPUnwrapped) {
        this.endNoteForPCPUnwrapped = endNoteForPCPUnwrapped;
        return this;
    }

    public PCPBuilder setStartNoteForPCPWrapped(int startNoteForPCPWrapped) {
        this.startNoteForPCPWrapped = startNoteForPCPWrapped;
        return this;
    }

    public PCPBuilder setEndNoteForPCPWrapped(int endNoteForPCPWrapped) {
        this.endNoteForPCPWrapped = endNoteForPCPWrapped;
        return this;
    }

    public PCPBuilder setFrameSizePCP(float frameSizePCP) {
        this.frameSizePCP = frameSizePCP;
        return this;
    }

    public PCPBuilder setSpectrum(SpectrumImpl spectrum) {
        this.spectrum = spectrum;
        return this;
    }

    public PCPBuilder setPcpData(float[][] pcpData) {
        this.pcpData = pcpData;
        return this;
    }

    public PCPBuilder setUnwrappedPCPData(boolean unwrappedPCPData) {
        isUnwrappedPCPData = unwrappedPCPData;
        return this;
    }

    public PCPBuilder setFilterBankConfigFilePath(String filterBankConfigFilePath) {
        this.filterBankConfigFilePath = filterBankConfigFilePath;
        return this;
    }

    public PCPBuilder setAudioFilePath(String audioFilePath) {
        this.audioFilePath = audioFilePath;
        return this;
    }

    public PCPBuilder setSamplingFreqOfAudio(float samplingFreqOfAudio) {
        this.samplingFreqOfAudio = samplingFreqOfAudio;
        return this;
    }

    public PCPBuilder setInitializationByParts(boolean initializationByParts) {
        this.initializationByParts = initializationByParts;
        return this;
    }


    public PCPBuilder setSpectrumMagnitudeRateForChromaCalculation(float spectrumMagnitudeRateForChromaCalculation) {
        this.spectrumMagnitudeRateForChromaCalculation = spectrumMagnitudeRateForChromaCalculation;
        return this;
    }

    public double getRefFreq() {
        return refFreq;
    }

    public int getAveraginFactor() {
        return averaginFactor;
    }

    public int getNumberOfBinsPerSemitone() {
        return numberOfBinsPerSemitone;
    }

    public boolean isToNormalize() {
        return isToNormalize;
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

    public double getFrameSizePCP() {
        return frameSizePCP;
    }

    public SpectrumImpl getSpectrum() {
        return spectrum;
    }


    public float[][] getPcpData() {
        return pcpData;
    }


    public boolean isUnwrappedPCPData() {
        return isUnwrappedPCPData;
    }


    public String getFilterBankConfigFilePath() {
        return filterBankConfigFilePath;
    }


    public String getAudioFilePath() {
        return audioFilePath;
    }


    public float getSamplingFreqOfAudio() {
        return samplingFreqOfAudio;
    }


    public int getPCPtype() {
        return PCPtype;
    }


    public float getSpectrumMagnitudeRateForChromaCalculation() {
        return spectrumMagnitudeRateForChromaCalculation;
    }

    public ExecParams getExecParams() {
        return execParams;
    }

    public boolean isInitializationByParts() {
        return initializationByParts;
    }

    public PCP build() {
        if (initializationByParts) {
            int extractionWindowLength = Math.round(SpectrumImpl.SEGMENT_SIZE_FOR_MEMORY_OPTIMIZED_EXTRACTION * spectrum.getSampleRateSpectrum() / averaginFactor) * averaginFactor;

            int counter = 0;
            List<float[][]> pcpData = new ArrayList<float[][]>();
            SpectrumImpl originalSpectrum = spectrum;
            PCP pcp = null;
            while (counter < originalSpectrum.getNumberOfFrames()) {
                int endIndex = Math.min(counter + extractionWindowLength, originalSpectrum.getNumberOfFrames());
                SpectrumImpl partSpectrum = originalSpectrum.extractSpectrumPart(counter, endIndex);
                pcp = this.setSpectrum(partSpectrum).setInitializationByParts(false).build();
                pcpData.add(pcp.getPCPUnwrapped());
                counter += extractionWindowLength;
            }
            float[][] concanatedData = HelperArrays.concat(pcpData);
            pcp.setPcpUnwrapped(concanatedData);
            return pcp;
        }
        switch (PCPtype) {
            case BASIC_ALG:
                return new PCPBasic(this);
            case ENHENCED_ALG:
                return new PCPEnhenced(this);
            case HARMONIC_ALG:
                return new PCPHarmonic(this);
            case COMB_TEMPOTAL_ALG:
                return new PCPCombTemporal(this);
            case COMB_SPECTRAL_ALG:
                return new PCPCombSpectral(this);
            default:
                return null;
        }
    }
}


