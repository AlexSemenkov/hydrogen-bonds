package com.asemenkov.gromacs.particles;

import com.asemenkov.gromacs.particles.exceptions.GmxAtomTypeException;
import com.asemenkov.gromacs.particles.utils.GmxAnglePredicate;
import com.asemenkov.gromacs.particles.utils.GmxAtomUtils;
import com.asemenkov.utils.io.Logger;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @author asemenkov
 * @since Apr 9, 2018
 */
public abstract class GmxResidue implements Serializable {

    private static final long serialVersionUID = 31417100405541690L;
    private final String abbreviation;
    private final String fullName;
    private int residueNo;

    private transient Field[] allAtomFields;
    private transient Field[] donorFields;
    private transient Field pivotField;
    private transient Field acceptorField;

    private GmxAtom[] allAtoms;
    private GmxAtom[] donorAtoms;
    private GmxAtom pivotAtom;
    private GmxAtom acceptorAtom;

    /**
     * @param fullName     final name of this residue, i.e. "Water"
     * @param abbreviation default name of the residue, i.e. "SOL"</br>
     *                     not final, can be changed later
     */
    protected GmxResidue(String fullName, String abbreviation) {
        this.abbreviation = abbreviation;
        this.fullName = fullName;
    }

    // ======== GETTERS ========

    public String getFullName() {
        return fullName;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public int getResidueNo() {
        return residueNo;
    }

    public double getRadiusVector() {
        return getPivotAtom().getRadiusVector();
    }

    public GmxAtom[] getAllAtoms() {
        return allAtoms;
    }

    public GmxAtom getAcceptorAtom() {
        return acceptorAtom;
    }

    public GmxAtom[] getDonorAtoms() {
        return donorAtoms;
    }

    public GmxAtom getPivotAtom() {
        return pivotAtom;
    }

    // ======== SETTERS ========

    public void setAllAtoms(GmxAtom... atoms) {
        if (allAtomFields == null || allAtomFields.length != atoms.length) //
            throw new GmxAtomTypeException("Cannot place provided atoms in residue: " + this);

        try {
            for (int i = 0; i < allAtomFields.length; i++)
                allAtomFields[i].set(this, atoms[i]);
            determineAllAtoms();
            determineDonorAtoms();
            determinePivotAtom();
            determineAcceptorAtom();
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new GmxAtomTypeException(e);
        }
    }

    public void setResidueNo(int residueNo) {
        this.residueNo = residueNo;
    }

    public void setAllAtomFields(Field[] allAtomFields) {
        this.allAtomFields = allAtomFields;
    }

    public void setDonorFields(Field[] donorFields) {
        this.donorFields = donorFields;
    }

    public void setPivotField(Field pivotField) {
        this.pivotField = pivotField;
    }

    public void setAcceptorField(Field acceptorField) {
        this.acceptorField = acceptorField;
    }

    // ======== SUPPORT METHODS ========

    private void determineAllAtoms() throws IllegalArgumentException, IllegalAccessException {
        allAtoms = new GmxAtom[allAtomFields.length];
        for (int i = 0; i < allAtomFields.length; i++)
            allAtoms[i] = (GmxAtom) allAtomFields[i].get(this);
    }

    private void determineDonorAtoms() throws IllegalArgumentException, IllegalAccessException {
        donorAtoms = new GmxAtom[donorFields.length];
        for (int i = 0; i < donorFields.length; i++)
            donorAtoms[i] = (GmxAtom) donorFields[i].get(this);
    }

    private void determinePivotAtom() throws IllegalArgumentException, IllegalAccessException {
        pivotAtom = (GmxAtom) pivotField.get(this);
    }

    private void determineAcceptorAtom() throws IllegalArgumentException, IllegalAccessException {
        acceptorAtom = (GmxAtom) acceptorField.get(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
