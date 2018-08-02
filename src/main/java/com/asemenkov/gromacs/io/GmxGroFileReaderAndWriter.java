package com.asemenkov.gromacs.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import com.asemenkov.gromacs.io.exceptions.GmxIoException;
import com.asemenkov.gromacs.frame.GmxFrame;
import com.asemenkov.gromacs.particles.GmxAtom;
import com.asemenkov.gromacs.particles.GmxResidue;
import com.asemenkov.utils.FileUtils;
import com.asemenkov.utils.Logger;
import com.asemenkov.utils.RegexPatterns;

/**
 * @author asemenkov
 * @since Apr 15, 2018
 */
public class GmxGroFileReaderAndWriter {

    private static final String EXTENSION = ".gro";
    private static final String BOX_FORMAT = "\r\n%8.3f%8.3f%8.3f";
    private static final String ATOMS_NUM_FORMAT = "\r\n  %d";

    private static final int DESCRIPTION_INDEX = 0;
    private static final int ATOMS_NO_INDEX = 1;
    private static final int ATOMS_START_INDEX = 2;

    public String readGroFileDescription(Path groFile) {
        FileUtils.verifyExtension(groFile, EXTENSION);
        return FileUtils.readWholeFile(groFile).get(DESCRIPTION_INDEX);
    }

    public Integer readGroFileAtomsNum(Path groFile) {
        FileUtils.verifyExtension(groFile, EXTENSION);
        List<String> lines = FileUtils.readWholeFile(groFile);
        return Integer.valueOf(lines.get(ATOMS_NO_INDEX).trim());
    }

    public float[] readGroFileBox(Path groFile) {
        FileUtils.verifyExtension(groFile, EXTENSION);
        List<String> lines = FileUtils.readWholeFile(groFile);
        Integer atomsNum = Integer.valueOf(lines.get(ATOMS_NO_INDEX).trim());
        return readBox(lines.get(ATOMS_START_INDEX + atomsNum));
    }

    public List<GmxGroFileAtomLine> readGroFileAtomLines(Path groFile) {
        FileUtils.verifyExtension(groFile, EXTENSION);
        List<String> lines = FileUtils.readWholeFile(groFile);
        Integer atomsNum = Integer.valueOf(lines.get(ATOMS_NO_INDEX).trim());

        Logger.log("Reading file: " + groFile);
        Logger.log("Number of atoms lines: " + atomsNum);

        return lines.subList(ATOMS_START_INDEX, ATOMS_START_INDEX + atomsNum).parallelStream() //
                .map(GmxGroFileAtomLine::fromStringLine) //
                .collect(Collectors.toList());
    }

    public Path writeGroFile(GmxFrame frame, Path directory, String fileName) {
        if (!fileName.endsWith(EXTENSION)) fileName += EXTENSION;

        FileUtils.createDirectoryIfNotExist(directory);
        Path path = Paths.get(directory.toString(), fileName);
        BufferedWriter writer = FileUtils.getBufferedWriter(path, true, true);
        Logger.log("Writing file: " + path);
        if (writer == null) throw new GmxIoException("Cannot initialize BufferedWriter.");

        try {
            writer.write(frame.getDescription());
            writer.write(String.format(ATOMS_NUM_FORMAT, frame.getAtomsNum()));

            GmxResidue[] residues = frame.getResidues();
            GmxAtom[] atoms = Arrays.copyOf(frame.getAtoms(), frame.getAtomsNum());

            Arrays.stream(residues).parallel() //
                    .flatMap(residue -> Arrays.stream(residue.getAllAtoms())) //
                    .forEach(atom -> atoms[atom.getAtomNo()] = null);

            Arrays.stream(atoms) //
                    .filter(Objects::nonNull) //
                    .map(GmxGroFileAtomLine::fromFreeAtom) //
                    .forEach(atomLine -> {
                        try {
                            writer.write(atomLine.toString());
                        } catch (IOException exception) {
                            FileUtils.close(writer);
                            throw new GmxIoException(exception);
                        }
                    });

            Arrays.stream(residues) //
                    .flatMap(residue -> Arrays.stream(GmxGroFileAtomLine.fromResidue(residue))) //
                    .forEach(atomLine -> {
                        try {
                            writer.write(atomLine.toString());
                        } catch (IOException exception) {
                            FileUtils.close(writer);
                            throw new GmxIoException(exception);
                        }
                    });

            float[] box = frame.getBox();
            writer.write(String.format(Locale.US, BOX_FORMAT, box[0], box[1], box[2]));
            writer.write("\n");

        } catch (IOException exception) {
            throw new GmxIoException(exception);
        } finally {
            FileUtils.close(writer);
        }

        return path;
    }

    private float[] readBox(String line) {
        Matcher matcher = RegexPatterns.FLOAT_PATTERN.matcher(line);
        float[] toReturn = new float[3];
        if (matcher.find()) toReturn[0] = Float.valueOf(matcher.group(1));
        if (matcher.find()) toReturn[1] = Float.valueOf(matcher.group(1));
        if (matcher.find()) toReturn[2] = Float.valueOf(matcher.group(1));
        return toReturn;
    }

}
