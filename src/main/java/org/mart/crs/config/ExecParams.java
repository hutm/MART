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

package org.mart.crs.config;

import org.mart.crs.utils.ReflectUtils;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.windowing.WindowType;

import java.io.Serializable;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Contains configuration parameters for  a given execution configuration
 *
 * @version 1.0 2/24/11 3:36 PM
 * @author: Hut
 */
public class ExecParams implements Cloneable, Serializable{


    static{
        Settings.initialize();
    }

    /**
     * Public instance of parameters read from config file
     */
    public static ExecParams _initialExecParameters;

    public ExecParams getClone() {
        try {
            return (ExecParams) this.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException(Helper.getStackTrace(e));
        }
    }


    public ExecParams parseExecParamsConfigurationFromString(String inString) {
        ExecParams outExecParams = getClone();

        StringTokenizer tokenizer = new StringTokenizer(inString, Settings.FIELD_SEPARATOR);
        String token;
        while(tokenizer.hasMoreTokens()){
            token = tokenizer.nextToken();
            if(token.contains("_")){
                StringTokenizer innerTokenizer = new StringTokenizer(token, "_");
                String innerTokenParameterName, innerTokenParameterValue;
                while(innerTokenizer.hasMoreTokens()){
                    innerTokenParameterName = innerTokenizer.nextToken();
                    innerTokenParameterValue = innerTokenizer.nextToken();
                    ReflectUtils.setVariableValue(outExecParams, innerTokenParameterName, innerTokenParameterValue);
                }
            }
        }
        return outExecParams;
    }


    public String getParamNamesCommaSeparated(){
        StringBuffer out = new StringBuffer();
        List<String> names = ReflectUtils.getDeclaredFields(this);
        for(String name:names){
            out.append(name).append(",");
        }
        return out.toString();
    }

    public String getParamValuesCommaSeparated(){
        StringBuffer out = new StringBuffer();
        List<String> names = ReflectUtils.getDeclaredFields(this);
        for(String name:names){
            String paramValue = String.valueOf(ReflectUtils.getVariableValueInStringFormat(this, name));
            out.append(paramValue).append(",");
        }
        return out.toString();
    }

    @Override
    /**
     * Two objects are equal if values of all the parameters apart from that, starting from "_" are equal
     */
    public boolean equals(Object obj) {
        ExecParams execParams = (ExecParams) obj;
        boolean equals = true;
        List<String> names = ReflectUtils.getDeclaredFields(this);
        for(String name:names){
            String paramValue ;
            String paramValueComparedTo;
            if (!name.startsWith("_")) {
                paramValue = String.valueOf(ReflectUtils.getVariableValueInStringFormat(this, name));
                paramValueComparedTo = String.valueOf(ReflectUtils.getVariableValueInStringFormat(execParams, name));
                if(paramValue.trim().equalsIgnoreCase(paramValueComparedTo.trim())){
                    equals = false;
                    break;
                }
            }
        }
        return equals;
    }


    //------------------------------Execution parameters section----------------------


    //Paths
    public String _workingDir;
    public String _waveFilesTrainFileList;
    public String _waveFilesTestFileList;

    //HTK
    public int states;
    public int statesBeat;
    public boolean isDiagonal;
    public int gaussianNumber;
    public float penalty;

    public int NBestCalculationLatticeOrder;
    public int latticeRescoringOrder;
    public int standardLmOrder;

    public float lmWeight;
    public float acWeight;
    public float wip;

    //Acoustic Model parameters
    public float samplingRate; // Sampling rate audio sream transformed to
    public int windowLength;  //Window length in samples.
    public int windowLengthBass;  //Window length in samples.
    public float overlapping;     //Overlapping factor.
    public int windowType = WindowType.HANNING_WINDOW.ordinal(); //Window Type
    public float kaiserWindowAlpha;
    public int startMidiNote;
    public int endMidiNote;
    public int startMidiNoteBass;
    public int endMidiNoteBass;


    //Beat extraction settings
    public int startFreqOnsetDetection;
    public int endFreqOnsetDetection;
    public int windowLengthOnsetDetection;
    public float overlappingOnsetDetection;
    public int windowLengthSVFChroma;
    public float overlappingSVFChroma;
    public float beatReasHarmonicPartThreshold;
    public float beatReasPercussivePartThreshold;
    public int contextLengthSVFChroma;
    public int contextLengthSVFChromaLarge;

    //PCP
    public float spectrumMagnitudeRateForChromaCalculation;
    public boolean isToNormalizeFeatureVectors;
    public int pcpAveragingFactor = 5;
    public boolean extractDeltaCoefficients;  //IF set to true, the output feature vectors are double sized by adding dynamics
    public int regressionWindowForDeltaCoefficients;

    //PQMF-based PCP
    public float PQMFBasedSpectrumFrameLength; //in sec
    public boolean isToConsiderHigerPeaks;

    //Reassigned spectrogram-based PCP settings
    public int reassignedSpectrogramType;
    public int numberOfFreqBinsInTheOutputSpectrogram;
    public float reassignedSpectrogramThreshold;
    public String[] featureExtractors; //Way to calculate PCP
    public float[] featureExtractorsWeights; //Way to calculate PCP

    public float maxAllowedDeltaFreq;
    public float maxAllowedDeltaTime;



    //----Batch prameters----------------------------
    //_TRAIN_FEATURES_
    //_TRAIN_MODELS_
    //_TEST_FEATURES_
    //_TEST_RECOGNIZE_


    public int[] _TRAIN_FEATURES_windowLength;
    public float[] _TRAIN_FEATURES_overlapping;

    public int[] _TRAIN_MODELS_statesBeat;

    public float[] _TEST_RECOGNIZE_penalty;
    public int[] _TEST_RECOGNIZE_gaussianNumber;


    public float[] _lmWeights;
    public float[] _acWeights;
    public float[] _wips;

}
