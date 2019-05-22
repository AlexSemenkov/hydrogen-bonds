package com.asemenkov.tests.frame;

import com.asemenkov.gromacs.frame.GmxFrame;
import com.asemenkov.gromacs.frame.coordinates.GmxFrameCoordinates;
import com.asemenkov.gromacs.frame.exceptions.GmxFrameException;
import com.asemenkov.gromacs.frame.structure.GmxFrameStructure;
import com.asemenkov.gromacs.frame.utils.GmxFrameUtils;
import com.asemenkov.gromacs.io.gro.GmxGroFileAtomLine;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;
import com.asemenkov.gromacs.particles.utils.GmxAtomUtils;
import com.asemenkov.particles.atoms.GmxAtomAr;
import com.asemenkov.particles.atoms.GmxAtomH;
import com.asemenkov.particles.atoms.GmxAtomO;
import com.asemenkov.particles.residues.GmxResidueH2O;
import com.asemenkov.tests.config.GmxAbstractTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author asemenkov
 * @since Mar 12, 2019
 */
@Test
public class GmxFrameUtilsTest extends GmxAbstractTest {

    private static final String REGEX_INVALID_POINT = "Invalid point coordinates: \\[.*\\]";
    private static final String REGEX_INVALID_ORDER = "Invalid neighbour order: .*";
    private static final String REGEX_INVALID_BOX = "Box multiplier is negative integer or zero";
    private static final String REGEX_ATOM_NOT_FREE = "Atom is not free: GmxAtom(O|H|Ar)\\[.*\\]";
    private static final String REGEX_NOT_IN_FRAME = "Error while removing (atoms|residues) from frame: .*";
    private static final String REGEX_DUPLICATE = "Cannot append duplicate (atoms|residues)";

    private GmxFrame frame;

    @BeforeMethod
    public void initFrame() {
        List<GmxGroFileAtomLine> groAtomLines = groFileReader.readGroFileAtomLines(GRO_WATER_IN_ARGON_PATH);

        GmxFrameStructure frameStructure = frameStructureFromGroFileBuilder() //
                .withDescription(groFileReader.readGroFileDescription(GRO_WATER_IN_ARGON_PATH)) //
                .withBox(groFileReader.readGroFileBox(GRO_WATER_IN_ARGON_PATH)) //
                .withGroFileAtomLines(groAtomLines) //
                .build();

        GmxFrameCoordinates frameCoordinates = frameCoordinatesFromGroFileBuilder() //
                .withGroFileAtomLines(groAtomLines) //
                .withFrameNo(1) //
                .build();

        frame = frameFactory.get(frameStructure, frameCoordinates);
    }

    // ======== MINIMAL & MAXIMAL ========

    @Test
    public void testMinimalAtomOfFrame() {
        float minX = GmxFrameUtils.getAtomWithMinimalX(frame).getCoordinateX();
        float minY = GmxFrameUtils.getAtomWithMinimalY(frame).getCoordinateY();
        float minZ = GmxFrameUtils.getAtomWithMinimalZ(frame).getCoordinateZ();
        double minR = GmxFrameUtils.getAtomWithMinimalRadiusVector(frame).getRadiusVector();
        Arrays.stream(frame.getAtoms()).forEach(a -> {
            Assert.assertFalse(minX > a.getCoordinateX(), "Wrong atom with min X");
            Assert.assertFalse(minY > a.getCoordinateY(), "Wrong atom with min Y");
            Assert.assertFalse(minZ > a.getCoordinateZ(), "Wrong atom with min Z");
            Assert.assertFalse(minR > a.getRadiusVector(), "Wrong atom with min R");
        });
    }

