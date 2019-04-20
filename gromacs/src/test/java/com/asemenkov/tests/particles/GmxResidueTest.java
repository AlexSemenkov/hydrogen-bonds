package com.asemenkov.tests.particles;

import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;
import com.asemenkov.gromacs.particles.utils.GmxAnglePredicate;
import com.asemenkov.gromacs.particles.utils.GmxAtomUtils;
import com.asemenkov.gromacs.particles.utils.GmxResidueUtils;
import com.asemenkov.particles.atoms.GmxAtomH;
import com.asemenkov.particles.atoms.GmxAtomO;
import com.asemenkov.particles.residues.GmxResidueH2O;
import com.asemenkov.tests.config.GmxAbstractTest;
import com.asemenkov.utils.io.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author asemenkov
 * @since May 10, 2018
 */
@Test
public class GmxResidueTest extends GmxAbstractTest {

    private static final float[][] H2O_COORDINATES = { //
            { 3.970f, 4.455f, 4.689f }, //
            { 4.005f, 4.486f, 4.778f }, //
            { 3.872f, 4.472f, 4.685f } };

    @Test
    public void testResidueCreation() {
        GmxAtom[] water = new GmxAtom[3];
        water[0] = atomFactory.get(GmxAtomO.class, "O", 0, H2O_COORDINATES[0]);
        water[1] = atomFactory.get(GmxAtomH.class, "H1", 1, H2O_COORDINATES[1]);
        water[2] = atomFactory.get(GmxAtomH.class, "H2", 2, H2O_COORDINATES[2]);
        GmxResidue h2o = residueByAtomsFactory.get(GmxResidueH2O.class, 100, water);
        Logger.log(h2o);

        Assert.assertNotNull(h2o);
        Assert.assertEquals(h2o.getAbbreviation(), "SOL", "Wrong abbreviation.");
        Assert.assertEquals(h2o.getFullName(), "Water", "Wrong full name.");
        Assert.assertEquals(h2o.getResidueNo(), 100, "Wrong residue number.");
        Assert.assertEquals(h2o.getPivotAtom(), water[0], "Wrong residue pivot atom.");
        Assert.assertEquals(h2o.getAcceptorAtom(), water[0], "Wrong residue acceptor atom.");

        Assert.assertEquals(h2o.getDonorAtoms().length, 2, "Wrong number of donor atoms.");
        Assert.assertEquals(h2o.getDonorAtoms()[0], water[1], "Wrong residue donor atom.");
        Assert.assertEquals(h2o.getDonorAtoms()[1], water[2], "Wrong residue donor atom.");

        Assert.assertEquals(h2o.getAllAtoms().length, 3, "Wrong total number of residue atoms.");
        Assert.assertEquals(h2o.getAllAtoms()[0], water[0], "Wrong residue atom.");
        Assert.assertEquals(h2o.getAllAtoms()[1], water[1], "Wrong residue atom.");
        Assert.assertEquals(h2o.getAllAtoms()[2], water[2], "Wrong residue atom.");
    }

    @Test
    public void testResiduesCreation() {
        float[][] h2oCoordinates = { //
                { 3.970f, 4.455f, 4.689f }, //
                { 4.005f, 4.486f, 4.778f }, //
                { 3.872f, 4.472f, 4.685f }, //
                { 4.970f, 5.455f, 5.689f }, //
                { 5.005f, 5.486f, 5.778f }, //
                { 4.872f, 5.472f, 5.685f } };

        GmxAtom[][] waters = new GmxAtom[2][3];
        waters[0][0] = atomFactory.get(GmxAtomO.class, "O", 0, h2oCoordinates[0]);
        waters[0][1] = atomFactory.get(GmxAtomH.class, "H1", 1, h2oCoordinates[1]);
        waters[0][2] = atomFactory.get(GmxAtomH.class, "H2", 2, h2oCoordinates[2]);
        waters[1][0] = atomFactory.get(GmxAtomO.class, "O", 3, h2oCoordinates[3]);
        waters[1][1] = atomFactory.get(GmxAtomH.class, "H1", 4, h2oCoordinates[4]);
        waters[1][2] = atomFactory.get(GmxAtomH.class, "H2", 5, h2oCoordinates[5]);
        GmxResidue[] h2os = residuesFactory.get(GmxResidueH2O.class, new int[] { 3, 4 }, waters);

        Assert.assertNotNull(h2os);
        Assert.assertEquals(h2os.length, 2, "Wrong total number of residues.");
        Assert.assertEquals(h2os[0].getAbbreviation(), "SOL", "Wrong abbreviation.");
        Assert.assertEquals(h2os[0].getFullName(), "Water", "Wrong full name.");
        Assert.assertEquals(h2os[0].getResidueNo(), 3, "Wrong residue number.");
        Assert.assertEquals(h2os[1].getResidueNo(), 4, "Wrong residue number.");

        Assert.assertEquals(h2os[0].getPivotAtom(), waters[0][0], "Wrong residue pivot atom.");
        Assert.assertEquals(h2os[1].getPivotAtom(), waters[1][0], "Wrong residue pivot atom.");
        Assert.assertEquals(h2os[0].getAllAtoms().length, 3, "Wrong total number of residue atoms.");
        Assert.assertEquals(h2os[1].getAllAtoms().length, 3, "Wrong total number of residue atoms.");
    }

