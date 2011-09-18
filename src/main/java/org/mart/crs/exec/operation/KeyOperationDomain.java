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

package org.mart.crs.exec.operation;

import org.mart.crs.config.ExecParams;
import org.mart.crs.config.Settings;
import org.mart.crs.exec.operation.models.htk.parser.chord.ChordHTKParser;
import org.mart.crs.exec.operation.models.test.key.RecognizeKeyOperation;
import org.mart.crs.exec.scenario.stage.StageParameters;

/**
 * @version 1.0 5/6/11 6:01 PM
 * @author: Hut
 */
public class KeyOperationDomain extends ChordOperationDomain {

    public KeyOperationDomain() {
        Settings.labelsGroundTruthDir = Settings.keyLabelsGroundTruthDir;
        ChordHTKParser.FEATURE_SAMPLE_RATE = 10000;
    }

     @Override
    public AbstractCRSOperation getRecognizeOperation(StageParameters stageParameters, ExecParams execParams) {
        return new RecognizeKeyOperation(stageParameters, execParams);
    }

    @Override
    public AbstractCRSOperation getRecognizeLanguageModelOperation(StageParameters stageParameters, ExecParams execParams) {
        return new RecognizeKeyOperation(stageParameters, execParams);          //TODO investigate use of LM here in the future
    }


}
