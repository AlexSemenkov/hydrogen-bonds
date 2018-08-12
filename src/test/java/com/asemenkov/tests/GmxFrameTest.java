package com.asemenkov.tests;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.asemenkov.waterinargon.GmxAtomAr;
import com.asemenkov.waterinargon.GmxAtomH;
import com.asemenkov.waterinargon.GmxAtomO;
import com.asemenkov.waterinargon.GmxResidueH2O;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.asemenkov.gromacs.frame.exceptions.GmxFrameException;
import com.asemenkov.gromacs.frame.GmxFrame;
import com.asemenkov.gromacs.frame.coordinates.GmxFrameCoordinates;
import com.asemenkov.gromacs.frame.structure.GmxFrameStructure;
import com.asemenkov.gromacs.io.GmxGroFileAtomLine;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;

/**
 * @author asemenkov
 * @since May 3, 2018
 */
@Test
public class GmxFrameTest extends GmxAbstractTest {

    private static final Path PATH_GRO_FROM_TESTS = Paths.get("src", "test", "resources", "gro-from-tests");
    private static final String REGEX_NO_STRUCTURE = "frameStructure is missing";
    private static final String REGEX_NO_COORDINATES = "frameCoordinates is missing";
    private static final String REGEX_INVALID_POINT = "Invalid point coordinates: \\[.*\\]";
    private static final String REGEX_INVALID_ORDER = "Invalid neighbour order: .*";
    private static final String REGEX_ATOM_NOT_FREE = "Atom is not free: GmxAtom(O|H|Ar)\\[.*\\]";
    private static final String REGEX_NOT_IN_FRAME = "Error while removing (atoms|residues) from frame: unexpected length";
    private static final String REGEX_DUPLICATE = "Cannot append duplicate (atoms|residues)";

    private List<GmxGroFileAtomLine> groFileAtomLines;
    private GmxFrameStructure frameStructure;
    private GmxFrameCoordinates frameCoordinates;
    private GmxFrame frame;
    private GmxAtomAr argon;
    private GmxResidueH2O water;

    @BeforeMethod
    public void initFrame(Method testMethod) {
        String[] initializationTests = new String[] { //
                "testFrameInitializationWithoutResidues", //
                "testFrameInitializationFromGroFile", //
                "testFrameInitializationFromXtcFile", //
                "testFrameInitializationFromScratch" };

        if (Arrays.stream(initializationTests).anyMatch(test -> test.equals(testMethod.getName()))) return;

        GmxAtom[] atoms = residueAtomsFactory.get(GmxResidueH2O.class, new float[] { 1, 1, 1 });
        water = (GmxResidueH2O) residueFactory.get(GmxResidueH2O.class, 1, atoms);
        argon = (GmxAtomAr) atomFactory.get(GmxAtomAr.class, "Ar", 0, new float[] { 1.1f, 1.1f, 1.1f });

        groFileAtomLines = groFileReaderAndWriter.readGroFileAtomLines(GRO_WATER_IN_ARGON_PATH);

        frameStructure = frameStructureFromGroFileBuilderSupplier.get() //
                .withDescription(groFileReaderAndWriter.readGroFileDescription(GRO_WATER_IN_ARGON_PATH)) //
                .withBox(groFileReaderAndWriter.readGroFileBox(GRO_WATER_IN_ARGON_PATH)) //
                .withGroFileAtomLines(groFileAtomLines) //
                .build();

        frameCoordinates = frameCoordinatesFromGroFileBuilderSupplier.get() //
                .withGroFileAtomLines(groFileAtomLines) //
                .withFrameNo(1) //
                .build();

        frame = frameFactory.get(frameStructure, frameCoordinates);
    }

    // ======== FRAME INITIALIZATION ========

