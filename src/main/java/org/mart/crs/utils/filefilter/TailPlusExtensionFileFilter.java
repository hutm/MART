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

package org.mart.crs.utils.filefilter;

import java.io.File;

/**
 * @version 1.0 7/7/11 3:37 PM
 * @author: Hut
 */
public class TailPlusExtensionFileFilter extends ExtensionFileFilter {

    protected String tail;

    public TailPlusExtensionFileFilter(String extension, String tail) {
        super(extension);
        this.tail = tail;
    }


    public TailPlusExtensionFileFilter(String extension, boolean isToIncludeDirs, String tail) {
        super(extension, isToIncludeDirs);
        this.tail = tail;
    }


    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return isToIncludeDirs;
        }
        String ending = String.format("%s%s", tail, extensions[0]).toLowerCase();
        return (file.getName().toLowerCase().endsWith(ending));
    }
}