    @Test
    public void testResidueAtomsCreation() {
        GmxAtom[] atoms = residueAtomsFactory.get(GmxResidueH2O.class, new float[] { 1, 1, 1 });
        Assert.assertEquals(atoms.length, 3, "Wrong total number of residue atoms.");

        Assert.assertEquals(atoms[0].getAbbreviation(), "OW", "Wrong abbreviation of residue atom.");
        Assert.assertEquals(atoms[0].getCoordinateX(), 1.f, "Wrong X coordinate of residue atom.");
        Assert.assertEquals(atoms[0].getCoordinateY(), 1.f, "Wrong Y coordinate of residue atom.");
        Assert.assertEquals(atoms[0].getCoordinateZ(), 1.f, "Wrong Z coordinate of residue atom.");

        Assert.assertEquals(atoms[1].getAbbreviation(), "HW1", "Wrong abbreviation of residue atom.");
        Assert.assertEquals(atoms[1].getCoordinateX(), 1.035f, "Wrong X coordinate of residue atom.");
        Assert.assertEquals(atoms[1].getCoordinateY(), 1.031f, "Wrong Y coordinate of residue atom.");
        Assert.assertEquals(atoms[1].getCoordinateZ(), 1.089f, "Wrong Z coordinate of residue atom.");

        Assert.assertEquals(atoms[2].getAbbreviation(), "HW2", "Wrong abbreviation of residue atom.");
        Assert.assertEquals(atoms[2].getCoordinateX(), 0.902f, "Wrong X coordinate of residue atom.");
        Assert.assertEquals(atoms[2].getCoordinateY(), 1.017f, "Wrong Y coordinate of residue atom.");
        Assert.assertEquals(atoms[2].getCoordinateZ(), 0.996f, "Wrong Z coordinate of residue atom.");
    }

    @Test
    public void testRotateWhile() {
        GmxAtom[] atoms1 = residueAtomsFactory.get(GmxResidueH2O.class, new float[] { 3f, 3f, 3f });
        GmxAtom[] atoms2 = residueAtomsFactory.get(GmxResidueH2O.class, new float[] { 3f, 3f, 3.3f });

        GmxResidue sol1 = residueByAtomsFactory.get(GmxResidueH2O.class, 1, atoms1);
        GmxResidue sol2 = residueByAtomsFactory.get(GmxResidueH2O.class, 2, atoms2);

        GmxAtom hDon0 = sol2.getDonorAtoms()[0];
        GmxAtom hDon1 = sol2.getDonorAtoms()[1];
        GmxAtom oAcc0 = sol1.getAcceptorAtom();
        GmxAtom oAcc1 = sol2.getAcceptorAtom();

        Logger.log("Cosine before rotation: " + GmxAtomUtils.angleCosine(hDon0, oAcc0, oAcc1));
        Logger.log("Cosine before rotation: " + GmxAtomUtils.angleCosine(hDon1, oAcc0, oAcc1));

        GmxAnglePredicate predicate0 = anglePredicateFactory.get(hDon0, oAcc0, oAcc1, 0.0, 0.01);
        GmxAnglePredicate predicate1 = anglePredicateFactory.get(hDon1, oAcc0, oAcc1, 0.0, 0.01);
        boolean result = GmxResidueUtils.rotateWhile(sol2, predicate0, predicate1);

        Logger.log("Cosine after rotation: " + GmxAtomUtils.angleCosine(hDon0, oAcc0, oAcc1));
        Logger.log("Cosine after rotation: " + GmxAtomUtils.angleCosine(hDon1, oAcc0, oAcc1));

        Assert.assertTrue(result, "Rotation failed.");
        Assert.assertTrue(Math.abs(GmxAtomUtils.angleCosine(hDon1, oAcc0, oAcc1)) < 0.01, "Rotation failed.");
        Assert.assertTrue(Math.abs(GmxAtomUtils.angleCosine(hDon0, oAcc0, oAcc1)) < 0.01, "Rotation failed.");

        verifyWaterCoordinates(sol2, //
                new float[] { 3.0000000f, 3.0000000f, 3.3000000f }, //
                new float[] { 2.9527469f, 3.0685146f, 3.2739182f }, //
                new float[] { 3.0858483f, 3.0095180f, 3.2723947f });
    }