    @Test
    public void testFrameInitializationWithoutResidues() {
        String description = "Test Frame Initialization Without Residues";

        frameStructure = frameStructureFromScratchBuilderSupplier.get() //
                .withDescription(description) //
                .withFreeAtoms("Ar", 100) //
                .withBox(BOX) //
                .build();

        frameCoordinates = frameCoordinatesFromScratchBuilderSupplier.get() //
                .withFrameStructure(frameStructure) //
                .withFrameNo(1) //
                .build();

        frame = frameFactory.get(frameStructure, frameCoordinates);
        groFileReaderAndWriter.writeGroFile(frame, PATH_GRO_FROM_TESTS, "without-residues.gro");

        verifyFrame(frame, description, 1, BOX);
        verifyFrameAtoms(frame, 100, "Argon", "Argon");
        verifyFrameResidues(frame, 0, null, null);
    }

    @Test
    public void testFrameInitializationFromGroFile() {
        String description = "Test Frame Initialization From Gro File";
        groFileAtomLines = groFileReaderAndWriter.readGroFileAtomLines(GRO_WATER_IN_ARGON_PATH);

        frameStructure = frameStructureFromGroFileBuilderSupplier.get() //
                .withDescription(description) //
                .withGroFileAtomLines(groFileAtomLines) //
                .withBox(BOX) //
                .build();

        frameCoordinates = frameCoordinatesFromGroFileBuilderSupplier.get() //
                .withGroFileAtomLines(groFileAtomLines) //
                .withFrameNo(2) //
                .build();

        frame = frameFactory.get(frameStructure, frameCoordinates);
        groFileReaderAndWriter.writeGroFile(frame, PATH_GRO_FROM_TESTS, "from-gro-file.gro");

        verifyFrame(frame, description, 2, BOX);
        verifyFrameAtoms(frame, 872, "Argon", "Hydrogen");
        verifyFrameResidues(frame, 1, "Water", "Oxygen");
        verifyRadiusVectors(frame, 6227, 7545, 7589);
    }

    @Test
    public void testFrameInitializationFromXtcFile() {
        String description = "Test Frame Initialization From Xtc File";
        groFileAtomLines = groFileReaderAndWriter.readGroFileAtomLines(GRO_WATER_IN_ARGON_PATH);

        frameStructure = frameStructureFromGroFileBuilderSupplier.get() //
                .withDescription(description) //
                .withGroFileAtomLines(groFileAtomLines) //
                .withBox(BOX) //
                .build();

        xtcFileNativeReader.openXtcFile(XTC_WATER_IN_ARGON_PATH);
        frameCoordinates = xtcFileNativeReader.readNextFrame();
        xtcFileNativeReader.closeXtcFile();

        frame = frameFactory.get(frameStructure, frameCoordinates);
        groFileReaderAndWriter.writeGroFile(frame, PATH_GRO_FROM_TESTS, "from-xtc-file.gro");

        verifyFrame(frame, description, 1, BOX);
        verifyFrameAtoms(frame, 872, "Argon", "Hydrogen");
        verifyFrameResidues(frame, 1, "Water", "Oxygen");
        verifyRadiusVectors(frame, 6306, 7847, 7757);
    }

    @Test
    public void testFrameInitializationFromScratch() {
        String description = "Test Frame Initialization From Scratch";

        frameStructure = frameStructureFromScratchBuilderSupplier.get() //
                .withDescription(description) //
                .withResidues("SOL", 50) //
                .withFreeAtoms("Ar", 100) //
                .withBox(BOX) //
                .build();

        frameCoordinates = frameCoordinatesFromScratchBuilderSupplier.get() //
                .withFrameStructure(frameStructure) //
                .withFrameNo(4) //
                .build();

        frame = frameFactory.get(frameStructure, frameCoordinates);
        groFileReaderAndWriter.writeGroFile(frame, PATH_GRO_FROM_TESTS, "from-scratch.gro");

        verifyFrame(frame, description, 4, BOX);
        verifyFrameAtoms(frame, 250, "Argon", "Hydrogen");
        verifyFrameResidues(frame, 50, "Water", "Oxygen");
    }

