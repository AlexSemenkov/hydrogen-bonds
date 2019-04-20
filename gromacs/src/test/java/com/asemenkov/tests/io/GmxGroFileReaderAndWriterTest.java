package com.asemenkov.tests.io;

import com.asemenkov.gromacs.frame.GmxFrame;
import com.asemenkov.gromacs.frame.coordinates.GmxFrameCoordinates;
import com.asemenkov.gromacs.frame.structure.GmxFrameStructure;
import com.asemenkov.gromacs.io.gro.GmxGroFileAtomLine;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;
import com.asemenkov.particles.atoms.GmxAtomH;
import com.asemenkov.particles.residues.GmxResidueH2O;
import com.asemenkov.tests.config.GmxAbstractTest;
import com.asemenkov.utils.io.FileUtils;
import com.asemenkov.utils.io.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * @author asemenkov
 * @since Apr 16, 2018
 */
@Test
public class GmxGroFileReaderAndWriterTest extends GmxAbstractTest {

    private static final String GRO_ATOM_LINE = "  348SOL    HW2   12   3.323   2.848   3.036 -1.9485  2.1328  2.1157";
    private static final String GRO_FREE_ATOM_LINE = "  348HW2    HW2  348   3.323   2.848   3.036";
    private static final String GRO_RESIDUE_OW_LINE = "  348SOL     OW   12   3.323   2.848   3.036";
    private static final String GRO_RESIDUE_HW1_LINE = "  348SOL    HW1   13   3.358   2.879   3.125";
    private static final String GRO_RESIDUE_HW2_LINE = "  348SOL    HW2   14   3.225   2.865   3.032";
    private static final float[] COORDINATES = new float[] { 3.323f, 2.848f, 3.036f };

    @Test
    public void testGroFileAtomLineFromString() {
        GmxGroFileAtomLine line = GmxGroFileAtomLine.fromStringLine(GRO_ATOM_LINE);
        Assert.assertNotNull(line, "GroFileAtomLine is null.");
        Assert.assertEquals(line.toString(), "\r\n" + GRO_ATOM_LINE, "Wrong atom line.");
        Assert.assertEquals(line.getVelocities()[0], -1.9485f, "Wrong X velocity.");
        Assert.assertEquals(line.getVelocities()[1], 2.13280f, "Wrong Y velocity.");
        Assert.assertEquals(line.getVelocities()[2], 2.11570f, "Wrong Z velocity.");
    }

    @Test
    public void testGroFileAtomLineFromFreeAtom() {
        GmxAtom atom = atomFactory.get(GmxAtomH.class, "HW2", 348, COORDINATES);
        GmxGroFileAtomLine line = GmxGroFileAtomLine.fromFreeAtom(atom);
        Assert.assertNotNull(line, "GroFileAtomLine is null.");
        Assert.assertEquals(line.toString(), "\r\n" + GRO_FREE_ATOM_LINE, "Wrong atom line.");
    }

    @Test
    public void testGroFileAtomLineFromResidue() {
        GmxAtom[] atoms = residueAtomsFactory.get(GmxResidueH2O.class, COORDINATES);
        atoms[0].setAtomNo(12);
        atoms[1].setAtomNo(13);
        atoms[2].setAtomNo(14);

        GmxResidue residue = residueByAtomsFactory.get(GmxResidueH2O.class, 348, atoms);
        GmxGroFileAtomLine[] lines = GmxGroFileAtomLine.fromResidue(residue);

        Assert.assertNotNull(lines, "GroFileAtomLines are null.");
        Assert.assertEquals(lines.length, 3, "Wrong number of GroFileAtomLines.");

        Assert.assertEquals(lines[0].toString(), "\r\n" + GRO_RESIDUE_OW_LINE, "Wrong OW atom line.");
        Assert.assertEquals(lines[1].toString(), "\r\n" + GRO_RESIDUE_HW1_LINE, "Wrong HW1 atom line.");
        Assert.assertEquals(lines[2].toString(), "\r\n" + GRO_RESIDUE_HW2_LINE, "Wrong HW2 atom line.");
    }

