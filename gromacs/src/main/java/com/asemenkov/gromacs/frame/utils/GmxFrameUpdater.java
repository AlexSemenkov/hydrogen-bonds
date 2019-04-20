package com.asemenkov.gromacs.frame.utils;

import com.asemenkov.gromacs.frame.GmxFrame;
import com.asemenkov.gromacs.frame.coordinates.GmxFrameCoordinatesFromArraysBuilder;
import com.asemenkov.gromacs.frame.exceptions.GmxFrameException;
import com.asemenkov.gromacs.frame.structure.GmxFrameStructureFromArraysBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * @author asemenkov
 * @since Mar 10, 2019
 */
@Component
public class GmxFrameUpdater {

    private static GmxFrameStructureFromArraysBuilder frameStructureFromArraysBuilder;
    private static GmxFrameCoordinatesFromArraysBuilder frameCoordinatesFromArraysBuilder;

    // ======== STATIC FIELDS INJECTIONS ========

    @Autowired
    protected void setFrameStructureFromArraysBuilder( //
            Supplier<GmxFrameStructureFromArraysBuilder> supplier) {
        GmxFrameUpdater.frameStructureFromArraysBuilder = supplier.get();
    }

    @Autowired
    protected void setFrameCoordinatesFromArraysBuilder( //
            Supplier<GmxFrameCoordinatesFromArraysBuilder> supplier) {
        GmxFrameUpdater.frameCoordinatesFromArraysBuilder = supplier.get();
    }

    // ======== PUBLIC INTERFACE ========

    public static void reindexAtoms(GmxFrame frame) {
        verifyNoAtomDuplicates(frame);
        AtomicInteger i = new AtomicInteger(0);
        Arrays.stream(frame.getAtoms()).forEach(atom -> atom.setAtomNo(i.getAndIncrement()));
    }

    public static void reindexResidues(GmxFrame frame) {
        verifyNoResidueDuplicates(frame);
        AtomicInteger i = new AtomicInteger(0);
        Arrays.stream(frame.getResidues()).forEach(residue -> residue.setResidueNo(i.getAndIncrement()));
    }

    public static void updateFrameStructure(GmxFrame frame) {
        reindexAtoms(frame);
        reindexResidues(frame);
        frame.setFrameStructure(frameStructureFromArraysBuilder //
                .withDescription(frame.getFrameStructure().getDescription()) //
                .withAtomsArray(frame.getAtoms()) //
                .withResiduesArray(frame.getResidues()) //
                .withBox(frame.getFrameStructure().getBox()) //
                .build());
    }

    public static void updateFrameCoordinates(GmxFrame frame) {
        reindexAtoms(frame);
        reindexResidues(frame);
        frame.setFrameCoordinates(frameCoordinatesFromArraysBuilder //
                .withFrameNo(frame.getFrameCoordinates().getFrameNo()) //
                .withAtomsArray(frame.getAtoms()) //
                .build());
    }

    // ======== PRIVATE VALIDATORS ========

    private static void verifyNoAtomDuplicates(GmxFrame frame) {
        long distinctAtoms = Arrays.stream(frame.getAtoms()).distinct().count();
        if (frame.getAtomsNum() != distinctAtoms) throw new GmxFrameException(
                "There are duplicates in the frame atoms: " + (frame.getAtomsNum() - distinctAtoms));
    }

    private static void verifyNoResidueDuplicates(GmxFrame frame) {
        long distinctResidues = Arrays.stream(frame.getResidues()).distinct().count();
        if (frame.getResiduesNum() != distinctResidues) throw new GmxFrameException(
                "There are duplicates in the frame residues: " + (frame.getResiduesNum() - distinctResidues));
    }
}
