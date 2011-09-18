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

package org.mart.crs.exec.scenario;

/**
 * @version 1.0 11-Oct-2010 16:43:31
 * @author: Hut
 */
public class BatchScenario {


//    public BatchScenario() {
//
//        final CRSScenario scenario = new CRSScenario(true);
//
//        if (!Settings.onlyAggregateResults) {
//            for (Boolean isEnergySpectrum : Settings.BATCH_IS_ENERGY_SPECTRUM_FOR_CHROMAGRAM) {
//                for (Boolean isToNormalizeFeatureVectors : Settings.BATCH_PCP_NORMALIZATION) {
//                    for (Integer winType : Settings.BATCH_WINDOW_TYPE) {
//                        for (Integer winLength : Settings.BATCH_WINDOW_LENGTHS) {
//                            for (Float overlapping : Settings.BATCH_OVERLAPPING) {
//                                Settings.IS_ENERGY_SPECTRUM_FOR_CHROMAGRAM = isEnergySpectrum;
//                                Settings.isToNormalizeFeatureVectors = isToNormalizeFeatureVectors;
//                                Settings.windowType = winType;
//                                Settings.windowLength = winLength;
//                                Settings.overlapping = overlapping;
//                                String _workingDir = String.format("%s_%s_%s_%d_%d_%3.2f", Settings._workingDir.replace("*", String.valueOf(_foldNumber)),
//                                        isEnergySpectrum, isToNormalizeFeatureVectors, winType, winLength, overlapping);
//
//                                FeaturesManager.reinitializeFeaturesManager();
//                                runTrainingStage(scenario, _workingDir);
//
//                                runTestStage(scenario, _workingDir);
//
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        //Summarize the output results
//        Operation summarize = new ResultsCompactRepresentationOperation(Settings._workingDir.replace("*", ""));
//        summarize.initialize();
//        summarize.operate();
//
//        //Save them to the output folder if necessary
//        if (ConfigSettings.outPathExternal != null && !ConfigSettings.outPathExternal.equals("")) {
//            String workingDirParentPath = (HelperFile.getFile(Settings._workingDir.replace("*", String.valueOf(_foldNumber))).getParentFile()).getPath();
//            HelperFile.copyFile(workingDirParentPath + File.separator + AbstractCRSOperation.SUMMARY_FILENAME, ConfigSettings.outPathExternal + File.separator + AbstractCRSOperation.SUMMARY_FILENAME);
//
//        }
//    }
//
//
//    protected void runTrainingStage(CRSScenario scenario, String _workingDir) {
//        if (Settings.isToTrainModels) {
//            if (Settings.IS_TO_CREATE_LM) {
//                scenario.addOperation(new TrainingLanguageModelsOperation(_workingDir, Settings._waveFilesTrainFileList.replace("*", String.valueOf(_foldNumber))));
//            }
//            if (Settings.isToExtractFeaturesForTrain) {
//                if (Settings.isToUseRefFreq) {
//                    scenario.addOperation(new ReferenceFrequencyExtractionOperation(_workingDir, Settings._waveFilesTrainFileList.replace("*", String.valueOf(_foldNumber))));
//                }
//                scenario.addOperation(new FeaturesExtractionOperation(_workingDir, Settings._waveFilesTrainFileList.replace("*", String.valueOf(_foldNumber)), true, 1));
//            }
//
//            scenario.addOperation(new TrainingAcousticModelsOperation(_workingDir));
//        }
//    }
//
//
//    protected void runTestStage(final CRSScenario scenario, String _workingDir) {
//        if (Settings.isToTestModels) {
//            if (Settings.isToExtractFeaturesForTest) {
//                if (Settings.isToUseRefFreq) {
//                    scenario.addOperation(new ReferenceFrequencyExtractionOperation(_workingDir, Settings._waveFilesTestFileList.replace("*", String.valueOf(_foldNumber))));
//                }
//                scenario.addOperation(new FeaturesExtractionOperation(_workingDir, Settings._waveFilesTestFileList.replace("*", String.valueOf(_foldNumber)), false, 1));
//            }
//
//            CRSThreadPoolExecutor pool = new CRSThreadPoolExecutor(1);
//            for (Integer gaussians : Settings.BATCH_GAUSSIANS) {
//                for (Float penalty : Settings.BATCH_PENALTIES) {
//                    final int gaussiansF = gaussians;
//                    final float penaltyF = penalty;
//                    final String workingDirF = _workingDir;
//                    Runnable runnable = new Runnable() {
//                        public void run() {
//                            if (!Settings.isToUseLMs) {
//                                scenario.addOperation(new RecognizeOperation(workingDirF, gaussiansF, penaltyF));
//                            } else {
//                                scenario.addOperation(new RecognizeConventionalVersionWithLMOperation(workingDirF, gaussiansF, penaltyF));
//                            }
//                        }
//                    };
//                    pool.runTask(runnable);
//                }
//            }
//            pool.waitCompletedAndshutDown();
//        }
//
//    }

}
