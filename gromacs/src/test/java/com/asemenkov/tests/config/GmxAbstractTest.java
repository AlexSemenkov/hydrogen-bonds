package com.asemenkov.tests.config;

import com.asemenkov.gromacs.frame.GmxFrame;
import com.asemenkov.gromacs.frame.config.GmxFrameConfig;
import com.asemenkov.gromacs.frame.coordinates.GmxFrameCoordinates;
import com.asemenkov.gromacs.frame.coordinates.GmxFrameCoordinatesFromArraysBuilder;
import com.asemenkov.gromacs.frame.coordinates.GmxFrameCoordinatesFromGroFileBuilder;
import com.asemenkov.gromacs.frame.coordinates.GmxFrameCoordinatesFromScratchBuilder;
import com.asemenkov.gromacs.frame.structure.GmxFrameStructure;
import com.asemenkov.gromacs.frame.structure.GmxFrameStructureFromArraysBuilder;
import com.asemenkov.gromacs.frame.structure.GmxFrameStructureFromGroFileBuilder;
import com.asemenkov.gromacs.frame.structure.GmxFrameStructureFromScratchBuilder;
import com.asemenkov.gromacs.frame.utils.GmxFrameUtils;
import com.asemenkov.gromacs.io.GmxXtcFileNativeReader;
import com.asemenkov.gromacs.io.config.GmxIoConfig;
import com.asemenkov.gromacs.io.gro.GmxGroFileReader;
import com.asemenkov.gromacs.io.gro.GmxGroFileWriter;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;
import com.asemenkov.gromacs.particles.utils.GmxAnglePredicate;
import com.asemenkov.utils.config.Factories.DuoFactory;
import com.asemenkov.utils.config.Factories.PentaFactory;
import com.asemenkov.utils.config.Factories.TetraFactory;
import com.asemenkov.utils.config.Factories.TriFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author asemenkov
 * @since May 3, 2018
 */
@ContextConfiguration(classes = { GmxTestsConfig.class, GmxFrameConfig.class, GmxIoConfig.class })
public abstract class GmxAbstractTest extends AbstractTestNGSpringContextTests {
    private @Autowired ApplicationContext context;

    protected static final float[] BOX = new float[] { 40, 50, 60 };
    protected static final Path XTC_WATER_IN_ARGON_PATH = //
            Paths.get("src", "test", "resources", "gro-to-tests/water-in-argon.xtc");
    protected static final Path GRO_WATER_IN_ARGON_PATH = //
            Paths.get("src", "test", "resources", "gro-to-tests/water-in-argon.gro");
    protected static final Path GRO_ARGON_PATH = //
            Paths.get("src", "test", "resources", "gro-to-tests/argon.gro");
    protected static final Path GRO_WATER_PATH = //
            Paths.get("src", "test", "resources", "gro-to-tests/water.gro");
    protected static final Path PATH_GRO_FROM_TESTS = //
            Paths.get("src", "test", "resources", "gro-from-tests");

    protected @Autowired GmxXtcFileNativeReader xtcFileNativeReader;
    protected @Autowired GmxGroFileWriter groFileWriter;
    protected @Autowired GmxGroFileReader groFileReader;

    public GmxFrame createFrame(GmxFrameStructure structure, GmxFrameCoordinates coordinates) {
        return frameFactory.get(structure, coordinates);
    }

    public GmxFrameStructureFromGroFileBuilder frameStructureFromGroFileBuilder() {
        return context.getBean(GmxFrameStructureFromGroFileBuilder.class);
    }

    public GmxFrameStructureFromScratchBuilder frameStructureFromScratchBuilder() {
        return context.getBean(GmxFrameStructureFromScratchBuilder.class);
    }

    public GmxFrameStructureFromArraysBuilder frameStructureFromArraysBuilder() {
        return context.getBean(GmxFrameStructureFromArraysBuilder.class);
    }

    public GmxFrameCoordinatesFromGroFileBuilder frameCoordinatesFromGroFileBuilder() {
        return context.getBean(GmxFrameCoordinatesFromGroFileBuilder.class);
    }

