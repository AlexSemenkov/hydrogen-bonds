package com.asemenkov.gromacs.frame.structure;

import com.asemenkov.gromacs.frame.exceptions.GmxFrameException;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;
import com.asemenkov.utils.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author asemenkov
 * @since Aug 01, 2018
 */
public class GmxFrameStructureFromArraysBuilder extends GmxAbstractFrameStructureBuilder<GmxFrameStructureFromArraysBuilder> {

    private GmxResidue[] residues;
    private GmxAtom[] atoms;

    // ======== INTERFACE ========

    public GmxFrameStructureFromArraysBuilder withAtomsArray(GmxAtom[] atoms) {
        this.atoms = new GmxAtom[atoms.length];
        System.arraycopy(atoms, 0, this.atoms, 0, atoms.length);
        return this;
    }

    public GmxFrameStructureFromArraysBuilder withResiduesArray(GmxResidue[] residues) {
        this.residues = new GmxResidue[residues.length];
        System.arraycopy(residues, 0, this.residues, 0, residues.length);
        return this;
    }

    @Override
    public GmxFrameStructure build() {
        validateAtomsArray();
        validateBox();

        int atomsNum = atoms.length;
        GmxFrameStructure frameStructure = new GmxFrameStructure(atomsNum);
        frameStructure.setDescription(description);
        frameStructure.setBox(box);

        Arrays.stream(atoms).parallel() //
                .peek(atom -> frameStructure.setAtomAbbreviation(atom.getAtomNo(), atom.getAbbreviation())) //
                .forEach(atom -> frameStructure.setAtomsClass(atom.getAtomNo(), atom.getClass()));

        if (residues == null || residues.length == 0) return frameStructure;

        validateResiduesArray();
        Arrays.stream(residues).parallel() //
                .collect(Collectors.groupingByConcurrent(GmxResidue::getClass)) //
                .forEach((key, value) -> frameStructure.setResidueIndexes(key, //
                        value.stream().mapToInt(GmxResidue::getResidueNo).toArray()));

        Arrays.stream(residues).parallel() //
                .collect(Collectors.groupingByConcurrent(GmxResidue::getResidueNo)) //
                .forEach((key, value) -> frameStructure.setResidueAtoms(key, //
                        Arrays.stream(value.get(0).getAllAtoms()).mapToInt(GmxAtom::getAtomNo).toArray()));

        Logger.log("Frame structure successfully created from arrays of atoms and residues");
        return frameStructure;
    }

    // ======== VALIDATION METHODS ========

    private void validateAtomsArray() {
        if (atoms == null || atoms.length == 0) throw new GmxFrameException("Atoms array is empty.");

        int[] errorAtoms = IntStream.range(0, atoms.length).parallel() //
                .filter(i -> atoms[i].getAtomNo() != i).toArray();

        if (errorAtoms.length > 0) //
            throw new GmxFrameException("The following GmxAtoms indexes are out of order: " + //
                    Arrays.toString(errorAtoms));
    }

    private void validateResiduesArray() {
        List<String> errorAtoms = Arrays.stream(residues).parallel() //
                .flatMap(residue -> Arrays.stream(residue.getAllAtoms())) //
                .filter(atom -> atom != atoms[atom.getAtomNo()]) //
                .map(GmxAtom::toString) //
                .collect(Collectors.toList());

        if (errorAtoms.size() > 0) //
            throw new GmxFrameException("The following residue's atoms aren't in atoms array:\n" + //
                    errorAtoms.stream().collect(Collectors.joining("\n")));

        AtomicInteger totalResidueAtoms = new AtomicInteger(0);
        long distinctResidueAtoms = Arrays.stream(residues).parallel() //
                .peek(residue -> totalResidueAtoms.getAndAdd(residue.getAllAtoms().length)) //
                .flatMap(residue -> Arrays.stream(residue.getAllAtoms())) //
                .distinct().count();

        if (distinctResidueAtoms != totalResidueAtoms.get()) //
            throw new GmxFrameException("Some residues compose the same GmxAtom object");
    }
}