    @Test
    public void testFrameInitializationFromArrays() {
        String description = "Test Frame Initialization From Arrays";

        frameStructure = frameStructureFromArraysBuilderSupplier.get() //
                .withDescription(description) //
                .withResiduesArray(frame.getResidues()) //
                .withAtomsArray(frame.getAtoms()) //
                .withBox(BOX) //
                .build();

        frameCoordinates = frameCoordinatesFromArraysBuilderSupplier.get() //
                .withAtomsArray(frame.getAtoms()) //
                .withFrameNo(5) //
                .build();

        frame = frameFactory.get(frameStructure, frameCoordinates);
        groFileReaderAndWriter.writeGroFile(frame, PATH_GRO_FROM_TESTS, "from-arrays.gro");

        verifyFrame(frame, description, 5, BOX);
        verifyFrameAtoms(frame, 872, "Argon", "Hydrogen");
        verifyFrameResidues(frame, 1, "Water", "Oxygen");
        verifyRadiusVectors(frame, 6227, 7545, 7589);
    }

    // ======== FRAME GETTERS ========

    @Test
    public void testMinAndMaxAtomsOfFrame() {
        verifyMaxCoordinates(frame, 6.489f, 6.247f, 6.439f);
        verifyMinCoordinates(frame, 2.054f, 2.389f, 2.232f);
    }

    @Test
    public void testAtomsNeighbours() {
        GmxAtom atom = frame.getAtoms()[100];
        GmxAtom expectedNeighbour = frame.getAtoms()[867];
        GmxAtom actualNeighbour = frame.getNeighbourAtom(atom.getCoordinates(), 1);
        float expectedDistance = 0.36857972f;

        Assert.assertEquals(actualNeighbour, expectedNeighbour, "Wrong neighbour atom.");
        Assert.assertEquals((float) atom.euclideanDistanceToAtom(actualNeighbour), expectedDistance);

        actualNeighbour = frame.getAtomsSortedByDistanceToPoint(atom.getCoordinates())[1];
        Assert.assertEquals(actualNeighbour, expectedNeighbour, "Wrong neighbour atom.");
        Assert.assertEquals((float) atom.euclideanDistanceToAtom(actualNeighbour), expectedDistance);

        Assert.assertEquals((float) atom.getRadiusVector(), 7.84798f, "Pivot atom shifted.");
        Assert.assertSame(frame.getAtoms()[100], atom, "Wrong pivot atom index.");
    }

    @Test
    public void testResiduesNeighbours() {
        GmxAtom atom = frame.getAtoms()[100];
        GmxResidue expectedNeighbour = frame.getResidues()[0];
        GmxResidue actualNeighbour = frame.getNeighbourResidue(atom.getCoordinates(), 0);
        Assert.assertEquals(actualNeighbour, expectedNeighbour, "Wrong neighbour residue.");

        actualNeighbour = frame.getResiduesSortedByDistanceToPoint(atom.getCoordinates())[0];
        Assert.assertEquals(actualNeighbour, expectedNeighbour, "Wrong neighbour residue.");
        Assert.assertEquals((float) atom.getRadiusVector(), 7.84798f, "Pivot atom shifted.");
        Assert.assertSame(frame.getAtoms()[100], atom, "Wrong pivot atom index.");
    }

    // ======== FRAME BOX ALTERATION ========

    @Test
    public void testDescriptionSetting() {
        String description = "New description";
        frame.setDescription(description);
        Assert.assertEquals(frame.getDescription(), description, "Wrong description.");
        Assert.assertEquals(frame.getFrameStructure().getDescription(), description, "Wrong description.");
    }

    @Test
    public void testBoxSetting() {
        float[] box = new float[] { 7.f, 8.f, 9.f };
        frame.setBox(box);
        Assert.assertNotSame(frame.getBox() == box, "Frame box is the same object as set.");
        verifyFrame(frame, "Ar+SOL", 1, box);
        verifyCoordinates(frame.getFrameStructure().getBox(), box[0], box[1], box[2]);
    }