    public GmxFrameCoordinatesFromScratchBuilder frameCoordinatesFromScratchBuilder() {
        return context.getBean(GmxFrameCoordinatesFromScratchBuilder.class);
    }

    public GmxFrameCoordinatesFromArraysBuilder frameCoordinatesFromArraysBuilder() {
        return context.getBean(GmxFrameCoordinatesFromArraysBuilder.class);
    }

    /**
     * @implSpec t1 -- (Class) atom class
     * @implSpec t2 -- (String) atom abbreviation
     * @implSpec t3 -- (Integer) atom no
     * @implSpec t4 -- (float[]) atom coordinates x, y, z
     */
    protected @Autowired TetraFactory<GmxAtom, Class<? extends GmxAtom>, String, Integer, float[]> atomFactory;

    /**
     * @implSpec t1 -- (Class) residue class
     * @implSpec t2 -- (float[]) pivot atom coordinates
     */
    protected @Autowired DuoFactory<GmxAtom[], Class<? extends GmxResidue>, float[]> residueAtomsFactory;

    /**
     * @implSpec t1 -- (Class) residue class
     * @implSpec t2 -- (Integer) residue no
     * @implSpec t3 -- (GmxAtom[]) residue atoms
     */
    protected @Autowired TriFactory<GmxResidue, Class<? extends GmxResidue>, Integer, GmxAtom[]> residueByAtomsFactory;

    /**
     * @implSpec t1 -- (Class) residue class
     * @implSpec t2 -- (Integer) residue no
     * @implSpec t3 -- (float[]) residue pivot point coordinates
     */
    protected @Autowired TriFactory<GmxResidue, Class<? extends GmxResidue>, Integer, float[]> residueByCoordsFactory;

    /**
     * @implSpec t1 -- (Class) residue class
     * @implSpec t2 -- (Integer[]) residues no
     * @implSpec t3 -- (GmxAtom[][]) residue atoms, 1 row = atoms of 1 residue
     */
    protected @Autowired TriFactory<GmxResidue[], Class<? extends GmxResidue>, int[], GmxAtom[][]> residuesFactory;

    /**
     * @implSpec t1 -- (GmxFrameStructure) frame structure
     * @implSpec t2 -- (GmxFrameCoordinates) frame coordinates
     */
    protected @Autowired DuoFactory<GmxFrame, GmxFrameStructure, GmxFrameCoordinates> frameFactory;

    /**
     * @implSpec t1 -- (GmxAtom) vertex atom
     * @implSpec t2 -- (GmxAtom) point1 atom
     * @implSpec t3 -- (GmxAtom) point2 atom
     * @implSpec t4 -- (Double) expected cosine value
     * @implSpec t5 -- (Double) closeness of fit
     */
    protected @Autowired PentaFactory<GmxAnglePredicate, GmxAtom, GmxAtom, GmxAtom, Double, Double> anglePredicateFactory;

    // ======== VALIDATORS ========

    /**
     * @param atom -- Actual atom
     * @param x    -- Expected X coordinate of atom
     * @param y    -- Expected Y coordinate of atom
     * @param z    -- Expected Z coordinate of atom
     */
    protected void verifyAtomCoordinates(GmxAtom atom, float x, float y, float z) {
        Assert.assertNotNull(atom);
        verifyCoordinates(atom.getCoordinates(), x, y, z);
    }

    /**
     * @param coordinates -- Actual coordinates
     * @param x           -- Expected X coordinate
     * @param y           -- Expected Y coordinate
     * @param z           -- Expected Z coordinate
     */
    protected void verifyCoordinates(float[] coordinates, float x, float y, float z) {
        Assert.assertNotNull(coordinates);
        Assert.assertEquals(coordinates[0], x, "Wrong X coordinate.");
        Assert.assertEquals(coordinates[1], y, "Wrong Y coordinate.");
        Assert.assertEquals(coordinates[2], z, "Wrong Z coordinate.");
    }

