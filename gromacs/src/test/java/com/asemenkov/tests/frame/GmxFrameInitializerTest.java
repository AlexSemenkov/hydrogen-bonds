package com.asemenkov.tests.frame;

import com.asemenkov.gromacs.frame.GmxFrame;
import com.asemenkov.gromacs.frame.coordinates.GmxFrameCoordinates;
import com.asemenkov.gromacs.frame.exceptions.GmxFrameException;
import com.asemenkov.gromacs.frame.structure.GmxFrameStructure;
import com.asemenkov.gromacs.io.gro.GmxGroFileAtomLine;
import com.asemenkov.tests.config.GmxAbstractTest;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author asemenkov
 * @since Mar 12, 2019
 */
@Test
public class GmxFrameInitializerTest extends GmxAbstractTest {

    private static final String REGEX_NO_STRUCTURE = "frameStructure is missing";
    private static final String REGEX_NO_COORDINATES = "frameCoordinates is missing";

    private List<GmxGroFileAtomLine> groFileAtomLines;
    private GmxFrameStructure frameStructure;
    private GmxFrameCoordinates frameCoordinates;
    private GmxFrame frame;

    @Test
    public void testFrameInitializationFromGroFile() {
        String description = "Test Frame Initialization From Gro File";
        groFileAtomLines = groFileReader.readGroFileAtomLines(GRO_WATER_IN_ARGON_PATH);

        frameStructure = frameStructureFromGroFileBuilderSupplier.get() //
                .withDescription(description) //
                .withGroFileAtomLines(groFileAtomLines) //
                .withBox(BOX) //
                .build();

        frameCoordinates = frameCoordinatesFromGroFileBuilderSupplier.get() //
                .withGroFileAtomLines(groFileAtomLines) //
                .withFrameNo(1) //
                .build();

        frame = frameFactory.get(frameStructure, frameCoordinates);
        groFileWriter.writeGroFile(frame, PATH_GRO_FROM_TESTS, "from-gro-file.gro");

        verifyFrame(frame, description, 1, BOX);
        verifyFrameAtoms(frame, 872, "Argon", "Hydrogen");
        verifyFrameResidues(frame, 1, "Water", "Oxygen");
        verifyRadiusVectors(frame, 6227, 7545, 7589);
    }

    @Test(dependsOnMethods = "testFrameInitializationFromGroFile")
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
        groFileWriter.writeGroFile(frame, PATH_GRO_FROM_TESTS, "from-arrays.gro");

        verifyFrame(frame, description, 5, BOX);
        verifyFrameAtoms(frame, 872, "Argon", "Hydrogen");
        verifyFrameResidues(frame, 1, "Water", "Oxygen");
        verifyRadiusVectors(frame, 6227, 7545, 7589);
    }

    @Test(dependsOnMethods = "testFrameInitializationFromArrays")
    public void testFrameInitializationWithoutResidues() {
        String description = "Test Frame Initialization Without Residues";

        frameStructure = frameStructureFromScratchBuilderSupplier.get() //
                .withDescription(description) //
                .withFreeAtoms("Ar", 100) //
                .withBox(BOX) //
                .build();

        frameCoordinates = frameCoordinatesFromScratchBuilderSupplier.get() //
                .withFrameStructure(frameStructure) //
                .withFrameNo(2) //
                .build();

        frame = frameFactory.get(frameStructure, frameCoordinates);
        groFileWriter.writeGroFile(frame, PATH_GRO_FROM_TESTS, "without-residues.gro");

        verifyFrame(frame, description, 2, BOX);
        verifyFrameAtoms(frame, 100, "Argon", "Argon");
        verifyFrameResidues(frame, 0, null, null);
    }

    @Test(dependsOnMethods = "testFrameInitializationWithoutResidues")
    public void testFrameInitializationFromXtcFile() {
        String description = "Test Frame Initialization From Xtc File";
        groFileAtomLines = groFileReader.readGroFileAtomLines(GRO_WATER_IN_ARGON_PATH);

        frameStructure = frameStructureFromGroFileBuilderSupplier.get() //
                .withDescription(description) //
                .withGroFileAtomLines(groFileAtomLines) //
                .withBox(BOX) //
                .build();

        xtcFileNativeReader.openXtcFile(XTC_WATER_IN_ARGON_PATH);
        frameCoordinates = xtcFileNativeReader.readNextFrame();
        xtcFileNativeReader.closeXtcFile();

        frame = frameFactory.get(frameStructure, frameCoordinates);
        groFileWriter.writeGroFile(frame, PATH_GRO_FROM_TESTS, "from-xtc-file.gro");

        verifyFrame(frame, description, 1, BOX);
        verifyFrameAtoms(frame, 872, "Argon", "Hydrogen");
        verifyFrameResidues(frame, 1, "Water", "Oxygen");
        verifyRadiusVectors(frame, 6306, 7847, 7757);
    }

    @Test(dependsOnMethods = "testFrameInitializationFromXtcFile")
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
        groFileWriter.writeGroFile(frame, PATH_GRO_FROM_TESTS, "from-scratch.gro");

        verifyFrame(frame, description, 4, BOX);
        verifyFrameAtoms(frame, 250, "Argon", "Hydrogen");
        verifyFrameResidues(frame, 50, "Water", "Oxygen");
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
}