    @Test
    public void testBoxRefiningPositiveIncrease() {
        frame.refineBox(3.f);
        verifyCoordinates(frame.getBox(), 10.435f, 9.858f, 10.207f);
        verifyMaxCoordinates(frame, 7.435f, 6.8580003f, 7.207f);
        verifyMinCoordinates(frame, 3.0f, 3.0f, 3.0f);
    }

    @Test
    public void testBoxRefiningPositiveDecrease() {
        frame.refineBox(0.3f);
        verifyMaxCoordinates(frame, 4.7349997f, 4.158f, 4.507f);
        verifyMinCoordinates(frame, 0.29999995f, 0.29999995f, 0.29999995f);
        verifyCoordinates(frame.getBox(), 5.035f, 4.458f, 4.807f);
    }

    @Test
    public void testBoxRefiningNegative() {
        frame.refineBox(-0.3f);
        verifyMaxCoordinates(frame, 4.1350000f, 3.5580003f, 3.9070000f);
        verifyMinCoordinates(frame, -0.29999995f, -0.29999995f, -0.29999995f);
        verifyCoordinates(frame.getBox(), 3.8349998f, 3.2580001f, 3.6069999f);
    }

    @Test
    public void testAtomsOutOfBoxRemoval() {
        frame.setBox(new float[] { 4.5f, 4.5f, 4.5f });
        frame.removeAtomsOutOfBox();
        groFileReaderAndWriter.writeGroFile(frame, PATH_GRO_FROM_TESTS, "out-of-box-removal.gro");

        Assert.assertEquals(frame.getAtomsNum(), 190, "Wrong number of atoms after removal.");
        Assert.assertEquals(frame.getResiduesNum(), 0, "Wrong number of residues after removal.");

        verifyCoordinates(frame.getBox(), 4.5f, 4.5f, 4.5f);
        verifyMaxCoordinates(frame, 4.489f, 4.484f, 4.484f);
        verifyMinCoordinates(frame, 2.054f, 2.389f, 2.232f);
    }

    @Test
    public void testBoxMultiplicationByIntegers() {
        frame.multiplyBox(3, 3, 3);
        groFileReaderAndWriter.writeGroFile(frame, PATH_GRO_FROM_TESTS, "after-multiplication.gro");

        verifyFrame(frame, "Ar+SOL", 1, new float[] { 21.f, 21.f, 21.f });
        verifyFrameAtoms(frame, 23544, "Argon", "Hydrogen");
        verifyFrameResidues(frame, 27, "Water", "Oxygen");
        verifyRadiusVectors(frame, 6227, 31776, 7589);
        Assert.assertEquals((int) (frame.getResidues()[26].getRadiusVector() * 1000), 31824, //
                "Wrong radius-vector of the last residue.");
    }

    // ======== FRAME PARTICLES ALTERATION ========

    @Test
    public void testAtomsAndResiduesReindex() {
        frame.getAtoms()[100].setAtomNo(999);
        frame.getResidues()[0].setResidueNo(999);
        frame.reindexAtoms();
        frame.reindexResidues();
        Assert.assertEquals(frame.getAtoms()[100].getAtomNo(), 100, "Wrong atom index.");
        Assert.assertEquals(frame.getResidues()[0].getResidueNo(), 0, "Wrong residue index.");
    }

    @Test
    public void testRemoveFreeAtoms() {
        frame.removeFreeAtoms(frame.getAtoms()[0], frame.getAtoms()[10], frame.getAtoms()[99]);
        verifyFrame(frame, "Ar+SOL", 1, new float[] { 7.f, 7.f, 7.f });
        verifyFrameAtoms(frame, 869, "Argon", "Hydrogen");
        verifyFrameResidues(frame, 1, "Water", "Oxygen");
        verifyRadiusVectors(frame, 6662, 7545, 7589);
    }

