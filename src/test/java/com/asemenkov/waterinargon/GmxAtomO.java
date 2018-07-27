package com.asemenkov.waterinargon;

import com.asemenkov.gromacs.annotations.Atom;
import com.asemenkov.gromacs.particles.GmxAtom;

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