    protected void verifyFrame(GmxFrame frame, String description, int frameNo, float[] box) {
        Assert.assertEquals(frame.getDescription(), description, "Wrong description");
        Assert.assertEquals(frame.getFrameNo(), frameNo, "Wrong frame No.");
        Assert.assertEquals(frame.getBox()[0], box[0], "Wrong X value of box.");
        Assert.assertEquals(frame.getBox()[1], box[1], "Wrong Y value of box.");
        Assert.assertEquals(frame.getBox()[2], box[2], "Wrong Z value of box.");
    }

    @SuppressWarnings("SameParameterValue")
    protected void verifyFrameAtoms(GmxFrame frame, int atomsNum, String firstAtomName, String lastAtomNAme) {
        Assert.assertEquals(frame.getAtomsNum(), atomsNum, "Wrong atoms number.");
        Assert.assertEquals(frame.getAtoms().length, atomsNum, "Wrong atoms number.");
        Assert.assertEquals(frame.getAtoms()[0].getFullName(), firstAtomName, "Wrong first atom type.");
        Assert.assertEquals(frame.getAtoms()[0].getAtomNo(), 0, "Wrong atoms indexation.");
        Assert.assertEquals(frame.getAtoms()[atomsNum - 1].getFullName(), lastAtomNAme,
                "Wrong last atom type.");
        Assert.assertEquals(frame.getAtoms()[atomsNum - 1].getAtomNo(), atomsNum - 1,
                "Wrong atoms indexation.");
    }

    protected void verifyFrameResidues(GmxFrame frame, int residuesNum, String firstResidueName,
            String firstResidueAcceptor) {
        Assert.assertEquals(frame.getResiduesNum(), residuesNum, "Wrong residues number.");
        Assert.assertEquals(frame.getResidues().length, residuesNum, "Wrong residues number.");
        if (residuesNum > 0) {
            Assert.assertEquals(frame.getResidues()[0].getFullName(), firstResidueName,
                    "Wrong residue type.");
            Assert.assertEquals(frame.getResidues()[0].getAcceptorAtom().getFullName(), firstResidueAcceptor,
                    "Wrong acceptor type.");
        }
    }

    protected void verifyRadiusVectors(GmxFrame frame, int firstAtomRvX1000, int lastAtomRvX1000,
            int firstResidueRvX1000) {
        int i = frame.getAtomsNum() - 1;
        Assert.assertEquals((int) (frame.getAtoms()[0].getRadiusVector() * 1000), firstAtomRvX1000,
                "Wrong first atom radius-vector.");
        Assert.assertEquals((int) (frame.getAtoms()[i].getRadiusVector() * 1000), lastAtomRvX1000,
                "Wrong last atom radius-vector.");
        Assert.assertEquals((int) (frame.getResidues()[0].getRadiusVector() * 1000), firstResidueRvX1000,
                "Wrong residue radius-vector.");
    }

    protected void verifyMaxCoordinates(GmxFrame frame, float maxX, float maxY, float maxZ) {
        Assert.assertEquals(GmxFrameUtils.getAtomWithMaximalX(frame).getCoordinateX(), maxX,
                "Wrong maximum X coordinate.");
        Assert.assertEquals(GmxFrameUtils.getAtomWithMaximalY(frame).getCoordinateY(), maxY,
                "Wrong maximum Y coordinate.");
        Assert.assertEquals(GmxFrameUtils.getAtomWithMaximalZ(frame).getCoordinateZ(), maxZ,
                "Wrong maximum Z coordinate.");
    }

    protected void verifyMinCoordinates(GmxFrame frame, float minX, float minY, float minZ) {
        Assert.assertEquals(GmxFrameUtils.getAtomWithMinimalX(frame).getCoordinateX(), minX,
                "Wrong minimum X coordinate.");
        Assert.assertEquals(GmxFrameUtils.getAtomWithMinimalY(frame).getCoordinateY(), minY,
                "Wrong minimum Y coordinate.");
        Assert.assertEquals(GmxFrameUtils.getAtomWithMinimalZ(frame).getCoordinateZ(), minZ,
                "Wrong minimum Z coordinate.");
    }

}