    @Test
    public void testRemoveFrameResidues() {
        frame.removeResidues(frame.getResidues()[0]);
        verifyFrame(frame, "Ar+SOL", 1, new float[] { 7.f, 7.f, 7.f });
        verifyFrameAtoms(frame, 869, "Argon", "Argon");
        verifyFrameResidues(frame, 0, null, null);
    }

    @Test
    public void testAppendFreeAtoms() {
        GmxAtom hydrogen = atomFactory.get(GmxAtomH.class, "H", 0, new float[] { 2.2f, 2.2f, 2.2f });
        GmxAtom oxygen = atomFactory.get(GmxAtomO.class, "O", 0, new float[] { 3.3f, 3.3f, 3.3f });
        frame.appendFreeAtoms(argon, hydrogen, oxygen);

        verifyFrame(frame, "Ar+SOL", 1, new float[] { 7.f, 7.f, 7.f });
        verifyFrameAtoms(frame, 875, "Argon", "Oxygen");
        verifyFrameResidues(frame, 1, "Water", "Oxygen");

        Assert.assertSame(frame.getAtoms()[872], argon, "Atom appended at wrong index.");
        Assert.assertSame(frame.getAtoms()[873], hydrogen, "Atom appended at wrong index.");
        Assert.assertSame(frame.getAtoms()[874], oxygen, "Atom appended at wrong index.");
    }

    @Test
    public void testAppendResidues() {
        frame.appendResidues(water);
        verifyFrame(frame, "Ar+SOL", 1, new float[] { 7.f, 7.f, 7.f });
        verifyFrameAtoms(frame, 875, "Argon", "Hydrogen");
        verifyFrameResidues(frame, 2, "Water", "Oxygen");
        verifyRadiusVectors(frame, 6227, 1685, 7589);
        Assert.assertSame(frame.getResidues()[1], water, "Residue appended at wrong index.");
    }

    @Test
    public void testReplaceAtomsWithAtoms() {
        int firstAtomRvX1000 = (int) (frame.getAtoms()[0].getRadiusVector() * 1000);
        int secondAtomRvX1000 = (int) (frame.getAtoms()[1].getRadiusVector() * 1000);
        int waterResidueRvX1000 = (int) (frame.getResidues()[0].getRadiusVector() * 1000);
        GmxAtom[] atoms = frame.replaceAtomsWithAtoms(GmxAtomO.class, "O", frame.getAtoms()[0]);

        verifyFrame(frame, "Ar+SOL", 1, new float[] { 7.f, 7.f, 7.f });
        verifyFrameAtoms(frame, 872, "Argon", "Oxygen");
        verifyFrameResidues(frame, 1, "Water", "Oxygen");
        verifyRadiusVectors(frame, secondAtomRvX1000, firstAtomRvX1000, waterResidueRvX1000);

        Assert.assertNotNull(atoms, "New atoms are missing.");
        Assert.assertEquals(atoms.length, 1, "Wrong number of new atoms.");
        Assert.assertEquals(atoms[0].getFullName(), "Oxygen", "Wrong type of new atom.");
        Assert.assertEquals((int) (atoms[0].getRadiusVector() * 1000), firstAtomRvX1000, "Wrong RV of new atom.");
    }

    @Test
    public void testReplaceAtomsWithResidues() {
        int firstAtomRvX1000 = (int) (frame.getAtoms()[0].getRadiusVector() * 1000);
        int secondAtomRvX1000 = (int) (frame.getAtoms()[1].getRadiusVector() * 1000);
        int waterResidueRvX1000 = (int) (frame.getResidues()[0].getRadiusVector() * 1000);
        GmxResidue[] residues = frame.replaceAtomsWithResidues(GmxResidueH2O.class, frame.getAtoms()[0]);

        verifyFrame(frame, "Ar+SOL", 1, new float[] { 7.f, 7.f, 7.f });
        verifyFrameAtoms(frame, 874, "Argon", "Hydrogen");
        verifyFrameResidues(frame, 2, "Water", "Oxygen");
        verifyRadiusVectors(frame, secondAtomRvX1000, 6171, waterResidueRvX1000);

        Assert.assertNotNull(residues, "New residues are missing.");
        Assert.assertEquals(residues.length, 1, "Wrong number of new residues.");
        Assert.assertEquals(residues[0].getFullName(), "Water", "Wrong type of new residue.");
        Assert.assertEquals((int) (residues[0].getRadiusVector() * 1000), firstAtomRvX1000, "Wrong RV of new residue.");
    }

