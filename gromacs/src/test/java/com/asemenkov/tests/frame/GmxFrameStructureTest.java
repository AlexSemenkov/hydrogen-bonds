package com.asemenkov.tests.frame;

import com.asemenkov.gromacs.frame.exceptions.GmxFrameException;
import com.asemenkov.gromacs.frame.structure.GmxFrameStructure;
import com.asemenkov.gromacs.io.exceptions.GmxIoException;
import com.asemenkov.gromacs.io.gro.GmxGroFileAtomLine;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;
import com.asemenkov.particles.atoms.GmxAtomAr;
import com.asemenkov.particles.atoms.GmxAtomH;
import com.asemenkov.particles.atoms.GmxAtomO;
import com.asemenkov.particles.residues.GmxResidueH2O;
import com.asemenkov.tests.config.GmxAbstractTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author asemenkov
 * @since May 8, 2018
 */
@Test
public class GmxFrameStructureTest extends GmxAbstractTest {

    // ======== TEST FRAME STRUCTURE FROM SCRATCH ========

    @Test
    public void testFrameStructureFromScratch() {
        GmxFrameStructure frameStructure = frameStructureFromScratchBuilderSupplier.get() //
                .withDescription("From scratch") //
                .withFreeAtoms("Ar", 200000) //
                .withResidues("SOL", 200000) //
                .withBox(BOX) //
                .build();

        int[] indexes = new int[] { 199999, 200000, 200002 };
        verifyStructureMeta(frameStructure, "From scratch", 800000);
        verifyAtomsSequence(frameStructure, 800000, indexes, new Class[] { GmxAtomAr.class, GmxAtomO.class, GmxAtomH.class });
        verifyAbbreviationsSequence(frameStructure, 800000, indexes, new String[] { "Ar", "OW", "HW2" });
        verifyResiduesSequence(frameStructure, 200000, 1);
    }

    @Test
    public void testFrameStructureFromScratchWithoutFreeAtoms() {
        GmxFrameStructure frameStructure = frameStructureFromScratchBuilderSupplier.get() //
                .withDescription("From scratch") //
                .withResidues("SOL", 200) //
                .withBox(BOX) //
                .build();

        int[] indexes = new int[] { 0, 2, 599 };
        verifyStructureMeta(frameStructure, "From scratch", 600);
        verifyAtomsSequence(frameStructure, 600, indexes, new Class[] { GmxAtomO.class, GmxAtomH.class, GmxAtomH.class });
        verifyAbbreviationsSequence(frameStructure, 600, indexes, new String[] { "OW", "HW2", "HW2" });
        verifyResiduesSequence(frameStructure, 200, 1);
    }

    @Test
    public void testFrameStructureFromScratchWithoutResidues() {
        GmxFrameStructure frameStructure = frameStructureFromScratchBuilderSupplier.get() //
                .withDescription("From scratch") //
                .withFreeAtoms("Ar", 1000) //
                .withBox(BOX) //
                .build();

        Assert.assertTrue(frameStructure.getResidueAtomsMap().isEmpty(), "ResidueAtomsMap must be empty.");
        Assert.assertTrue(frameStructure.getResidueIndexesMap().isEmpty(), "ResidueIndexesMap must be empty.");

        int[] indexes = new int[] { 0, 999 };
        verifyStructureMeta(frameStructure, "From scratch", 1000);
        verifyAtomsSequence(frameStructure, 1000, indexes, new Class[] { GmxAtomAr.class, GmxAtomAr.class });
        verifyAbbreviationsSequence(frameStructure, 1000, indexes, new String[] { "Ar", "Ar" });
    }

    @Test(expectedExceptions = GmxIoException.class)
    public void testFrameStructureFromScratchWithoutBox() {
        frameStructureFromScratchBuilderSupplier.get() //
                .withDescription("From scratch") //
                .withFreeAtoms("Ar", 1000) //
                .withResidues("SOL", 200) //
                .build();
    }

    @Test(expectedExceptions = GmxFrameException.class)
    public void testFrameStructureFromScratchWithInvalidFreeAtom() {
        frameStructureFromScratchBuilderSupplier.get() //
                .withDescription("From scratch") //
                .withFreeAtoms("Argon", 1000) //
                .withResidues("SOL", 200) //
                .withBox(BOX) //
                .build();
    }

    // ======== TEST FRAME STRUCTURE FROM GRO FILE ========

