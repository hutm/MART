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

package org.mart.crs.exec.operation.features;

import org.mart.crs.config.Settings;
import org.mart.crs.core.AudioReader;
import org.mart.crs.core.pcp.PCPBuilder;
import org.mart.crs.core.pcp.spectral.PCP;
import org.mart.crs.core.spectrum.SpectrumImpl;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrum;
import org.mart.crs.core.spectrum.reassigned.ReassignedSpectrumHarmonicPart;
import org.mart.crs.exec.operation.Operation;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.HelperArrays;

import java.io.File;

import static org.mart.crs.config.ExecParams._initialExecParameters;

/**
 * @version 1.0 19-Oct-2010 02:38:03
 * @author: Hut
 */
public class ChromaQualityEstimationOperation extends Operation {

    protected int[] goodIndeces = new int[]{0, 4, 7, 12, 16, 19, 23, 24, 26, 28, 31};
//    protected int[] goodIndeces = new int[]{0, 4, 7, 12, 16, 19, 23, 24};

   protected  float[] goodIndecesWeights = new float[]{1, 1, 1, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f};



    public ChromaQualityEstimationOperation(String workingDir) {
        super(workingDir);
    }

    @Override
    public void initialize() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void operate() {
        File[] fileList = (new File(workingDir)).listFiles(new ExtensionFileFilter(Settings.WAV_EXT));

        float[] ratios1 = new float[fileList.length];
        float[] cosines1 = new float[fileList.length];

        float[] ratios2 = new float[fileList.length];
        float[] cosines2 = new float[fileList.length];

        float[] ratios3 = new float[fileList.length];
        float[] cosines3 = new float[fileList.length];

        int counter = 0;
        for (File aFile : fileList) {
            System.out.println(String.format("Processing file %s", aFile.getName()));
            AudioReader reader = new AudioReader(aFile.getPath(), _initialExecParameters.samplingRate);

            SpectrumImpl spectrum1 = new SpectrumImpl(reader.getSamples(), reader.getSampleRate(), _initialExecParameters);
            ReassignedSpectrum spectrum2 = new ReassignedSpectrum(reader.getSamples(), reader.getSampleRate(), _initialExecParameters);
            ReassignedSpectrum spectrum3 = new ReassignedSpectrumHarmonicPart(reader.getSamples(), reader.getSampleRate(), _initialExecParameters.reassignedSpectrogramThreshold, _initialExecParameters);

            float[] pcp1 = getPCPPart(getAveragePCPData(spectrum1));
            float[] pcp2 = getPCPPart(getAveragePCPData(spectrum2));
            float[] pcp3 = getPCPPart(getAveragePCPData(spectrum3));

            float[] template = createTemplate();
            float[] templateCosine = createTemplateCosineMeasure();

            ratios1[counter] = getRatio(pcp1, template);
            ratios2[counter] = getRatio(pcp2, template);
            ratios3[counter] = getRatio(pcp3, template);

            cosines1[counter] = getCosineMeasure(pcp1, templateCosine);
            cosines2[counter] = getCosineMeasure(pcp2, templateCosine);
            cosines3[counter] = getCosineMeasure(pcp3, templateCosine);
            counter++;
        }

        float ratio1Mean = HelperArrays.calculateMean(ratios1);
        float ratio2Mean = HelperArrays.calculateMean(ratios2);
        float ratio3Mean = HelperArrays.calculateMean(ratios3);

        float cosine1Mean = HelperArrays.calculateMean(cosines1);
        float cosine2Mean = HelperArrays.calculateMean(cosines2);
        float cosine3Mean = HelperArrays.calculateMean(cosines3);
        System.out.println("ratio1 mean " + ratio1Mean);
        System.out.println("ratio2 mean " + ratio2Mean);
        System.out.println("ratio3 mean " + ratio3Mean);

        System.out.println("cosine1 mean " + cosine1Mean);
        System.out.println("cosine2 mean " + cosine2Mean);
        System.out.println("cosine3 mean " + cosine3Mean);


    }


    protected float[] getAveragePCPData(SpectrumImpl spectrum) {
        PCP pcp1 = new PCPBuilder(PCPBuilder.BASIC_ALG).setSpectrum(spectrum).build();
        float[][] pcpUnwrapped = pcp1.getPCPUnwrapped();
        return HelperArrays.average(pcpUnwrapped, 0, pcpUnwrapped.length);
    }


    protected float[] createTemplate() {
        float[] template = new float[(_initialExecParameters.endMidiNote - _initialExecParameters.startMidiNote + 1)];
        for (int i : goodIndeces) {
            template[i + 60 - _initialExecParameters.startMidiNote] = 1;
        }
        return template;
    }

    protected float[] createTemplateCosineMeasure() {
        float[] template = new float[(_initialExecParameters.endMidiNote - _initialExecParameters.startMidiNote + 1)];

        for (int i = 0; i < goodIndeces.length; i++) {
            int index = goodIndeces[i];
            template[index + 60 - _initialExecParameters.startMidiNote] = goodIndecesWeights[i];
        }
        return template;
    }


    protected float getRatio(float[] data, float[] template) {
        float[] product = HelperArrays.product(data, template);
        return HelperArrays.sum(product) / HelperArrays.sum(data);
    }

    protected float getCosineMeasure(float[] data, float[] template) {
//        data = HelperArrays.normalizeVector(data);
        float[] product = HelperArrays.product(data, template);
        return HelperArrays.sum(product) / (HelperArrays.module(data) * HelperArrays.module(template));
    }


    protected float[] getPCPPart(float[] pcpFull) {
        float[] out = new float[(_initialExecParameters.endMidiNote - _initialExecParameters.startMidiNote + 1)];
        for (int i = _initialExecParameters.startMidiNote; i <= _initialExecParameters.endMidiNote; i++) {
            out[i - _initialExecParameters.startMidiNote] = pcpFull[i - Settings.START_NOTE_FOR_PCP_UNWRAPPED];
        }
        return out;
    }

    public static void main(String[] args) {
        ChromaQualityEstimationOperation operation = new ChromaQualityEstimationOperation("d:\\work\\notesCmaj\\onlyCMajChords");
        operation.initialize();
        operation.operate();
    }

}
