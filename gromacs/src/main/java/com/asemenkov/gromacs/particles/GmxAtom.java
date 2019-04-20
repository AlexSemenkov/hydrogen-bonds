package com.asemenkov.gromacs.particles;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * @author asemenkov
 * @since Apr 9, 2018
 */
public abstract class GmxAtom implements Serializable {

    private static final long serialVersionUID = 5526554742772127782L;
    private float[] xyz;
    private double radiusVector = -1;
    private final String fullName;
    private String abbreviation;
    private int atomNo;

    /**
     * @param fullName            name of this atom, i.e. "Oxygen"
     * @param defaultAbbreviation default name of the atom, i.e. "O"</br>
     *                            not final, can be changed later
     */
    protected GmxAtom(String fullName, String defaultAbbreviation) {
        this.abbreviation = defaultAbbreviation;
        this.fullName = fullName;
    }

    // ======== GETTERS ========

    public String getFullName() {
        return fullName;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public int getAtomNo() {
        return atomNo;
    }

    public double getRadiusVector() {
        if (radiusVector == -1) //
            radiusVector = Math.sqrt(xyz[0] * xyz[0] + xyz[1] * xyz[1] + xyz[2] * xyz[2]);
        return radiusVector;
    }

    public float[] getCoordinates() {
        return xyz;
    }

    public float getCoordinateX() {
        return xyz[0];
    }

    public float getCoordinateY() {
        return xyz[1];
    }

    public float getCoordinateZ() {
        return xyz[2];
    }

    // ======== SETTERS ========

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public void setAtomNo(int atomNo) {
        this.atomNo = atomNo;
    }

    public void setCoordinates(float[] xyz) {
        if (xyz == null) return;
        this.radiusVector = -1;
        if (this.xyz == null) this.xyz = new float[3];
        System.arraycopy(xyz, 0, this.xyz, 0, 3);
    }

    public void setCoordinateY(float y) {
        this.radiusVector = -1;
        this.xyz[1] = y;
    }

    public void setCoordinateX(float x) {
        this.radiusVector = -1;
        this.xyz[0] = x;
    }

    public void setCoordinateZ(float z) {
        this.radiusVector = -1;
        this.xyz[2] = z;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