    @Test
    public void testMoveTo() {
        GmxAtom[] water = new GmxAtom[3];
        water[0] = atomFactory.get(GmxAtomO.class, "OW", 0, H2O_COORDINATES[0]);
        water[1] = atomFactory.get(GmxAtomH.class, "HW1", 1, H2O_COORDINATES[1]);
        water[2] = atomFactory.get(GmxAtomH.class, "HW2", 2, H2O_COORDINATES[2]);

        GmxResidue h2o = residueByAtomsFactory.get(GmxResidueH2O.class, 100, water);
        GmxResidueUtils.moveTo(new float[] { 1.f, 1.f, 1.f }, h2o);
        verifyWaterCoordinates(h2o, //
                new float[] { 1.00000000f, 1.0000000f, 1.0000000f }, //
                new float[] { 1.03500010f, 1.0310001f, 1.0889997f }, //
                new float[] { 0.90199995f, 1.0170002f, 0.9959998f });
    }

    @Test
    public void testShiftX() {
        GmxAtom[] water = new GmxAtom[3];
        water[0] = atomFactory.get(GmxAtomO.class, "OW", 0, H2O_COORDINATES[0]);
        water[1] = atomFactory.get(GmxAtomH.class, "HW1", 1, H2O_COORDINATES[1]);
        water[2] = atomFactory.get(GmxAtomH.class, "HW2", 2, H2O_COORDINATES[2]);

        GmxResidue h2o = residueByAtomsFactory.get(GmxResidueH2O.class, 100, water);
        GmxResidueUtils.shiftX(0.1f, h2o);
        verifyWaterCoordinates(h2o, //
                new float[] { 4.070f, 4.455f, 4.689f }, //
                new float[] { 4.105f, 4.486f, 4.778f }, //
                new float[] { 3.972f, 4.472f, 4.685f });
    }

    @Test
    public void testShiftY() {
        GmxAtom[] water = new GmxAtom[3];
        water[0] = atomFactory.get(GmxAtomO.class, "OW", 0, H2O_COORDINATES[0]);
        water[1] = atomFactory.get(GmxAtomH.class, "HW1", 1, H2O_COORDINATES[1]);
        water[2] = atomFactory.get(GmxAtomH.class, "HW2", 2, H2O_COORDINATES[2]);

        GmxResidue h2o = residueByAtomsFactory.get(GmxResidueH2O.class, 100, water);
        GmxResidueUtils.shiftY(0.1f, h2o);
        verifyWaterCoordinates(h2o, //
                new float[] { 3.970f, 4.555f, 4.689f }, //
                new float[] { 4.005f, 4.586f, 4.778f }, //
                new float[] { 3.872f, 4.572f, 4.685f });
    }

    @Test
    public void testShiftZ() {
        GmxAtom[] water = new GmxAtom[3];
        water[0] = atomFactory.get(GmxAtomO.class, "OW", 0, H2O_COORDINATES[0]);
        water[1] = atomFactory.get(GmxAtomH.class, "HW1", 1, H2O_COORDINATES[1]);
        water[2] = atomFactory.get(GmxAtomH.class, "HW2", 2, H2O_COORDINATES[2]);

        GmxResidue h2o = residueByAtomsFactory.get(GmxResidueH2O.class, 100, water);
        GmxResidueUtils.shiftZ(0.1f, h2o);
        verifyWaterCoordinates(h2o, //
                new float[] { 3.970f, 4.455f, 4.789f }, //
                new float[] { 4.005f, 4.486f, 4.878f }, //
                new float[] { 3.872f, 4.472f, 4.785f });
    }

