package com.asemenkov.gromacs.particles;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.asemenkov.gromacs.particles.exceptions.GmxAtomTypeException;
import com.asemenkov.utils.Logger;

/**
 * @author asemenkov
 * @since Apr 9, 2018
 */
public abstract class GmxResidue {

    private final String abbreviation;
    private final String fullName;
    private int residueNo;

    private Field[] allAtomFields;
    private Field[] donorFields;
    private Field pivotField;
    private Field acceptorField;

    private GmxAtom[] allAtoms;
    private GmxAtom[] donorAtoms;
    private GmxAtom pivotAtom;
    private GmxAtom acceptorAtom;

    /**
     * @param fullName     -- final name of this residue, i.e. "Water"
     * @param abbreviation -- default name of the residue, i.e. "SOL"</br>
     *                     not final, can be changed later
     */
    protected GmxResidue(String fullName, String abbreviation) {
        this.abbreviation = abbreviation;
        this.fullName = fullName;
    }

    // ======== COMPARATORS ========

    public static int cmpByRadiusVector(GmxResidue residue1, GmxResidue residue2) {
        return Double.compare(residue1.getRadiusVector(), residue2.getRadiusVector());
    }

    // ======== ACTIONS WITH COORDINATES ========

    public void moveTo(float[] xyz) {
        float[] pivotXYZ = pivotAtom.getCoordinates();
        float deltaX = xyz[0] - pivotXYZ[0];
        float deltaY = xyz[1] - pivotXYZ[1];
        float deltaZ = xyz[2] - pivotXYZ[2];

        pivotAtom.setCoordinates(xyz);
        Arrays.stream(allAtoms) //
                .filter(atom -> atom != pivotAtom) //
                .forEach(atom -> atom.shift(deltaX, deltaY, deltaZ));
    }

    public void shiftX(float deltaX) {
        Arrays.stream(allAtoms).forEach(atom -> atom.shiftX(deltaX));
    }

    public void shiftY(float deltaY) {
        Arrays.stream(allAtoms).forEach(atom -> atom.shiftY(deltaY));
    }

    public void shiftZ(float deltaZ) {
        Arrays.stream(allAtoms).forEach(atom -> atom.shiftZ(deltaZ));
    }

    public void shift(float deltaX, float deltaY, float deltaZ) {
        Arrays.stream(allAtoms).forEach(atom -> atom.shift(deltaX, deltaY, deltaZ));
    }

    public void rotate(double degreeAboutX, double degreeAboutY, double degreeAboutZ) {
        Arrays.stream(allAtoms).filter(atom -> atom != pivotAtom).forEach(atom -> {
            float[] xyz = new float[3];
            xyz[0] = atom.getCoordinateX() - pivotAtom.getCoordinateX();
            xyz[1] = atom.getCoordinateY() - pivotAtom.getCoordinateY();
            xyz[2] = atom.getCoordinateZ() - pivotAtom.getCoordinateZ();
            if (degreeAboutX != 0) xyz = rotateAroundX(xyz, degreeAboutX);
            if (degreeAboutY != 0) xyz = rotateAroundY(xyz, degreeAboutY);
            if (degreeAboutZ != 0) xyz = rotateAroundZ(xyz, degreeAboutZ);
            xyz[0] += pivotAtom.getCoordinateX();
            xyz[1] += pivotAtom.getCoordinateY();
            xyz[2] += pivotAtom.getCoordinateZ();
            atom.setCoordinates(xyz);
        });
    }

    public boolean rotateWhile(GmxAnglePredicate... predicates) {
        final double step = Math.PI * 0.01;
        final boolean singlePredicate = predicates.length == 1;
        Logger.log("Rotating residue No " + this.getResidueNo());

        for (double i = 0; i < 200; i++, this.rotate(step, 0, 0))
            for (double j = 0; j < 200; j++, this.rotate(0, step, 0))
                for (double k = 0; k < 200; k++, this.rotate(0, 0, step))
                    if (singlePredicate ? predicates[0].isAngleAsExpected() //
                            : Arrays.stream(predicates).allMatch(GmxAnglePredicate::isAngleAsExpected)) return true;
        return false;
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

        for (int i = 0; i < allAtomFields.length; i++)
            try {
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

    private float[] rotateAroundX(float[] xyz, double degreeAboutX) {
        float sinDegreeAboutX = (float) Math.sin(degreeAboutX);
        float cosDegreeAboutX = (float) Math.cos(degreeAboutX);
        float[] toReturn = new float[3];
        toReturn[0] = xyz[0];
        toReturn[1] = xyz[1] * cosDegreeAboutX - xyz[2] * sinDegreeAboutX;
        toReturn[2] = xyz[1] * sinDegreeAboutX + xyz[2] * cosDegreeAboutX;
        return toReturn;
    }

    private float[] rotateAroundY(float[] xyz, double degreeAboutY) {
        float sinDegreeAboutY = (float) Math.sin(degreeAboutY);
        float cosDegreeAboutY = (float) Math.cos(degreeAboutY);
        float[] toReturn = new float[3];
        toReturn[0] = xyz[0] * cosDegreeAboutY + xyz[2] * sinDegreeAboutY;
        toReturn[1] = xyz[1];
        toReturn[2] = xyz[2] * cosDegreeAboutY - xyz[0] * sinDegreeAboutY;
        return toReturn;
    }

    private float[] rotateAroundZ(float[] xyz, double degreeAboutZ) {
        float sinDegreeAboutZ = (float) Math.sin(degreeAboutZ);
        float cosDegreeAboutZ = (float) Math.cos(degreeAboutZ);
        float[] toReturn = new float[3];
        toReturn[0] = xyz[0] * cosDegreeAboutZ - xyz[1] * sinDegreeAboutZ;
        toReturn[1] = xyz[0] * sinDegreeAboutZ + xyz[1] * cosDegreeAboutZ;
        toReturn[2] = xyz[2];
        return toReturn;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
