package com.asemenkov.gromacs.frame;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import com.asemenkov.gromacs.frame.coordinates.GmxFrameCoordinates;
import com.asemenkov.gromacs.frame.coordinates.GmxFrameCoordinatesFromArraysBuilder;
import com.asemenkov.gromacs.frame.structure.GmxFrameStructure;
import com.asemenkov.gromacs.frame.structure.GmxFrameStructureFromArraysBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import com.asemenkov.gromacs.frame.exceptions.GmxFrameException;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;
import com.asemenkov.utils.Factories.DuoFactory;
import com.asemenkov.utils.Factories.TetraFactory;
import com.asemenkov.utils.Factories.TriFactory;
import com.asemenkov.utils.Logger;

/**
 * @author asemenkov
 * @since Apr 15, 2018
 */
public final class GmxFrame {

    private @Autowired TetraFactory<GmxAtom, Class<? extends GmxAtom>, String, Integer, float[]> atomFactory;
    private @Autowired DuoFactory<GmxAtom[], Class<? extends GmxResidue>, float[]> residueAtomsFactory;
    private @Autowired TriFactory<GmxResidue, Class<? extends GmxResidue>, Integer, GmxAtom[]> residueFactory;
    private @Autowired TriFactory<GmxResidue[], Class<? extends GmxResidue>, int[], GmxAtom[][]> residuesFactory;
    private @Autowired GmxFrameStructureFromArraysBuilder frameStructureFromArraysBuilder;
    private @Autowired GmxFrameCoordinatesFromArraysBuilder frameCoordinatesFromArraysBuilder;

    private GmxFrameStructure frameStructure;
    private GmxFrameCoordinates frameCoordinates;
    private GmxAtom[] atoms;
    private GmxResidue[] residues;

    // ======== FRAME VALIDATION ========

    public void validateFrameStructureAtoms() {
        if (atomFactory == null) throw new GmxFrameException("atomFactory isn't injected");
        if (frameStructure == null) throw new GmxFrameException("frameStructure is missing");
        if (frameCoordinates == null) throw new GmxFrameException("frameCoordinates is missing");
        if (frameStructure.getAtomsSequence() == null || frameStructure.getAtomsSequence().length == 0) //
            throw new GmxFrameException("frameStructure doesn't specify atoms sequence");
        if (Arrays.stream(frameStructure.getAtomsSequence()).parallel().anyMatch(Objects::isNull)) //
            throw new GmxFrameException("frameStructure atoms array contains null pointers");
        if (frameStructure.getAtomAbbreviationsSequence() == null || //
                frameStructure.getAtomAbbreviationsSequence().length != frameStructure.getAtomsSequence().length) //
            throw new GmxFrameException("frameStructure doesn't specify atoms abbreviations sequence");
    }

    public void validateFrameStructureResidues() {
        if (residueFactory == null) throw new GmxFrameException("residueFactory isn't injected");
        if (residuesFactory == null) throw new GmxFrameException("residuesFactory isn't injected");
        if (frameStructure == null) throw new GmxFrameException("frameStructure is missing");
        if (frameCoordinates == null) throw new GmxFrameException("frameCoordinates is missing");
        if (frameStructure.getResidueIndexesMap() == null || frameStructure.getResidueIndexesMap().isEmpty()) //
            throw new GmxFrameException("frameStructure doesn't specify residues indexes");
        if (frameStructure.getResidueAtomsMap() == null || frameStructure.getResidueAtomsMap().isEmpty()) //
            throw new GmxFrameException("frameStructure doesn't specify residues atoms");
    }

    // ======== FRAME INITIALIZATION ========

    public void initAtoms() {
        atoms = new GmxAtom[frameStructure.getAtomsNum()];
        Class<? extends GmxAtom>[] atomClasses = frameStructure.getAtomsSequence();
        String[] atomAbbreviations = frameStructure.getAtomAbbreviationsSequence();
        float[][] atomCoordinates = frameCoordinates.getCoordinates();
        IntStream.range(0, getAtomsNum()).parallel().forEach(i -> //
                atoms[i] = atomFactory.get(atomClasses[i], atomAbbreviations[i], i, atomCoordinates[i]));
    }