    @Test
    public void testShift() {
        GmxAtom[] water = new GmxAtom[3];
        water[0] = atomFactory.get(GmxAtomO.class, "OW", 0, H2O_COORDINATES[0]);
        water[1] = atomFactory.get(GmxAtomH.class, "HW1", 1, H2O_COORDINATES[1]);
        water[2] = atomFactory.get(GmxAtomH.class, "HW2", 2, H2O_COORDINATES[2]);

        GmxResidue h2o = residueByAtomsFactory.get(GmxResidueH2O.class, 100, water);
        GmxResidueUtils.shift(0.1f, 0.1f, 0.1f, h2o);
        verifyWaterCoordinates(h2o, //
                new float[] { 4.070f, 4.555f, 4.789f }, //
                new float[] { 4.105f, 4.586f, 4.878f }, //
                new float[] { 3.972f, 4.572f, 4.785f });
    }

    @Test
    public void testRotate() {
        GmxAtom[] water = new GmxAtom[3];
        water[0] = atomFactory.get(GmxAtomO.class, "OW", 0, H2O_COORDINATES[0]);
        water[1] = atomFactory.get(GmxAtomH.class, "HW1", 1, H2O_COORDINATES[1]);
        water[2] = atomFactory.get(GmxAtomH.class, "HW2", 2, H2O_COORDINATES[2]);

        GmxResidue h2o = residueByAtomsFactory.get(GmxResidueH2O.class, 100, water);
        GmxResidueUtils.rotate(Math.PI / 2, Math.PI / 2, Math.PI / 2, h2o);
        verifyWaterCoordinates(h2o, //
                new float[] { 3.9700000f, 4.455f, 4.6890000f }, //
                new float[] { 4.0590000f, 4.486f, 4.6540003f }, //
                new float[] { 3.9659998f, 4.472f, 4.7870000f });
    }

    @Test
    public void testRadiusVector() {
        GmxAtom[] water = new GmxAtom[3];
        water[0] = atomFactory.get(GmxAtomO.class, "OW", 0, H2O_COORDINATES[0]);
        water[1] = atomFactory.get(GmxAtomH.class, "HW1", 1, H2O_COORDINATES[1]);
        water[2] = atomFactory.get(GmxAtomH.class, "HW2", 2, H2O_COORDINATES[2]);

        GmxResidue h2o = residueByAtomsFactory.get(GmxResidueH2O.class, 100, water);
        Assert.assertEquals(h2o.getRadiusVector(), 7.5891139317152625, "Wrong radius-vector.");

        GmxResidueUtils.rotate(1.f, 1.f, 1.f, h2o);
        Assert.assertEquals(h2o.getRadiusVector(), 7.5891139317152625, "Wrong radius-vector.");

        GmxResidueUtils.shiftX(0.1f, h2o);
        Assert.assertEquals(h2o.getRadiusVector(), 7.64190101499927, "Wrong radius-vector.");

        GmxResidueUtils.shiftY(0.1f, h2o);
        Assert.assertEquals(h2o.getRadiusVector(), 7.700626734208676, "Wrong radius-vector.");

        GmxResidueUtils.shiftZ(0.1f, h2o);
        Assert.assertEquals(h2o.getRadiusVector(), 7.7619229555265035, "Wrong radius-vector.");

        GmxResidueUtils.shift(0.1f, 0.1f, 0.1f, h2o);
        Assert.assertEquals(h2o.getRadiusVector(), 7.934749096543485, "Wrong radius-vector.");

        GmxResidueUtils.moveTo(new float[] { 1.1f, 2.2f, 3.3f }, h2o);
        Assert.assertEquals(h2o.getRadiusVector(), 4.115822958620668, "Wrong radius-vector.");
    }

    private void verifyWaterCoordinates(GmxResidue water, float[] o, float[] h1, float[] h2) {
        Assert.assertNotNull(water);
        GmxAtom[] atoms = water.getAllAtoms();
        Assert.assertEquals(atoms.length, 3, "Wrong number of atoms in water.");

        Assert.assertEquals(atoms[0].getAbbreviation(), "OW", "Wrong oxygen abbreviation.");
        Assert.assertEquals(atoms[1].getAbbreviation(), "HW1", "Wrong 1st hydrogen abbreviation.");
        Assert.assertEquals(atoms[2].getAbbreviation(), "HW2", "Wrong 2nd hydrogen abbreviation.");

        verifyAtomCoordinates(atoms[0], o[0], o[1], o[2]);
        verifyAtomCoordinates(atoms[1], h1[0], h1[1], h1[2]);
        verifyAtomCoordinates(atoms[2], h2[0], h2[1], h2[2]);
    }
}