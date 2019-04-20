package com.asemenkov.gromacs.particles.utils;

import com.asemenkov.gromacs.particles.GmxAtom;

/**
 * @author asemenkov
 * @since May 30, 2018
 */
public class GmxAnglePredicate {

    private GmxAtom vertex, left, right;

    private double expectedCos, precision;

    public void setVertex(GmxAtom vertex) {
        this.vertex = vertex;
    }

    public void setLeft(GmxAtom left) {
        this.left = left;
    }

    public void setRight(GmxAtom right) {
        this.right = right;
    }

    public void setExpectedCos(double expectedCos) {
        this.expectedCos = expectedCos;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public boolean isAngleAsExpected() {
        return Math.abs(GmxAtomUtils.angleCosine(vertex, left, right) - expectedCos) < precision;
    }

}