    @Test
    public void testFrameStructureFromGroFile() {
        List<GmxGroFileAtomLine> groFileAtomLines = groFileReader.readGroFileAtomLines(GRO_WATER_IN_ARGON_PATH);

        GmxFrameStructure frameStructure = frameStructureFromGroFileBuilderSupplier.get() //
                .withDescription("From .gro file") //
                .withGroFileAtomLines(groFileAtomLines) //
                .withBox(BOX) //
                .build();

        int[] indexes = new int[] { 868, 869, 870 };
        verifyStructureMeta(frameStructure, "From .gro file", 872);
        verifyAtomsSequence(frameStructure, 872, indexes, new Class[] { GmxAtomAr.class, GmxAtomO.class, GmxAtomH.class });
        verifyAbbreviationsSequence(frameStructure, 872, indexes, new String[] { "Ar", "OW", "HW1" });
        verifyResiduesSequence(frameStructure, 1, 869);
    }

    @Test
    public void testFrameStructureFromGroFileWithoutResidue() {
        List<GmxGroFileAtomLine> groFileAtomLines = groFileReader.readGroFileAtomLines(GRO_ARGON_PATH);

        GmxFrameStructure frameStructure = frameStructureFromGroFileBuilderSupplier.get() //
                .withDescription("From .gro file") //
                .withGroFileAtomLines(groFileAtomLines) //
                .withBox(BOX) //
                .build();

        Assert.assertTrue(frameStructure.getResidueAtomsMap().isEmpty(), "ResidueAtomsMap must be empty.");
        Assert.assertTrue(frameStructure.getResidueIndexesMap().isEmpty(), "ResidueIndexesMap must be empty.");

        int[] indexes = new int[] { 0, 869 };
        verifyStructureMeta(frameStructure, "From .gro file", 870);
        verifyAtomsSequence(frameStructure, 870, indexes, new Class[] { GmxAtomAr.class, GmxAtomAr.class });
        verifyAbbreviationsSequence(frameStructure, 870, indexes, new String[] { "Ar", "Ar" });
    }

    @Test
    public void testFrameStructureFromGroFileWithoutFreeAtoms() {
        List<GmxGroFileAtomLine> groFileAtomLines = groFileReader.readGroFileAtomLines(GRO_WATER_PATH);

        GmxFrameStructure frameStructure = frameStructureFromGroFileBuilderSupplier.get() //
                .withDescription("From .gro file") //
                .withGroFileAtomLines(groFileAtomLines) //
                .withBox(BOX) //
                .build();

        int[] indexes = new int[] { 0, 5 };
        verifyStructureMeta(frameStructure, "From .gro file", 6);
        verifyAtomsSequence(frameStructure, 6, indexes, new Class[] { GmxAtomO.class, GmxAtomH.class });
        verifyAbbreviationsSequence(frameStructure, 6, indexes, new String[] { "OW", "HW2" });
        verifyResiduesSequence(frameStructure, 2, 0);
    }

    @Test(expectedExceptions = GmxIoException.class)
    public void testFrameStructureFromGroFileWithoutBox() {
        List<GmxGroFileAtomLine> groFileAtomLines = groFileReader.readGroFileAtomLines(GRO_WATER_IN_ARGON_PATH);

        frameStructureFromGroFileBuilderSupplier.get() //
                .withDescription("From .gro file") //
                .withGroFileAtomLines(groFileAtomLines) //
                .build();
    }

    @Test(expectedExceptions = GmxFrameException.class)
    public void testFrameStructureFromGroFileGroFileAtomLines() {
        frameStructureFromScratchBuilderSupplier.get() //
                .withDescription("From .gro file") //
                .withBox(BOX) //
                .build();
    }

    // ======== TEST FRAME STRUCTURE FROM ARRAYS ========

    @Test
    public void testFrameStructureFromArrays() {
        GmxAtom[] atoms = new GmxAtom[800000];
        GmxResidue[] residues = new GmxResidue[200000];

        IntStream.iterate(0, i -> i + 4).parallel().limit(200000).forEach(i -> {
            atoms[i] = atomFactory.get(GmxAtomAr.class, "Ar", i, null);
            atoms[i + 1] = atomFactory.get(GmxAtomO.class, "OW", i + 1, null);
            atoms[i + 2] = atomFactory.get(GmxAtomH.class, "HW1", i + 2, null);
            atoms[i + 3] = atomFactory.get(GmxAtomH.class, "HW2", i + 3, null);
            residues[i / 4] = residueByAtomsFactory.get(GmxResidueH2O.class, i / 4, Arrays.copyOfRange(atoms, i + 1, i + 4));
        });

        GmxFrameStructure frameStructure = frameStructureFromArraysBuilderSupplier.get() //
                .withDescription("From arrays") //
                .withAtomsArray(atoms) //
                .withResiduesArray(residues) //
                .withBox(BOX) //
                .build();

        int[] indexes = new int[] { 100000, 100001, 100002 };
        verifyStructureMeta(frameStructure, "From arrays", 800000);
        verifyAtomsSequence(frameStructure, 800000, indexes, new Class[] { GmxAtomAr.class, GmxAtomO.class, GmxAtomH.class });
        verifyAbbreviationsSequence(frameStructure, 800000, indexes, new String[] { "Ar", "OW", "HW1" });
        verifyResiduesSequence(frameStructure, 200000, 199999);
    }

