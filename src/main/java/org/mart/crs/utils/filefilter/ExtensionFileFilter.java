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

package org.mart.crs.utils.filefilter;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 13.02.2009
 * Time: 13:49:07
 * To change this template use File | Settings | File Templates.
 */
public class ExtensionFileFilter implements FileFilter {

    protected String[] extensions;
    protected boolean isToIncludeDirs = true;

    public ExtensionFileFilter(String[] extensions) {
        this.extensions = extensions;
    }

    public ExtensionFileFilter(String extension) {
        this.extensions = new String[1];
        this.extensions[0] = extension;
    }

    public ExtensionFileFilter(String[] extensions, boolean isToIncludeDirs) {
        this.extensions = extensions;
        this.isToIncludeDirs = isToIncludeDirs;
    }

    public ExtensionFileFilter(String extension, boolean isToIncludeDirs) {
        this(extension);
        this.isToIncludeDirs = isToIncludeDirs;
    }

    public boolean accept(File file) {
        if (file.isDirectory()) {
            return isToIncludeDirs;
        }

        String extension = getExtension(file);
        if (extension != null) {
            for (String ext : extensions) {
                if (ext.equalsIgnoreCase(extension)) {
                    return true;
                }
            }
            return false;
        }

        return false;
    }

    /*
    * Get the extension of a file.
    */

    protected String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i).toLowerCase();
        }
        return ext;
    }
}

