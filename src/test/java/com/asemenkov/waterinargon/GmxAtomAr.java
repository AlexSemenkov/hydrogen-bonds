package com.asemenkov.waterinargon;

import com.asemenkov.gromacs.particles.annotations.Atom;
import com.asemenkov.gromacs.particles.GmxAtom;

/**
 * @author asemenkov
 * @since Apr 17, 2018
 */
@Atom(abbreviations = "Ar")
public class GmxAtomAr extends GmxAtom {

    public GmxAtomAr() {
        super("Argon", "Ar");
    }
}
