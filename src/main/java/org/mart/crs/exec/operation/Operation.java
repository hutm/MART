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

import static org.mart.crs.exec.scenario.stage.StageParameters.*;
import static org.mart.crs.config.Settings.*;

/**
 * @version 1.0 17-Jun-2010 20:45:40
 * @author: Hut
 */
public abstract class Operation {


    protected String workingDir;


    public Operation(String workingDir) {
        this.workingDir = workingDir;
    }

    public abstract void initialize();

    public abstract void operate();

}