    public void initResidues() {
        this.residues = new GmxResidue[frameStructure.getResiduesNum()];
        AtomicInteger arrayIndex = new AtomicInteger(0);

        frameStructure.getResidueIndexesMap().forEach((key, value) -> {
            GmxAtom[][] residuesAtoms = Arrays.stream(value) //
                    .mapToObj(i -> frameStructure.getResidueAtomsMap().get(i)) //
                    .map(i -> IntStream.of(i).mapToObj(j -> atoms[j]).toArray(GmxAtom[]::new)) //
                    .toArray(GmxAtom[][]::new);

            GmxResidue[] residues = residuesFactory.get(key, value, residuesAtoms);
            int index = arrayIndex.getAndAdd(residues.length);
            System.arraycopy(residues, 0, this.residues, index, residues.length);
        });
    }

    // ======== GETTERS ========

    public GmxFrameStructure getFrameStructure() {
        return frameStructure;
    }

    public GmxFrameCoordinates getFrameCoordinates() {
        return frameCoordinates;
    }

    public GmxAtom[] getAtoms() {
        return Arrays.copyOf(atoms, getAtomsNum());
    }

    public GmxResidue[] getResidues() {
        return Arrays.copyOf(residues, getResiduesNum());
    }

    public int getAtomsNum() {
        return atoms.length;
    }

    public int getResiduesNum() {
        return residues.length;
    }

    public int getFrameNo() {
        return frameCoordinates.getFrameNo();
    }

    public String getDescription() {
        return frameStructure.getDescription();
    }

    public float[] getBox() {
        return Arrays.copyOf(frameStructure.getBox(), 3);
    }

    public GmxAtom getAtomWithMinimumX() {
        return Arrays.stream(atoms).min(GmxAtom::cmpByCoordinateX).orElseThrow(GmxFrameException::new);
    }

    public GmxAtom getAtomWithMinimumY() {
        return Arrays.stream(atoms).min(GmxAtom::cmpByCoordinateY).orElseThrow(GmxFrameException::new);
    }

    public GmxAtom getAtomWithMinimumZ() {
        return Arrays.stream(atoms).min(GmxAtom::cmpByCoordinateZ).orElseThrow(GmxFrameException::new);
    }

    public GmxAtom getAtomWithMaximumX() {
        return Arrays.stream(atoms).max(GmxAtom::cmpByCoordinateX).orElseThrow(GmxFrameException::new);
    }

    public GmxAtom getAtomWithMaximumY() {
        return Arrays.stream(atoms).max(GmxAtom::cmpByCoordinateY).orElseThrow(GmxFrameException::new);
    }

    public GmxAtom getAtomWithMaximumZ() {
        return Arrays.stream(atoms).max(GmxAtom::cmpByCoordinateZ).orElseThrow(GmxFrameException::new);
    }

    // ======== SETTERS ========

    public void setFrameStructure(GmxFrameStructure frameStructure) {
        this.frameStructure = frameStructure;
    }

    public void setFrameCoordinates(GmxFrameCoordinates frameCoordinates) {
        this.frameCoordinates = frameCoordinates;
    }

    public void setDescription(String description) {
        frameStructure.setDescription(description);
        Logger.log("Frame description is updated: " + description);
    }

    public void setBox(float[] box) {
        if (box == null || box.length != 3) throw new GmxFrameException("invalid box: " + Arrays.toString(box));
        frameStructure.setBox(Arrays.copyOf(box, 3));
        Logger.log("Frame box is updated: " + Arrays.toString(box));
    }

    // ======== NEIGHBOURS ========

    public GmxAtom getNeighbourAtom(float[] coordinates, int order) {
        if (coordinates == null || coordinates.length != 3) //
            throw new GmxFrameException("Invalid point coordinates: " + Arrays.toString(coordinates));
        if (order < 0) throw new GmxFrameException("Invalid neighbour order: " + order);
        GmxAtom[] atoms = getAtomsSortedByDistanceToPoint(coordinates);
        return atoms[order];
    }