    @Test
    public void testMinimalResidueOfFrame() {
        float minX = GmxFrameUtils.getResidueWithMinimalX(frame).getPivotAtom().getCoordinateX();
        float minY = GmxFrameUtils.getResidueWithMinimalY(frame).getPivotAtom().getCoordinateY();
        float minZ = GmxFrameUtils.getResidueWithMinimalZ(frame).getPivotAtom().getCoordinateZ();
        double minR = GmxFrameUtils.getResidueWithMinimalRadiusVector(frame).getRadiusVector();
        Arrays.stream(frame.getResidues()).forEach(r -> {
            Assert.assertFalse(minX > r.getPivotAtom().getCoordinateX(), "Wrong residue with min X");
            Assert.assertFalse(minY > r.getPivotAtom().getCoordinateY(), "Wrong residue with min Y");
            Assert.assertFalse(minZ > r.getPivotAtom().getCoordinateZ(), "Wrong residue with min Z");
            Assert.assertFalse(minR > r.getRadiusVector(), "Wrong residue with min R");
        });
    }

    @Test
    public void testMaximalAtomOfFrame() {
        float maxX = GmxFrameUtils.getAtomWithMaximalX(frame).getCoordinateX();
        float maxY = GmxFrameUtils.getAtomWithMaximalY(frame).getCoordinateY();
        float maxZ = GmxFrameUtils.getAtomWithMaximalZ(frame).getCoordinateZ();
        double maxR = GmxFrameUtils.getAtomWithMaximalRadiusVector(frame).getRadiusVector();
        Arrays.stream(frame.getAtoms()).forEach(a -> {
            Assert.assertFalse(maxX < a.getCoordinateX(), "Wrong atom with max X");
            Assert.assertFalse(maxY < a.getCoordinateY(), "Wrong atom with max Y");
            Assert.assertFalse(maxZ < a.getCoordinateZ(), "Wrong atom with max Z");
            Assert.assertFalse(maxR < a.getRadiusVector(), "Wrong atom with max R");
        });
    }

    @Test
    public void testMaximalResidueOfFrame() {
        float maxX = GmxFrameUtils.getResidueWithMaximalX(frame).getPivotAtom().getCoordinateX();
        float maxY = GmxFrameUtils.getResidueWithMaximalY(frame).getPivotAtom().getCoordinateY();
        float maxZ = GmxFrameUtils.getResidueWithMaximalZ(frame).getPivotAtom().getCoordinateZ();
        double maxR = GmxFrameUtils.getResidueWithMaximalRadiusVector(frame).getRadiusVector();
        Arrays.stream(frame.getResidues()).forEach(r -> {
            Assert.assertFalse(maxX < r.getPivotAtom().getCoordinateX(), "Wrong residue with max X");
            Assert.assertFalse(maxY < r.getPivotAtom().getCoordinateY(), "Wrong residue with max Y");
            Assert.assertFalse(maxZ < r.getPivotAtom().getCoordinateZ(), "Wrong residue with max Z");
            Assert.assertFalse(maxR < r.getRadiusVector(), "Wrong residue with max R");
        });
    }

    // ======== SORTING FUNCTIONS ========

    @Test
    public void testAtomsSorting() {
        GmxAtom atom = frame.getAtoms()[100];
        GmxAtom expectedNeighbour = frame.getAtoms()[867];
        GmxAtom neighbour = GmxFrameUtils.getAtomsSortedByDistanceToPoint(frame, atom.getCoordinates())[1];

        Assert.assertEquals(neighbour, expectedNeighbour, "Wrong neighbour atom.");
        Assert.assertEquals((float) GmxAtomUtils.euclideanDistance(atom, neighbour), 0.36857972f);
        Assert.assertEquals((float) atom.getRadiusVector(), 7.84798f, "Pivot atom shifted.");
        Assert.assertSame(frame.getAtoms()[100], atom, "Wrong pivot atom index.");
    }

