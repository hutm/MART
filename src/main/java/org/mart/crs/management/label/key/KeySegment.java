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

package org.mart.crs.management.label.key;

import org.mart.crs.management.label.chord.Root;

/**
 * @version 1.0 5/2/11 3:31 PM
 * @author: Hut
 */
public class KeySegment {


    protected Modality modality;
    protected Root root;


	protected double onset;
	protected double offset;
	protected String label;


    public KeySegment(String label) {
        this(0, 0, label);
    }


    public KeySegment(double onset, double offset, String label) {
        this.onset = onset;
        this.offset = offset;
        this.label = label;
        this.root = Root.fromString(label);
        if(this.label.contains(":min")){
            this.modality = Modality.MINOR;
        } else{
            this.modality = Modality.MAJOR;
        }
    }


    @Override
    public String toString() {
        return String.format("%s%s", root.toString(), modality.toString());
    }

    public Modality getModality() {
        return modality;
    }

    public Root getRoot() {
        return root;
    }

    public double getOnset() {
        return onset;
    }

    public double getOffset() {
        return offset;
    }
}
