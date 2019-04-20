package com.asemenkov.procedure.main;

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
import com.asemenkov.gromacs.io.GmxXtcFileNativeReader;
import com.asemenkov.gromacs.io.config.GmxIoConfig;
import com.asemenkov.gromacs.io.gro.GmxGroFileReader;
import com.asemenkov.gromacs.io.gro.GmxGroFileWriter;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;
import com.asemenkov.gromacs.particles.utils.GmxAnglePredicate;
import com.asemenkov.procedure.config.ProceduresConfig;
import com.asemenkov.utils.config.Factories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.function.Supplier;

/**
 * @author asemenkov
 * @since Sep 07, 2018
 */
@Import({ ProceduresConfig.class, GmxFrameConfig.class, GmxIoConfig.class })
public abstract class AbstractProcedure {

    protected @Autowired GmxXtcFileNativeReader xtcFileNativeReader;
    protected @Autowired GmxGroFileWriter groFileWriter;
    protected @Autowired GmxGroFileReader groFileReader;

    protected @Autowired Supplier<GmxFrameStructureFromGroFileBuilder> frameStructureFromGroFileBuilderSupplier;
    protected @Autowired Supplier<GmxFrameStructureFromScratchBuilder> frameStructureFromScratchBuilderSupplier;
    protected @Autowired Supplier<GmxFrameStructureFromArraysBuilder> frameStructureFromArraysBuilderSupplier;

    protected @Autowired Supplier<GmxFrameCoordinatesFromGroFileBuilder> frameCoordinatesFromGroFileBuilderSupplier;
    protected @Autowired Supplier<GmxFrameCoordinatesFromScratchBuilder> frameCoordinatesFromScratchBuilderSupplier;
    protected @Autowired Supplier<GmxFrameCoordinatesFromArraysBuilder> frameCoordinatesFromArraysBuilderSupplier;

    /**
     * @implSpec t1 -- (Class) atom class
     * @implSpec t2 -- (String) atom abbreviation
     * @implSpec t3 -- (Integer) atom no
     * @implSpec t4 -- (float[]) atom coordinates x, y, z
     */
    protected @Autowired Factories.TetraFactory<GmxAtom, Class<? extends GmxAtom>, String, Integer, float[]> atomFactory;

    /**
     * @implSpec t1 -- (Class) residue class
     * @implSpec t2 -- (float[]) pivot atom coordinates
     */
    protected @Autowired Factories.DuoFactory<GmxAtom[], Class<? extends GmxResidue>, float[]> residueAtomsFactory;

    /**
     * @implSpec t1 -- (Class) residue class
     * @implSpec t2 -- (Integer) residue no
     * @implSpec t3 -- (GmxAtom[]) residue atoms
     */
    protected @Autowired Factories.TriFactory<GmxResidue, Class<? extends GmxResidue>, Integer, GmxAtom[]> residueFactory;

    /**
     * @implSpec t1 -- (Class) residue class
     * @implSpec t2 -- (Integer[]) residues no
     * @implSpec t3 -- (GmxAtom[][]) residue atoms, 1 row = atoms of 1 residue
     */
    protected @Autowired Factories.TriFactory<GmxResidue[], Class<? extends GmxResidue>, int[], GmxAtom[][]> residuesFactory;

    /**
     * @implSpec t1 -- (GmxFrameStructure) frame structure
     * @implSpec t2 -- (GmxFrameCoordinates) frame coordinates
     */
    protected @Autowired Factories.DuoFactory<GmxFrame, GmxFrameStructure, GmxFrameCoordinates> frameFactory;

    /**
     * @implSpec t1 -- (GmxAtom) vertex atom
     * @implSpec t2 -- (GmxAtom) point1 atom
     * @implSpec t3 -- (GmxAtom) point2 atom
     * @implSpec t4 -- (Double) expected cosine value
     * @implSpec t5 -- (Double) closeness of fit
     */
    protected @Autowired Factories.PentaFactory<GmxAnglePredicate, GmxAtom, GmxAtom, GmxAtom, Double, Double> anglePredicateFactory;

}