    @Test
    public void testResiduesSorting() {
        GmxAtom atom = frame.getAtoms()[100];
        GmxResidue expectedNeighbour = frame.getResidues()[0];
        GmxResidue neighbour = GmxFrameUtils.getResiduesSortedByDistanceToPoint(frame,
                atom.getCoordinates())[0];

        Assert.assertEquals(neighbour, expectedNeighbour, "Wrong neighbour residue.");
        Assert.assertEquals((float) atom.getRadiusVector(), 7.84798f, "Pivot atom shifted.");
        Assert.assertSame(frame.getAtoms()[100], atom, "Wrong pivot atom index.");
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_INVALID_POINT)
    public void testNeighborAtomsSortedByDistanceToInvalidPoint() {
        GmxFrameUtils.getAtomsSortedByDistanceToPoint(frame, new float[] {});
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_INVALID_POINT)
    public void testNeighborResiduesSortedByDistanceToInvalidPoint() {
        GmxFrameUtils.getResiduesSortedByDistanceToPoint(frame, new float[] {});
    }

    // ======== NEIGHBOUR FUNCTIONS ========

    @Test
    public void testAtomsNeighbours() {
        GmxAtom atom = frame.getAtoms()[100];
        GmxAtom expectedNeighbour = frame.getAtoms()[867];
        GmxAtom neighbour = GmxFrameUtils.getNeighbourAtom(frame, atom.getCoordinates(), 1);

        Assert.assertEquals(neighbour, expectedNeighbour, "Wrong neighbour atom.");
        Assert.assertEquals((float) GmxAtomUtils.euclideanDistance(atom, neighbour), 0.36857972f);
        Assert.assertEquals((float) atom.getRadiusVector(), 7.84798f, "Pivot atom shifted.");
        Assert.assertSame(frame.getAtoms()[100], atom, "Wrong pivot atom index.");
    }

    @Test
    public void testResiduesNeighbours() {
        GmxAtom atom = frame.getAtoms()[100];
        GmxResidue expectedNeighbour = frame.getResidues()[0];
        GmxResidue neighbour = GmxFrameUtils.getNeighbourResidue(frame, atom.getCoordinates(), 0);

        Assert.assertEquals(neighbour, expectedNeighbour, "Wrong neighbour residue.");
        Assert.assertEquals((float) atom.getRadiusVector(), 7.84798f, "Pivot atom shifted.");
        Assert.assertSame(frame.getAtoms()[100], atom, "Wrong pivot atom index.");
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_INVALID_ORDER)
    public void testNeighborAtomWithInvalidOrder() {
        GmxFrameUtils.getNeighbourAtom(frame, new float[] { 3.f, 3.f, 3.f }, -1);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_INVALID_POINT)
    public void testNeighborAtomWithInvalidPoint() {
        GmxFrameUtils.getNeighbourAtom(frame, new float[] {}, 1);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_INVALID_ORDER)
    public void testNeighborResidueWithInvalidOrder() {
        GmxFrameUtils.getNeighbourResidue(frame, new float[] { 3.f, 3.f, 3.f }, -1);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_INVALID_POINT)
    public void testNeighborResidueWithInvalidPoint() {
        GmxFrameUtils.getNeighbourResidue(frame, new float[] {}, 0);
    }

    // ======== FRAME BOX ALTERATION ========

    @Test
    public void testBoxRefiningPositiveIncrease() {
        GmxFrameUtils.refineBox(frame, 3.f);
        verifyCoordinates(frame.getBox(), 10.435f, 9.858f, 10.207f);
        verifyMaxCoordinates(frame, 7.435f, 6.8580003f, 7.207f);
        verifyMinCoordinates(frame, 3.0f, 3.0f, 3.0f);
    }

    @Test
    public void testBoxRefiningPositiveDecrease() {
        GmxFrameUtils.refineBox(frame, 0.3f);
        verifyMaxCoordinates(frame, 4.7349997f, 4.158f, 4.507f);
        verifyMinCoordinates(frame, 0.29999995f, 0.29999995f, 0.29999995f);
        verifyCoordinates(frame.getBox(), 5.035f, 4.458f, 4.807f);
    }

