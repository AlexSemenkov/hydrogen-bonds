package com.asemenkov.gromacs.io;

import static com.asemenkov.utils.RegexPatterns.*;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;

import com.asemenkov.gromacs.exceptions.GmxIoException;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;

/**
 * @author asemenkov
 * @since Apr 15, 2018
 */
public class GmxGroFileAtomLine {

    private static final String ATOM_INFO_FORMAT = "\r\n%5d%-5s%5s%5d";
    private static final String COORDINATES_FORMAT = "%8.3f%8.3f%8.3f";
    private static final String VELOCITIES_FORMAT = "%8.4f%8.4f%8.4f";
    private static final String ATOM_LINE_WO_VELOCITIES_FORMAT = ATOM_INFO_FORMAT + COORDINATES_FORMAT;
    private static final String ATOM_LINE_WITH_VELOCITIES_FORMAT = ATOM_LINE_WO_VELOCITIES_FORMAT + VELOCITIES_FORMAT;
    private int residueNo;
    private int atomNo;
    private String residueAbbreviation;
    private String atomAbbreviation;
    private float[] coordinates;
    private float[] velocities;

    private GmxGroFileAtomLine() {
    }

    public static GmxGroFileAtomLine fromStringLine(String groFileAtomLine) {
        GmxGroFileAtomLine toReturn = new GmxGroFileAtomLine();
        Matcher matcher;

        matcher = INTEGER_PATTERN.matcher(groFileAtomLine.subSequence(0, 5));
        if (matcher.find()) toReturn.residueNo = Integer.valueOf(matcher.group(1));

        matcher = STRING_PATTERN.matcher(groFileAtomLine.subSequence(5, 10));
        if (matcher.find()) toReturn.residueAbbreviation = String.valueOf(matcher.group(1));

        matcher = STRING_PATTERN.matcher(groFileAtomLine.subSequence(10, 15));
        if (matcher.find()) toReturn.atomAbbreviation = String.valueOf(matcher.group(1));

        matcher = INTEGER_PATTERN.matcher(groFileAtomLine.subSequence(15, 20));
        if (matcher.find()) toReturn.atomNo = Integer.valueOf(matcher.group(1));

        if (groFileAtomLine.length() >= 44) {
            toReturn.coordinates = new float[3];

            matcher = FLOAT_PATTERN.matcher(groFileAtomLine.subSequence(20, 28));
            if (matcher.find()) toReturn.coordinates[0] = Float.valueOf(matcher.group(1));

            matcher = FLOAT_PATTERN.matcher(groFileAtomLine.subSequence(28, 36));
            if (matcher.find()) toReturn.coordinates[1] = Float.valueOf(matcher.group(1));

            matcher = FLOAT_PATTERN.matcher(groFileAtomLine.subSequence(36, 44));
            if (matcher.find()) toReturn.coordinates[2] = Float.valueOf(matcher.group(1));
        }

        if (groFileAtomLine.length() >= 68) {
            toReturn.velocities = new float[3];

            matcher = FLOAT_PATTERN.matcher(groFileAtomLine.subSequence(44, 52));
            if (matcher.find()) toReturn.velocities[0] = Float.valueOf(matcher.group(1));

            matcher = FLOAT_PATTERN.matcher(groFileAtomLine.subSequence(52, 60));
            if (matcher.find()) toReturn.velocities[1] = Float.valueOf(matcher.group(1));

            matcher = FLOAT_PATTERN.matcher(groFileAtomLine.subSequence(60, 68));
            if (matcher.find()) toReturn.velocities[2] = Float.valueOf(matcher.group(1));
        }

        return toReturn;
    }

    public static GmxGroFileAtomLine fromFreeAtom(GmxAtom atom) {
        GmxGroFileAtomLine toReturn = new GmxGroFileAtomLine();
        toReturn.atomNo = toReturn.residueNo = atom.getAtomNo();
        toReturn.atomAbbreviation = toReturn.residueAbbreviation = atom.getAbbreviation();
        toReturn.coordinates = new float[3];
        System.arraycopy(atom.getCoordinates(), 0, toReturn.coordinates, 0, 3);
        return toReturn;
    }

    public static GmxGroFileAtomLine[] fromResidue(GmxResidue residue) {
        return Arrays.stream(residue.getAllAtoms()).map(atom -> {
            GmxGroFileAtomLine toReturn = new GmxGroFileAtomLine();
            toReturn.atomNo = atom.getAtomNo();
            toReturn.residueNo = residue.getResidueNo();
            toReturn.atomAbbreviation = atom.getAbbreviation();
            toReturn.residueAbbreviation = residue.getAbbreviation();
            toReturn.coordinates = new float[3];
            System.arraycopy(atom.getCoordinates(), 0, toReturn.coordinates, 0, 3);
            return toReturn;
        }).toArray(GmxGroFileAtomLine[]::new);
    }

    public int getResidueNo() {
        return residueNo;
    }

    public void setResidueNo(int residueNo) {
        this.residueNo = residueNo;
    }

    public int getAtomNo() {
        return atomNo;
    }

    public void setAtomNo(int atomNo) {
        this.atomNo = atomNo;
    }

    public String getAtomAbbreviation() {
        return atomAbbreviation;
    }

    public String getResidueAbbreviation() {
        return residueAbbreviation;
    }

    public float[] getCoordinates() {
        return coordinates;
    }

    public float[] getVelocities() {
        return velocities;
    }

    @Override
    public String toString() {
        if (coordinates == null || coordinates.length != 3) //
            throw new GmxIoException("Invlaid GRO line coordinates: " + Arrays.toString(coordinates));
        if (velocities == null || velocities.length != 3) //
            return String.format(Locale.US, ATOM_LINE_WO_VELOCITIES_FORMAT, //
                    residueNo, residueAbbreviation, atomAbbreviation, atomNo, //
                    coordinates[0], coordinates[1], coordinates[2]);
        else //
            return String.format(Locale.US, ATOM_LINE_WITH_VELOCITIES_FORMAT, //
                    residueNo, residueAbbreviation, atomAbbreviation, atomNo, //
                    coordinates[0], coordinates[1], coordinates[2], //
                    velocities[0], velocities[1], velocities[2]);
    }

}
