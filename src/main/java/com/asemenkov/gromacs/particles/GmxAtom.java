package com.asemenkov.gromacs.particles;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author asemenkov
 * @since Apr 9, 2018
 */
public abstract class GmxAtom {

    private float[] xyz;
    private double radiusVector = -1;
    private String abbreviation;
    private String fullName;
    private int atomNo;

    /**
     * @param fullName            -- name of this atom, i.e. "Oxygen"
     * @param defaultAbbreviation -- default name of the atom, i.e. "O"</br>
     *                            not final, can be changed later
     */
    public GmxAtom(String fullName, String defaultAbbreviation) {
        this.abbreviation = defaultAbbreviation;
        this.fullName = fullName;
    }

    // ======== GETTERS AND SETTERS ========

    public static int cmpByRadiusVector(GmxAtom atom1, GmxAtom atom2) {
        return Double.compare(atom1.getRadiusVector(), atom2.getRadiusVector());
    }

    public static int cmpByCoordinateX(GmxAtom atom1, GmxAtom atom2) {
        return Float.compare(atom1.getCoordinateX(), atom2.getCoordinateX());
    }

    public static int cmpByCoordinateY(GmxAtom atom1, GmxAtom atom2) {
        return Float.compare(atom1.getCoordinateY(), atom2.getCoordinateY());
    }

    public static int cmpByCoordinateZ(GmxAtom atom1, GmxAtom atom2) {
        return Float.compare(atom1.getCoordinateZ(), atom2.getCoordinateZ());
    }

    public String getFullName() {
        return fullName;
    }

    // ======== ACTIONS WITH COORDINATES ========

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public int getAtomNo() {
        return atomNo;
    }

    public void setAtomNo(int atomNo) {
        this.atomNo = atomNo;
    }

    public double getRadiusVector() {
        if (radiusVector == -1) //
            radiusVector = Math.sqrt(xyz[0] * xyz[0] + xyz[1] * xyz[1] + xyz[2] * xyz[2]);
        return radiusVector;
    }

    public float[] getCoordinates() {
        return xyz;
    }

    public void setCoordinates(float[] xyz) {
        if (xyz == null) return;
        this.radiusVector = -1;
        if (this.xyz == null) this.xyz = new float[3];
        System.arraycopy(xyz, 0, this.xyz, 0, 3);
    }

    public float getCoordinateX() {
        return xyz[0];
    }

    public void setCoordinateX(float x) {
        this.radiusVector = -1;
        this.xyz[0] = x;
    }

    public float getCoordinateY() {
        return xyz[1];
    }

    public void setCoordinateY(float y) {
        this.radiusVector = -1;
        this.xyz[1] = y;
    }

    public float getCoordinateZ() {
        return xyz[2];
    }

    public void setCoordinateZ(float z) {
        this.radiusVector = -1;
        this.xyz[2] = z;
    }

    // ======== ANGLES ========

    public void shiftX(float deltaX) {
        this.radiusVector = -1;
        this.xyz[0] += deltaX;
    }

    public void shiftY(float deltaY) {
        this.radiusVector = -1;
        this.xyz[1] += deltaY;
    }

    public void shiftZ(float deltaZ) {
        this.radiusVector = -1;
        this.xyz[2] += deltaZ;
    }

    public void shift(float deltaX, float deltaY, float deltaZ) {
        this.radiusVector = -1;
        this.xyz[0] += deltaX;
        this.xyz[1] += deltaY;
        this.xyz[2] += deltaZ;
    }

    // ======== COMPARATORS ========

    public double euclideanDistanceToAtom(GmxAtom that) {
        float deltaX = this.xyz[0] - that.xyz[0];
        float deltaY = this.xyz[1] - that.xyz[1];
        float deltaZ = this.xyz[2] - that.xyz[2];
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    public double cosOfAngleBetweenTwoAtoms(GmxAtom atom1, GmxAtom atom2) {
        float deltaX1 = atom1.xyz[0] - this.xyz[0];
        float deltaY1 = atom1.xyz[1] - this.xyz[1];
        float deltaZ1 = atom1.xyz[2] - this.xyz[2];

        float deltaX2 = atom2.xyz[0] - this.xyz[0];
        float deltaY2 = atom2.xyz[1] - this.xyz[1];
        float deltaZ2 = atom2.xyz[2] - this.xyz[2];

        double numeratorF = deltaX1 * deltaX2 + deltaY1 * deltaY2 + deltaZ1 * deltaZ2;
        double disToAtom1 = Math.sqrt(deltaX1 * deltaX1 + deltaY1 * deltaY1 + deltaZ1 * deltaZ1);
        double disToAtom2 = Math.sqrt(deltaX2 * deltaX2 + deltaY2 * deltaY2 + deltaZ2 * deltaZ2);

        return numeratorF / (disToAtom1 * disToAtom2);
    }

    public double cosOfAngleBetweenTwoAtoms(GmxAtom atom1, GmxAtom atom2, double disToAtom1, double disToAtom2) {
        float deltaX1 = atom1.xyz[0] - this.xyz[0];
        float deltaY1 = atom1.xyz[1] - this.xyz[1];
        float deltaZ1 = atom1.xyz[2] - this.xyz[2];

        float deltaX2 = atom2.xyz[0] - this.xyz[0];
        float deltaY2 = atom2.xyz[1] - this.xyz[1];
        float deltaZ2 = atom2.xyz[2] - this.xyz[2];

        return (deltaX1 * deltaX2 + deltaY1 * deltaY2 + deltaZ1 * deltaZ2) / (disToAtom1 * disToAtom2);
    }

    public double cosOfAngleToAtomWithZeroVertex(GmxAtom that) {
        double numerator = this.xyz[0] * that.xyz[0] + this.xyz[1] * that.xyz[1] + this.xyz[2] * that.xyz[2];
        return numerator / (this.getRadiusVector() * that.getRadiusVector());
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
