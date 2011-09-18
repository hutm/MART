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

package org.mart.crs.utils;

import org.mart.crs.config.Settings;
import org.mart.crs.exec.scenario.BatchParameter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 1/27/11 5:15 PM
 * @author: Hut
 */
public class Combinator {

    protected List<BatchParameter> batchParameterList;

    protected int[] divisors;

    protected int[] lengths;

    protected int numberOfCombinations;

    /**
     * Index of the current combination
     */
    protected int currentCombinationIndex;

    public Combinator(int[] lengths) {
        this.lengths = lengths;
        initialize();
    }

    public Combinator(List<BatchParameter> batchParameterList) {
        List<BatchParameter> batchParameterListFiltered = new ArrayList<BatchParameter>();
        for(BatchParameter parameter:batchParameterList){
            if(parameter.getArray() != null){
                batchParameterListFiltered.add(parameter);
            }
        }

        this.batchParameterList = batchParameterListFiltered;
        this.lengths = new int[batchParameterListFiltered.size()];
        for (int i = 0; i < batchParameterListFiltered.size(); i++) {
            lengths[i] = Array.getLength(batchParameterListFiltered.get(i).getArray());
        }
        initialize();
    }

    public void initialize() {
        divisors = new int[lengths.length + 1];
        divisors[divisors.length - 1] = 1;
        for (int i = lengths.length - 1; i >= 0; i--) {
            divisors[i] = lengths[i] * divisors[i + 1];
        }

        numberOfCombinations = divisors[0];
        currentCombinationIndex = 0;
    }


    public int[] getIndexArray(int combinationIndex) {
        int[] output = new int[lengths.length];
        int rest = combinationIndex;
        for (int i = 0; i < output.length; i++) {
            output[i] = rest / divisors[i + 1];
            rest = rest % divisors[i + 1];
        }
        return output;
    }


    public String setNextConfiguration() {
        int[] parameterIndexes = getIndexArray(currentCombinationIndex++);
        StringBuffer outStringBuffer = new StringBuffer();
        for (int i = 0; i < batchParameterList.size(); i++) {
            batchParameterList.get(i).setParameterWithIndex(parameterIndexes[i]);
            outStringBuffer.append(Settings.FIELD_SEPARATOR).append(batchParameterList.get(i).getStringWithValue(parameterIndexes[i]));
        }
        return outStringBuffer.toString();
    }


    public String setNextConfiguration(Object targetClassOrInstance) {
        int[] parameterIndexes = getIndexArray(currentCombinationIndex++);
        StringBuffer outStringBuffer = new StringBuffer();
        for (int i = 0; i < batchParameterList.size(); i++) {
            batchParameterList.get(i).setParameterWithIndex(parameterIndexes[i], targetClassOrInstance);
            outStringBuffer.append(Settings.FIELD_SEPARATOR).append(batchParameterList.get(i).getStringWithValue(parameterIndexes[i]));
        }
        return outStringBuffer.toString();
    }


    public int getCurrentCombinationIndex() {
        return currentCombinationIndex;
    }

    public boolean hasMoreCombinations() {
        return currentCombinationIndex < numberOfCombinations;
    }

    public int getNumberOfCombinations() {
        return numberOfCombinations;
    }
}
