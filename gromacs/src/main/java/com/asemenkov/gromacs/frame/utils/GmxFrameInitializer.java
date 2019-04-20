package com.asemenkov.gromacs.frame.utils;

import com.asemenkov.gromacs.frame.GmxFrame;
import com.asemenkov.gromacs.frame.exceptions.GmxFrameException;
import com.asemenkov.gromacs.frame.structure.GmxFrameStructure;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;
import com.asemenkov.utils.config.Factories.TetraFactory;
import com.asemenkov.utils.config.Factories.TriFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * @author asemenkov
 * @since Feb 07, 2019
 */
@Component
public class GmxFrameInitializer {

    private static TetraFactory<GmxAtom, Class<? extends GmxAtom>, String, Integer, float[]> atomFactory;
    private static TriFactory<GmxResidue[], Class<? extends GmxResidue>, int[], GmxAtom[][]> residuesFactory;

    // ======== STATIC FIELDS INJECTIONS ========

    @Autowired
    public void setAtomFactory(
            TetraFactory<GmxAtom, Class<? extends GmxAtom>, String, Integer, float[]> atomFactory) {
        GmxFrameInitializer.atomFactory = atomFactory;
    }

    @Autowired
    public void setResiduesFactory(
            TriFactory<GmxResidue[], Class<? extends GmxResidue>, int[], GmxAtom[][]> residuesFactory) {
        GmxFrameInitializer.residuesFactory = residuesFactory;
    }

    // ======== PUBLIC INTERFACE ========

    public static void initAtoms(GmxFrame frame) {
        validateFrameStructureAtoms(frame);
        GmxAtom[] newAtoms = new GmxAtom[frame.getFrameStructure().getAtomsNum()];
        frame.setAtoms(newAtoms);

        Class<? extends GmxAtom>[] atomClasses = frame.getFrameStructure().getAtomsSequence();
        String[] atomAbbreviations = frame.getFrameStructure().getAtomAbbreviationsSequence();
        float[][] atomCoordinates = frame.getFrameCoordinates().getCoordinates();

        IntStream.range(0, frame.getFrameStructure().getAtomsNum()).parallel().forEach(i -> //
                newAtoms[i] = atomFactory.get(atomClasses[i], atomAbbreviations[i], i, atomCoordinates[i]));
    }

    public static void initResidues(GmxFrame frame) {
        if (frame.getFrameStructure().getResiduesNum() > 0) validateFrameStructureResidues(frame);
        GmxResidue[] newResidues = new GmxResidue[frame.getFrameStructure().getResiduesNum()];
        frame.setResidues(newResidues);

        AtomicInteger arrayIndex = new AtomicInteger(0);
        frame.getFrameStructure().getResidueIndexesMap().forEach((key, value) -> {

            GmxAtom[][] residuesAtoms = Arrays.stream(value) //
                    .mapToObj(i -> frame.getFrameStructure().getResidueAtomsMap().get(i)) //
                    .map(i -> IntStream.of(i).mapToObj(j -> frame.getAtoms()[j]).toArray(GmxAtom[]::new)) //
                    .toArray(GmxAtom[][]::new);

            GmxResidue[] residues = residuesFactory.get(key, value, residuesAtoms);
            int index = arrayIndex.getAndAdd(residues.length);
            System.arraycopy(residues, 0, newResidues, index, residues.length);
        });
    }

    // ======== PRIVATE INTERFACE ========

    private static void validateFrameStructureAtoms(GmxFrame frame) {
        GmxFrameStructure structure = frame.getFrameStructure();
        if (frame.getFrameStructure() == null) throw new GmxFrameException("frameStructure is missing");
        if (frame.getFrameCoordinates() == null) throw new GmxFrameException("frameCoordinates is missing");
        if (structure.getAtomsSequence() == null || structure.getAtomsSequence().length == 0)
            throw new GmxFrameException("frameStructure doesn't specify atoms sequence");
        if (Arrays.stream(structure.getAtomsSequence()).parallel().anyMatch(Objects::isNull))
            throw new GmxFrameException("frameStructure atoms array contains null pointers");
        if (structure.getAtomAbbreviationsSequence() == null
                || structure.getAtomAbbreviationsSequence().length != structure.getAtomsSequence().length)
            throw new GmxFrameException("frameStructure doesn't specify atoms abbreviations sequence");
        if (structure.getAtomsNum() != frame.getFrameCoordinates().getCoordinates().length)
            throw new GmxFrameException("frameStructure atomsNum isn't equal to frameCoordinates coordsNum");
    }

    private static void validateFrameStructureResidues(GmxFrame frame) {
        GmxFrameStructure structure = frame.getFrameStructure();
        if (frame.getFrameStructure() == null) throw new GmxFrameException("frameStructure is missing");
        if (frame.getFrameCoordinates() == null) throw new GmxFrameException("frameCoordinates is missing");
        if (structure.getResidueIndexesMap() == null || structure.getResidueIndexesMap().isEmpty())
            throw new GmxFrameException("frameStructure doesn't specify residues indexes");
        if (structure.getResidueAtomsMap() == null || structure.getResidueAtomsMap().isEmpty())
            throw new GmxFrameException("frameStructure doesn't specify residues atoms");
    }

}
