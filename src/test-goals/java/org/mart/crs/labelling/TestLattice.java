package org.mart.crs.labelling;

import org.mart.crs.management.label.lattice.Lattice;
import junit.framework.TestCase;

/**
 * @version 1.0 Nov 11, 2009 3:27:09 PM
 * @author: Maksim Khadkevich
 */
public class TestLattice extends TestCase {


    public void testReadWriteLattice(){
        Lattice aLattice = new Lattice("data/1.lattice");
        aLattice.storeInFile("data/2.lattice");
    }
}