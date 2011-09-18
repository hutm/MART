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
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.CRSThreadPoolExecutor;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.mart.crs.utils.helper.Helper;
import org.mart.crs.utils.helper.HelperFile;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @version 1.0 12-Oct-2010 21:10:44
 * @author: Hut
 */
public class ConfigMonitorService {

    protected static Logger logger = CRSLogger.getLogger(ConfigMonitorService.class);


    public static final int MAX_SLOTS = Settings.NumberOfParallelThreadsForConfigListenerService;

    public static final String OUT_FOLDER_NAME = "out";

    protected CRSThreadPoolExecutor poolExecutor;

    public ConfigMonitorService(String monitorDirFilePath) {

        poolExecutor = new CRSThreadPoolExecutor(MAX_SLOTS * Settings.numberOfFolds);


        File dirToMonitor = HelperFile.getFile(monitorDirFilePath);
        while (true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            File[] files = dirToMonitor.listFiles(new ExtensionFileFilter(Settings.CFG_EXT, false));
            if (files == null || files.length == 0) {
                continue;
            }
            for (File file : files) {
                if (getNumberOfSlots() + 1 <= MAX_SLOTS) {
                    executeConfig(file);
                }
            }
        }

    }


    protected int getNumberOfSlots() {
        return poolExecutor.getTasks().size();
    }


    protected void executeConfig(final File config) {

        final String outPath = config.getParentFile().getAbsolutePath() + File.separator + OUT_FOLDER_NAME;
        final String outPathForThisConfig = outPath + File.separator + HelperFile.getNameWithoutExtension(config.getName());
        final File outPathoutPathForThisConfig = HelperFile.getFile(outPathForThisConfig);
        if (outPathoutPathForThisConfig.exists()) {
            logger.error("Config already exists... skipping the test.");
            return;
        }


        logger.info(String.format("Running configuration with config file %s", config.getName()));

        HelperFile.createDir(outPathForThisConfig);
        HelperFile.createDir(outPathForThisConfig + File.separator + "logs");

        String configToCopy = outPathForThisConfig + File.separator + config.getName();
        final File destConfig = HelperFile.getFile(configToCopy);
        HelperFile.copyFile(config, destConfig);
        config.delete();

        //Now save libs in a separate folder to prevent from errors when updating jars
        final String newLibsDirName = "lib_" + HelperFile.getNameWithoutExtension(config.getName());
        HelperFile.copyDirectory("lib", newLibsDirName);

        Properties props = new Properties();
        int numberOfFolds = 0;
        String memorySize = "1600m";
        try {
            props.load(new FileInputStream(configToCopy));
            numberOfFolds = Helper.getInt(props, "numberOfFolds");
            String parsedMemSize = Helper.getString(props, "memorySize");
            if (parsedMemSize != null && !parsedMemSize.equals("")) {
                memorySize = parsedMemSize;
            }
        } catch (IOException e) {
            logger.error("Could not parse numberOfFolds from config file");
            logger.error(Helper.getStackTrace(e));
        }


        for (int i = 0; i < numberOfFolds; i++) {
            final int finalI = i;
            final String finalMemorySize = memorySize;
            poolExecutor.runTask(new Runnable() {
                public void run() {
                    try {
                        //Exceptional case here with file separators
                        String specialSeparator = "/";
                        if (System.getProperty("os.name").contains("Windows")) {
                            specialSeparator = "\\/";
                        }

                        String cmd = String.format("java -Xmx%s -DoutPathExternal=%s%s -Dfold=%d -jar ./%s/crs-1.0-SNAPSHOT.jar -c %s", finalMemorySize, outPathForThisConfig, specialSeparator, finalI, newLibsDirName, destConfig);
                        Helper.execCmd(cmd);
                    } catch (Exception e) {
                        logger.error(Helper.getStackTrace(e));
                    }
                }
            });

            //Wait 100 sec for fold to start
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}


