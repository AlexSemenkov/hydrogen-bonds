package com.asemenkov.tests;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.asemenkov.gromacs.frame.exceptions.GmxFrameException;
import com.asemenkov.gromacs.io.exceptions.GmxIoException;
import com.asemenkov.gromacs.frame.coordinates.GmxFrameCoordinates;
import com.asemenkov.gromacs.frame.structure.GmxFrameStructure;
import com.asemenkov.gromacs.io.GmxGroFileAtomLine;
import com.asemenkov.gromacs.io.GmxGroFileReaderAndWriter;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.waterinargon.GmxAtomH;
import com.asemenkov.waterinargon.GmxAtomO;

/**
 * @author asemenkov
 * @since May 8, 2018
 */
@Test
public class GmxFrameCoordinatesTest extends GmxAbstractTest {

    // ======== TEST FRAME STRUCTURE FROM SCRATCH ========

    @Test
    public void testFrameCoordinatesFromScratch() {
        GmxFrameStructure frameStructure = frameStructureFromScratchBuilderSupplier.get() //
                .withDescription("From scratch") //
                .withFreeAtoms("Ar", 200000) //
                .withResidues("SOL", 200000) //
                .withBox(BOX) //
                .build();

        GmxFrameCoordinates frameCoordinates = frameCoordinatesBuilderSupplier.get() //
                .withFrameStructure(frameStructure) //
                .withFrameNo(100) //
                .buildFromScratch();

        Assert.assertNotEquals(frameCoordinates.getCoordinates()[0][0], frameCoordinates.getCoordinates()[799999][0]);
        Assert.assertNotEquals(frameCoordinates.getCoordinates()[0][1], frameCoordinates.getCoordinates()[799999][1]);
        Assert.assertNotEquals(frameCoordinates.getCoordinates()[0][2], frameCoordinates.getCoordinates()[799999][2]);
        verifyFrameCoordinates(frameCoordinates, 100, 800000);
    }

    @Test
    public void testFrameCoordinatesFromScratchWithoutResidues() {
        GmxFrameStructure frameStructure = frameStructureFromScratchBuilderSupplier.get() //
                .withDescription("From scratch") //
                .withFreeAtoms("Ar", 2000) //
                .withBox(BOX) //
                .build();

        GmxFrameCoordinates frameCoordinates = frameCoordinatesBuilderSupplier.get() //
                .withFrameStructure(frameStructure) //
                .withFrameNo(110) //
                .buildFromScratch();

        verifyFrameCoordinates(frameCoordinates, 110, 2000);
    }

    @Test
    public void testFrameCoordinatesFromScratchWithoutFreeAtoms() {
        GmxFrameStructure frameStructure = frameStructureFromScratchBuilderSupplier.get() //
                .withDescription("From scratch") //
                .withResidues("SOL", 2000) //
                .withBox(BOX) //
                .build();

        GmxFrameCoordinates frameCoordinates = frameCoordinatesBuilderSupplier.get() //
                .withFrameStructure(frameStructure) //
                .withFrameNo(111) //
                .buildFromScratch();

        verifyFrameCoordinates(frameCoordinates, 111, 6000);
    }

    @Test(expectedExceptions = GmxFrameException.class)
    public void testFrameCoordinatesFromScratchWithoutFrameStructure() {
        frameCoordinatesBuilderSupplier.get().withFrameNo(110).buildFromScratch();
    }

    // ======== TEST FRAME STRUCTURE FROM GRO FILE ========

    @Test
    public void testFrameCoordinatesFromGroFile() {
        List<GmxGroFileAtomLine> groFileAtomLines = new GmxGroFileReaderAndWriter().readGroFileAtomLines(GRO_WATER_IN_ARGON_PATH);

        GmxFrameCoordinates frameCoordinates = frameCoordinatesBuilderSupplier.get() //
                .withGroFileAtomLines(groFileAtomLines) //
                .withFrameNo(123) //
                .buildFromGroFile();

        verifyFrameCoordinates(frameCoordinates, 123, 872);
        verifyCoordinates(frameCoordinates.getCoordinates()[0], 3.804f, 2.449f, 4.279f);
        verifyCoordinates(frameCoordinates.getCoordinates()[871], 3.872f, 4.472f, 4.685f);
    }

