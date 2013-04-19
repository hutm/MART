/*
 * Copyright (c) 2008-2013 Maksim Khadkevich and Fondazione Bruno Kessler.
 *
 * This file is part of MART.
 * MART is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2, as published
 * by the Free Software Foundation.
 *
 * MART is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with MART; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.mart.crs.management.label;

import org.mart.crs.config.Extensions;

import java.util.ArrayList;
import java.util.List;

import static org.mart.crs.utils.helper.HelperFile.getPathForFileWithTheSameName;

/**
 * @version 1.0 Sep 23, 2009 11:24:44 AM
 * @author: Maksim Khadkevich
 */
public class LabelsManager {

    public static int NUMBER_OF_LABEL_SOURCES = 3;
    public static final String RECOGNIZE_NOW_DIR = "recognizeNow";

    protected LabelsSource[] labelsSources;

    protected String[] possibleLabelExtensions;

    public LabelsManager(String[] possibleLabelExtensions) {
        labelsSources = new LabelsSource[NUMBER_OF_LABEL_SOURCES + 1];
        labelsSources[NUMBER_OF_LABEL_SOURCES] = new LabelsSource(RECOGNIZE_NOW_DIR, false, "RecognizeNow!!!", new String[]{Extensions.LABEL_EXT});
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