    @Test
    public void testFrameStructureFromArraysWithoutResidues() {
        GmxAtom[] atoms = new GmxAtom[2000];

        IntStream.iterate(0, i -> i + 4).parallel().limit(500).forEach(i -> {
            atoms[i] = atomFactory.get(GmxAtomAr.class, "Ar", i, null);
            atoms[i + 1] = atomFactory.get(GmxAtomO.class, "O", i + 1, null);
            atoms[i + 2] = atomFactory.get(GmxAtomH.class, "H", i + 2, null);
            atoms[i + 3] = atomFactory.get(GmxAtomH.class, "H", i + 3, null);
        });

        GmxFrameStructure frameStructure = frameStructureFromArraysBuilderSupplier.get() //
                .withDescription("From arrays") //
                .withAtomsArray(atoms) //
                .withBox(BOX) //
                .build();

        Assert.assertTrue(frameStructure.getResidueAtomsMap().isEmpty(), "ResidueAtomsMap must be empty.");
        Assert.assertTrue(frameStructure.getResidueIndexesMap().isEmpty(), "ResidueIndexesMap must be empty.");

        int[] indexes = new int[] { 1996, 1997, 1998 };
        verifyStructureMeta(frameStructure, "From arrays", 2000);
        verifyAtomsSequence(frameStructure, 2000, indexes, new Class[] { GmxAtomAr.class, GmxAtomO.class, GmxAtomH.class });
        verifyAbbreviationsSequence(frameStructure, 2000, indexes, new String[] { "Ar", "O", "H" });
    }

    @Test
    public void testFrameStructureFromArraysWithoutFreeAtoms() {
        GmxAtom[] atoms = new GmxAtom[900];
        GmxResidue[] residues = new GmxResidue[300];

        IntStream.iterate(0, i -> i + 3).parallel().limit(300).forEach(i -> {
            atoms[i] = atomFactory.get(GmxAtomO.class, "OW", i, null);
            atoms[i + 1] = atomFactory.get(GmxAtomH.class, "HW1", i + 1, null);
            atoms[i + 2] = atomFactory.get(GmxAtomH.class, "HW2", i + 2, null);
            residues[i / 3] = residueByAtomsFactory.get(GmxResidueH2O.class, i / 3, Arrays.copyOfRange(atoms, i, i + 3));
        });

        GmxFrameStructure frameStructure = frameStructureFromArraysBuilderSupplier.get() //
                .withDescription("From arrays") //
                .withAtomsArray(atoms) //
                .withResiduesArray(residues) //
                .withBox(BOX) //
                .build();

        int[] indexes = new int[] { 897, 898, 899 };
        verifyStructureMeta(frameStructure, "From arrays", 900);
        verifyAtomsSequence(frameStructure, 900, indexes, new Class[] { GmxAtomO.class, GmxAtomH.class, GmxAtomH.class });
        verifyAbbreviationsSequence(frameStructure, 900, indexes, new String[] { "OW", "HW1", "HW2" });
        verifyResiduesSequence(frameStructure, 300, 299);
    }

    @Test(expectedExceptions = GmxIoException.class)
    public void testFrameStructureFromArraysWithoutBox() {
        frameStructureFromArraysBuilderSupplier.get() //
                .withDescription("From arrays") //
                .withAtomsArray(new GmxAtom[] { atomFactory.get(GmxAtomO.class, "O", 0, null) }) //
                .build();
    }

    @Test(expectedExceptions = GmxFrameException.class)
    public void testFrameStructureFromArraysWithoutAtoms() {
        GmxResidue[] residues = new GmxResidue[300];
        frameStructureFromArraysBuilderSupplier.get() //
                .withDescription("From arrays") //
                .withResiduesArray(residues) //
                .withBox(BOX) //
                .build();
    }