    @Test
    public void testReplaceResiduesWithAtoms() {
        double firstAtomRvX1000 = frame.getAtoms()[0].getRadiusVector();
        double waterResidueRvX1000 = frame.getResidues()[0].getRadiusVector();
        GmxAtom[] atoms = frame.replaceResiduesWithAtoms(GmxAtomAr.class, "Ar", frame.getResidues()[0]);

        verifyFrame(frame, "Ar+SOL", 1, new float[] { 7.f, 7.f, 7.f });
        verifyFrameAtoms(frame, 870, "Argon", "Argon");
        verifyFrameResidues(frame, 0, null, null);

        Assert.assertEquals(frame.getAtoms()[0].getRadiusVector(), firstAtomRvX1000, "Wrong first atom radius-vector.");
        Assert.assertEquals(frame.getAtoms()[869].getRadiusVector(), waterResidueRvX1000, "Wrong last atom radius-vector.");

        Assert.assertNotNull(atoms, "New atoms are missing.");
        Assert.assertEquals(atoms.length, 1, "Wrong number of new atoms.");
        Assert.assertEquals(atoms[0].getFullName(), "Argon", "Wrong type of new atom.");
        Assert.assertEquals(atoms[0].getRadiusVector(), waterResidueRvX1000, "Wrong RV of new atom.");
    }

    @Test
    public void testReplaceResiduesWithResidues() {
        int firstAtomRvX1000 = (int) (frame.getAtoms()[0].getRadiusVector() * 1000);
        int waterResidueRvX1000 = (int) (frame.getResidues()[0].getRadiusVector() * 1000);
        GmxResidue[] residues = frame.replaceResiduesWithResidues(GmxResidueH2O.class, frame.getResidues()[0]);

        verifyFrame(frame, "Ar+SOL", 1, new float[] { 7.f, 7.f, 7.f });
        verifyFrameAtoms(frame, 872, "Argon", "Hydrogen");
        verifyFrameResidues(frame, 1, "Water", "Oxygen");
        verifyRadiusVectors(frame, firstAtomRvX1000, 7545, waterResidueRvX1000);

        Assert.assertNotNull(residues, "New residues are missing.");
        Assert.assertEquals(residues.length, 1, "Wrong number of new residues.");
        Assert.assertEquals(residues[0].getFullName(), "Water", "Wrong type of new residue.");
        Assert.assertEquals((int) (residues[0].getRadiusVector() * 1000), waterResidueRvX1000, "Wrong RV of new residue.");
    }

