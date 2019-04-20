package com.asemenkov.gromacs.frame.utils;

import com.asemenkov.gromacs.frame.GmxFrame;
import com.asemenkov.gromacs.frame.exceptions.GmxFrameException;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;
import com.asemenkov.gromacs.particles.utils.GmxAtomUtils;
import com.asemenkov.gromacs.particles.utils.GmxResidueUtils;
import com.asemenkov.utils.config.Factories.DuoFactory;
import com.asemenkov.utils.config.Factories.TetraFactory;
import com.asemenkov.utils.config.Factories.TriFactory;
import com.asemenkov.utils.io.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author asemenkov
 * @since Feb 07, 2019
 */
@Component
public class GmxFrameUtils {

    private static TetraFactory<GmxAtom, Class<? extends GmxAtom>, String, Integer, float[]> atomFactory;
    private static TriFactory<GmxResidue, Class<? extends GmxResidue>, Integer, GmxAtom[]> residueFactory;
    private static DuoFactory<GmxAtom[], Class<? extends GmxResidue>, float[]> residueAtomsFactory;

    // ======== STATIC FIELDS INJECTIONS ========

    @Autowired
    public void setAtomFactory(
            TetraFactory<GmxAtom, Class<? extends GmxAtom>, String, Integer, float[]> atomFactory) {
        GmxFrameUtils.atomFactory = atomFactory;
    }

    @Autowired
    public void setResiduesFactory(
            TriFactory<GmxResidue, Class<? extends GmxResidue>, Integer, GmxAtom[]> residueFactory) {
        GmxFrameUtils.residueFactory = residueFactory;
    }

    @Autowired
    public void setResidueAtomsFactory(
            DuoFactory<GmxAtom[], Class<? extends GmxResidue>, float[]> residueAtomsFactory) {
        GmxFrameUtils.residueAtomsFactory = residueAtomsFactory;
    }

    // ======== MINIMAL ========

    public static GmxAtom getAtomWithMinimalX(GmxFrame frame) {
        return frame.getMinAtom(GmxAtomUtils::cmpByCoordinateX);
    }

    public static GmxAtom getAtomWithMinimalY(GmxFrame frame) {
        return frame.getMinAtom(GmxAtomUtils::cmpByCoordinateY);
    }

    public static GmxAtom getAtomWithMinimalZ(GmxFrame frame) {
        return frame.getMinAtom(GmxAtomUtils::cmpByCoordinateZ);
    }

    public static GmxAtom getAtomWithMinimalRadiusVector(GmxFrame frame) {
        return frame.getMinAtom(GmxAtomUtils::cmpByRadiusVector);
    }

    public static GmxResidue getResidueWithMinimalX(GmxFrame frame) {
        return frame.getMinResidue(GmxResidueUtils::cmpByCoordinateX);
    }

    public static GmxResidue getResidueWithMinimalY(GmxFrame frame) {
        return frame.getMinResidue(GmxResidueUtils::cmpByCoordinateY);
    }

    public static GmxResidue getResidueWithMinimalZ(GmxFrame frame) {
        return frame.getMinResidue(GmxResidueUtils::cmpByCoordinateZ);
    }

    public static GmxResidue getResidueWithMinimalRadiusVector(GmxFrame frame) {
        return frame.getMinResidue(GmxResidueUtils::cmpByRadiusVector);
    }

    // ======== MAXIMAL ========

    public static GmxAtom getAtomWithMaximalX(GmxFrame frame) {
        return frame.getMaxAtom(GmxAtomUtils::cmpByCoordinateX);
    }

    public static GmxAtom getAtomWithMaximalY(GmxFrame frame) {
        return frame.getMaxAtom(GmxAtomUtils::cmpByCoordinateY);
    }

    public static GmxAtom getAtomWithMaximalZ(GmxFrame frame) {
        return frame.getMaxAtom(GmxAtomUtils::cmpByCoordinateZ);
    }

    public static GmxAtom getAtomWithMaximalRadiusVector(GmxFrame frame) {
        return frame.getMaxAtom(GmxAtomUtils::cmpByRadiusVector);
    }

    public static GmxResidue getResidueWithMaximalX(GmxFrame frame) {
        return frame.getMaxResidue(GmxResidueUtils::cmpByCoordinateX);
    }

    public static GmxResidue getResidueWithMaximalY(GmxFrame frame) {
        return frame.getMaxResidue(GmxResidueUtils::cmpByCoordinateY);
    }

    public static GmxResidue getResidueWithMaximalZ(GmxFrame frame) {
        return frame.getMaxResidue(GmxResidueUtils::cmpByCoordinateZ);
    }