    public GmxAtom[] getAtomsSortedByDistanceToPoint(float[] coordinates) {
        if (coordinates == null || coordinates.length != 3) //
            throw new GmxFrameException("Invalid point coordinates: " + Arrays.toString(coordinates));

        float[] xyz = Arrays.copyOf(coordinates, 3);
        GmxAtom[] atoms = new GmxAtom[getAtomsNum()];
        System.arraycopy(this.atoms, 0, atoms, 0, getAtomsNum());

        Arrays.stream(atoms).parallel().forEach(atom -> atom.shift(-xyz[0], -xyz[1], -xyz[2]));
        atoms = Arrays.stream(atoms).sorted(GmxAtom::cmpByRadiusVector).toArray(GmxAtom[]::new);
        Arrays.stream(atoms).parallel().forEach(atom -> atom.shift(xyz[0], xyz[1], xyz[2]));

        return atoms;
    }

    public GmxResidue getNeighbourResidue(float[] coordinates, int order) {
        if (coordinates == null || coordinates.length != 3) //
            throw new GmxFrameException("Invalid point coordinates: " + Arrays.toString(coordinates));
        if (order < 0) throw new GmxFrameException("Invalid neighbour order: " + order);
        GmxResidue[] residues = getResiduesSortedByDistanceToPoint(coordinates);
        return residues[order];
    }

    public GmxResidue[] getResiduesSortedByDistanceToPoint(float[] coordinates) {
        if (coordinates == null || coordinates.length != 3) //
            throw new GmxFrameException("Invalid point coordinates: " + Arrays.toString(coordinates));

        float[] xyz = Arrays.copyOf(coordinates, 3);
        GmxResidue[] residues = new GmxResidue[getResiduesNum()];
        System.arraycopy(this.residues, 0, residues, 0, getResiduesNum());

        Arrays.stream(residues).parallel().forEach(residue -> residue.shift(-xyz[0], -xyz[1], -xyz[2]));
        residues = Arrays.stream(residues).sorted(GmxResidue::cmpByRadiusVector).toArray(GmxResidue[]::new);
        Arrays.stream(residues).parallel().forEach(residue -> residue.shift(xyz[0], xyz[1], xyz[2]));

        return residues;
    }

    // ======== BOX ALTERATION ========

    public void refineBox(float padding) {
        float minX = getAtomWithMinimumX().getCoordinateX() - padding;
        float minY = getAtomWithMinimumY().getCoordinateY() - padding;
        float minZ = getAtomWithMinimumZ().getCoordinateZ() - padding;
        float maxX = getAtomWithMaximumX().getCoordinateX() + padding;
        float maxY = getAtomWithMaximumY().getCoordinateY() + padding;
        float maxZ = getAtomWithMaximumZ().getCoordinateZ() + padding;

        Arrays.stream(atoms).parallel().forEach(atom -> atom.shift(-minX, -minY, -minZ));
        setBox(new float[] { maxX - minX, maxY - minY, maxZ - minZ });
        updateFrameCoordinates();
        Logger.log("Frame box is updated: " + Arrays.toString(getBox()));
    }

    public void removeAtomsOutOfBox() {
        Predicate<GmxAtom> atomOutOfBox = atom //
                -> atom.getCoordinateX() >= getBox()[0] //
                || atom.getCoordinateY() >= getBox()[1] //
                || atom.getCoordinateZ() >= getBox()[2] //
                || atom.getCoordinateX() <= 0.f //
                || atom.getCoordinateY() <= 0.f //
                || atom.getCoordinateZ() <= 0.f;

        GmxResidue[] residuesOutOfBox = Arrays.stream(residues).parallel() //
                .filter(residue -> Arrays.stream(residue.getAllAtoms()).anyMatch(atomOutOfBox)) //
                .toArray(GmxResidue[]::new);
        removeResidues(residuesOutOfBox);

        GmxAtom[] atomsOutOfBox = Arrays.stream(atoms).parallel() //
                .filter(atomOutOfBox) //
                .toArray(GmxAtom[]::new);
        removeFreeAtoms(atomsOutOfBox);
    }

    public void multiplyBox(int multiX, int multiY, int multiZ) {
        if (multiX <= 0 || multiY <= 0 || multiZ <= 0) //
            throw new GmxFrameException("Box multiplier is negative integer or zero.");

        multiplySingleDimension(0, multiX);
        multiplySingleDimension(1, multiY);
        multiplySingleDimension(2, multiZ);

        updateFrameStructure();
        updateFrameCoordinates();
    }

    // ======== FRAME ALTERATION ========

    public void reindexAtoms() {
        AtomicInteger i = new AtomicInteger(0);
        Arrays.stream(atoms).forEach(atom -> atom.setAtomNo(i.getAndIncrement()));
    }