    @Test
    public void testFrameCoordinatesFromGroFileWithoutResidues() {
        List<GmxGroFileAtomLine> groFileAtomLines = new GmxGroFileReaderAndWriter().readGroFileAtomLines(GRO_ARGON_PATH);

        GmxFrameCoordinates frameCoordinates = frameCoordinatesBuilderSupplier.get() //
                .withGroFileAtomLines(groFileAtomLines) //
                .withFrameNo(987) //
                .buildFromGroFile();

        verifyFrameCoordinates(frameCoordinates, 987, 870);
        verifyCoordinates(frameCoordinates.getCoordinates()[0], 2.339f, 4.123f, 4.722f);
        verifyCoordinates(frameCoordinates.getCoordinates()[869], 2.645f, 4.468f, 5.608f);
    }

    @Test
    public void testFrameCoordinatesFromGroFileWithoutFreeAtoms() {
        List<GmxGroFileAtomLine> groFileAtomLines = new GmxGroFileReaderAndWriter().readGroFileAtomLines(GRO_WATER_PATH);

        GmxFrameCoordinates frameCoordinates = frameCoordinatesBuilderSupplier.get() //
                .withGroFileAtomLines(groFileAtomLines) //
                .withFrameNo(5000) //
                .buildFromGroFile();

        verifyFrameCoordinates(frameCoordinates, 5000, 6);
        verifyCoordinates(frameCoordinates.getCoordinates()[0], 3.970f, 4.455f, 4.689f);
        verifyCoordinates(frameCoordinates.getCoordinates()[5], 4.872f, 3.472f, 3.685f);
    }

    @Test(expectedExceptions = GmxIoException.class)
    public void testFrameCoordinatesFromGroFileWithoutGroFileAtomLines() {
        frameCoordinatesBuilderSupplier.get().withFrameNo(110).buildFromGroFile();
    }

    // ======== TEST FRAME STRUCTURE FROM ARRAYS ========

    @Test
    public void testFrameCoordinatesFromAtomsArray() {
        GmxAtom[] atoms = new GmxAtom[200000];
        IntStream.iterate(0, i -> i + 2).parallel().limit(100000).forEach(i -> {
            Random r = new Random(i);
            float[] oCoordinates = new float[] { r.nextFloat(), r.nextFloat(), r.nextFloat() };
            float[] hCoordinates = new float[] { r.nextFloat(), r.nextFloat(), r.nextFloat() };
            atoms[i] = atomFactory.get(GmxAtomO.class, "O", i, oCoordinates);
            atoms[i + 1] = atomFactory.get(GmxAtomH.class, "H", i + 1, hCoordinates);
        });

        GmxFrameCoordinates frameCoordinates = frameCoordinatesBuilderSupplier.get() //
                .withAtomsArray(atoms) //
                .withFrameNo(2) //
                .buildFromArray();

        verifyFrameCoordinates(frameCoordinates, 2, 200000);
        verifyCoordinates(frameCoordinates.getCoordinates()[0], 0.73096776f, 0.831441f, 0.24053639f);
        verifyCoordinates(frameCoordinates.getCoordinates()[199999], 0.43030268f, 0.44900858f, 0.067195535f);
    }

    @Test(expectedExceptions = GmxFrameException.class)
    public void testFrameCoordinatesFromAtomsArrayWithoutArray() {
        frameCoordinatesBuilderSupplier.get().withFrameNo(110).buildFromArray();
    }

    private void verifyFrameCoordinates(GmxFrameCoordinates frameCoordinates, int frameNo, int length) {
        Assert.assertEquals(frameCoordinates.getFrameNo(), frameNo, "Wrong frameNo.");
        Assert.assertEquals(frameCoordinates.getCoordinates().length, length, "Wrong number of coordinates.");
        Arrays.stream(frameCoordinates.getCoordinates()).parallel().forEach(coordinates -> {
            Assert.assertEquals(coordinates.length, 3, "Wrong coordinates length.");
            Assert.assertTrue(coordinates[0] >= 0, "Wrong X coordinate.");
            Assert.assertTrue(coordinates[1] >= 0, "Wrong Y coordinate.");
            Assert.assertTrue(coordinates[2] >= 0, "Wrong Z coordinate.");
            Assert.assertTrue(coordinates[0] <= BOX[0], "Wrong X coordinate.");
            Assert.assertTrue(coordinates[1] <= BOX[1], "Wrong Y coordinate.");
            Assert.assertTrue(coordinates[2] <= BOX[2], "Wrong Z coordinate.");
        });
    }
}