    @Test
    public void testGroFileReading() {
        List<GmxGroFileAtomLine> atomLines = groFileReader.readGroFileAtomLines(GRO_WATER_IN_ARGON_PATH);
        String description = groFileReader.readGroFileDescription(GRO_WATER_IN_ARGON_PATH);
        Integer atomsNum = groFileReader.readGroFileAtomsNum(GRO_WATER_IN_ARGON_PATH);
        float[] box = groFileReader.readGroFileBox(GRO_WATER_IN_ARGON_PATH);

        Logger.log("GRO file description: " + description);
        Logger.log("GRO file atomsNum: " + atomsNum);
        Logger.log("GRO file box: " + Arrays.toString(box));

        Assert.assertEquals(description, "Ar+SOL", "Wrong description.");
        Assert.assertEquals(atomsNum, Integer.valueOf(872), "Wrong atomsNo.");
        Assert.assertEquals(atomLines.size(), 872, "Wrong atoms number.");

        verifyCoordinates(box, 7.0f, 7.0f, 7.0f);
        verifyAtomLine(atomLines.get(0), 1, 1, "Ar", "Ar", new float[] { 3.804f, 2.449f, 4.279f });
        verifyAtomLine(atomLines.get(871), 872, 870, "HW2", "SOL", new float[] { 3.872f, 4.472f, 4.685f });
    }

    @Test
    public void testGroFileWriting() {
        List<GmxGroFileAtomLine> atomLines = groFileReader.readGroFileAtomLines(GRO_WATER_IN_ARGON_PATH);
        GmxFrameStructure frameStructure = frameStructureFromGroFileBuilderSupplier.get() //
                .withDescription(groFileReader.readGroFileDescription(GRO_WATER_IN_ARGON_PATH)) //
                .withBox(groFileReader.readGroFileBox(GRO_WATER_IN_ARGON_PATH)) //
                .withGroFileAtomLines(atomLines) //
                .build();

        GmxFrameCoordinates frameCoordinates = frameCoordinatesFromGroFileBuilderSupplier.get() //
                .withGroFileAtomLines(atomLines) //
                .withFrameNo(0) //
                .build();

        GmxFrame frame = frameFactory.get(frameStructure, frameCoordinates);
        Path path = groFileWriter.writeGroFile(frame, GRO_WATER_IN_ARGON_PATH.getParent(), "test-H2O-in-Ar");

        atomLines = groFileReader.readGroFileAtomLines(path);
        String description = groFileReader.readGroFileDescription(path);
        Integer atomsNum = groFileReader.readGroFileAtomsNum(path);
        float[] box = groFileReader.readGroFileBox(path);
        FileUtils.deleteFileIfExists(path);

        Logger.log("test-H2O-in-Ar GRO file description: " + description);
        Logger.log("test-H2O-in-Ar GRO file atomsNum: " + atomsNum);
        Logger.log("test-H2O-in-Ar GRO file box: " + Arrays.toString(box));

        Assert.assertNotNull(frame.getAtoms()[871], "GRO File writer nullified atom pointer.");
        Assert.assertEquals(description, "Ar+SOL", "Wrong description.");
        Assert.assertEquals(atomsNum, Integer.valueOf(872), "Wrong atomsNo.");
        Assert.assertEquals(atomLines.size(), 872, "Wrong atoms number.");

        verifyCoordinates(box, 7.0f, 7.0f, 7.0f);
        verifyAtomLine(atomLines.get(0), 0, 0, "Ar", "Ar", new float[] { 3.804f, 2.449f, 4.279f });
        verifyAtomLine(atomLines.get(871), 871, 869, "HW2", "SOL", new float[] { 3.872f, 4.472f, 4.685f });
    }

    private void verifyAtomLine(GmxGroFileAtomLine line, int atomNo, int residueNo, String atomAbbr, String residueAbbr, float[] coordinates) {
        Assert.assertEquals(line.getAtomNo(), atomNo, "Wrong line's atom No.");
        Assert.assertEquals(line.getResidueNo(), residueNo, "Wrong line's residue No.");
        Assert.assertEquals(line.getAtomAbbreviation(), atomAbbr, "Wrong line's atom abbr.");
        Assert.assertEquals(line.getResidueAbbreviation(), residueAbbr, "Wrong line's residue abbr.");
        Assert.assertEquals(line.getCoordinates()[0], coordinates[0], "Wrong line's X coordinate.");
        Assert.assertEquals(line.getCoordinates()[1], coordinates[1], "Wrong line's Y coordinate.");
        Assert.assertEquals(line.getCoordinates()[2], coordinates[2], "Wrong line's Z coordinate.");
    }
}
