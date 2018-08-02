package com.asemenkov.waterinargon;

import com.asemenkov.gromacs.particles.annotations.Atom;
import com.asemenkov.gromacs.particles.GmxAtom;

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
