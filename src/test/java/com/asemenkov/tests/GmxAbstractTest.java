package com.asemenkov.tests;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

import com.asemenkov.gromacs.frame.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import com.asemenkov.gromacs.frame.GmxFrame;
import com.asemenkov.gromacs.frame.config.GmxFrameConfig;
import com.asemenkov.gromacs.frame.coordinates.GmxFrameCoordinates;
import com.asemenkov.gromacs.frame.coordinates.GmxFrameCoordinatesBuilder;
import com.asemenkov.gromacs.io.GmxGroFileReaderAndWriter;
import com.asemenkov.gromacs.io.config.GmxIoConfig;
import com.asemenkov.gromacs.io.GmxXtcFileNativeReader;
import com.asemenkov.gromacs.particles.GmxAnglePredicate;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;
import com.asemenkov.utils.Factories.DuoFactory;
import com.asemenkov.utils.Factories.PentaFactory;
import com.asemenkov.utils.Factories.TetraFactory;
import com.asemenkov.utils.Factories.TriFactory;
import org.testng.Assert;

/**
 * @author asemenkov
 * @since May 3, 2018
 */
@ContextConfiguration(classes = { GmxFrameConfig.class, GmxIoConfig.class })
public abstract class GmxAbstractTest extends AbstractTestNGSpringContextTests {

    protected static final float[] BOX = new float[] { 40, 50, 60 };
    protected static final Path XTC_WATER_IN_ARGON_PATH = Paths.get("src", "test", "resources", "water-in-argon.xtc");
    protected static final Path GRO_WATER_IN_ARGON_PATH = Paths.get("src", "test", "resources", "water-in-argon.gro");
    protected static final Path GRO_ARGON_PATH = Paths.get("src", "test", "resources", "argon.gro");
    protected static final Path GRO_WATER_PATH = Paths.get("src", "test", "resources", "water.gro");

    protected @Autowired GmxXtcFileNativeReader xtcFileNativeReader;
    protected @Autowired GmxGroFileReaderAndWriter groFileReaderAndWriter;

    protected @Autowired Supplier<GmxFrameStructureFromGroFileBuilder> frameStructureFromGroFileBuilderSupplier;
    protected @Autowired Supplier<GmxFrameStructureFromScratchBuilder> frameStructureFromScratchBuilderSupplier;
    protected @Autowired Supplier<GmxFrameStructureFromArraysBuilder> frameStructureFromArraysBuilderSupplier;

    protected @Autowired Supplier<GmxFrameCoordinatesBuilder> frameCoordinatesBuilderSupplier;

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
    protected @Autowired TriFactory<GmxResidue, Class<? extends GmxResidue>, Integer, GmxAtom[]> residueFactory;

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
}
