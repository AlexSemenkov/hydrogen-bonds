package com.asemenkov.gromacs.particles.utils;

import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;
import com.asemenkov.utils.io.Logger;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author asemenkov
 * @since Feb 07, 2019
 */
public class GmxResidueUtils {

    // ======== COMPARATORS ========

    public static int cmpByRadiusVector(GmxResidue residue1, GmxResidue residue2) {
        return Double.compare(residue1.getRadiusVector(), residue2.getRadiusVector());
    }

    public static int cmpByCoordinateX(GmxResidue residue1, GmxResidue residue2) {
        return Float.compare(residue1.getPivotAtom().getCoordinateX(),
                residue2.getPivotAtom().getCoordinateX());
    }

    public static int cmpByCoordinateY(GmxResidue residue1, GmxResidue residue2) {
        return Float.compare(residue1.getPivotAtom().getCoordinateY(),
                residue2.getPivotAtom().getCoordinateY());
    }

    public static int cmpByCoordinateZ(GmxResidue residue1, GmxResidue residue2) {
        return Float.compare(residue1.getPivotAtom().getCoordinateZ(),
                residue2.getPivotAtom().getCoordinateZ());
    }

    // ======== ACTIONS WITH COORDINATES ========

    public static void shiftX(float deltaX, GmxResidue... residues) {
        Stream.of(residues).parallel().forEach(residue -> //
                GmxAtomUtils.shiftX(deltaX, residue.getAllAtoms()));
    }

    public static void shiftY(float deltaY, GmxResidue... residues) {
        Stream.of(residues).parallel().forEach(residue -> //
                GmxAtomUtils.shiftY(deltaY, residue.getAllAtoms()));
    }

    public static void shiftZ(float deltaZ, GmxResidue... residues) {
        Stream.of(residues).parallel().forEach(residue -> //
                GmxAtomUtils.shiftZ(deltaZ, residue.getAllAtoms()));
    }

    public static void shift(float deltaX, float deltaY, float deltaZ, GmxResidue... residues) {
        Stream.of(residues).parallel().forEach(residue -> //
                GmxAtomUtils.shift(deltaX, deltaY, deltaZ, residue.getAllAtoms()));
    }

    public static void moveTo(float[] xyz, GmxResidue... residues) {
        Stream.of(residues).parallel().forEach(residue -> {
            float[] pivotXYZ = residue.getPivotAtom().getCoordinates();
            float deltaX = xyz[0] - pivotXYZ[0];
            float deltaY = xyz[1] - pivotXYZ[1];
            float deltaZ = xyz[2] - pivotXYZ[2];
            GmxAtomUtils.shift(deltaX, deltaY, deltaZ, residue.getAllAtoms());
        });
    }

    public static void rotate(double degreeX, double degreeY, double degreeZ, GmxResidue... residues) {
        Stream.of(residues).parallel().forEach(residue -> //
                Arrays.stream(residue.getAllAtoms()) //
                        .filter(atom -> atom != residue.getPivotAtom()) //
                        .forEach(atom -> {
                            float[] xyz = new float[3];
                            GmxAtom pivotAtom = residue.getPivotAtom();
                            xyz[0] = atom.getCoordinateX() - pivotAtom.getCoordinateX();
                            xyz[1] = atom.getCoordinateY() - pivotAtom.getCoordinateY();
                            xyz[2] = atom.getCoordinateZ() - pivotAtom.getCoordinateZ();
                            if (degreeX != 0) xyz = rotateAroundX(xyz, degreeX);
                            if (degreeY != 0) xyz = rotateAroundY(xyz, degreeY);
                            if (degreeZ != 0) xyz = rotateAroundZ(xyz, degreeZ);
                            xyz[0] += pivotAtom.getCoordinateX();
                            xyz[1] += pivotAtom.getCoordinateY();
                            xyz[2] += pivotAtom.getCoordinateZ();
                            atom.setCoordinates(xyz);
                        }));
    }

    public static boolean rotateWhile(GmxResidue residue, GmxAnglePredicate... predicates) {
        final double step = Math.PI * 0.01;
        final boolean singlePredicate = predicates.length == 1;
        Logger.log("Rotating residue No " + residue.getResidueNo());

        for (double i = 0; i < 200; i++, rotate(step, 0, 0, residue))
            for (double j = 0; j < 200; j++, rotate(0, step, 0, residue))
                for (double k = 0; k < 200; k++, rotate(0, 0, step, residue))
                    if (singlePredicate ? predicates[0].isAngleAsExpected() //
                            : Arrays.stream(predicates).allMatch(GmxAnglePredicate::isAngleAsExpected))
                        return true;
        return false;
    }

    // ======== SUPPORT METHODS ========

    private static float[] rotateAroundX(float[] xyz, double degreeAboutX) {
        float sinDegreeAboutX = (float) Math.sin(degreeAboutX);
        float cosDegreeAboutX = (float) Math.cos(degreeAboutX);
        float[] toReturn = new float[3];
        toReturn[0] = xyz[0];
        toReturn[1] = xyz[1] * cosDegreeAboutX - xyz[2] * sinDegreeAboutX;
        toReturn[2] = xyz[1] * sinDegreeAboutX + xyz[2] * cosDegreeAboutX;
        return toReturn;
    }

    private static float[] rotateAroundY(float[] xyz, double degreeAboutY) {
        float sinDegreeAboutY = (float) Math.sin(degreeAboutY);
        float cosDegreeAboutY = (float) Math.cos(degreeAboutY);
        float[] toReturn = new float[3];
        toReturn[0] = xyz[0] * cosDegreeAboutY + xyz[2] * sinDegreeAboutY;
        toReturn[1] = xyz[1];
        toReturn[2] = xyz[2] * cosDegreeAboutY - xyz[0] * sinDegreeAboutY;
        return toReturn;
    }

    private static float[] rotateAroundZ(float[] xyz, double degreeAboutZ) {
        float sinDegreeAboutZ = (float) Math.sin(degreeAboutZ);
        float cosDegreeAboutZ = (float) Math.cos(degreeAboutZ);
        float[] toReturn = new float[3];
        toReturn[0] = xyz[0] * cosDegreeAboutZ - xyz[1] * sinDegreeAboutZ;
        toReturn[1] = xyz[0] * sinDegreeAboutZ + xyz[1] * cosDegreeAboutZ;
        toReturn[2] = xyz[2];
        return toReturn;
    }
}
