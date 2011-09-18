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

package org.mart.crs.logging;


import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.ErrorCode;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


/**
 * @version 1.0 Feb 4, 2010 3:30:14 PM
 * @author: Maksim Khadkevich
 */
public class NewFileLogger extends FileAppender {

    protected static Random random;

    public NewFileLogger() {
        if(random == null){
            random = new Random();
        }
    }

    public NewFileLogger(Layout layout, String filename,
                         boolean append, boolean bufferedIO, int bufferSize)
            throws IOException {
        super(layout, filename, append, bufferedIO, bufferSize);
    }

    public NewFileLogger(Layout layout, String filename,
                         boolean append) throws IOException {
        super(layout, filename, append);
    }

    public NewFileLogger(Layout layout, String filename)
            throws IOException {
        super(layout, filename);
    }

    public void activateOptions() {
        if (fileName != null) {
            try {
                fileName = getNewLogFileName();
                setFile(fileName, fileAppend, bufferedIO, bufferSize);
            } catch (Exception e) {
                errorHandler.error("Error while activating log options", e,
                        ErrorCode.FILE_OPEN_FAILURE);
            }
        }
    }

    private String getNewLogFileName() {
        if (fileName != null) {
            final String DOT = ".";
            final String HIPHEN = "_";
            final File logFile = new File(fileName);
            final String fileName = logFile.getName();
            String newFileName = "";

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SS");
            String dateStr = formatter.format(new Date());


            final int dotIndex = fileName.indexOf(DOT);
            if (dotIndex != -1) {
                // the file name has an extension. so, insert the time stamp
                // between the file name and the extension
                newFileName = fileName.substring(0, dotIndex) + HIPHEN
                        + dateStr + DOT
                        + fileName.substring(dotIndex + 1);
            } else {
                // the file name has no extension. So, just append the timestamp
                // at the end.
                newFileName = fileName + HIPHEN + dateStr;
            }
            String outFilePath = logFile.getParent() + File.separator + newFileName + String.format("%d", (int)(random.nextFloat()*100));
            return outFilePath;
        }
        return null;
    }
}
