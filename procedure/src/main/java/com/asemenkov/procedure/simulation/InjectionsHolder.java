package com.asemenkov.procedure.simulation;

import com.asemenkov.command.config.CmdGmxCommandsConfig;
import com.asemenkov.command.execution.CmdExecutor;
import com.asemenkov.command.gmx.CmdGmxGrompp;
import com.asemenkov.command.gmx.CmdGmxMdrun;
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
import com.asemenkov.utils.config.Factories.DuoFactory;
import com.asemenkov.utils.config.Factories.PentaFactory;
import com.asemenkov.utils.config.Factories.TetraFactory;
import com.asemenkov.utils.config.Factories.TriFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import java.nio.file.Path;

/**
 * @author asemenkov
 * @since Sep 07, 2018
 */
@Import({ CmdGmxCommandsConfig.class, GmxFrameConfig.class, GmxIoConfig.class })
public abstract class InjectionsHolder {

    protected @Autowired ApplicationContext context;
    protected @Autowired Path simulationPath;
    protected @Autowired CmdExecutor executor;

    // ======== COMMAND LINE INJECTIONS ========

    protected Cmd cmd = new Cmd();

    protected class Cmd {

        public CmdGmxGrompp grompp() {
            return context.getBean(CmdGmxGrompp.class);
        }

        public CmdGmxMdrun mdrun() {
            return context.getBean(CmdGmxMdrun.class);
        }

    }

    // ======== IO INJECTIONS ========

    protected Io io = new Io();

    protected class Io {

        public GmxXtcFileNativeReader xtcFileNativeReader() {
            return context.getBean(GmxXtcFileNativeReader.class);
        }

        public GmxGroFileWriter groFileWriter() {
            return context.getBean(GmxGroFileWriter.class);
        }

        public GmxGroFileReader groFileReader() {
            return context.getBean(GmxGroFileReader.class);
        }
    }

    // ======== FRAME INJECTIONS ========

    protected Frame frame = new Frame();

    protected class Frame {

        private @Autowired DuoFactory< //
                GmxFrame, GmxFrameStructure, GmxFrameCoordinates> frameFactory;

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
    }

    // ======== ATOM INJECTIONS ========

    protected Atom atom = new Atom();

    protected class Atom {

        private @Autowired TetraFactory< //
                GmxAtom, Class<? extends GmxAtom>, String, Integer, float[]> atomFactory;

        private @Autowired PentaFactory< //
                GmxAnglePredicate, GmxAtom, GmxAtom, GmxAtom, Double, Double> anglePredicateFactory;

        public GmxAtom createAtom(Class<? extends GmxAtom> atomClass, //
                String atomAbbreviation, Integer atomNo, float[] atomCoordinates) {
            return atomFactory.get(atomClass, atomAbbreviation, atomNo, atomCoordinates);
        }

        public GmxAnglePredicate anglePredicate(GmxAtom vertexAtom, //
                GmxAtom point1Atom, GmxAtom point2Atom, Double expectedCosine, Double closeness) {
            return anglePredicateFactory.get(vertexAtom, point1Atom, point2Atom, expectedCosine, closeness);
        }
    }

    // ======== RESIDUE INJECTIONS ========

    protected Residue residue = new Residue();

    protected class Residue {

        private @Autowired TriFactory< //
                GmxResidue, Class<? extends GmxResidue>, Integer, GmxAtom[]> residueFactory;

        private @Autowired DuoFactory< //
                GmxAtom[], Class<? extends GmxResidue>, float[]> residueAtomsFactory;

        private @Autowired TriFactory< //
                GmxResidue, Class<? extends GmxResidue>, Integer, GmxAtom[]> residueByAtomsFactory;

        private @Autowired TriFactory< //
                GmxResidue, Class<? extends GmxResidue>, Integer, float[]> residueByCoordsFactory;

        private @Autowired TriFactory< //
                GmxResidue[], Class<? extends GmxResidue>, int[], GmxAtom[][]> residuesFactory;

        public GmxResidue createResidue(Class<? extends GmxResidue> residueClass, //
                Integer residueNo, GmxAtom[] residueAtoms) {
            return residueFactory.get(residueClass, residueNo, residueAtoms);
        }

        public GmxAtom[] createAtomsForResidue(Class<? extends GmxResidue> residueClass, //
                float[] pivotAtomCoordinates) {
            return residueAtomsFactory.get(residueClass, pivotAtomCoordinates);
        }

        public GmxResidue residueByAtomsFactory(Class<? extends GmxResidue> residueClass, //
                Integer residueNo, GmxAtom[] residueAtoms) {
            return residueByAtomsFactory.get(residueClass, residueNo, residueAtoms);
        }

        public GmxResidue createResidue(Class<? extends GmxResidue> residueClass, //
                Integer residueNo, float[] pivotPointCoordinates) {
            return residueByCoordsFactory.get(residueClass, residueNo, pivotPointCoordinates);
        }

        public GmxResidue[] createResidues(Class<? extends GmxResidue> residueClass, //
                int[] residuesNo, GmxAtom[][] residueAtoms) {
            return residuesFactory.get(residueClass, residuesNo, residueAtoms);
        }
    }
}