    @Test
    public void testBoxRefiningNegative() {
        GmxFrameUtils.refineBox(frame, -0.3f);
        verifyMaxCoordinates(frame, 4.1350000f, 3.5580003f, 3.9070000f);
        verifyMinCoordinates(frame, -0.29999995f, -0.29999995f, -0.29999995f);
        verifyCoordinates(frame.getBox(), 3.8349998f, 3.2580001f, 3.6069999f);
    }

    @Test
    public void testAtomsOutOfBoxRemoval() {
        frame.getFrameStructure().setBox(new float[] { 4.5f, 4.5f, 4.5f });
        GmxFrameUtils.removeAtomsOutOfBox(frame);
        groFileWriter.writeGroFile(frame, PATH_GRO_FROM_TESTS, "out-of-box-removal.gro");

        Assert.assertEquals(frame.getAtomsNum(), 190, "Wrong number of atoms after removal.");
        Assert.assertEquals(frame.getResiduesNum(), 0, "Wrong number of residues after removal.");

        verifyCoordinates(frame.getBox(), 4.5f, 4.5f, 4.5f);
        verifyMaxCoordinates(frame, 4.489f, 4.484f, 4.484f);
        verifyMinCoordinates(frame, 2.054f, 2.389f, 2.232f);
    }

    @Test
    public void testBoxMultiplicationByIntegers() {
        GmxFrameUtils.multiplyFrame(frame, 3, 3, 3);
        groFileWriter.writeGroFile(frame, PATH_GRO_FROM_TESTS, "after-multiplication.gro");

        verifyFrame(frame, "Ar+SOL", 1, new float[] { 21.f, 21.f, 21.f });
        verifyFrameAtoms(frame, 23544, "Argon", "Hydrogen");
        verifyFrameResidues(frame, 27, "Water", "Oxygen");
        verifyRadiusVectors(frame, 6227, 31776, 7589);
        Assert.assertEquals((int) (frame.getResidues()[26].getRadiusVector() * 1000), 31824, //
                "Wrong radius-vector of the last residue.");
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_INVALID_BOX)
    public void testFrameMultiplicationWithInvalidMultiplier() {
        GmxFrameUtils.multiplyFrame(frame, 1, 0, -1);
    }

    // ======== PARTICLES REMOVAL ========

    @Test
    public void testRemoveFreeAtoms() {
        GmxFrameUtils.removeFreeAtoms(frame, frame.getAtoms()[0], frame.getAtoms()[10], frame.getAtoms()[99]);
        verifyFrame(frame, "Ar+SOL", 1, new float[] { 7.f, 7.f, 7.f });
        verifyFrameAtoms(frame, 869, "Argon", "Hydrogen");
        verifyFrameResidues(frame, 1, "Water", "Oxygen");
        verifyRadiusVectors(frame, 6662, 7545, 7589);
    }

    @Test
    public void testRemoveFrameResidues() {
        GmxFrameUtils.removeResidues(frame, frame.getResidues()[0]);
        verifyFrame(frame, "Ar+SOL", 1, new float[] { 7.f, 7.f, 7.f });
        verifyFrameAtoms(frame, 869, "Argon", "Argon");
        verifyFrameResidues(frame, 0, null, null);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_ATOM_NOT_FREE)
    public void testRemoveAtomNotFree() {
        GmxFrameUtils.removeFreeAtoms(frame, frame.getAtoms()[870]);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_NOT_IN_FRAME)
    public void testRemoveAtomNotInFrame() {
        GmxAtom argon = atomFactory.get(GmxAtomAr.class, "Ar", 0, new float[] { 1.1f, 1.1f, 1.1f });
        GmxFrameUtils.removeFreeAtoms(frame, frame.getAtoms()[0], argon, frame.getAtoms()[99]);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_NOT_IN_FRAME)
    public void testRemoveResidueNotInFrame() {
        GmxResidue water = residueByCoordsFactory.get(GmxResidueH2O.class, 1, new float[] { 1, 1, 1 });
        GmxFrameUtils.removeResidues(frame, frame.getResidues()[0], water);
    }

