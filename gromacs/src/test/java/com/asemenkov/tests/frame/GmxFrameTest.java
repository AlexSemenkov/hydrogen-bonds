package com.asemenkov.tests.frame;

import com.asemenkov.gromacs.frame.GmxFrame;
import com.asemenkov.gromacs.frame.coordinates.GmxFrameCoordinates;
import com.asemenkov.gromacs.frame.structure.GmxFrameStructure;
import com.asemenkov.gromacs.io.gro.GmxGroFileAtomLine;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;
import com.asemenkov.tests.config.GmxAbstractTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author asemenkov
 * @since May 3, 2018
 */
@Test
public class GmxFrameTest extends GmxAbstractTest {

    private List<GmxGroFileAtomLine> groFileAtomLines;
    private GmxFrameStructure frameStructure;
    private GmxFrameCoordinates frameCoordinates;
    private GmxFrame frame;

    @BeforeClass
    public void initLinesFromGroFile() {
        groFileAtomLines = groFileReader.readGroFileAtomLines(GRO_WATER_IN_ARGON_PATH);
    }

    @BeforeMethod
    public void initFrame() {
        frameStructure = frameStructureFromGroFileBuilderSupplier.get() //
                .withDescription(groFileReader.readGroFileDescription(GRO_WATER_IN_ARGON_PATH)) //
                .withBox(groFileReader.readGroFileBox(GRO_WATER_IN_ARGON_PATH)) //
                .withGroFileAtomLines(groFileAtomLines) //
                .build();

        frameCoordinates = frameCoordinatesFromGroFileBuilderSupplier.get() //
                .withGroFileAtomLines(groFileAtomLines) //
                .withFrameNo(1) //
                .build();

        frame = frameFactory.get(frameStructure, frameCoordinates);
    }

    @Test
    public void testFrameStructureGetter() {
        GmxFrameStructure frameStructure = frame.getFrameStructure();
        Assert.assertSame(frameStructure, this.frameStructure, "Frame structure getter is broken.");
    }

    @Test
    public void testFrameCoordinatesGetter() {
        GmxFrameCoordinates frameCoordinates = frame.getFrameCoordinates();
        Assert.assertSame(frameCoordinates, this.frameCoordinates, "Frame coordinates getter is broken.");
    }

    @Test
    public void testAtomsGetter() {
        GmxAtom[] atoms1 = frame.getAtoms();
        GmxAtom[] atoms2 = frame.getAtoms();
        Assert.assertNotSame(atoms1, atoms2, "Atoms getter doesn't make a copy of array.");
        Assert.assertSame(atoms1[0], atoms2[0], "The 1st atom in array is not as expected");
        Assert.assertEquals(atoms1.length, 872, "Length of atoms array is not as expected.");
        Assert.assertEquals(atoms2.length, 872, "Length of atoms array is not as expected.");
    }

    @Test
    public void testAtomsDeepCopyGetter() {
        GmxAtom[] atoms1 = frame.getAtoms();
        GmxAtom[] atoms2 = frame.getAtomsDeepCopy();
        Assert.assertNotSame(atoms1, atoms2, "Atoms getter doesn't make a copy of array.");
        Assert.assertNotSame(atoms1[0], atoms2[0], "Atoms deep copy doesn't make a full copy.");
        Assert.assertEquals(atoms1[0].getRadiusVector(), atoms2[0].getRadiusVector());
        Assert.assertEquals(atoms1.length, 872, "Length of atoms array is not as expected.");
        Assert.assertEquals(atoms2.length, 872, "Length of atoms array is not as expected.");
    }

    @Test
    public void testResiduesDeepCopyGetter() {
        GmxResidue[] residues1 = frame.getResidues();
        GmxResidue[] residues2 = frame.getResiduesDeepCopy();
        Assert.assertNotSame(residues1, residues2, "Residues getter doesn't make a copy of array.");
        Assert.assertNotSame(residues1[0], residues2[0], "Residues deep copy doesn't make a full copy.");
        Assert.assertNotSame(residues1[0].getPivotAtom(), residues2[0].getPivotAtom());
        Assert.assertEquals(residues1[0].getRadiusVector(), residues2[0].getRadiusVector());
        Assert.assertEquals(residues1.length, 1, "Length of residues array is not as expected.");
        Assert.assertEquals(residues2.length, 1, "Length of residues array is not as expected.");
    }

    @Test
    public void testResiduesGetter() {
        GmxResidue[] residues1 = frame.getResidues();
        GmxResidue[] residues2 = frame.getResidues();
        Assert.assertNotSame(residues1, residues2, "Residues getter doesn't make a copy of array.");
        Assert.assertSame(residues1[0], residues2[0], "The 1st residue in array is not as expected");
        Assert.assertEquals(residues1.length, 1, "Length of residues array is not as expected.");
        Assert.assertEquals(residues2.length, 1, "Length of residues array is not as expected.");
    }

    @Test
    public void testAtomsNumGetter() {
        int atomsNum = frame.getAtomsNum();
        Assert.assertEquals(atomsNum, 872, "Atoms num is not as expected.");
    }

    @Test
    public void testResiduesNumGetter() {
        int residuesNum = frame.getResiduesNum();
        Assert.assertEquals(residuesNum, 1, "Residues num is not as expected.");
    }

    @Test
    public void testFrameNoGetter() {
        int frameNo = frame.getFrameNo();
        Assert.assertEquals(frameNo, 1, "Frame No is not as expected.");
    }

    @Test
    public void testDescriptionGetter() {
        String description = frame.getDescription();
        Assert.assertEquals(description, "Ar+SOL", "Frame description is not as expected.");
    }

    @Test
    public void testBoxGetter() {
        float[] box1 = frame.getBox();
        float[] box2 = frame.getBox();
        Assert.assertEquals(box1.length, 3, "Box size is not as expected.");
        Assert.assertEquals(box2.length, 3, "Box size is not as expected.");
        Assert.assertEquals(box1[0], 7.f, "X dimension of box is not as expected.");
        Assert.assertEquals(box2[0], 7.f, "X dimension of box is not as expected.");
        Assert.assertNotSame(box1, box2, "Box getter doesn't make a copy of array.");
    }

}
