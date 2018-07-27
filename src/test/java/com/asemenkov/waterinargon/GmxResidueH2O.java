package com.asemenkov.waterinargon;

import com.asemenkov.gromacs.annotations.Acceptor;
import com.asemenkov.gromacs.annotations.Donor;
import com.asemenkov.gromacs.annotations.Pivot;
import com.asemenkov.gromacs.annotations.PivotDeltas;
import com.asemenkov.gromacs.annotations.Residue;
import com.asemenkov.gromacs.annotations.ResidueAtom;
import com.asemenkov.gromacs.particles.GmxResidue;

/**
 * @author asemenkov
 * @since Apr 17, 2018
 */
@Residue("SOL")
public class GmxResidueH2O extends GmxResidue {

    @Acceptor
    @ResidueAtom("OW")
    @Pivot
    public GmxAtomO ow;

    @Donor
    @ResidueAtom("HW1")
    @PivotDeltas(x = 0.035f, y = 0.031f, z = 0.089f)
    public GmxAtomH hw1;

    @Donor
    @ResidueAtom("HW2")
    @PivotDeltas(x = -0.098f, y = 0.017f, z = -0.004f)
    public GmxAtomH hw2;

    public GmxResidueH2O() {
        super("Water", "SOL");
    }

}