    public void reindexResidues() {
        AtomicInteger i = new AtomicInteger(0);
        Arrays.stream(residues).forEach(residue -> residue.setResidueNo(i.getAndIncrement()));
    }

    @SuppressWarnings("WeakerAccess")
    public void updateFrameStructure() {
        frameStructure = frameStructureFromArraysBuilder //
                .withDescription(frameStructure.getDescription()) //
                .withAtomsArray(this.atoms) //
                .withResiduesArray(this.residues) //
                .withBox(frameStructure.getBox()) //
                .build();
    }

    @SuppressWarnings("WeakerAccess")
    public void updateFrameCoordinates() {
        frameCoordinates = frameCoordinatesFromArraysBuilder //
                .withFrameNo(frameCoordinates.getFrameNo()) //
                .withAtomsArray(this.atoms) //
                .build();
    }

    public void removeFreeAtoms(GmxAtom... atoms) {
        Arrays.stream(residues).parallel() //
                .flatMap(residue -> Arrays.stream(residue.getAllAtoms())) //
                .filter(atom1 -> Arrays.stream(atoms).anyMatch(atom2 -> atom2 == atom1)) //
                .findFirst().ifPresent(atom -> {
            throw new GmxFrameException("Atom is not free: " + atom);
        });

        int expectedArrayAfterRemoval = this.atoms.length - atoms.length;
        this.atoms = Arrays.stream(this.atoms).parallel() //
                .filter(atom1 -> Arrays.stream(atoms).noneMatch(atom2 -> atom1 == atom2)) //
                .toArray(GmxAtom[]::new);

        if (this.atoms.length != expectedArrayAfterRemoval) //
            throw new GmxFrameException("Error while removing atoms from frame: unexpected length");

        reindexAtoms();
        updateFrameStructure();
        updateFrameCoordinates();
        Logger.log("Number of atoms removed from frame: " + atoms.length);
    }

    public void removeResidues(GmxResidue... residues) {
        int expectedArrayAfterRemoval = this.residues.length - residues.length;
        this.residues = Arrays.stream(this.residues).parallel() //
                .filter(residue1 -> Arrays.stream(residues).noneMatch(residue2 -> residue1 == residue2)) //
                .toArray(GmxResidue[]::new);

        if (this.residues.length != expectedArrayAfterRemoval) //
            throw new GmxFrameException("Error while removing residues from frame: unexpected length");

        reindexResidues();
        GmxAtom[] atomsToRemove = Arrays.stream(residues) //
                .flatMap(residue -> Arrays.stream(residue.getAllAtoms())) //
                .toArray(GmxAtom[]::new);
        removeFreeAtoms(atomsToRemove);
        Logger.log("Number of residues removed from frame: " + residues.length);
    }

    public void appendFreeAtoms(GmxAtom... atoms) {
        int length = this.atoms.length + atoms.length;
        GmxAtom[] newAtoms = new GmxAtom[length];

        System.arraycopy(this.atoms, 0, newAtoms, 0, this.atoms.length);
        System.arraycopy(atoms, 0, newAtoms, this.atoms.length, atoms.length);

        if (Arrays.stream(newAtoms).distinct().count() < length) //
            throw new GmxFrameException("Cannot append duplicate atoms");

        this.atoms = newAtoms;
        reindexAtoms();
        updateFrameStructure();
        updateFrameCoordinates();
        Logger.log("Number of atoms added to frame: " + atoms.length);
    }

    public void appendResidues(GmxResidue... residues) {
        GmxResidue[] newResidues = new GmxResidue[this.residues.length + residues.length];
        System.arraycopy(this.residues, 0, newResidues, 0, this.residues.length);
        System.arraycopy(residues, 0, newResidues, this.residues.length, residues.length);
        this.residues = newResidues;
        reindexResidues();

        GmxAtom[] atomsToAppend = Arrays.stream(residues) //
                .flatMap(residue -> Arrays.stream(residue.getAllAtoms())) //
                .toArray(GmxAtom[]::new);
        appendFreeAtoms(atomsToAppend);
        Logger.log("Number of residues added to frame: " + residues.length);
    }

