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

import java.util.ArrayList;
import java.util.List;

import static org.mart.crs.utils.helper.HelperFile.getPathForFileWithTheSameName;

/**
 * @version 1.0 Sep 23, 2009 11:24:44 AM
 * @author: Maksim Khadkevich
 */
public class LabelsManager {

    public static final int NUMBER_OF_LABEL_SOURCES = 3;

    protected LabelsSource[] labelsSources;

    protected String[] possibleLabelExtensions;

    public LabelsManager(String[] possibleLabelExtensions) {
        labelsSources = new LabelsSource[NUMBER_OF_LABEL_SOURCES];
        this.possibleLabelExtensions = possibleLabelExtensions;
    }





    public List<LabelsSource> getLabelSourcesForFile(String fileName) {
        String path;
        List<LabelsSource> labelsList = new ArrayList<LabelsSource>();

        for (LabelsSource labelsSource : labelsSources) {
            if (labelsSource != null) {
                for (String extension : possibleLabelExtensions) {
                    path = getPathForFileWithTheSameName(fileName, labelsSource.getPath(), extension);
                    if (path != null) {
                        labelsList.add(labelsSource);
                    }
                }
            }
        }
        return labelsList;
    }


    public LabelsSource[] getLabelsSources() {
        return labelsSources;
    }
}

