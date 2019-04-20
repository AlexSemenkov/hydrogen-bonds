package com.asemenkov.gromacs.frame;

import com.asemenkov.gromacs.frame.coordinates.GmxFrameCoordinates;
import com.asemenkov.gromacs.frame.exceptions.GmxFrameException;
import com.asemenkov.gromacs.frame.structure.GmxFrameStructure;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author asemenkov
 * @since Apr 15, 2018
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class GmxFrame {

    private GmxFrameStructure frameStructure;
    private GmxFrameCoordinates frameCoordinates;
    private GmxAtom[] atoms;
    private GmxResidue[] residues;

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

    public GmxAtom[] getAtomsDeepCopy() {
        return (GmxAtom[]) SerializationUtils.deserialize(SerializationUtils.serialize(atoms));
    }

    public GmxResidue[] getResiduesDeepCopy() {
        return (GmxResidue[]) SerializationUtils.deserialize(SerializationUtils.serialize(residues));
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

    // ======== SETTERS ========

    public void setFrameStructure(GmxFrameStructure frameStructure) {
        this.frameStructure = frameStructure;
    }

    public void setFrameCoordinates(GmxFrameCoordinates frameCoordinates) {
        this.frameCoordinates = frameCoordinates;
    }

    public void setAtoms(GmxAtom[] atoms) {
        this.atoms = atoms;
    }

    public void setResidues(GmxResidue[] residues) {
        this.residues = residues;
    }

    // ======== ATOM FUNCTIONAL INTERFACE ========

    public void applyConsumerToAtoms(Consumer<? super GmxAtom> atomsConsumer) {
        Arrays.stream(atoms).parallel().forEach(atomsConsumer);
    }

    public GmxAtom getMinAtom(Comparator<? super GmxAtom> atomsComparator) {
        return Arrays.stream(atoms).min(atomsComparator).orElseThrow(GmxFrameException::new);
    }

    public GmxAtom getMaxAtom(Comparator<? super GmxAtom> atomsComparator) {
        return Arrays.stream(atoms).max(atomsComparator).orElseThrow(GmxFrameException::new);
    }

    public GmxAtom[] getSortedAtoms(Comparator<? super GmxAtom> atomsComparator) {
        return Arrays.stream(atoms).sorted(atomsComparator).toArray(GmxAtom[]::new);
    }

    public GmxAtom[] getFilteredAtoms(Predicate<? super GmxAtom> atomsPredicate) {
        return Arrays.stream(atoms).parallel().filter(atomsPredicate).toArray(GmxAtom[]::new);
    }

    // ======== RESIDUE FUNCTIONAL INTERFACE ========

    public void applyConsumerToResidues(Consumer<? super GmxResidue> residuesConsumer) {
        Arrays.stream(residues).parallel().forEach(residuesConsumer);
    }

    public GmxResidue getMinResidue(Comparator<? super GmxResidue> residuesComparator) {
        return Arrays.stream(residues).min(residuesComparator).orElseThrow(GmxFrameException::new);
    }

    public GmxResidue getMaxResidue(Comparator<? super GmxResidue> residuesComparator) {
        return Arrays.stream(residues).max(residuesComparator).orElseThrow(GmxFrameException::new);
    }

    public GmxResidue[] getSortedResidues(Comparator<? super GmxResidue> residuesComparator) {
        return Arrays.stream(residues).sorted(residuesComparator).toArray(GmxResidue[]::new);
    }

    public GmxResidue[] getFilteredResidues(Predicate<? super GmxResidue> residuesPredicate) {
        return Arrays.stream(residues).parallel().filter(residuesPredicate).toArray(GmxResidue[]::new);
    }
}