    // ======== NEGATIVE TESTS ========

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_NO_STRUCTURE)
    public void testFrameInitializationWithoutStructure() {
        frameFactory.get(null, frameCoordinates);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_NO_COORDINATES)
    public void testFrameInitializationWithoutCoordinates() {
        frameFactory.get(frameStructure, null);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_INVALID_ORDER)
    public void testNeighborAtomWithInvalidOrder() {
        frame.getNeighbourAtom(new float[] { 3.f, 3.f, 3.f }, -1);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_INVALID_POINT)
    public void testNeighborAtomWithInvalidPoint() {
        frame.getNeighbourAtom(new float[] {}, 1);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_INVALID_ORDER)
    public void testNeighborResidueWithInvalidOrder() {
        frame.getNeighbourResidue(new float[] { 3.f, 3.f, 3.f }, -1);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_INVALID_POINT)
    public void testNeighborResidueWithInvalidPoint() {
        frame.getNeighbourResidue(new float[] {}, 1);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_INVALID_POINT)
    public void testNeighborAtomsSortedByDistanceToInvalidPoint() {
        frame.getAtomsSortedByDistanceToPoint(new float[] {});
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_INVALID_POINT)
    public void testNeighborResiduesSortedByDistanceToInvalidPoint() {
        frame.getResiduesSortedByDistanceToPoint(new float[] {});
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_ATOM_NOT_FREE)
    public void testRemoveAtomNotFree() {
        frame.removeFreeAtoms(frame.getAtoms()[870]);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_NOT_IN_FRAME)
    public void testRemoveAtomNotInFrame() {
        frame.removeFreeAtoms(frame.getAtoms()[0], argon, frame.getAtoms()[99]);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_NOT_IN_FRAME)
    public void testRemoveResidueNotInFrame() {
        frame.removeResidues(frame.getResidues()[0], water);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_DUPLICATE)
    public void testAppendDuplicateArgumentAtoms() {
        frame.appendFreeAtoms(argon, argon);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_DUPLICATE)
    public void testAppendDuplicateFrameAtoms() {
        frame.appendFreeAtoms(argon, frame.getAtoms()[100]);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_DUPLICATE)
    public void testAppendDuplicateArgumentResidues() {
        frame.appendResidues(water, water);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_DUPLICATE)
    public void testAppendDuplicateFrameResidues() {
        frame.appendResidues(water, frame.getResidues()[0]);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_ATOM_NOT_FREE)
    public void testReplaceNotFreeAtomsWithAtoms() {
        frame.replaceAtomsWithAtoms(GmxAtomO.class, "O", frame.getAtoms()[871]);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_NOT_IN_FRAME)
    public void testReplaceNotInFrameAtomsWithAtoms() {
        frame.replaceAtomsWithAtoms(GmxAtomO.class, "O", argon);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_NOT_IN_FRAME)
    public void testReplaceDuplicateAtomsWithAtoms() {
        frame.replaceAtomsWithAtoms(GmxAtomO.class, "O", frame.getAtoms()[0], frame.getAtoms()[0]);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_NOT_IN_FRAME)
    public void testReplaceNotInFrameResiduesWithAtoms() {
        frame.replaceResiduesWithAtoms(GmxAtomO.class, "O", water);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_NOT_IN_FRAME)
    public void testReplaceDuplicateResiduesWithAtoms() {
        frame.replaceResiduesWithAtoms(GmxAtomO.class, "O", frame.getResidues()[0], frame.getResidues()[0]);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_ATOM_NOT_FREE)
    public void testReplaceNotFreeAtomsWithResidues() {
        frame.replaceAtomsWithResidues(GmxResidueH2O.class, frame.getAtoms()[871]);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_NOT_IN_FRAME)
    public void testReplaceNotInFrameAtomsWithResidues() {
        frame.replaceAtomsWithResidues(GmxResidueH2O.class, argon);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_NOT_IN_FRAME)
    public void testReplaceDuplicateAtomsWithResidues() {
        frame.replaceAtomsWithResidues(GmxResidueH2O.class, frame.getAtoms()[0], frame.getAtoms()[0]);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_NOT_IN_FRAME)
    public void testReplaceNotInFrameResiduesWithResidues() {
        frame.replaceResiduesWithResidues(GmxResidueH2O.class, water);
    }

    @Test(expectedExceptions = GmxFrameException.class, expectedExceptionsMessageRegExp = REGEX_NOT_IN_FRAME)
    public void testReplaceDuplicateResiduesWithResidues() {
        frame.replaceResiduesWithResidues(GmxResidueH2O.class, frame.getResidues()[0], frame.getResidues()[0]);
    }

    // ======== VALIDATORS ========

    private void verifyFrame(GmxFrame frame, String description, int frameNo, float[] box) {
        Assert.assertEquals(frame.getDescription(), description, "Wrong description");
        Assert.assertEquals(frame.getFrameNo(), frameNo, "Wrong frame No.");
        Assert.assertEquals(frame.getBox()[0], box[0], "Wrong X value of box.");
        Assert.assertEquals(frame.getBox()[1], box[1], "Wrong Y value of box.");
        Assert.assertEquals(frame.getBox()[2], box[2], "Wrong Z value of box.");
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyFrameAtoms(GmxFrame frame, int atomsNum, String firstAtomName, String lastAtomNAme) {
        Assert.assertEquals(frame.getFrameCoordinates().getCoordinates().length, atomsNum, "Wrong number of coordinates.");
        Assert.assertEquals(frame.getAtomsNum(), atomsNum, "Wrong atoms number.");
        Assert.assertEquals(frame.getAtoms().length, atomsNum, "Wrong atoms number.");
        Assert.assertEquals(frame.getAtoms()[0].getFullName(), firstAtomName, "Wrong first atom type.");
        Assert.assertEquals(frame.getAtoms()[0].getAtomNo(), 0, "Wrong atoms indexation.");
        Assert.assertEquals(frame.getAtoms()[atomsNum - 1].getFullName(), lastAtomNAme, "Wrong last atom type.");
        Assert.assertEquals(frame.getAtoms()[atomsNum - 1].getAtomNo(), atomsNum - 1, "Wrong atoms indexation.");
    }

    private void verifyFrameResidues(GmxFrame frame, int residuesNum, String firstResidueName, String firstResidueAcceptor) {
        Assert.assertEquals(frame.getResiduesNum(), residuesNum, "Wrong residues number.");
        Assert.assertEquals(frame.getResidues().length, residuesNum, "Wrong residues number.");
        if (residuesNum > 0) {
            Assert.assertEquals(frame.getResidues()[0].getFullName(), firstResidueName, "Wrong residue type.");
            Assert.assertEquals(frame.getResidues()[0].getAcceptorAtom().getFullName(), firstResidueAcceptor, "Wrong acceptor type.");
        }
    }

    private void verifyRadiusVectors(GmxFrame frame, int firstAtomRvX1000, int lastAtomRvX1000, int firstResidueRvX1000) {
        int i = frame.getAtomsNum() - 1;
        Assert.assertEquals((int) (frame.getAtoms()[0].getRadiusVector() * 1000), firstAtomRvX1000, "Wrong first atom radius-vector.");
        Assert.assertEquals((int) (frame.getAtoms()[i].getRadiusVector() * 1000), lastAtomRvX1000, "Wrong last atom radius-vector.");
        Assert.assertEquals((int) (frame.getResidues()[0].getRadiusVector() * 1000), firstResidueRvX1000, "Wrong residue radius-vector.");
    }

    private void verifyMaxCoordinates(GmxFrame frame, float maxX, float maxY, float maxZ) {
        Assert.assertEquals(frame.getAtomWithMaximumX().getCoordinateX(), maxX, "Wrong maximum X coordinate.");
        Assert.assertEquals(frame.getAtomWithMaximumY().getCoordinateY(), maxY, "Wrong maximum Y coordinate.");
        Assert.assertEquals(frame.getAtomWithMaximumZ().getCoordinateZ(), maxZ, "Wrong maximum Z coordinate.");
    }

    private void verifyMinCoordinates(GmxFrame frame, float minX, float minY, float minZ) {
        Assert.assertEquals(frame.getAtomWithMinimumX().getCoordinateX(), minX, "Wrong minimum X coordinate.");
        Assert.assertEquals(frame.getAtomWithMinimumY().getCoordinateY(), minY, "Wrong minimum Y coordinate.");
        Assert.assertEquals(frame.getAtomWithMinimumZ().getCoordinateZ(), minZ, "Wrong minimum Z coordinate.");
    }
}