    // ======== PARTICLES APPENDAGE ========

    @Test
    public void testAppendFreeAtoms() {
        GmxAtom hydrogen = atomFactory.get(GmxAtomH.class, "H", 0, new float[] { 2.2f, 2.2f, 2.2f });
        GmxAtom oxygen = atomFactory.get(GmxAtomO.class, "O", 0, new float[] { 3.3f, 3.3f, 3.3f });
        GmxAtom argon = atomFactory.get(GmxAtomAr.class, "Ar", 0, new float[] { 1.1f, 1.1f, 1.1f });
        GmxFrameUtils.appendFreeAtoms(frame, argon, hydrogen, oxygen);

        verifyFrame(frame, "Ar+SOL", 1, new float[] { 7.f, 7.f, 7.f });
        verifyFrameAtoms(frame, 875, "Argon", "Oxygen");
        verifyFrameResidues(frame, 1, "Water", "Oxygen");

        Assert.assertSame(frame.getAtoms()[872], argon, "Atom appended at wrong index.");
        Assert.assertSame(frame.getAtoms()[873], hydrogen, "Atom appended at wrong index.");
        Assert.assertSame(frame.getAtoms()[874], oxygen, "Atom appended at wrong index.");
    }

    @Test
    public void testAppendResidues() {
        GmxResidue water = residueByCoordsFactory.get(GmxResidueH2O.class, 1, new float[] { 1, 1, 1 });
        GmxFrameUtils.appendResidues(frame, water);

        verifyFrame(frame, "Ar+SOL", 1, new float[] { 7.f, 7.f, 7.f });
        verifyFrameAtoms(frame, 875, "Argon", "Hydrogen");
        verifyFrameResidues(frame, 2, "Water", "Oxygen");
        verifyRadiusVectors(frame, 6227, 1685, 7589);
        Assert.assertSame(frame.getResidues()[1], water, "Residue appended at wrong index.");
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_DUPLICATE)
    public void testAppendDuplicateArgumentAtoms() {
        GmxAtom argon = atomFactory.get(GmxAtomAr.class, "Ar", 0, new float[] { 1.1f, 1.1f, 1.1f });
        GmxFrameUtils.appendFreeAtoms(frame, argon, argon);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_DUPLICATE)
    public void testAppendDuplicateFrameAtoms() {
        GmxAtom argon = atomFactory.get(GmxAtomAr.class, "Ar", 0, new float[] { 1.1f, 1.1f, 1.1f });
        GmxFrameUtils.appendFreeAtoms(frame, argon, frame.getAtoms()[100]);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_DUPLICATE)
    public void testAppendDuplicateArgumentResidues() {
        GmxResidue water = residueByCoordsFactory.get(GmxResidueH2O.class, 1, new float[] { 1, 1, 1 });
        GmxFrameUtils.appendResidues(frame, water, water);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_DUPLICATE)
    public void testAppendDuplicateFrameResidues() {
        GmxResidue water = residueByCoordsFactory.get(GmxResidueH2O.class, 1, new float[] { 1, 1, 1 });
        GmxFrameUtils.appendResidues(frame, water, frame.getResidues()[0]);
    }

    // ======== PARTICLES REPLACEMENT ========

