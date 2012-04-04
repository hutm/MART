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

package org.mart.crs.management.label;

import org.apache.log4j.Logger;
import org.mart.crs.logging.CRSLogger;

import java.util.ArrayList;
import java.util.List;

import static org.mart.crs.utils.helper.HelperFile.getPathForFileWithTheSameName;

public class LabelsSource {

    protected static Logger logger = CRSLogger.getLogger(LabelsSource.class);


    protected String path;
    protected boolean isGroundTruth;
    protected String name;
    protected String[] possibleExtensions;


    public LabelsSource(String path, boolean groundTruth, String name, String[] possibleExtensions) {
        this.path = path;
        this.isGroundTruth = groundTruth;
        this.name = name;
        this.possibleExtensions = possibleExtensions;
    }

    public LabelsSource(String path, boolean groundTruth, String name, String possibleExtension) {
        this(path, groundTruth, name, new String[]{possibleExtension});
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isGroundTruth() {
        return isGroundTruth;
    }

    public void setGroundTruth(boolean groundTruth) {
        isGroundTruth = groundTruth;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getFilePathForSong(String song) {
        List<String> foundSources = new ArrayList<String>();
        for (String extension : possibleExtensions) {
            String songPath = getPathForFileWithTheSameName(song, this.path, extension);
            if (songPath != null) {
                foundSources.add(songPath);
            }
        }
        if (foundSources.size() == 0) {
            logger.error(String.format("No label files for %swere found in directory %s", song, path));
            return null;
        }
        if (foundSources.size() > 1) {
            logger.warn(String.format("More than 1 label files for %s were found in directory %s", song, path));
        }
        return foundSources.get(0);
    }

    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && ((LabelsSource) o).getPath().equals(getPath());
    }
}
