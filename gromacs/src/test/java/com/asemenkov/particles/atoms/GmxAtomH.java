package com.asemenkov.particles.atoms;

import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.annotations.Atom;

/**
 * @author asemenkov
 * @since Apr 17, 2018
 */
@Atom(abbreviations = { "HW1", "HW2" })
public class GmxAtomH extends GmxAtom {

    public GmxAtomH() {
        super("Hydrogen", "H");
    }

}