    @Test
    public void testReplaceAtomsWithAtoms() {
        int firstAtomRvX1000 = (int) (frame.getAtoms()[0].getRadiusVector() * 1000);
        int secondAtomRvX1000 = (int) (frame.getAtoms()[1].getRadiusVector() * 1000);
        int waterResidueRvX1000 = (int) (frame.getResidues()[0].getRadiusVector() * 1000);
        GmxAtom[] atoms = GmxFrameUtils.replaceAtomsWithAtoms(frame, GmxAtomO.class, "O",
                frame.getAtoms()[0]);

        verifyFrame(frame, "Ar+SOL", 1, new float[] { 7.f, 7.f, 7.f });
        verifyFrameAtoms(frame, 872, "Argon", "Oxygen");
        verifyFrameResidues(frame, 1, "Water", "Oxygen");
        verifyRadiusVectors(frame, secondAtomRvX1000, firstAtomRvX1000, waterResidueRvX1000);

        Assert.assertNotNull(atoms, "New atoms are missing.");
        Assert.assertEquals(atoms.length, 1, "Wrong number of new atoms.");
        Assert.assertEquals(atoms[0].getFullName(), "Oxygen", "Wrong type of new atom.");
        Assert.assertEquals((int) (atoms[0].getRadiusVector() * 1000), firstAtomRvX1000,
                "Wrong RV of new atom.");
    }

    @Test
    public void testReplaceAtomsWithResidues() {
        int firstAtomRvX1000 = (int) (frame.getAtoms()[0].getRadiusVector() * 1000);
        int secondAtomRvX1000 = (int) (frame.getAtoms()[1].getRadiusVector() * 1000);
        int waterResidueRvX1000 = (int) (frame.getResidues()[0].getRadiusVector() * 1000);
        GmxResidue[] residues = GmxFrameUtils.replaceAtomsWithResidues(frame, GmxResidueH2O.class,
                frame.getAtoms()[0]);

        verifyFrame(frame, "Ar+SOL", 1, new float[] { 7.f, 7.f, 7.f });
        verifyFrameAtoms(frame, 874, "Argon", "Hydrogen");
        verifyFrameResidues(frame, 2, "Water", "Oxygen");
        verifyRadiusVectors(frame, secondAtomRvX1000, 6171, waterResidueRvX1000);

        Assert.assertNotNull(residues, "New residues are missing.");
        Assert.assertEquals(residues.length, 1, "Wrong number of new residues.");
        Assert.assertEquals(residues[0].getFullName(), "Water", "Wrong type of new residue.");
        Assert.assertEquals((int) (residues[0].getRadiusVector() * 1000), firstAtomRvX1000,
                "Wrong RV of new residue.");
    }

    @Test
    public void testReplaceResiduesWithAtoms() {
        double firstAtomRvX1000 = frame.getAtoms()[0].getRadiusVector();
        double waterResidueRvX1000 = frame.getResidues()[0].getRadiusVector();
        GmxAtom[] atoms = GmxFrameUtils.replaceResiduesWithAtoms(frame, GmxAtomAr.class, "Ar",
                frame.getResidues()[0]);

        verifyFrame(frame, "Ar+SOL", 1, new float[] { 7.f, 7.f, 7.f });
        verifyFrameAtoms(frame, 870, "Argon", "Argon");
        verifyFrameResidues(frame, 0, null, null);

        Assert.assertEquals(frame.getAtoms()[0].getRadiusVector(), firstAtomRvX1000,
                "Wrong first atom radius-vector.");
        Assert.assertEquals(frame.getAtoms()[869].getRadiusVector(), waterResidueRvX1000,
                "Wrong last atom radius-vector.");

        Assert.assertNotNull(atoms, "New atoms are missing.");
        Assert.assertEquals(atoms.length, 1, "Wrong number of new atoms.");
        Assert.assertEquals(atoms[0].getFullName(), "Argon", "Wrong type of new atom.");
        Assert.assertEquals(atoms[0].getRadiusVector(), waterResidueRvX1000, "Wrong RV of new atom.");
    }

