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

/**
 * @version 1.0 Dec 3, 2010 3:44:28 PM
 * @author: Hut
 */
public enum OperationType {

    CHORD_OPERATION("chord", ChordOperationDomain.class),
    CHORD_OPERATION_FULL_TRAIN("chordFullTrain", ChordFullTrainingOperationDomain.class),
    ONSET_OPERATION("onset", OnsetOperationDomain.class),
    BEAT_OPERATION("beat", BeatOperationDomain.class),
    BEAT_ONLY_OPERATION("beatOnly", BeatOnlyOperationDomain.class),
    KEY_OPERATION("key", KeyOperationDomain.class);


    protected String operationName;
    protected Class<? extends OperationDomain> operationDomainClass;


    public OperationDomain getOperationDomain(AbstractCRSOperation crsOperation) {
        OperationDomain operationDomain = null;
        try {
            operationDomain = operationDomainClass.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not instantiate operation domain " + operationDomainClass);
        }
        if (crsOperation != null) {
            operationDomain.setCrsOperation(crsOperation);
        }
        return operationDomain;
    }

    public OperationDomain getOperationDomain() {
        return this.getOperationDomain(null);
    }


    OperationType(String text, Class operationDomainClass) {
        this.operationName = text;
        this.operationDomainClass = operationDomainClass;
    }


    public static OperationType fromString(String text) {
        if (text != null) {
            for (OperationType b : OperationType.values()) {
                if (text.equalsIgnoreCase(b.operationName)) {
                    return b;
                }
            }
        }
        return null;
    }

}
