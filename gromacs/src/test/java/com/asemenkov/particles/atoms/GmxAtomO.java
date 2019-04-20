package com.asemenkov.particles.atoms;

import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.annotations.Atom;

/**
 * @author asemenkov
 * @since Apr 17, 2018
 */
@Atom(abbreviations = "OW")
public class GmxAtomO extends GmxAtom {

    public GmxAtomO() {
        super("Oxygen", "O");
    }
}
