package com.asemenkov.gromacs.particles;

/**
 * @author asemenkov
 * @since May 30, 2018
 */
public class GmxAnglePredicate {

    private GmxAtom vertex;
    private GmxAtom point1;
    private GmxAtom point2;
    private double expectedCos;
    private double precision;

    public GmxAtom getVertex() {
        return vertex;
    }

    public void setVertex(GmxAtom vertex) {
        this.vertex = vertex;
    }

    public GmxAtom getPoint1() {
        return point1;
    }

    public void setPoint1(GmxAtom point1) {
        this.point1 = point1;
    }

    public GmxAtom getPoint2() {
        return point2;
    }

    public void setPoint2(GmxAtom point2) {
        this.point2 = point2;
    }

    public double getExpectedCos() {
        return expectedCos;
    }

    public void setExpectedCos(double expectedCos) {
        this.expectedCos = expectedCos;
    }

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public boolean isAngleAsExpected() {
        return Math.abs(vertex.cosOfAngleBetweenTwoAtoms(point1, point2) - expectedCos) < precision;
    }

}