    public static GmxResidue getResidueWithMaximalRadiusVector(GmxFrame frame) {
        return frame.getMaxResidue(GmxResidueUtils::cmpByRadiusVector);
    }

    // ======== SORTING ========

    public static GmxAtom[] getAtomsSortedByDistanceToPoint(GmxFrame frame, float[] coordinates) {
        if (coordinates == null || coordinates.length != 3) //
            throw new GmxFrameException("Invalid point coordinates: " + Arrays.toString(coordinates));

        float[] xyz =  Arrays.copyOf(coordinates, 3);
        GmxAtomUtils.shift(-xyz[0], -xyz[1], -xyz[2], frame.getAtoms());
        GmxAtom[] atoms = frame.getSortedAtoms(GmxAtomUtils::cmpByRadiusVector);
        GmxAtomUtils.shift(xyz[0], xyz[1], xyz[2], frame.getAtoms());
        return atoms;
    }

    public static GmxResidue[] getResiduesSortedByDistanceToPoint(GmxFrame frame, float[] coordinates) {
        if (coordinates == null || coordinates.length != 3) //
            throw new GmxFrameException("Invalid point coordinates: " + Arrays.toString(coordinates));

        GmxResidueUtils.shift(-coordinates[0], -coordinates[1], -coordinates[2], frame.getResidues());
        GmxResidue[] residues = frame.getSortedResidues(GmxResidueUtils::cmpByRadiusVector);
        GmxResidueUtils.shift(coordinates[0], coordinates[1], coordinates[2], frame.getResidues());
        return residues;
    }

    // ======== NEIGHBOURS ========

    public static GmxAtom getNeighbourAtom(GmxFrame frame, float[] coordinates, int order) {
        if (order < 0 || order >= frame.getAtomsNum()) //
            throw new GmxFrameException("Invalid neighbour order: " + order);
        return getAtomsSortedByDistanceToPoint(frame, coordinates)[order];
    }

    public static GmxResidue getNeighbourResidue(GmxFrame frame, float[] coordinates, int order) {
        if (order < 0 || order >= frame.getResiduesNum()) //
            throw new GmxFrameException("Invalid neighbour order: " + order);
        return getResiduesSortedByDistanceToPoint(frame, coordinates)[order];
    }

    // ======== BOX ALTERATION ========

    public static void refineBox(GmxFrame frame, float padding) {
        float minX = getAtomWithMinimalX(frame).getCoordinateX() - padding;
        float minY = getAtomWithMinimalY(frame).getCoordinateY() - padding;
        float minZ = getAtomWithMinimalZ(frame).getCoordinateZ() - padding;
        float maxX = getAtomWithMaximalX(frame).getCoordinateX() + padding;
        float maxY = getAtomWithMaximalY(frame).getCoordinateY() + padding;
        float maxZ = getAtomWithMaximalZ(frame).getCoordinateZ() + padding;

        float[] newBox = new float[] { maxX - minX, maxY - minY, maxZ - minZ };
        GmxAtomUtils.shift(-minX, -minY, -minZ, frame.getAtoms());
        frame.getFrameStructure().setBox(newBox);

        GmxFrameUpdater.updateFrameCoordinates(frame);
        Logger.log(String.format("Box is refined. New size: %f x %f x %f", newBox[0], newBox[1], newBox[2]));
    }

    public static void removeAtomsOutOfBox(GmxFrame frame) {
        Predicate<GmxAtom> isAtomOutOfBox = atom //
                -> atom.getCoordinateX() >= frame.getBox()[0] //
                || atom.getCoordinateY() >= frame.getBox()[1] //
                || atom.getCoordinateZ() >= frame.getBox()[2] //
                || atom.getCoordinateX() <= 0.f //
                || atom.getCoordinateY() <= 0.f //
                || atom.getCoordinateZ() <= 0.f;

        Predicate<GmxResidue> isResidueOutOfBox = //
                residue -> Arrays.stream(residue.getAllAtoms()).anyMatch(isAtomOutOfBox);

        removeResidues(frame, frame.getFilteredResidues(isResidueOutOfBox));
        removeFreeAtoms(frame, frame.getFilteredAtoms(isAtomOutOfBox));
    }

