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

package org.mart.crs.logging;

/**
 * @version 1.0 Feb 2, 2010 11:41:44 AM
 * @author: Maksim Khadkevich
 */
public class CRSException extends Exception {

    /**
     * Constructs an instance of <code>CRSException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public CRSException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>CRSException</code> with the specified detail message and the nested exception.
     *
     * @param msg the detail message.
     * @param ex  The original exception
     */
    public CRSException(String msg, Exception ex) {
        super(msg, ex);
    }

}
