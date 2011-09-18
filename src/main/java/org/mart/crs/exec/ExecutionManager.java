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

package org.mart.crs.exec;

import org.mart.crs.config.Settings;
import org.mart.crs.exec.scenario.MIREXTestScenario;
import org.mart.crs.exec.scenario.MIREXTrainScenario;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;
import org.apache.log4j.Logger;
import org.mart.crs.config.ConfigSettings;
import org.mart.crs.config.ExecParams;

import java.util.ArrayList;
import java.util.List;

import static org.mart.crs.config.Settings.readProperties;


public class ExecutionManager {

    protected static Logger logger = CRSLogger.getLogger(ExecutionManager.class);

    public static final String scenarioPackage = "org.mart.crs.exec.scenario.";


    public static void main(String[] args) {
        ConfigSettings.outPathExternal = System.getProperty("outPathExternal");

        parseArgs(args);
        String scenarioClass = scenarioPackage + Settings.scenario;
        try {
            Class.forName(scenarioClass).newInstance();
        } catch (ClassNotFoundException e) {
            logger.error(String.format("Error instantiating scenario class %s", scenarioClass));
            logger.error(Helper.getStackTrace(e));
        } catch (InstantiationException e) {
            logger.error(String.format("Error instantiating scenario class %s", scenarioClass));
            logger.error(Helper.getStackTrace(e));
        } catch (IllegalAccessException e) {
            logger.error(String.format("Error instantiating scenario class %s", scenarioClass));
            logger.error(Helper.getStackTrace(e));
        } catch (Exception e) {
            logger.error(String.format("Error running scenario class %s", scenarioClass));
            logger.error(Helper.getStackTrace(e));
        } catch (Throwable e) {
            logger.error(String.format("Error running scenario class %s", scenarioClass));
            logger.error(Helper.getStackTrace(e));
            System.exit(1);
        }
    }


    public static void parseArgs(String[] args) {
        List<String> testList = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            if ((args[i].length() == 2) && (args[i].charAt(0) == '-')) {
                switch (args[i].charAt(1)) {
                    case 'h':
                        printOutHelp();
                        System.exit(0);
                    case 'c':
                        ConfigSettings.CONFIG_FILE_PATH = args[++i];
                        logger.info("Using config file " + ConfigSettings.CONFIG_FILE_PATH);
                        readProperties(ConfigSettings.CONFIG_FILE_PATH);
                        break;

                    case 'm':
                        Settings.isMIREX = true;
                        break;

                    case 'q':
                        Settings.isQUAERO = true;
                        break;

                    case 'b':
                        //Run beat detection scenario
                        ExecParams._initialExecParameters._waveFilesTestFileList = "./testList.txt";

                        testList.add(args[++i]);
                        HelperFile.saveCollectionInFile(testList, ExecParams._initialExecParameters._waveFilesTestFileList, false);

                        ConfigSettings.outPathExternal = args[++i];

                        //Delete previously recognized data
                        HelperFile.deleteDirectory(String.format("%s/4-tsPr", ExecParams._initialExecParameters._workingDir));
                        HelperFile.deleteDirectory(String.format("%s/5-testFeat", ExecParams._initialExecParameters._workingDir));
                        HelperFile.deleteDirectory(String.format("%s/6-testRec", ExecParams._initialExecParameters._workingDir));

                        new MIREXTestScenario();
                        System.exit(0);

                    case 'k':
                        //Run key detection scenario
                        ExecParams._initialExecParameters._waveFilesTestFileList = "./testList.txt";
                        testList = new ArrayList<String>();
                        testList.add(args[++i]);
                        HelperFile.saveCollectionInFile(testList, ExecParams._initialExecParameters._waveFilesTestFileList, false);

                        ConfigSettings.outPathExternal = args[++i];

                        //Delete previously recognized data
                        HelperFile.deleteDirectory(String.format("%s/4-tsPr", ExecParams._initialExecParameters._workingDir));
                        HelperFile.deleteDirectory(String.format("%s/5-testFeat", ExecParams._initialExecParameters._workingDir));
                        HelperFile.deleteDirectory(String.format("%s/6-testRec", ExecParams._initialExecParameters._workingDir));

                        new MIREXTestScenario();
                        System.exit(0);


                    //starts service for monitoring input configs and running the corresponding configurations
                    case 's':
                        String monitorPath = (args[++i]);
                        ConfigMonitorService service = new ConfigMonitorService(monitorPath);
                        break;

                    case 't':
                        Settings.isMIREX = true;
                        ExecParams._initialExecParameters._waveFilesTrainFileList = (args[++i]);
                        ExecParams._initialExecParameters._workingDir = (args[++i]);
                        new MIREXTrainScenario();
                        System.exit(0);

                    case 'r':
                        Settings.isMIREX = true;
                        ExecParams._initialExecParameters._waveFilesTestFileList = (args[++i]);
                        if (i + 2 < args.length) {
                            ExecParams._initialExecParameters._workingDir = (args[++i]);
                        }
                        ConfigSettings.outPathExternal = args[++i];

                        logger.info(ExecParams._initialExecParameters._waveFilesTestFileList);
                        logger.info(ExecParams._initialExecParameters._workingDir);
                        logger.info(ConfigSettings.outPathExternal);

                        new MIREXTestScenario();
                        System.exit(0);

                }
            }
        }
    }


    private static void printOutHelp() {
        System.out.println("Usage: java org.mart.crs.ExecutionManager [args]");
        System.out.println("Arguments are:");
        System.out.println("-h Print out this help");
        System.out.println("-c configFilePath : configuration file with all settings");
    }


}