    public static void multiplyFrame(GmxFrame frame, int multiX, int multiY, int multiZ) {
        if (multiX <= 0 || multiY <= 0 || multiZ <= 0) //
            throw new GmxFrameException("Box multiplier is negative integer or zero");

        multiplySingleDimension(frame, 0, multiX);
        multiplySingleDimension(frame, 1, multiY);
        multiplySingleDimension(frame, 2, multiZ);

        GmxFrameUpdater.updateFrameStructure(frame);
        GmxFrameUpdater.updateFrameCoordinates(frame);
        Logger.log(String.format("Box is multiplied: %d x %d x %d", multiX, multiY, multiZ));
    }

    // ======== PARTICLES REMOVAL ========

    public static void removeFreeAtoms(GmxFrame frame, GmxAtom... atoms) {
        Arrays.stream(frame.getResidues()).parallel() //
                .flatMap(residue -> Arrays.stream(residue.getAllAtoms())) //
                .filter(atom1 -> Arrays.stream(atoms).anyMatch(atom2 -> atom2 == atom1)) //
                .findFirst().ifPresent(atom -> {
            throw new GmxFrameException("Atom is not free: " + atom);
        });

        int expectedNumAfterRemoval = frame.getAtomsNum() - atoms.length;
        frame.setAtoms(frame.getFilteredAtoms( //
                atom1 -> Arrays.stream(atoms).noneMatch(atom2 -> atom1 == atom2)));

        if (frame.getAtomsNum() != expectedNumAfterRemoval) //
            throw new GmxFrameException("Error while removing atoms from frame: unexpected length");

        GmxFrameUpdater.updateFrameStructure(frame);
        GmxFrameUpdater.updateFrameCoordinates(frame);
        Logger.log("Number of atoms removed from frame: " + atoms.length);
    }

    public static void removeResidues(GmxFrame frame, GmxResidue... residues) {
        int expectedNumAfterRemoval = frame.getResiduesNum() - residues.length;
        frame.setResidues(frame.getFilteredResidues( //
                residue1 -> Arrays.stream(residues).noneMatch(residue2 -> residue1 == residue2)));

        if (frame.getResiduesNum() != expectedNumAfterRemoval) //
            throw new GmxFrameException("Error while removing residues from frame: unexpected length");

        removeFreeAtoms(frame, Arrays.stream(residues) //
                .flatMap(residue -> Arrays.stream(residue.getAllAtoms())) //
                .toArray(GmxAtom[]::new));
        Logger.log("Number of residues removed from frame: " + residues.length);
    }

    // ======== PARTICLES APPENDAGE ========

    public static void appendFreeAtoms(GmxFrame frame, GmxAtom... atoms) {
        int expectedNumAfterAppendage = frame.getAtomsNum() + atoms.length;
        GmxAtom[] allAtoms = new GmxAtom[expectedNumAfterAppendage];

        System.arraycopy(frame.getAtoms(), 0, allAtoms, 0, frame.getAtomsNum());
        System.arraycopy(atoms, 0, allAtoms, frame.getAtomsNum(), atoms.length);

        if (Arrays.stream(allAtoms).distinct().count() < expectedNumAfterAppendage) //
            throw new GmxFrameException("Cannot append duplicate atoms");

        frame.setAtoms(allAtoms);
        GmxFrameUpdater.updateFrameStructure(frame);
        GmxFrameUpdater.updateFrameCoordinates(frame);
        Logger.log("Number of atoms added to frame: " + atoms.length);
    }

    public static void appendResidues(GmxFrame frame, GmxResidue... residues) {
        int expectedNumAfterAppendage = frame.getResiduesNum() + residues.length;
        GmxResidue[] allResidues = new GmxResidue[expectedNumAfterAppendage];

        System.arraycopy(frame.getResidues(), 0, allResidues, 0, frame.getResiduesNum());
        System.arraycopy(residues, 0, allResidues, frame.getResiduesNum(), residues.length);

        if (Arrays.stream(allResidues).distinct().count() < expectedNumAfterAppendage) //
            throw new GmxFrameException("Cannot append duplicate residues");

        frame.setResidues(allResidues);
        appendFreeAtoms(frame, Arrays.stream(residues) //
                .flatMap(residue -> Arrays.stream(residue.getAllAtoms())) //
                .toArray(GmxAtom[]::new));
        Logger.log("Number of residues added to frame: " + residues.length);
    }

    // ======== PARTICLES REPLACEMENT ========

    public static GmxAtom[] replaceAtomsWithAtoms(GmxFrame frame, //
            Class<? extends GmxAtom> atomClass, String abbr, GmxAtom... atomsToReplace) {

        GmxAtom[] newAtoms = Arrays.stream(atomsToReplace).distinct().parallel() //
                .map(atom -> atomFactory.get(atomClass, abbr, -1, atom.getCoordinates())) //
                .toArray(GmxAtom[]::new);

        removeFreeAtoms(frame, atomsToReplace);
        appendFreeAtoms(frame, newAtoms);
        return newAtoms;
    }