    @Test
    public void testReplaceResiduesWithResidues() {
        int firstAtomRvX1000 = (int) (frame.getAtoms()[0].getRadiusVector() * 1000);
        int waterResidueRvX1000 = (int) (frame.getResidues()[0].getRadiusVector() * 1000);
        GmxResidue[] residues = GmxFrameUtils.replaceResiduesWithResidues(frame, GmxResidueH2O.class,
                frame.getResidues()[0]);

        verifyFrame(frame, "Ar+SOL", 1, new float[] { 7.f, 7.f, 7.f });
        verifyFrameAtoms(frame, 872, "Argon", "Hydrogen");
        verifyFrameResidues(frame, 1, "Water", "Oxygen");
        verifyRadiusVectors(frame, firstAtomRvX1000, 7545, waterResidueRvX1000);

        Assert.assertNotNull(residues, "New residues are missing.");
        Assert.assertEquals(residues.length, 1, "Wrong number of new residues.");
        Assert.assertEquals(residues[0].getFullName(), "Water", "Wrong type of new residue.");
        Assert.assertEquals((int) (residues[0].getRadiusVector() * 1000), waterResidueRvX1000,
                "Wrong RV of new residue.");
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_ATOM_NOT_FREE)
    public void testReplaceNotFreeAtomsWithAtoms() {
        GmxFrameUtils.replaceAtomsWithAtoms(frame, GmxAtomO.class, "O", frame.getAtoms()[871]);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_NOT_IN_FRAME)
    public void testReplaceNotInFrameAtomsWithAtoms() {
        GmxAtom argon = atomFactory.get(GmxAtomAr.class, "Ar", 0, new float[] { 1.1f, 1.1f, 1.1f });
        GmxFrameUtils.replaceAtomsWithAtoms(frame, GmxAtomO.class, "O", argon);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_NOT_IN_FRAME)
    public void testReplaceDuplicateAtomsWithAtoms() {
        GmxFrameUtils.replaceAtomsWithAtoms(frame, GmxAtomO.class, "O", frame.getAtoms()[0],
                frame.getAtoms()[0]);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_NOT_IN_FRAME)
    public void testReplaceNotInFrameResiduesWithAtoms() {
        GmxResidue water = residueByCoordsFactory.get(GmxResidueH2O.class, 1, new float[] { 1, 1, 1 });
        GmxFrameUtils.replaceResiduesWithAtoms(frame, GmxAtomO.class, "O", water);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_NOT_IN_FRAME)
    public void testReplaceDuplicateResiduesWithAtoms() {
        GmxFrameUtils.replaceResiduesWithAtoms(frame, GmxAtomO.class, "O", frame.getResidues()[0],
                frame.getResidues()[0]);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_ATOM_NOT_FREE)
    public void testReplaceNotFreeAtomsWithResidues() {
        GmxFrameUtils.replaceAtomsWithResidues(frame, GmxResidueH2O.class, frame.getAtoms()[871]);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_NOT_IN_FRAME)
    public void testReplaceNotInFrameAtomsWithResidues() {
        GmxAtom argon = atomFactory.get(GmxAtomAr.class, "Ar", 0, new float[] { 1.1f, 1.1f, 1.1f });
        GmxFrameUtils.replaceAtomsWithResidues(frame, GmxResidueH2O.class, argon);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_NOT_IN_FRAME)
    public void testReplaceDuplicateAtomsWithResidues() {
        GmxFrameUtils.replaceAtomsWithResidues(frame, GmxResidueH2O.class, frame.getAtoms()[0],
                frame.getAtoms()[0]);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_NOT_IN_FRAME)
    public void testReplaceNotInFrameResiduesWithResidues() {
        GmxResidue water = residueByCoordsFactory.get(GmxResidueH2O.class, 1, new float[] { 1, 1, 1 });
        GmxFrameUtils.replaceResiduesWithResidues(frame, GmxResidueH2O.class, water);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_NOT_IN_FRAME)
    public void testReplaceDuplicateResiduesWithResidues() {
        GmxFrameUtils.replaceResiduesWithResidues(frame, GmxResidueH2O.class, frame.getResidues()[0],
                frame.getResidues()[0]);
    }

}