    public GmxAtom[] replaceAtomsWithAtoms(Class<? extends GmxAtom> atomClass, String abbr, GmxAtom... atoms) {
        GmxAtom[] newAtoms = Arrays.stream(atoms).distinct().parallel() //
                .map(atom -> atomFactory.get(atomClass, abbr, -1, atom.getCoordinates())) //
                .toArray(GmxAtom[]::new);
        removeFreeAtoms(atoms);
        appendFreeAtoms(newAtoms);
        return newAtoms;
    }

    public GmxResidue[] replaceAtomsWithResidues(Class<? extends GmxResidue> residueClass, GmxAtom... atoms) {
        GmxResidue[] newResidues = Arrays.stream(atoms).distinct().parallel() //
                .map(atom -> residueAtomsFactory.get(residueClass, atom.getCoordinates())) //
                .map(newAtoms -> residueFactory.get(residueClass, 0, newAtoms)) //
                .toArray(GmxResidue[]::new);
        removeFreeAtoms(atoms);
        appendResidues(newResidues);
        return newResidues;
    }

    public GmxAtom[] replaceResiduesWithAtoms(Class<? extends GmxAtom> atomClass, String abbr, GmxResidue... residues) {
        GmxAtom[] newAtoms = Arrays.stream(residues).distinct().parallel() //
                .map(residue -> atomFactory.get(atomClass, abbr, -1, residue.getPivotAtom().getCoordinates())) //
                .toArray(GmxAtom[]::new);
        removeResidues(residues);
        appendFreeAtoms(newAtoms);
        return newAtoms;
    }

    public GmxResidue[] replaceResiduesWithResidues(Class<? extends GmxResidue> residueClass, GmxResidue... residues) {
        GmxResidue[] newResidues = Arrays.stream(residues).distinct().parallel() //
                .map(residue -> residueAtomsFactory.get(residueClass, residue.getPivotAtom().getCoordinates())) //
                .map(newAtoms -> residueFactory.get(residueClass, 0, newAtoms)) //
                .toArray(GmxResidue[]::new);
        removeResidues(residues);
        appendResidues(newResidues);
        return newResidues;
    }

    // ======== SUPPORT METHODS ========

    private void multiplySingleDimension(int dimension, int multiplier) {
        float edge = getBox()[dimension];
        float[] newBox = Arrays.copyOf(getBox(), 3);
        newBox[dimension] = edge * multiplier;

        int residuesNum = getResiduesNum();
        int atomsNum = getAtomsNum();

        GmxAtom[] atoms = new GmxAtom[atomsNum * multiplier];
        System.arraycopy(this.atoms, 0, atoms, 0, atomsNum);

        GmxResidue[] residues = new GmxResidue[residuesNum * multiplier];
        System.arraycopy(this.residues, 0, residues, 0, residuesNum);

        Consumer<GmxAtom> shiftDimension = atom -> Logger.error("Cannot resolve Consumer for dimension " + dimension);
        for (AtomicInteger i = new AtomicInteger(1); i.get() < multiplier; i.getAndIncrement()) {
            if (dimension == 0) shiftDimension = atom -> atom.shiftX(i.get() * edge);
            if (dimension == 1) shiftDimension = atom -> atom.shiftY(i.get() * edge);
            if (dimension == 2) shiftDimension = atom -> atom.shiftZ(i.get() * edge);

            GmxAtom[] newAtoms = Arrays.stream(this.atoms).parallel() //
                    .map(atom -> atomFactory.get(atom.getClass(), atom.getAbbreviation(), atom.getAtomNo(), atom.getCoordinates())) //
                    .peek(shiftDimension).toArray(GmxAtom[]::new);

            GmxResidue[] newResidues = Arrays.stream(this.residues).parallel() //
                    .map(residue -> residueFactory.get(residue.getClass(), residue.getResidueNo(), Arrays.stream(residue.getAllAtoms()) //
                            .map(atom -> newAtoms[atom.getAtomNo()]) //
                            .toArray(GmxAtom[]::new))) //
                    .toArray(GmxResidue[]::new);

            System.arraycopy(newAtoms, 0, atoms, atomsNum * i.get(), atomsNum);
            System.arraycopy(newResidues, 0, residues, residuesNum * i.get(), residuesNum);
        }

        this.atoms = atoms;
        this.residues = residues;
        reindexAtoms();
        reindexResidues();
        setBox(newBox);
    }

}