    public static GmxResidue[] replaceAtomsWithResidues(GmxFrame frame, //
            Class<? extends GmxResidue> residueClass, GmxAtom... atomsToReplace) {

        GmxResidue[] newResidues = Arrays.stream(atomsToReplace).distinct().parallel() //
                .map(atom -> residueAtomsFactory.get(residueClass, atom.getCoordinates())) //
                .map(newAtoms -> residueFactory.get(residueClass, 0, newAtoms)) //
                .toArray(GmxResidue[]::new);

        removeFreeAtoms(frame, atomsToReplace);
        appendResidues(frame, newResidues);
        return newResidues;
    }

    public static GmxAtom[] replaceResiduesWithAtoms(GmxFrame frame, //
            Class<? extends GmxAtom> atomClass, String abbr, GmxResidue... residuesToReplace) {

        GmxAtom[] newAtoms = Arrays.stream(residuesToReplace).distinct().parallel() //
                .map(res -> atomFactory.get(atomClass, abbr, -1, res.getPivotAtom().getCoordinates())) //
                .toArray(GmxAtom[]::new);

        removeResidues(frame, residuesToReplace);
        appendFreeAtoms(frame, newAtoms);
        return newAtoms;
    }

    public static GmxResidue[] replaceResiduesWithResidues(GmxFrame frame, //
            Class<? extends GmxResidue> residueClass, GmxResidue... residuesToReplace) {

        GmxResidue[] newResidues = Arrays.stream(residuesToReplace).distinct().parallel() //
                .map(res -> residueAtomsFactory.get(residueClass, res.getPivotAtom().getCoordinates())) //
                .map(newAtoms -> residueFactory.get(residueClass, 0, newAtoms)) //
                .toArray(GmxResidue[]::new);

        removeResidues(frame, residuesToReplace);
        appendResidues(frame, newResidues);
        return newResidues;
    }

    // ======== SUPPORT METHODS ========

    private static void multiplySingleDimension(GmxFrame frame, int dimension, int multiplier) {
        float edge = frame.getBox()[dimension];
        float[] newBox = frame.getBox();
        newBox[dimension] = edge * multiplier;
        frame.getFrameStructure().setBox(newBox);

        int atomsNum = frame.getAtomsNum();
        int residuesNum = frame.getResiduesNum();

        GmxAtom[] atoms = new GmxAtom[atomsNum * multiplier];
        GmxResidue[] residues = new GmxResidue[residuesNum * multiplier];
        System.arraycopy(frame.getAtoms(), 0, atoms, 0, atomsNum);
        System.arraycopy(frame.getResidues(), 0, residues, 0, residuesNum);

        Consumer<GmxAtom> shiftDimension = atom -> Logger.error(
                "Cannot resolve Consumer for dimension " + dimension);

        for (AtomicInteger i = new AtomicInteger(1); i.get() < multiplier; i.getAndIncrement()) {
            if (dimension == 0) shiftDimension = atom -> GmxAtomUtils.shiftX(i.get() * edge, atom);
            if (dimension == 1) shiftDimension = atom -> GmxAtomUtils.shiftY(i.get() * edge, atom);
            if (dimension == 2) shiftDimension = atom -> GmxAtomUtils.shiftZ(i.get() * edge, atom);

            GmxAtom[] newAtoms = Arrays.stream(frame.getAtoms()).parallel() //
                    .map(atom -> atomFactory.get(atom.getClass(), atom.getAbbreviation(), atom.getAtomNo(),
                            atom.getCoordinates())) //
                    .peek(shiftDimension) //
                    .toArray(GmxAtom[]::new);

            GmxResidue[] newResidues = Arrays.stream(frame.getResidues()).parallel() //
                    .map(residue -> residueFactory.get(residue.getClass(), residue.getResidueNo(), //
                            Arrays.stream(residue.getAllAtoms()) //
                                    .map(atom -> newAtoms[atom.getAtomNo()]) //
                                    .toArray(GmxAtom[]::new))) //
                    .toArray(GmxResidue[]::new);

            System.arraycopy(newAtoms, 0, atoms, atomsNum * i.get(), atomsNum);
            System.arraycopy(newResidues, 0, residues, residuesNum * i.get(), residuesNum);
        }

        frame.setAtoms(atoms);
        frame.setResidues(residues);

        GmxFrameUpdater.reindexAtoms(frame);
        GmxFrameUpdater.reindexResidues(frame);
    }
}