    @Test(expectedExceptions = GmxFrameException.class)
    public void testFrameStructureFromArraysWithInvalidAtomNo() {
        GmxAtom[] atoms = IntStream.range(0, 10) //
                .mapToObj(i -> atomFactory.get(GmxAtomO.class, "O", i + 1, null)) //
                .toArray(GmxAtom[]::new);

        frameStructureFromArraysBuilderSupplier.get() //
                .withDescription("From arrays") //
                .withAtomsArray(atoms) //
                .withBox(BOX) //
                .build();
    }

    @Test(expectedExceptions = GmxFrameException.class)
    public void testFrameStructureFromArraysWithResidueAtomDuplicate() {
        GmxAtom o = atomFactory.get(GmxAtomO.class, "O", 0, null);
        GmxAtom h = atomFactory.get(GmxAtomH.class, "H", 1, null);
        GmxResidue h2o = residueByAtomsFactory.get(GmxResidueH2O.class, 0, new GmxAtom[] { o, h, h });

        frameStructureFromArraysBuilderSupplier.get() //
                .withDescription("From arrays") //
                .withAtomsArray(new GmxAtom[] { o, h }) //
                .withResiduesArray(new GmxResidue[] { h2o }) //
                .withBox(BOX) //
                .build();
    }

    @Test(expectedExceptions = GmxFrameException.class)
    public void testFrameStructureFromArraysWithResidueAtomOutOfArray() {
        GmxAtom o = atomFactory.get(GmxAtomO.class, "O", 0, null);
        GmxAtom h1 = atomFactory.get(GmxAtomH.class, "H", 1, null);
        GmxAtom h2 = atomFactory.get(GmxAtomH.class, "H", 2, null);
        GmxAtom h3 = atomFactory.get(GmxAtomH.class, "H", 3, null);
        GmxResidue h2o = residueByAtomsFactory.get(GmxResidueH2O.class, 0, new GmxAtom[] { o, h1, h2 });

        frameStructureFromArraysBuilderSupplier.get() //
                .withDescription("From arrays") //
                .withAtomsArray(new GmxAtom[] { o, h1, h3 }) //
                .withResiduesArray(new GmxResidue[] { h2o }) //
                .withBox(BOX) //
                .build();
    }

    // ======== VALIDATORS ========

    private void verifyStructureMeta(GmxFrameStructure frameStructure, String description, Integer atomsNum) {
        Assert.assertEquals(frameStructure.getBox(), BOX, "Wrong box.");
        Assert.assertEquals(frameStructure.getDescription(), description, "Wrong description.");
        Assert.assertEquals(frameStructure.getAtomsNum(), atomsNum, "Wrong atoms number.");
    }

    private void verifyAtomsSequence(GmxFrameStructure frameStructure, int length, int[] indexes, Class[] classes) {
        Assert.assertEquals(frameStructure.getAtomsSequence().length, length, "Wrong atoms sequence.");
        Assert.assertEquals(indexes.length, classes.length, "Different lengths of indexes and classes arrays.");
        IntStream.range(0, indexes.length).forEach(i -> Assert //
                .assertEquals(frameStructure.getAtomsSequence()[indexes[i]], classes[i], "Wrong atoms sequence."));
    }

    private void verifyAbbreviationsSequence(GmxFrameStructure frameStructure, int length, int[] indexes, String[] abbreviations) {
        Assert.assertEquals(frameStructure.getAtomAbbreviationsSequence().length, length, "Wrong atoms abbreviation.");
        Assert.assertEquals(indexes.length, abbreviations.length, "Different lengths of indexes and abbreviations arrays.");
        IntStream.range(0, indexes.length).forEach(i -> Assert //
                .assertEquals(frameStructure.getAtomAbbreviationsSequence()[indexes[i]], abbreviations[i], "Wrong atoms sequence."));
    }

    private void verifyResiduesSequence(GmxFrameStructure frameStructure, int residuesNum, int residueIndex) {
        Assert.assertEquals(frameStructure.getResidueIndexesMap().keySet().size(), 1, "Wrong residue index map.");
        Assert.assertEquals(frameStructure.getResidueIndexesMap().get(GmxResidueH2O.class).length, residuesNum);
        Assert.assertEquals(frameStructure.getResidueAtomsMap().keySet().size(), residuesNum, "Wrong residue atoms map.");
        Assert.assertEquals(frameStructure.getResidueAtomsMap().get(residueIndex).length, 3, "Wrong residue.");
    }

}
