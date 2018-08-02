package com.asemenkov.gromacs.particles;

/**
 * @author asemenkov
 * @since May 30, 2018
 */
public class GmxAnglePredicate {

    private GmxAtom vertex, point1, point2;

    private double expectedCos, precision;

    public void setVertex(GmxAtom vertex) {
        this.vertex = vertex;
    }

    public void setPoint1(GmxAtom point1) {
        this.point1 = point1;
    }

    public void setPoint2(GmxAtom point2) {
        this.point2 = point2;
    }

    public void setExpectedCos(double expectedCos) {
        this.expectedCos = expectedCos;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public boolean isAngleAsExpected() {
        return Math.abs(vertex.cosOfAngleBetweenTwoAtoms(point1, point2) - expectedCos) < precision;
    }

}
