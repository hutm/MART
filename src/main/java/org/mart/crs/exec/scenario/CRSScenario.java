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

import org.mart.crs.exec.operation.Operation;

import java.util.LinkedList;

/**
 * @version 1.0 11-Jun-2010 18:09:18
 * @author: Hut
 */
public class CRSScenario {

    protected LinkedList<Operation> operationList;

    protected boolean runAtOnce;

    public CRSScenario(boolean runAtOnce) {
        this.runAtOnce = runAtOnce;
        operationList = new LinkedList<Operation>();
    }

    public void addOperation(Operation operation) {
        if (runAtOnce) {
            operation.initialize();
            operation.operate();
        } else {
            operationList.add(operation);
        }
    }

    public void run() {
        for (Operation operation : operationList) {
            operation.initialize();
            operation.operate();
        }
    }

}
