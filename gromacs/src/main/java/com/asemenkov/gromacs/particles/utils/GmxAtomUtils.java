package com.asemenkov.gromacs.particles.utils;

import com.asemenkov.gromacs.particles.GmxAtom;

import java.util.stream.Stream;

/**
 * @author asemenkov
 * @since Oct 31, 2018
 */
public class GmxAtomUtils {

    // ======== COMPARATORS ========

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

    // ======== ACTIONS WITH COORDINATES ========

    public static void shiftX(float deltaX, GmxAtom... atoms) {
        Stream.of(atoms).parallel().forEach(atom -> atom.setCoordinateX(atom.getCoordinateX() + deltaX));
    }

    public static void shiftY(float deltaY, GmxAtom... atoms) {
        Stream.of(atoms).parallel().forEach(atom -> atom.setCoordinateY(atom.getCoordinateY() + deltaY));
    }

    public static void shiftZ(float deltaZ, GmxAtom... atoms) {
        Stream.of(atoms).parallel().forEach(atom -> atom.setCoordinateZ(atom.getCoordinateZ() + deltaZ));
    }

    public static void shift(float deltaX, float deltaY, float deltaZ, GmxAtom... atoms) {
        Stream.of(atoms).parallel().forEach(atom -> {
            atom.setCoordinateX(atom.getCoordinateX() + deltaX);
            atom.setCoordinateY(atom.getCoordinateY() + deltaY);
            atom.setCoordinateZ(atom.getCoordinateZ() + deltaZ);
        });
    }

    public static double euclideanDistance(float[] point1, float[] point2) {
        float deltaX = point1[0] - point2[0];
        float deltaY = point1[1] - point2[1];
        float deltaZ = point1[2] - point2[2];
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    public static double euclideanDistance(GmxAtom atom1, GmxAtom atom2) {
        return euclideanDistance(atom1.getCoordinates(), atom2.getCoordinates());
    }

    // ======== ANGLES ========

    public static double angleCosine(float[] vertex, float[] left, float[] right) {
        float deltaX1 = left[0] - vertex[0];
        float deltaY1 = left[1] - vertex[1];
        float deltaZ1 = left[2] - vertex[2];

        float deltaX2 = right[0] - vertex[0];
        float deltaY2 = right[1] - vertex[1];
        float deltaZ2 = right[2] - vertex[2];

        double numeratorF = deltaX1 * deltaX2 + deltaY1 * deltaY2 + deltaZ1 * deltaZ2;
        double disToAtom1 = Math.sqrt(deltaX1 * deltaX1 + deltaY1 * deltaY1 + deltaZ1 * deltaZ1);
        double disToAtom2 = Math.sqrt(deltaX2 * deltaX2 + deltaY2 * deltaY2 + deltaZ2 * deltaZ2);

        return numeratorF / (disToAtom1 * disToAtom2);
    }

    public static double angleCosine(GmxAtom vertex, GmxAtom left, GmxAtom right) {
        return angleCosine(vertex.getCoordinates(), left.getCoordinates(), right.getCoordinates());
    }

    public static double angleCosine(GmxAtom vertex, GmxAtom left, GmxAtom right, double leftDist,
            double rightDist) {
        float deltaX1 = left.getCoordinateX() - vertex.getCoordinateX();
        float deltaY1 = left.getCoordinateY() - vertex.getCoordinateY();
        float deltaZ1 = left.getCoordinateZ() - vertex.getCoordinateZ();

        float deltaX2 = right.getCoordinateX() - vertex.getCoordinateX();
        float deltaY2 = right.getCoordinateY() - vertex.getCoordinateY();
        float deltaZ2 = right.getCoordinateZ() - vertex.getCoordinateZ();

        return (deltaX1 * deltaX2 + deltaY1 * deltaY2 + deltaZ1 * deltaZ2) / (leftDist * rightDist);
    }

    public static double angleCosine(GmxAtom left, GmxAtom right) {
        return (left.getCoordinateX() * right.getCoordinateX() + //
                left.getCoordinateY() * right.getCoordinateY() + //
                left.getCoordinateZ() * right.getCoordinateZ()) / //
                (left.getRadiusVector() * right.getRadiusVector());
    }
}
